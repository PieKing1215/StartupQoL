package me.pieking1215.startupqol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StartupQoLConfig {
    public static float fadeOutTime = 1000.0f;
    public static float fadeInTime = 500.0f;
    public static boolean timerOnTop = false;

    public static final File DOT_MINECRAFT = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).toFile().getParentFile();
    public static final File TIMES_FILE = new File(DOT_MINECRAFT, "config/startupQoL/startup_times.json");

    public static long getTimeEstimate(){
        // try to load previous times
        try {
            TIMES_FILE.getParentFile().mkdirs();
            if(!TIMES_FILE.exists()) TIMES_FILE.createNewFile();

            JsonReader jr = new JsonReader(new FileReader(TIMES_FILE));
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

                        return sum;
                    }
                }
            }
            jr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void addStartupTime(long startupTime){
        // times file format
        // {
        //   "times": [
        //     34784,
        //     34726,
        //     36204
        //   ]
        // }

        try {

            // make file
            TIMES_FILE.getParentFile().mkdirs();
            if(!TIMES_FILE.exists()) TIMES_FILE.createNewFile();

            // read times

            long[] times = new long[0];
            JsonReader jr = new JsonReader(new FileReader(TIMES_FILE));
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

            // write times

            JsonWriter jw = new JsonWriter(new FileWriter(TIMES_FILE));
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
