package com.zapaxe.zeeg.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zapaxe.zeeg.ZEEG;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

public class GlintConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("zeeg.json");
    private static int red = 150;
    private static int green = 75;
    private static int blue = 200;
    private static final List<NamedColor> namedColors = new ArrayList<>();
    private static final List<ItemColor> itemColors = new ArrayList<>();

    private static boolean packOverridesEnabled = false;
    private static final List<PackData> packsData = new ArrayList<>();
    private static final Set<String> enabledPackIds = new HashSet<>();

    public static int getRed() {
        for (int i = packsData.size() - 1; i >= 0; i--) {
            if (packOverridesEnabled && enabledPackIds.contains(packsData.get(i).packId))
                return packsData.get(i).red;
        }
        return red;
    }
    public static int getGreen() {
        for (int i = packsData.size() - 1; i >= 0; i--) {
            if (packOverridesEnabled && enabledPackIds.contains(packsData.get(i).packId))
                return packsData.get(i).green;
        }
        return green;
    }
    public static int getBlue() {
        for (int i = packsData.size() - 1; i >= 0; i--) {
            if (packOverridesEnabled && enabledPackIds.contains(packsData.get(i).packId))
                return packsData.get(i).blue;
        }
        return blue;
    }

    public static int getFileRed() { return red; }
    public static int getFileGreen() { return green; }
    public static int getFileBlue() { return blue; }

    public static void setRed(int r) { red = clamp(r); }
    public static void setGreen(int g) { green = clamp(g); }
    public static void setBlue(int b) { blue = clamp(b); }

    public static List<NamedColor> getNamedColors() {
        return namedColors;
    }
    public static List<ItemColor> getItemColors() {
        return itemColors;
    }

    public static boolean isPackOverridesEnabled() { return packOverridesEnabled; }
    public static void setPackOverridesEnabled(boolean v) { packOverridesEnabled = v; }

    public static boolean isPackEnabled(String packId) { return enabledPackIds.contains(packId); }
    public static void setPackEnabled(String packId, boolean en) {
        if (en) enabledPackIds.add(packId); else enabledPackIds.remove(packId);
    }

    public static List<PackData> getPacksData() { return packsData; }

    public static void setResourcePackConfigs(List<PackData> packs) {
        packsData.clear();
        packsData.addAll(packs);
        Set<String> current = new HashSet<>();
        for (PackData pd : packs) {
            current.add(pd.packId);
            enabledPackIds.add(pd.packId);
        }
        enabledPackIds.retainAll(current);
    }

    public static void clearResourcePackData() {
        packsData.clear();
    }

    static PackData readPackConfig(String packId, java.io.Reader reader) {
        Data data = GSON.fromJson(reader, Data.class);
        PackData pd = new PackData();
        pd.packId = packId;
        if (data != null) {
            pd.red = clamp(data.red);
            pd.green = clamp(data.green);
            pd.blue = clamp(data.blue);
            if (data.namedColors != null) pd.namedColors.addAll(data.namedColors);
            if (data.itemColors != null) pd.itemColors.addAll(data.itemColors);
        }
        return pd;
    }

    public static class PackData {
        String packId;
        int red = 150, green = 75, blue = 200;
        final List<NamedColor> namedColors = new ArrayList<>();
        final List<ItemColor> itemColors = new ArrayList<>();
        PackData() {}
        public String getPackId() { return packId; }
        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
        public List<NamedColor> getNamedColors() { return namedColors; }
        public List<ItemColor> getItemColors() { return itemColors; }
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public static String getShaderVec4() {
        float r = getRed() / 255.0f;
        float g = getGreen() / 255.0f;
        float b = getBlue() / 255.0f;
        return String.format("%.2f, %.2f, %.2f", r, g, b);
    }

    public static NamedColor matchName(String customName) {
        if (customName == null || customName.isEmpty()) return null;
        if (packOverridesEnabled) {
            for (int i = packsData.size() - 1; i >= 0; i--) {
                if (enabledPackIds.contains(packsData.get(i).packId)) {
                    for (NamedColor nc : packsData.get(i).namedColors) {
                        if (nc.name.equals(customName)) return nc;
                    }
                }
            }
        }
        for (NamedColor nc : namedColors) {
            if (nc.name.equals(customName)) return nc;
        }
        return null;
    }

    private static final Set<String> warnedItems = new HashSet<>();

    public static ItemColor matchItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        if (packOverridesEnabled) {
            for (int i = packsData.size() - 1; i >= 0; i--) {
                if (enabledPackIds.contains(packsData.get(i).packId)) {
                    for (ItemColor ic : packsData.get(i).itemColors) {
                        if (ic.itemId.equals(itemId)) return ic;
                    }
                }
            }
        }
        for (ItemColor ic : itemColors) {
            if (ic.itemId.equals(itemId)) return ic;
        }
        if (!itemId.startsWith("minecraft:") && warnedItems.add(itemId)) {
            StringBuilder sb = new StringBuilder();
            for (ItemColor ic : itemColors) {
                if (sb.length() > 0) sb.append(", ");
                sb.append("'").append(ic.itemId).append("'");
            }
            ZEEG.LOGGER.warn("No item match for '{}'. Configured items: [{}]", itemId, sb);
        }
        return null;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (java.io.Reader reader = Files.newBufferedReader(PATH)) {
                    Data data = GSON.fromJson(reader, Data.class);
                    if (data != null) {
                        packOverridesEnabled = data.packOverridesEnabled;
                        enabledPackIds.clear();
                        if (data.enabledPacks != null) enabledPackIds.addAll(data.enabledPacks);
                        red = clamp(data.red);
                        green = clamp(data.green);
                        blue = clamp(data.blue);
                        namedColors.clear();
                        if (data.namedColors != null) namedColors.addAll(data.namedColors);
                        itemColors.clear();
                        if (data.itemColors != null) itemColors.addAll(data.itemColors);
                    }
                }
            }
        } catch (IOException e) {
            ZEEG.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (java.io.Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(new Data(red, green, blue, packOverridesEnabled, new ArrayList<>(enabledPackIds), namedColors, itemColors), writer);
            }
        } catch (IOException e) {
            ZEEG.LOGGER.error("Failed to save config", e);
        }
    }

    public static class NamedColor {
        String name;
        int red, green, blue;
        NamedColor() {}
        public NamedColor(String name, int r, int g, int b) {
            this.name = name; red = r; green = g; blue = b;
        }
        public String getName() { return name; }
        public void setName(String n) { name = n; }
        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
        public void setRed(int r) { red = clamp(r); }
        public void setGreen(int g) { green = clamp(g); }
        public void setBlue(int b) { blue = clamp(b); }
    }

    public static class ItemColor {
        String itemId;
        int red, green, blue;
        ItemColor() {}
        public ItemColor(String itemId, int r, int g, int b) {
            this.itemId = itemId; red = r; green = g; blue = b;
        }
        public String getItemId() { return itemId; }
        public void setItemId(String id) { itemId = id; }
        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
        public void setRed(int r) { red = clamp(r); }
        public void setGreen(int g) { green = clamp(g); }
        public void setBlue(int b) { blue = clamp(b); }
    }

    private static class Data {
        int red, green, blue;
        boolean packOverridesEnabled;
        List<String> enabledPacks;
        List<NamedColor> namedColors;
        List<ItemColor> itemColors;
        Data() {}
        Data(int r, int g, int b, boolean poe, List<String> ep, List<NamedColor> nc, List<ItemColor> ic) {
            red = r; green = g; blue = b; packOverridesEnabled = poe;
            enabledPacks = ep; namedColors = nc; itemColors = ic;
        }
    }
}
