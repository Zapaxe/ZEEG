package com.zapaxe.zeeg.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zapaxe.zeeg.ZEEG;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class GlintConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("zeeg.json");
    private static int red = 150;
    private static int green = 75;
    private static int blue = 200;

    public static int getRed() { return red; }
    public static int getGreen() { return green; }
    public static int getBlue() { return blue; }

    public static void setRed(int r) { red = clamp(r); }
    public static void setGreen(int g) { green = clamp(g); }
    public static void setBlue(int b) { blue = clamp(b); }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public static String getShaderVec4() {
        float r = red / 255.0f;
        float g = green / 255.0f;
        float b = blue / 255.0f;
        return String.format("%.2f, %.2f, %.2f", r, g, b);
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
                GSON.toJson(new Data(red, green, blue), writer);
            }
        } catch (IOException e) {
            ZEEG.LOGGER.error("Failed to save config", e);
        }
    }

    private static class Data {
        int red, green, blue;
        Data(int r, int g, int b) { red = r; green = g; blue = b; }
    }
}
