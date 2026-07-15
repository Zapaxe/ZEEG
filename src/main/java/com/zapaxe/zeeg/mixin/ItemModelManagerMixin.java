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
        if (stack.isEmpty()) return;
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return;
        String name = customName.getString();
        GlintConfig.NamedColor match = GlintConfig.matchName(name);
        if (match != null) {
            ((NamedColorAccess) state).zeeg$setNamedColor(match.getRed(), match.getGreen(), match.getBlue());
        }
    }
}
