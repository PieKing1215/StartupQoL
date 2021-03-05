package me.pieking1215.startupqol.mixin.client;

import me.pieking1215.startupqol.StartupQoLConfig;
import net.minecraft.client.gui.screen.MainMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MainMenuScreen.class)
public class MixinMainMenuScreen {

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 1000.0f, ordinal = 0))
    private float modifyFade(float f){
        return StartupQoLConfig.fadeOutTime;
    }

}
