package net.minecraftforge.fml.client;

import net.minecraft.client.MainWindow;

import java.lang.management.ManagementFactory;

public class CustomEarlyLoaderGUI extends EarlyLoaderGUI {

    MainWindow wind;

    public CustomEarlyLoaderGUI(MainWindow window) {
        super(window);
        this.wind = window;
    }

    @Override
    void renderFromGUI(){

        long startupTime = ManagementFactory.getRuntimeMXBean().getUptime() * 10;
        long minutes = (startupTime / 1000) / 60;
        long seconds = (startupTime / 1000) % 60;

        renderMessage("Startup time: " + minutes + "m " + seconds + "s    ", new float[]{1f, 1f, 0f, 1f}, 2, 1.0f);
        super.renderFromGUI();
    }

}
