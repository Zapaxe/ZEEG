package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.ZeegRenderHooks;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderType.class)
public class RenderLayerMixin {
    @Unique
    private static float[] zeeg$hsvToRgb(float hue, float saturation, float value) {
        float r = 0, g = 0, b = 0;
        int i = (int) (hue * 6);
        float f = hue * 6 - i;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);
        switch (i % 6) {
            case 0: r = value; g = t; b = p; break;
            case 1: r = q; g = value; b = p; break;
            case 2: r = p; g = value; b = t; break;
            case 3: r = p; g = q; b = value; break;
            case 4: r = t; g = p; b = value; break;
            case 5: r = value; g = p; b = q; break;
        }
        return new float[]{r, g, b};
    }

    @ModifyArg(
        method = "draw",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"),
        index = 1
    )
    private Vector4fc zeeg$modifyColorModulator(Vector4fc original) {
        if (((RenderType) (Object) this).pipeline() != RenderPipelines.GLINT) return original;
        int[] color = ZeegRenderHooks.GLINT_COLOR.get();
        if (color == null) return original;
        int mode = color.length > 6 ? color[6] : (color.length > 4 && color[4] == 1 ? 1 : 0);
        int speed = color.length > 5 ? color[5] : 25;
        long time = System.currentTimeMillis();
        if (mode == 1) {
            float hue = (float) (((time * speed) % 100000L) / 100000.0);
            float[] rgb = zeeg$hsvToRgb(hue, 1.0f, 1.0f);
            return new Vector4f(rgb[0], rgb[1], rgb[2], color.length > 3 ? color[3] / 255.0f : 1.0f);
        } else if (mode == 2) {
            int r2 = color.length > 7 ? color[7] : 255;
            int g2 = color.length > 8 ? color[8] : 255;
            int b2 = color.length > 9 ? color[9] : 255;
            double factor = (time * speed / 100000.0) * 2.0 * Math.PI;
            double t = 0.5 + 0.5 * Math.sin(factor);
            float r = (float) ((color[0] * (1 - t) + r2 * t) / 255.0);
            float g = (float) ((color[1] * (1 - t) + g2 * t) / 255.0);
            float b = (float) ((color[2] * (1 - t) + b2 * t) / 255.0);
            return new Vector4f(r, g, b, color.length > 3 ? color[3] / 255.0f : 1.0f);
        }
        return new Vector4f(color[0] / 255.0f, color[1] / 255.0f, color[2] / 255.0f, color.length > 3 ? color[3] / 255.0f : 1.0f);
    }
}
