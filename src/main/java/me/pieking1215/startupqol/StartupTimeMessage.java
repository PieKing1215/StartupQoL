package me.pieking1215.startupqol;

import net.minecraftforge.fml.loading.progress.StartupMessageManager;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

public class StartupTimeMessage extends StartupMessageManager.Message {

    private static Object type;
    private static Field type_f;
    private static Field timestamp_f;
    static {
        try {
            Class<?> enumElement = Class.forName("net.minecraftforge.fml.loading.progress.StartupMessageManager$MessageType");
            Object[] enumElements = enumElement.getEnumConstants();

            StartupTimeMessage.type = enumElements[3];


            type_f = StartupMessageManager.Message.class.getDeclaredField("type");
            type_f.setAccessible(true);

            timestamp_f = StartupMessageManager.Message.class.getDeclaredField("timestamp");
            timestamp_f.setAccessible(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private long expectedTime = 0;
    private long doneTime = 0;

    public StartupTimeMessage() {
        super("", null);

        try {
            type_f.set(this, type);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            timestamp_f.set(this, System.nanoTime() + 3600000000000L);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setExpectedTime(long time){
        this.expectedTime = time;
    }

    public void markDone(long time){
        this.doneTime = time;
        try {
            timestamp_f.set(this, System.nanoTime() - 3000000000L);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getText() {

        long startupTime = ManagementFactory.getRuntimeMXBean().getUptime();

        if(doneTime > 0) startupTime = doneTime;

        long minutes = (startupTime / 1000) / 60;
        long seconds = (startupTime / 1000) % 60;

        String str = "Startup: " + minutes + "m " + seconds + "s";

        if(expectedTime > 0){
            long ex_minutes = (expectedTime / 1000) / 60;
            long ex_seconds = (expectedTime / 1000) % 60;

            str += " / ~" + ex_minutes + "m " + ex_seconds + "s";
        }

        return str;
    }

    @Override
    public float[] getTypeColour() {
        return new float[]{0.75f, 0.75f, 0f, 1f};
    }
}
