package com.zapaxe.zeeg.mixin;

import com.zapaxe.zeeg.NamedColorAccess;
import com.zapaxe.zeeg.GlintComponent;
import com.zapaxe.zeeg.config.GlintConfig;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public class ItemModelManagerMixin {
    @Inject(method = "updateForTopItem", at = @At("RETURN"))
    private void zeeg$captureNamedColor(ItemStackRenderState state, ItemStack stack, ItemDisplayContext displayContext, Level world, ItemOwner owner, int seed, CallbackInfo ci) {
        int r = -1, g = -1, b = -1, s = 255, spd = 25, mode = 0, r2 = 255, g2 = 255, b2 = 255;
        boolean rainbow = false;
        if (!stack.isEmpty()) {
            GlintComponent comp = stack.get(GlintComponent.TYPE);
            if (comp != null) {
                r = comp.r(); g = comp.g(); b = comp.b(); s = comp.strength();
                mode = comp.cycleMode(); spd = comp.speed();
                r2 = comp.r2(); g2 = comp.g2(); b2 = comp.b2();
                rainbow = (mode == 1);
            }
            Component customName = stack.get(DataComponents.CUSTOM_NAME);
            if (customName != null) {
                String name = customName.getString();
                GlintConfig.NamedColor match = GlintConfig.matchName(name);
                if (match != null) {
                    r = match.getRed(); g = match.getGreen(); b = match.getBlue(); s = match.getStrength();
                    rainbow = match.isRainbow(); spd = match.getRainbowSpeed(); mode = match.getCycleMode();
                    r2 = match.getRed2(); g2 = match.getGreen2(); b2 = match.getBlue2();
                }
            }
            if (r == -1) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                GlintConfig.ItemColor itemMatch = GlintConfig.matchItem(itemId);
                if (itemMatch != null) {
                    r = itemMatch.getRed(); g = itemMatch.getGreen(); b = itemMatch.getBlue(); s = itemMatch.getStrength();
                    rainbow = itemMatch.isRainbow(); spd = itemMatch.getRainbowSpeed(); mode = itemMatch.getCycleMode();
                    r2 = itemMatch.getRed2(); g2 = itemMatch.getGreen2(); b2 = itemMatch.getBlue2();
                }
            }
        }
        ((NamedColorAccess) state).zeeg$setNamedColor(r, g, b, s, rainbow, spd, mode, r2, g2, b2);
        if (r != -1) {
            state.appendModelIdentityElement(java.util.List.of(r, g, b, r2, g2, b2, mode));
        }
    }
}
