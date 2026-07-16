package com.zapaxe.zeeg.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShaderManager.class)
public class ShaderLoaderMixin {
    @Redirect(
        method = "loadShader",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;process(Ljava/lang/String;)Ljava/util/List;"
        )
    )
    private static List<String> zeeg$redirectReadSource(
        GlslPreprocessor processor,
        String source,
        Identifier id,
        Resource resource,
        ShaderType type,
        Map<Identifier, Resource> allResources,
        ImmutableMap.Builder builder
    ) {
        if (id.getPath().contains("glint.fsh")) {
            source = source.replace(
                "vec4 color = texture(Sampler0, texCoord0) * ColorModulator;",
                "vec4 _t = texture(Sampler0, texCoord0); float _l = max(_t.r, max(_t.g, _t.b)); vec4 color = vec4(_l * ColorModulator.rgb, _t.a * ColorModulator.a);"
            );
        }
        return processor.process(source);
    }
}
