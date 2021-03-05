package me.pieking1215.startupqol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

@Mod("startupqol")
public class StartupQoL {
    private static final Logger LOGGER = LogManager.getLogger();

    public static long expectedTime = 0;
    public static long doneTime = 0;

    boolean triggered = false;
    boolean trueFullscreen;

    long startupTime;
    boolean hasBeenMainMenu = false;
    boolean hasLeftMainMenu = false;

    static {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            StartupQoLConfig.loadConfig();
            expectedTime = StartupQoLConfig.getTimeEstimate();
        });
    }

    public StartupQoL() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(this);
            trueFullscreen = Minecraft.getInstance().gameSettings.fullscreen;
            Minecraft.getInstance().gameSettings.fullscreen = false;
        });
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!triggered && event.getGui() instanceof MainMenuScreen) {
            triggered = true;

            Minecraft.getInstance().gameSettings.fullscreen = trueFullscreen;
            if (Minecraft.getInstance().gameSettings.fullscreen && !Minecraft.getInstance().getMainWindow().isFullscreen()) {
                Minecraft.getInstance().getMainWindow().toggleFullscreen();
                Minecraft.getInstance().gameSettings.fullscreen = Minecraft.getInstance().getMainWindow().isFullscreen();
            }

            startupTime = ManagementFactory.getRuntimeMXBean().getUptime();
            LOGGER.info("Startup took " + startupTime + "ms.");

            doneTime = startupTime;

            StartupQoLConfig.addStartupTime(startupTime);
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent event){
        if(!hasLeftMainMenu && event.getGui() instanceof MainMenuScreen){
            hasBeenMainMenu = true;
            long minutes = (startupTime / 1000) / 60;
            long seconds = (startupTime / 1000) % 60;

            float guiScale = (float)Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
            if(guiScale <= 0) guiScale = 1; // failsafe to prevent divide by 0

            String txt = "Startup took " + minutes + "m " + seconds + "s.";
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(new MatrixStack(), txt, Minecraft.getInstance().getMainWindow().getWidth()/2 / guiScale - Minecraft.getInstance().fontRenderer.getStringWidth(txt)/2, Minecraft.getInstance().getMainWindow().getHeight() / guiScale - 20, Color.YELLOW.getRGB());
        }else if(hasBeenMainMenu){
            hasLeftMainMenu = true;
        }
    }

}
