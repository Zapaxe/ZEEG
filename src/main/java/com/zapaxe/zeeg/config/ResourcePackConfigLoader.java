package com.zapaxe.zeeg.config;

import com.zapaxe.zeeg.ZEEG;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class ResourcePackConfigLoader implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath("zeeg", "resource_pack_config");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        try {
            List<Resource> resources = manager.getResourceStack(Identifier.fromNamespaceAndPath("zeeg", "config.zg"));
            List<GlintConfig.PackData> packs = new ArrayList<>();
            for (Resource resource : resources) {
                try (BufferedReader reader = resource.openAsReader()) {
                    packs.add(GlintConfig.readPackConfig(resource.sourcePackId(), reader));
                }
            }
            GlintConfig.setResourcePackConfigs(packs);
            if (!packs.isEmpty()) {
                ZEEG.LOGGER.info("Loaded config.zg from {} resource pack(s)", packs.size());
            }
        } catch (Exception e) {
            ZEEG.LOGGER.error("Failed to load config.zg from resource packs", e);
        }
    }
}
