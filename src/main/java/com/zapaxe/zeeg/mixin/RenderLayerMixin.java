package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.ZeegRenderHooks;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderLayer.class)
public class RenderLayerMixin {
    @ModifyArg(
        method = "draw",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/DynamicUniforms;write(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"),
        index = 1
    )
    private Vector4fc zeeg$modifyColorModulator(Vector4fc original) {
        if (((RenderLayer) (Object) this).getRenderPipeline() != RenderPipelines.GLINT) return original;
        int[] color = ZeegRenderHooks.GLINT_COLOR.get();
        if (color == null) return original;
        return new Vector4f(color[0] / 255.0f, color[1] / 255.0f, color[2] / 255.0f, 1.0f);
    }
}
