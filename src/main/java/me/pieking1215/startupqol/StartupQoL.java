package me.pieking1215.startupqol;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.lang.management.ManagementFactory;

@Mod("startupqol")
public class StartupQoL {
    private static final Logger LOGGER = LogManager.getLogger();

    boolean triggered = false;
    boolean trueFullscreen;

    long startupTime;
    boolean hasBeenMainMenu = false;
    boolean hasLeftMainMenu = false;

    public StartupQoL() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(this);
            trueFullscreen = Minecraft.getInstance().gameSettings.fullscreen;
            Minecraft.getInstance().gameSettings.fullscreen = false;
        });
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof MainMenuScreen && !triggered) {
            triggered = true;

            Minecraft.getInstance().gameSettings.fullscreen = trueFullscreen;
            if (Minecraft.getInstance().gameSettings.fullscreen && !Minecraft.getInstance().getMainWindow().isFullscreen()) {
                Minecraft.getInstance().getMainWindow().toggleFullscreen();
                Minecraft.getInstance().gameSettings.fullscreen = Minecraft.getInstance().getMainWindow().isFullscreen();
            }

            startupTime = ManagementFactory.getRuntimeMXBean().getUptime();
            LOGGER.info("Startup took " + startupTime + "ms.");
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent event){
        if(event.getGui() instanceof MainMenuScreen && !hasLeftMainMenu){
            hasBeenMainMenu = true;
            long minutes = (startupTime / 1000) / 60;
            long seconds = (startupTime / 1000) % 60;

            float guiScale = (float)Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
            if(guiScale <= 0) guiScale = 1; // failsafe to prevent divide by 0

            String txt = "Startup took " + minutes + "m " + seconds + "s.";
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(new MatrixStack(), txt, Minecraft.getInstance().getMainWindow().getWidth()/2 / guiScale - Minecraft.getInstance().fontRenderer.getStringWidth(txt)/2, Minecraft.getInstance().getMainWindow().getHeight() / guiScale - 16, Color.YELLOW.getRGB());
        }else if(hasBeenMainMenu){
            hasLeftMainMenu = true;
        }
    }

}
