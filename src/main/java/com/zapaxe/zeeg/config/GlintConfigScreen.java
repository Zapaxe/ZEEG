package com.zapaxe.zeeg.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GlintConfigScreen extends Screen {
    private static final Text TITLE = Text.literal("Glint Color Config");
    private final Screen parent;
    private TextFieldWidget redField;
    private TextFieldWidget greenField;
    private TextFieldWidget blueField;
    private TextFieldWidget hexField;
    private boolean updating = false;
    private int red = GlintConfig.getRed();
    private int green = GlintConfig.getGreen();
    private int blue = GlintConfig.getBlue();

    public GlintConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int fieldX = cx - 55;

        redField = new TextFieldWidget(textRenderer, fieldX, 40, 110, 20, Text.literal("Red"));
        redField.setText(String.valueOf(red));
        redField.setChangedListener(s -> updateFromRgbFields());
        addDrawableChild(redField);

        greenField = new TextFieldWidget(textRenderer, fieldX, 70, 110, 20, Text.literal("Green"));
        greenField.setText(String.valueOf(green));
        greenField.setChangedListener(s -> updateFromRgbFields());
        addDrawableChild(greenField);

        blueField = new TextFieldWidget(textRenderer, fieldX, 100, 110, 20, Text.literal("Blue"));
        blueField.setText(String.valueOf(blue));
        blueField.setChangedListener(s -> updateFromRgbFields());
        addDrawableChild(blueField);

        hexField = new TextFieldWidget(textRenderer, fieldX, 130, 110, 20, Text.literal("Hex"));
        hexField.setText(toHex(red, green, blue));
        hexField.setChangedListener(s -> updateFromHexField());
        addDrawableChild(hexField);

        int presetsY = 175;
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Cyan"), btn -> setColor(0, 255, 255)
        ).dimensions(cx - 95, presetsY, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Red"), btn -> setColor(255, 0, 0)
        ).dimensions(cx - 30, presetsY, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Green"), btn -> setColor(0, 255, 0)
        ).dimensions(cx + 35, presetsY, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Blue"), btn -> setColor(0, 0, 255)
        ).dimensions(cx - 95, presetsY + 25, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.literal("White"), btn -> setColor(255, 255, 255)
        ).dimensions(cx - 30, presetsY + 25, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Original"), btn -> setColor(150, 75, 200)
        ).dimensions(cx + 35, presetsY + 25, 60, 20).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Save & Reload"), btn -> saveAndReload()
        ).dimensions(cx - 100, height - 55, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Back"), btn -> close()
        ).dimensions(cx - 100, height - 30, 200, 20).build());
    }

    private void setColor(int r, int g, int b) {
        if (updating) return;
        updating = true;
        red = r; green = g; blue = b;
        redField.setText(String.valueOf(r));
        greenField.setText(String.valueOf(g));
        blueField.setText(String.valueOf(b));
        hexField.setText(toHex(r, g, b));
        updating = false;
    }

    private static String toHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private void updateFromRgbFields() {
        if (updating) return;
        updating = true;
        try {
            int r = clamp(Integer.parseInt(redField.getText()));
            int g = clamp(Integer.parseInt(greenField.getText()));
            int b = clamp(Integer.parseInt(blueField.getText()));
            red = r; green = g; blue = b;
            hexField.setText(toHex(r, g, b));
        } catch (NumberFormatException ignored) {}
        updating = false;
    }

    private void updateFromHexField() {
        if (updating) return;
        updating = true;
        String t = hexField.getText().trim();
        if (t.startsWith("#") || t.startsWith("0x")) {
            t = t.replaceFirst("^[#0][xX]?", "");
        }
        if (t.length() == 6) {
            try {
                int color = Integer.parseInt(t, 16);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                red = r; green = g; blue = b;
                redField.setText(String.valueOf(r));
                greenField.setText(String.valueOf(g));
                blueField.setText(String.valueOf(b));
            } catch (NumberFormatException ignored) {}
        }
        updating = false;
    }

    private void saveAndReload() {
        GlintConfig.setRed(red);
        GlintConfig.setGreen(green);
        GlintConfig.setBlue(blue);
        GlintConfig.save();
        MinecraftClient.getInstance().reloadResources();
        close();
    }

    @Override
    public void close() {
        GlintConfig.setRed(red);
        GlintConfig.setGreen(green);
        GlintConfig.setBlue(blue);
        GlintConfig.save();
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        int cx = width / 2;

        ctx.drawCenteredTextWithShadow(textRenderer, TITLE, cx, 15, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Color").formatted(Formatting.GRAY), cx, 28, 0xFFAAAAAA);

        ctx.drawTextWithShadow(textRenderer, Text.literal("R:"), cx - 90, 44, 0xFFFF5555);
        ctx.drawTextWithShadow(textRenderer, Text.literal("G:"), cx - 90, 74, 0xFF55FF55);
        ctx.drawTextWithShadow(textRenderer, Text.literal("B:"), cx - 90, 104, 0xFF5555FF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Hex:"), cx - 90, 134, 0xFFFFFFFF);

        int previewColor = 0xFF000000 | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
        int px = cx + 65;
        ctx.fill(px, 40, px + 55, 150, previewColor);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Preview").formatted(Formatting.GRAY), px + 27, 158, 0xFFAAAAAA);

        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Presets").formatted(Formatting.GRAY), cx, 165, 0xFFAAAAAA);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
