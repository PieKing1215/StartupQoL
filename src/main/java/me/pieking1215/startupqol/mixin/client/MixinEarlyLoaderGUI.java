package me.pieking1215.startupqol.mixin.client;

import me.pieking1215.startupqol.StartupQoLConfig;
import me.pieking1215.startupqol.StartupQoL;
import net.minecraft.client.MainWindow;
import net.minecraftforge.fml.client.EarlyLoaderGUI;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.management.ManagementFactory;

@Mixin(EarlyLoaderGUI.class)
public abstract class MixinEarlyLoaderGUI {

    @Shadow @Final private MainWindow window;
    @Shadow abstract void renderMessage(String message, float[] colour, int line, float alpha);

    /**
     * Render the timer and estimate
     */
    @Inject(method = "renderMessages", at = @At("HEAD"), cancellable = true, remap = false)
    private void renderMessages(CallbackInfo callback){
        if(StartupQoLConfig.timerOnTop) {
            // in top section
            renderMessage(getString(), new float[]{1f, 1f, 0f, 1f}, 2, 1.0f);
        } else {
            // in bottom section
            renderMessage(getString(), new float[]{1f, 1f, 0f, 1f}, ((window.getScaledHeight() - modifyBottomOffset(15)) / 10) + 1, 1.0f);
        }
    }

    /**
     * Shift the default Forge messages up a line so there's room for ours
     */
    @Redirect(method = "renderMessages", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/EarlyLoaderGUI;renderMessage(Ljava/lang/String;[FIF)V", ordinal = 0), remap = false)
    private void redirectRenderMessage(EarlyLoaderGUI earlyLoaderGUI, String message, float[] colour, int line, float alpha){
        renderMessage(message, colour, line - 1, alpha);
    }

    /**
     * Move the bottom boundary for message lines up a bit
     */
    @ModifyConstant(method = "renderMessages", constant = @Constant(intValue = 15), remap = false)
    private int modifyBottomOffset(int orig){
        return 20;
    }

    /**
     * Get formatted timer + estimate string
     */
    private String getString(){
        long startupTime = ManagementFactory.getRuntimeMXBean().getUptime();

        if(StartupQoL.doneTime > 0) startupTime = StartupQoL.doneTime;

        long minutes = (startupTime / 1000) / 60;
        long seconds = (startupTime / 1000) % 60;

        String str = "Startup: " + minutes + "m " + seconds + "s";

        if(StartupQoL.expectedTime > 0){
            long ex_minutes = (StartupQoL.expectedTime / 1000) / 60;
            long ex_seconds = (StartupQoL.expectedTime / 1000) % 60;

            str += " / ~" + ex_minutes + "m " + ex_seconds + "s";
        }

        return str;
    }

}
