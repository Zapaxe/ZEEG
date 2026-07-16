package com.zapaxe.zeeg.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hasFoil", at = @At("RETURN"), cancellable = true)
    private void zeeg$fixGlintOverride(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        ItemStack self = (ItemStack) (Object) this;
        Boolean override = self.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (override == null || !override) return;
        DataComponentMap defaults = self.getPrototype();
        Boolean defaultOverride = defaults.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (defaultOverride != null && defaultOverride) return;
        if (self.isEnchanted()) return;
        cir.setReturnValue(false);
    }
}
