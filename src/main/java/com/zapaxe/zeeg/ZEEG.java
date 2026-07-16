package com.zapaxe.zeeg;

import com.zapaxe.zeeg.config.GlintConfig;
import com.zapaxe.zeeg.config.ResourcePackConfigLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZEEG implements ModInitializer {
    public static final String MOD_ID = "zeeg";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        GlintComponent.register();
        GlintConfig.load();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(new ResourcePackConfigLoader());
        LOGGER.info("Zap's Enhanced Enchantment Glints initialized");
    }
}
