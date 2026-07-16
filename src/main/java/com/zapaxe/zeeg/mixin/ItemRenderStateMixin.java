package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.NamedColorAccess;
import com.zapaxe.zeeg.ZeegRenderHooks;
import com.zapaxe.zeeg.config.GlintConfig;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.class)
public abstract class ItemRenderStateMixin implements NamedColorAccess {
    @Unique
    private int[] zeeg$namedColor;

    @Override
    public void zeeg$setNamedColor(int r, int g, int b, int strength, boolean rainbow, int speed, int mode, int r2, int g2, int b2) {
        if (r == -1) {
            this.zeeg$namedColor = null;
        } else {
            this.zeeg$namedColor = new int[]{r, g, b, strength, rainbow ? 1 : 0, speed, mode, r2, g2, b2};
        }
    }

    @Override
    public int[] zeeg$getNamedColor() {
        return this.zeeg$namedColor;
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void zeeg$setGlintColorOnRender(PoseStack matrices, SubmitNodeCollector renderCommands, int light, int overlay, int seed, CallbackInfo ci) {
        int[] color = this.zeeg$namedColor;
        if (color == null) {
            color = new int[]{
                GlintConfig.getRed(), GlintConfig.getGreen(), GlintConfig.getBlue(), 
                GlintConfig.getStrength(), GlintConfig.getRainbow() ? 1 : 0, 
                GlintConfig.getRainbowSpeed(), GlintConfig.getCycleMode(), 
                GlintConfig.getRed2(), GlintConfig.getGreen2(), GlintConfig.getBlue2()
            };
        }
        ZeegRenderHooks.GLINT_COLOR.set(color);
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void zeeg$clearGlintColor(PoseStack matrices, SubmitNodeCollector renderCommands, int light, int overlay, int seed, CallbackInfo ci) {
        ZeegRenderHooks.GLINT_COLOR.remove();
    }
}
