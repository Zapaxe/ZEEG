package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.NamedColorAccess;
import com.zapaxe.zeeg.ZeegRenderHooks;
import com.zapaxe.zeeg.config.GlintConfig;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @Inject(method = "prepareItemInitially", at = @At("HEAD"))
    private void zeeg$setGlintColor(KeyedItemRenderState state, MatrixStack matrices, int x, int y, int size, CallbackInfo ci) {
        if (state == null) return;
        int[] color = ((NamedColorAccess) state).zeeg$getNamedColor();
        if (color == null) {
            color = new int[]{GlintConfig.getRed(), GlintConfig.getGreen(), GlintConfig.getBlue()};
        }
        ZeegRenderHooks.GLINT_COLOR.set(color);
    }
}
