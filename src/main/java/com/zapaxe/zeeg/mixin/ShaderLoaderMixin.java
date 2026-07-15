package com.zapaxe.zeeg.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.shaders.ShaderType;
import com.zapaxe.zeeg.config.GlintConfig;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gl.GlImportProcessor;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShaderLoader.class)
public class ShaderLoaderMixin {
    @Redirect(
        method = "loadShaderSource",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gl/GlImportProcessor;readSource(Ljava/lang/String;)Ljava/util/List;"
        )
    )
    private static List<String> zeeg$redirectReadSource(
        GlImportProcessor processor,
        String source,
        Identifier id,
        Resource resource,
        ShaderType type,
        Map<Identifier, Resource> allResources,
        ImmutableMap.Builder builder
    ) {
        if (id.getPath().contains("glint.fsh")) {
            String userRGB = GlintConfig.getShaderVec4();
            source = source.replace(
                "vec4 color = texture(Sampler0, texCoord0) * ColorModulator;",
                "vec4 _t = texture(Sampler0, texCoord0); float _l = max(_t.r, max(_t.g, _t.b)); vec4 color = vec4(_l * vec3(" + userRGB + "), _t.a * ColorModulator.a);"
            );
        }
        return processor.readSource(source);
    }
}
