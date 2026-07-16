package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.NamedColorAccess;
import com.zapaxe.zeeg.ZeegRenderHooks;
import com.zapaxe.zeeg.config.GlintConfig;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderState.class)
public abstract class ItemRenderStateMixin implements NamedColorAccess {
    @Unique
    private int[] zeeg$namedColor;

    @Override
    public void zeeg$setNamedColor(int r, int g, int b, int strength, boolean rainbow, int speed) {
        if (r == -1) {
            this.zeeg$namedColor = null;
        } else {
            this.zeeg$namedColor = new int[]{r, g, b, strength, rainbow ? 1 : 0, speed};
        }
    }

    @Override
    public int[] zeeg$getNamedColor() {
        return this.zeeg$namedColor;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void zeeg$setGlintColorOnRender(MatrixStack matrices, OrderedRenderCommandQueue renderCommands, int light, int overlay, int seed, CallbackInfo ci) {
        int[] color = this.zeeg$namedColor;
        if (color == null) {
            color = new int[]{GlintConfig.getRed(), GlintConfig.getGreen(), GlintConfig.getBlue(), GlintConfig.getStrength(), GlintConfig.getRainbow() ? 1 : 0, GlintConfig.getRainbowSpeed()};
        }
        ZeegRenderHooks.GLINT_COLOR.set(color);
    }
}
