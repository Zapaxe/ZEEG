package com.zapaxe.zeeg.mixin;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hasGlint", at = @At("RETURN"), cancellable = true)
    private void zeeg$fixGlintOverride(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        ItemStack self = (ItemStack) (Object) this;
        Boolean override = self.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        if (override == null || !override) return;
        ComponentMap defaults = self.getDefaultComponents();
        Boolean defaultOverride = defaults.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        if (defaultOverride != null && defaultOverride) return;
        if (self.hasEnchantments()) return;
        cir.setReturnValue(false);
    }
}
