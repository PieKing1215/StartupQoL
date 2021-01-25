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
import net.minecraftforge.fml.client.ClientModLoader;
import net.minecraftforge.fml.client.CustomEarlyLoaderGUI;
import net.minecraftforge.fml.client.EarlyLoaderGUI;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.progress.StartupMessageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Mod("startupqol")
public class StartupQoL {
    private static final Logger LOGGER = LogManager.getLogger();

    boolean triggered = false;
    boolean trueFullscreen;

    long startupTime;
    boolean hasBeenMainMenu = false;
    boolean hasLeftMainMenu = false;
    static StartupTimeMessage msg;

    static {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            try {

                // Alternative way I was looking into displaying time on the startup screen
                // More flexible but can't hook as early
//                Field f = ClientModLoader.class.getDeclaredField("earlyLoaderGUI");
//                f.setAccessible(true);
//                EarlyLoaderGUI earlyLoaderGUI = (EarlyLoaderGUI)f.get(null);
//                CustomEarlyLoaderGUI ngui = new CustomEarlyLoaderGUI(Minecraft.getInstance().getMainWindow());
//                f.set(null, ngui);

                Class<?> enumElement = Class.forName("net.minecraftforge.fml.loading.progress.StartupMessageManager$MessageType");
                Object[] enumElements = enumElement.getEnumConstants();

                Object element1 = enumElements[3];

                msg = new StartupTimeMessage();
                StartupBlankMessage msg2 = new StartupBlankMessage();

                // try to load previous times
                try {
                    File dotMinecraft = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).toFile().getParentFile();
                    File f = new File(dotMinecraft, "config/startupQoL/startup_times.json");
                    f.getParentFile().mkdirs();
                    if(!f.exists()) f.createNewFile();

                    JsonReader jr = new JsonReader(new FileReader(f));
                    JsonElement jp = new JsonParser().parse(jr);
                    if(jp.isJsonObject()) {
                        JsonObject obj = jp.getAsJsonObject();
                        if(obj.has("times") && obj.get("times").isJsonArray()){
                            JsonArray arr = obj.get("times").getAsJsonArray();
                            if(arr.size() > 0) {
                                long sum = 0;
                                for (int i = 0; i < arr.size(); i++) {
                                    sum += arr.get(i).getAsLong();
                                }
                                sum /= arr.size();
                                msg.setExpectedTime(sum);
                            }
                        }
                    }
                    jr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Field messages_f = StartupMessageManager.class.getDeclaredField("messages");
                messages_f.setAccessible(true);
                EnumMap messages = (EnumMap)messages_f.get(null);

                List<StartupMessageManager.Message> modMessages = (List<StartupMessageManager.Message>)messages.get(element1);
                if(modMessages == null){
                    modMessages = new ArrayList<>();
                    messages.put((Enum) element1, modMessages);
                }
                modMessages.add(msg);
                modMessages.add(msg2);

            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
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
        if (event.getGui() instanceof MainMenuScreen && !triggered) {
            triggered = true;

            Minecraft.getInstance().gameSettings.fullscreen = trueFullscreen;
            if (Minecraft.getInstance().gameSettings.fullscreen && !Minecraft.getInstance().getMainWindow().isFullscreen()) {
                Minecraft.getInstance().getMainWindow().toggleFullscreen();
                Minecraft.getInstance().gameSettings.fullscreen = Minecraft.getInstance().getMainWindow().isFullscreen();
            }

            startupTime = ManagementFactory.getRuntimeMXBean().getUptime();
            LOGGER.info("Startup took " + startupTime + "ms.");

            msg.markDone(startupTime);

            try {
                File dotMinecraft = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).toFile().getParentFile();
                File f = new File(dotMinecraft, "config/startupQoL/startup_times.json");
                f.getParentFile().mkdirs();
                if(!f.exists()) f.createNewFile();

                long[] times = new long[0];
                JsonReader jr = new JsonReader(new FileReader(f));
                JsonElement jp = new JsonParser().parse(jr);
                if(jp.isJsonObject()) {
                    JsonObject obj = jp.getAsJsonObject();
                    if(obj.has("times") && obj.get("times").isJsonArray()){
                        JsonArray arr = obj.get("times").getAsJsonArray();
                        if(arr.size() > 0) {
                            times = new long[arr.size()];
                            for (int i = 0; i < arr.size(); i++) {
                                times[i] = arr.get(i).getAsLong();
                            }
                        }
                    }
                }
                jr.close();

                JsonWriter jw = new JsonWriter(new FileWriter(f));
                jw.setIndent("  ");
                jw.beginObject();

                jw.name("times");
                jw.beginArray();
                // only keep 3 times
                if(times.length > 2){
                    for (int i = times.length - 2; i < times.length; i++) {
                        jw.value(times[i]);
                    }
                }else {
                    for (int i = 0; i < times.length; i++) {
                        jw.value(times[i]);
                    }
                }
                jw.value(startupTime);
                jw.endArray();

                jw.endObject();
                jw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
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
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(txt, Minecraft.getInstance().getMainWindow().getWidth()/2 / guiScale - Minecraft.getInstance().fontRenderer.getStringWidth(txt)/2, Minecraft.getInstance().getMainWindow().getHeight() / guiScale - 16, Color.YELLOW.getRGB());
        }else if(hasBeenMainMenu){
            hasLeftMainMenu = true;
        }
    }

}
