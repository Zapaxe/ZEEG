package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.NamedColorAccess;
import com.zapaxe.zeeg.config.GlintConfig;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.text.Text;
import net.minecraft.util.HeldItemContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
    @Inject(method = "clearAndUpdate", at = @At("RETURN"))
    private void zeeg$captureNamedColor(ItemRenderState state, ItemStack stack, ItemDisplayContext displayContext, World world, HeldItemContext heldItemContext, int seed, CallbackInfo ci) {
        int r = -1, g = -1, b = -1, s = 255;
        boolean rainbow = false;
        if (!stack.isEmpty()) {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName != null) {
                String name = customName.getString();
                GlintConfig.NamedColor match = GlintConfig.matchName(name);
                if (match != null) {
                    r = match.getRed(); g = match.getGreen(); b = match.getBlue(); s = match.getStrength(); rainbow = match.isRainbow();
                }
            }
            if (r == -1) {
                String itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
                GlintConfig.ItemColor itemMatch = GlintConfig.matchItem(itemId);
                if (itemMatch != null) {
                    r = itemMatch.getRed(); g = itemMatch.getGreen(); b = itemMatch.getBlue(); s = itemMatch.getStrength(); rainbow = itemMatch.isRainbow();
                }
            }
        }
        ((NamedColorAccess) state).zeeg$setNamedColor(r, g, b, s, rainbow);
        if (r != -1) {
            state.addModelKey(java.util.List.of(r, g, b));
        }
    }
}
