package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.ZeegRenderHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.renderer.SubmitNodeStorage$ItemSubmit")
public class ItemSubmitMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void zeeg$captureColor(CallbackInfo ci) {
        int[] color = ZeegRenderHooks.GLINT_COLOR.get();
        if (color != null) {
            ZeegRenderHooks.ITEM_SUBMIT_COLORS.put(this, color);
        }
    }
}
