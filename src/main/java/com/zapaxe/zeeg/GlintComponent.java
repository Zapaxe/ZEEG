package com.zapaxe.zeeg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public record GlintComponent(int r, int g, int b, int strength, int cycleMode, int speed, int r2, int g2, int b2) {
    public static final Codec<GlintComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("r", 255).forGetter(GlintComponent::r),
        Codec.INT.optionalFieldOf("g", 255).forGetter(GlintComponent::g),
        Codec.INT.optionalFieldOf("b", 255).forGetter(GlintComponent::b),
        Codec.INT.optionalFieldOf("strength", 255).forGetter(GlintComponent::strength),
        Codec.INT.optionalFieldOf("cycle_mode", 0).forGetter(GlintComponent::cycleMode),
        Codec.INT.optionalFieldOf("speed", 25).forGetter(GlintComponent::speed),
        Codec.INT.optionalFieldOf("r2", 255).forGetter(GlintComponent::r2),
        Codec.INT.optionalFieldOf("g2", 255).forGetter(GlintComponent::g2),
        Codec.INT.optionalFieldOf("b2", 255).forGetter(GlintComponent::b2)
    ).apply(instance, GlintComponent::new));

    public static ComponentType<GlintComponent> TYPE;

    public static void register() {
        TYPE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("zeeg", "glint"),
            ComponentType.<GlintComponent>builder().codec(CODEC).build()
        );
    }
}
