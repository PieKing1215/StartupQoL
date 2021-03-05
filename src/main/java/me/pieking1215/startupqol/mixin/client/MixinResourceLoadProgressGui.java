package me.pieking1215.startupqol.mixin.client;

import me.pieking1215.startupqol.StartupQoLConfig;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ResourceLoadProgressGui.class)
public class MixinResourceLoadProgressGui {

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 1000.0f, ordinal = 0))
    private float modifyFadeOut(float f){
        return StartupQoLConfig.fadeOutTime;
    }

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 500.0f, ordinal = 0))
    private float modifyFadeIn(float f){
        return StartupQoLConfig.fadeInTime;
    }

}
