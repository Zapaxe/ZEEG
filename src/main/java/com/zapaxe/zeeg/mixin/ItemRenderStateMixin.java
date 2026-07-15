package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.NamedColorAccess;
import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemRenderState.class)
public abstract class ItemRenderStateMixin implements NamedColorAccess {
    @Unique
    private int[] zeeg$namedColor;

    @Override
    public void zeeg$setNamedColor(int r, int g, int b) {
        zeeg$namedColor = new int[]{r, g, b};
    }

    @Override
    public int[] zeeg$getNamedColor() {
        return zeeg$namedColor;
    }
}
