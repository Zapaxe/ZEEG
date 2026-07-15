package com.zapaxe.zeeg;

import com.zapaxe.zeeg.config.GlintConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZEEG implements ModInitializer {
    public static final String MOD_ID = "zeeg";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        GlintConfig.load();
        LOGGER.info("Zap's Enhanced Enchantment Glints initialized");
    }
}
