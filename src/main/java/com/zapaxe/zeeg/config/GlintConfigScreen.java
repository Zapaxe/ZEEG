package com.zapaxe.zeeg.config;

import java.util.List;
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
    
    // Main Screen Widgets
    private TextFieldWidget redField;
    private TextFieldWidget greenField;
    private TextFieldWidget blueField;
    private TextFieldWidget hexField;
    private TextFieldWidget nameField;
    
    // State Variables
    private boolean updating = false;
    private int red = GlintConfig.getRed();
    private int green = GlintConfig.getGreen();
    private int blue = GlintConfig.getBlue();

    // Popup State & Widgets
    private boolean showPopup = false;
    private String popupName = "";
    private int popupRed = 255;
    private int popupGreen = 255;
    private int popupBlue = 255;
    private TextFieldWidget popupRedField;
    private TextFieldWidget popupGreenField;
    private TextFieldWidget popupBlueField;
    private TextFieldWidget popupHexField;

    public GlintConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;

        if (showPopup) {
            // Popup inputs
            popupRedField = new TextFieldWidget(textRenderer, cx - 110, cy - 65, 60, 20, Text.literal("Red"));
            popupRedField.setText(String.valueOf(popupRed));
            popupRedField.setChangedListener(s -> updatePopupFromRgbFields());
            addDrawableChild(popupRedField);

            popupGreenField = new TextFieldWidget(textRenderer, cx - 110, cy - 40, 60, 20, Text.literal("Green"));
            popupGreenField.setText(String.valueOf(popupGreen));
            popupGreenField.setChangedListener(s -> updatePopupFromRgbFields());
            addDrawableChild(popupGreenField);

            popupBlueField = new TextFieldWidget(textRenderer, cx - 110, cy - 15, 60, 20, Text.literal("Blue"));
            popupBlueField.setText(String.valueOf(popupBlue));
            popupBlueField.setChangedListener(s -> updatePopupFromRgbFields());
            addDrawableChild(popupBlueField);

            popupHexField = new TextFieldWidget(textRenderer, cx - 110, cy + 10, 60, 20, Text.literal("Hex"));
            popupHexField.setText(toHex(popupRed, popupGreen, popupBlue));
            popupHexField.setChangedListener(s -> updatePopupFromHexField());
            addDrawableChild(popupHexField);

            // Popup Preset Buttons
            int py = cy - 65;
            addDrawableChild(ButtonWidget.builder(Text.literal("Cyan"), btn -> setPopupColor(0, 255, 255))
                .dimensions(cx + 30, py, 55, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Red"), btn -> setPopupColor(255, 0, 0))
                .dimensions(cx + 90, py, 55, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Green"), btn -> setPopupColor(0, 255, 0))
                .dimensions(cx + 30, py + 25, 55, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Blue"), btn -> setPopupColor(0, 0, 255))
                .dimensions(cx + 90, py + 25, 55, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("White"), btn -> setPopupColor(255, 255, 255))
                .dimensions(cx + 30, py + 50, 55, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("OG"), btn -> setPopupColor(150, 75, 200))
                .dimensions(cx + 90, py + 50, 55, 20).build());

            // OK / Cancel Buttons
            addDrawableChild(ButtonWidget.builder(Text.literal("OK"), btn -> confirmPopup())
                .dimensions(cx - 90, cy + 55, 80, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn -> cancelPopup())
                .dimensions(cx + 10, cy + 55, 80, 20).build());

        } else {
            // Main Screen Left Column
            redField = new TextFieldWidget(textRenderer, cx - 150, 50, 70, 20, Text.literal("Red"));
            redField.setText(String.valueOf(red));
            redField.setChangedListener(s -> updateFromRgbFields());
            addDrawableChild(redField);

            greenField = new TextFieldWidget(textRenderer, cx - 150, 75, 70, 20, Text.literal("Green"));
            greenField.setText(String.valueOf(green));
            greenField.setChangedListener(s -> updateFromRgbFields());
            addDrawableChild(greenField);

            blueField = new TextFieldWidget(textRenderer, cx - 150, 100, 70, 20, Text.literal("Blue"));
            blueField.setText(String.valueOf(blue));
            blueField.setChangedListener(s -> updateFromRgbFields());
            addDrawableChild(blueField);

            hexField = new TextFieldWidget(textRenderer, cx - 150, 125, 70, 20, Text.literal("Hex"));
            hexField.setText(toHex(red, green, blue));
            hexField.setChangedListener(s -> updateFromHexField());
            addDrawableChild(hexField);

            // Left Column Presets
            int py = 155;
            addDrawableChild(ButtonWidget.builder(Text.literal("Cyan"), btn -> setColor(0, 255, 255))
                .dimensions(cx - 190, py, 55, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Red"), btn -> setColor(255, 0, 0))
                .dimensions(cx - 130, py, 55, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Green"), btn -> setColor(0, 255, 0))
                .dimensions(cx - 70, py, 55, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Blue"), btn -> setColor(0, 0, 255))
                .dimensions(cx - 190, py + 25, 55, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("White"), btn -> setColor(255, 255, 255))
                .dimensions(cx - 130, py + 25, 55, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("OG"), btn -> setColor(150, 75, 200))
                .dimensions(cx - 70, py + 25, 55, 20).build());

            // Main Screen Right Column (Named Colors List)
            nameField = new TextFieldWidget(textRenderer, cx + 15, 50, 125, 20, Text.literal("Name"));
            addDrawableChild(nameField);

            addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> addNamed())
                .dimensions(cx + 145, 50, 45, 20).build());

            List<GlintConfig.NamedColor> named = GlintConfig.getNamedColors();
            for (int i = 0; i < named.size(); i++) {
                int idx = i;
                GlintConfig.NamedColor nc = named.get(i);
                int ey = 78 + i * 22;

                addDrawableChild(ButtonWidget.builder(
                    Text.literal(nc.getName().substring(0, Math.min(nc.getName().length(), 14))),
                    btn -> {
                        setColor(nc.getRed(), nc.getGreen(), nc.getBlue());
                        nameField.setText(nc.getName());
                    }
                ).dimensions(cx + 30, ey, 125, 20).build());

                addDrawableChild(ButtonWidget.builder(
                    Text.literal("X"), btn -> {
                        GlintConfig.getNamedColors().remove(idx);
                        rebuild();
                    }
                ).dimensions(cx + 160, ey, 25, 20).build());
            }

            // Bottom Buttons
            addDrawableChild(ButtonWidget.builder(Text.literal("Save & Reload"), btn -> saveAndReload())
                .dimensions(cx - 100, height - 55, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Back"), btn -> close())
                .dimensions(cx - 100, height - 30, 200, 20).build());
        }
    }

    private void rebuild() {
        clearChildren();
        init();
    }

    private void addNamed() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) return;
        popupName = name;
        popupRed = red;
        popupGreen = green;
        popupBlue = blue;
        showPopup = true;
        nameField.setText("");
        rebuild();
    }

    private void confirmPopup() {
        GlintConfig.getNamedColors().add(new GlintConfig.NamedColor(popupName, popupRed, popupGreen, popupBlue));
        showPopup = false;
        rebuild();
    }

    private void cancelPopup() {
        showPopup = false;
        rebuild();
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

    private void setPopupColor(int r, int g, int b) {
        if (updating) return;
        updating = true;
        popupRed = r; popupGreen = g; popupBlue = b;
        popupRedField.setText(String.valueOf(r));
        popupGreenField.setText(String.valueOf(g));
        popupBlueField.setText(String.valueOf(b));
        popupHexField.setText(toHex(r, g, b));
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

    private void updatePopupFromRgbFields() {
        if (updating) return;
        updating = true;
        try {
            int r = clamp(Integer.parseInt(popupRedField.getText()));
            int g = clamp(Integer.parseInt(popupGreenField.getText()));
            int b = clamp(Integer.parseInt(popupBlueField.getText()));
            popupRed = r; popupGreen = g; popupBlue = b;
            popupHexField.setText(toHex(r, g, b));
        } catch (NumberFormatException ignored) {}
        updating = false;
    }

    private void updatePopupFromHexField() {
        if (updating) return;
        updating = true;
        String t = popupHexField.getText().trim();
        if (t.startsWith("#") || t.startsWith("0x")) {
            t = t.replaceFirst("^[#0][xX]?", "");
        }
        if (t.length() == 6) {
            try {
                int color = Integer.parseInt(t, 16);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                popupRed = r; popupGreen = g; popupBlue = b;
                popupRedField.setText(String.valueOf(r));
                popupGreenField.setText(String.valueOf(g));
                popupBlueField.setText(String.valueOf(b));
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
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        super.renderBackground(ctx, mx, my, delta);
        int cx = width / 2;
        int cy = height / 2;

        if (showPopup) {
            // Dark overlay
            ctx.fill(0, 0, width, height, 0x55000000);

            // Popup container
            ctx.fill(cx - 150, cy - 95, cx + 150, cy + 95, 0xFF181818);
            ctx.drawStrokedRectangle(cx - 150, cy - 95, 300, 190, 0xFF666666);

            // Popup Title
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Pick Color for: " + popupName).formatted(Formatting.YELLOW), cx, cy - 85, 0xFFFFFFFF);

            // Labels for fields
            ctx.drawTextWithShadow(textRenderer, Text.literal("R:"), cx - 130, cy - 61, 0xFFFF5555);
            ctx.drawTextWithShadow(textRenderer, Text.literal("G:"), cx - 130, cy - 36, 0xFF55FF55);
            ctx.drawTextWithShadow(textRenderer, Text.literal("B:"), cx - 130, cy - 11, 0xFF5555FF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("Hex:"), cx - 137, cy + 14, 0xFFFFFFFF);

            // Color Preview Box
            int previewColor = 0xFF000000 | (clamp(popupRed) << 16) | (clamp(popupGreen) << 8) | clamp(popupBlue);
            ctx.fill(cx - 40, cy - 65, cx + 20, cy + 30, previewColor);
            ctx.drawStrokedRectangle(cx - 40, cy - 65, 60, 95, 0xFF888888);

        } else {
            // General Title
            ctx.drawCenteredTextWithShadow(textRenderer, TITLE, cx, 12, 0xFFFFFFFF);

            // Left panel (General Color)
            ctx.fill(cx - 200, 30, cx - 5, 215, 0x55000000);
            ctx.drawStrokedRectangle(cx - 200, 30, 195, 185, 0xFF555555);
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("General Color").formatted(Formatting.BOLD, Formatting.GRAY), cx - 102, 35, 0xFFFFFFFF);

            ctx.drawTextWithShadow(textRenderer, Text.literal("R:"), cx - 190, 54, 0xFFFF5555);
            ctx.drawTextWithShadow(textRenderer, Text.literal("G:"), cx - 190, 79, 0xFF55FF55);
            ctx.drawTextWithShadow(textRenderer, Text.literal("B:"), cx - 190, 104, 0xFF5555FF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("Hex:"), cx - 195, 129, 0xFFFFFFFF);

            int previewColor = 0xFF000000 | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
            ctx.fill(cx - 70, 50, cx - 15, 145, previewColor);
            ctx.drawStrokedRectangle(cx - 70, 50, 55, 95, 0xFF555555);

            // Right panel (Named Colors)
            ctx.fill(cx + 5, 30, cx + 200, 215, 0x55000000);
            ctx.drawStrokedRectangle(cx + 5, 30, 195, 185, 0xFF555555);
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Named Colors").formatted(Formatting.BOLD, Formatting.GRAY), cx + 102, 35, 0xFFFFFFFF);

            // Colored indicator squares in list
            List<GlintConfig.NamedColor> named = GlintConfig.getNamedColors();
            for (int i = 0; i < named.size(); i++) {
                GlintConfig.NamedColor nc = named.get(i);
                int ey = 78 + i * 22;
                int colorVal = 0xFF000000 | (nc.getRed() << 16) | (nc.getGreen() << 8) | nc.getBlue();
                ctx.drawStrokedRectangle(cx + 10, ey + 3, 14, 14, 0xFF888888);
                ctx.fill(cx + 11, ey + 4, cx + 23, ey + 16, colorVal);
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
