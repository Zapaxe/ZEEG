package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.ZeegRenderHooks;
import com.zapaxe.zeeg.config.GlintConfig;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Redirect(
        method = "writeDynamicTransforms",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
        )
    )
    private com.mojang.blaze3d.buffers.GpuBufferSlice zeeg$modifyColorModulator(
        net.minecraft.client.renderer.DynamicUniforms uniforms,
        org.joml.Matrix4f modelView,
        org.joml.Matrix4f textureTransform
    ) {
        if (((RenderType) (Object) this).pipeline() != RenderPipelines.GLINT) {
            return uniforms.writeTransform(modelView, textureTransform);
        }
        int[] color = ZeegRenderHooks.GLINT_COLOR.get();
        if (color == null) {
            color = new int[]{
                GlintConfig.getRed(), GlintConfig.getGreen(), GlintConfig.getBlue(), 
                GlintConfig.getStrength(), GlintConfig.getRainbow() ? 1 : 0, 
                GlintConfig.getRainbowSpeed(), GlintConfig.getCycleMode(), 
                GlintConfig.getRed2(), GlintConfig.getGreen2(), GlintConfig.getBlue2()
            };
        }
        int mode = color.length > 6 ? color[6] : (color.length > 4 && color[4] == 1 ? 1 : 0);
        int speed = color.length > 5 ? color[5] : 25;
        long time = System.currentTimeMillis();
        
        float r, g, b;
        if (mode == 1) {
            float hue = (float) (((time * speed) % 100000L) / 100000.0);
            float[] rgb = zeeg$hsvToRgb(hue, 1.0f, 1.0f);
            r = rgb[0];
            g = rgb[1];
            b = rgb[2];
        } else if (mode == 2) {
            int r2 = color.length > 7 ? color[7] : 255;
            int g2 = color.length > 8 ? color[8] : 255;
            int b2 = color.length > 9 ? color[9] : 255;
            double factor = (time * speed / 100000.0) * 2.0 * Math.PI;
            double t = 0.5 + 0.5 * Math.sin(factor);
            r = (float) ((color[0] * (1 - t) + r2 * t) / 255.0);
            g = (float) ((color[1] * (1 - t) + g2 * t) / 255.0);
            b = (float) ((color[2] * (1 - t) + b2 * t) / 255.0);
        } else {
            r = color[0] / 255.0f;
            g = color[1] / 255.0f;
            b = color[2] / 255.0f;
        }
        float a = color.length > 3 ? color[3] / 255.0f : 1.0f;
        
        org.joml.Vector4f colorModulator = new org.joml.Vector4f(r, g, b, a);
        org.joml.Vector3f noOffset = new org.joml.Vector3f(0f, 0f, 0f);
        return uniforms.writeTransform(modelView, colorModulator, noOffset, textureTransform);
    }

    @Inject(
        method = "canConsolidateConsecutiveGeometry",
        at = @At("HEAD"),
        cancellable = true
    )
    private void zeeg$disableGlintConsolidation(CallbackInfoReturnable<Boolean> cir) {
        if (((RenderType) (Object) this).pipeline() == RenderPipelines.GLINT) {
            cir.setReturnValue(false);
        }
    }
}
