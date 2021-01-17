package me.pieking1215.startupqol;

import net.minecraftforge.fml.loading.progress.StartupMessageManager;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

public class StartupBlankMessage extends StartupMessageManager.Message {

    private static Object type;
    private static Field type_f;
    private static Field timestamp_f;
    static {
        try {
            Class<?> enumElement = Class.forName("net.minecraftforge.fml.loading.progress.StartupMessageManager$MessageType");
            Object[] enumElements = enumElement.getEnumConstants();

            StartupBlankMessage.type = enumElements[3];


            type_f = StartupMessageManager.Message.class.getDeclaredField("type");
            type_f.setAccessible(true);

            timestamp_f = StartupMessageManager.Message.class.getDeclaredField("timestamp");
            timestamp_f.setAccessible(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public StartupBlankMessage() {
        super("", null);

        try {
            type_f.set(this, type);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            timestamp_f.set(this, System.nanoTime() + 3600000000000L - 1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
