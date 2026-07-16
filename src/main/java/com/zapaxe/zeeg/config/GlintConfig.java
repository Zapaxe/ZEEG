package com.zapaxe.zeeg.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zapaxe.zeeg.ZEEG;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public class GlintConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("zeeg.json");
    private static int red = 150;
    private static int green = 75;
    private static int blue = 200;
    private static final List<NamedColor> namedColors = new ArrayList<>();
    private static final List<ItemColor> itemColors = new ArrayList<>();

    public static int getRed() { return red; }
    public static int getGreen() { return green; }
    public static int getBlue() { return blue; }

    public static void setRed(int r) { red = clamp(r); }
    public static void setGreen(int g) { green = clamp(g); }
    public static void setBlue(int b) { blue = clamp(b); }

    public static List<NamedColor> getNamedColors() { return namedColors; }
    public static List<ItemColor> getItemColors() { return itemColors; }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public static String getShaderVec4() {
        float r = red / 255.0f;
        float g = green / 255.0f;
        float b = blue / 255.0f;
        return String.format("%.2f, %.2f, %.2f", r, g, b);
    }

    public static NamedColor matchName(String customName) {
        if (customName == null || customName.isEmpty()) return null;
        for (NamedColor nc : namedColors) {
            if (nc.name.equals(customName)) return nc;
        }
        return null;
    }

    public static ItemColor matchItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        for (ItemColor ic : itemColors) {
            if (ic.itemId.equals(itemId)) return ic;
        }
        return null;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (java.io.Reader reader = Files.newBufferedReader(PATH)) {
                    Data data = GSON.fromJson(reader, Data.class);
                    if (data != null) {
                        red = clamp(data.red);
                        green = clamp(data.green);
                        blue = clamp(data.blue);
                        namedColors.clear();
                        if (data.namedColors != null) {
                            namedColors.addAll(data.namedColors);
                        }
                        itemColors.clear();
                        if (data.itemColors != null) {
                            itemColors.addAll(data.itemColors);
                        }
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
                GSON.toJson(new Data(red, green, blue, namedColors, itemColors), writer);
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
        List<NamedColor> namedColors;
        List<ItemColor> itemColors;
        Data() {}
        Data(int r, int g, int b, List<NamedColor> nc, List<ItemColor> ic) {
            red = r; green = g; blue = b; namedColors = nc; itemColors = ic;
        }
    }
}
