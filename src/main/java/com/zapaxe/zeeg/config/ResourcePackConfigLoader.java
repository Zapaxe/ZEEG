package com.zapaxe.zeeg.config;

import com.zapaxe.zeeg.ZEEG;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class ResourcePackConfigLoader implements IdentifiableResourceReloadListener, SynchronousResourceReloader {
    @Override
    public Identifier getFabricId() {
        return Identifier.of("zeeg", "resource_pack_config");
    }

    @Override
    public void reload(ResourceManager manager) {
        try {
            List<Resource> resources = manager.getAllResources(Identifier.of("zeeg", "config.zg"));
            List<GlintConfig.PackData> packs = new ArrayList<>();
            for (Resource resource : resources) {
                try (BufferedReader reader = new BufferedReader(resource.getReader())) {
                    packs.add(GlintConfig.readPackConfig(resource.getPackId(), reader));
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
