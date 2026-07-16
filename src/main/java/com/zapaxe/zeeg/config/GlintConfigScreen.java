package com.zapaxe.zeeg.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class GlintConfigScreen extends Screen {
    private static final Text TITLE = Text.literal("Glint Color Config");
    private final Screen parent;
    
    // Tab State
    private int activeTab = 0;
    private int itemPage = 0;
    private int namedPage = 0;
    private int packPage = 0;

    // Main Tab Widgets
    private TextFieldWidget redField;
    private TextFieldWidget greenField;
    private TextFieldWidget blueField;
    private TextFieldWidget hexField;
    private StrengthSlider strengthSlider;

    // Item Tab Widgets
    private TextFieldWidget itemField;
    private String tempSearchQuery = "";
    private final List<net.minecraft.item.Item> searchMatches = new ArrayList<>();

    // Named Tab Widgets
    private TextFieldWidget nameField;
    
    // Config State
    private boolean updating = false;
    private int red = GlintConfig.getFileRed();
    private int green = GlintConfig.getFileGreen();
    private int blue = GlintConfig.getFileBlue();
    private int strength = GlintConfig.getFileStrength();
    private boolean rainbow = GlintConfig.getFileRainbow();
    private int rainbowSpeed = GlintConfig.getFileRainbowSpeed();
    private SpeedSlider speedSlider;

    // Popup Color Picker State & Widgets
    private boolean showPopup = false;
    private boolean popupIsItem = false;
    private boolean popupIsEdit = false;
    private int popupEditIndex = -1;
    private String popupName = "";
    private int popupRed = 255;
    private int popupGreen = 255;
    private int popupBlue = 255;
    private int popupStrength = 255;
    private boolean popupRainbow = false;
    private int popupRainbowSpeed = 25;
    private TextFieldWidget popupRedField;
    private TextFieldWidget popupGreenField;
    private TextFieldWidget popupBlueField;
    private TextFieldWidget popupHexField;
    private StrengthSlider popupStrSlider;
    private SpeedSlider popupSpeedSlider;

    public GlintConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;

        if (showPopup) {
            if (popupRainbow) {
                popupSpeedSlider = new SpeedSlider(cx - 110, cy - 15, 60, 20, popupRainbowSpeed, () -> popupRainbowSpeed = popupSpeedSlider.getIntValue());
                addDrawableChild(popupSpeedSlider);
            } else {
                // Popup Inputs
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
            }

            popupStrSlider = new StrengthSlider(cx - 110, cy + 35, 60, 20, popupStrength, () -> popupStrength = popupStrSlider.getIntValue());
            addDrawableChild(popupStrSlider);

            ButtonWidget popupRainbowBtn = ButtonWidget.builder(Text.literal(popupRainbow ? "ON" : "OFF"), btn -> {
                popupRainbow = !popupRainbow;
                rebuild();
            }).dimensions(cx - 110, cy + 60, 60, 20).build();
            addDrawableChild(popupRainbowBtn);

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
                .dimensions(cx - 90, cy + 90, 80, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn -> cancelPopup())
                .dimensions(cx + 10, cy + 90, 80, 20).build());

        } else {
            // Tab Selection Header
            ButtonWidget tab0 = ButtonWidget.builder(Text.literal("General"), btn -> { activeTab = 0; tempSearchQuery = ""; searchMatches.clear(); rebuild(); })
                .dimensions(cx - 155, 22, 100, 20).build();
            ButtonWidget tab1 = ButtonWidget.builder(Text.literal("Item Colors"), btn -> { activeTab = 1; tempSearchQuery = ""; searchMatches.clear(); rebuild(); })
                .dimensions(cx - 50, 22, 100, 20).build();
            ButtonWidget tab2 = ButtonWidget.builder(Text.literal("Named Colors"), btn -> { activeTab = 2; tempSearchQuery = ""; searchMatches.clear(); rebuild(); })
                .dimensions(cx + 55, 22, 100, 20).build();

            tab0.active = (activeTab != 0);
            tab1.active = (activeTab != 1);
            tab2.active = (activeTab != 2);
            ButtonWidget tab3 = ButtonWidget.builder(Text.literal("Pack Configs"), btn -> { activeTab = 3; rebuild(); })
                .dimensions(cx + 160, 22, 100, 20).build();
            tab3.active = (activeTab != 3);

            addDrawableChild(tab0);
            addDrawableChild(tab1);
            addDrawableChild(tab2);
            addDrawableChild(tab3);

            if (activeTab == 0) {
                if (rainbow) {
                    speedSlider = new SpeedSlider(cx - 130, 95, 80, 20, rainbowSpeed, () -> rainbowSpeed = speedSlider.getIntValue());
                    addDrawableChild(speedSlider);
                } else {
                    // General Settings Layout
                    redField = new TextFieldWidget(textRenderer, cx - 130, 70, 80, 20, Text.literal("Red"));
                    redField.setText(String.valueOf(red));
                    redField.setChangedListener(s -> updateFromRgbFields());
                    addDrawableChild(redField);

                    greenField = new TextFieldWidget(textRenderer, cx - 130, 95, 80, 20, Text.literal("Green"));
                    greenField.setText(String.valueOf(green));
                    greenField.setChangedListener(s -> updateFromRgbFields());
                    addDrawableChild(greenField);

                    blueField = new TextFieldWidget(textRenderer, cx - 130, 120, 80, 20, Text.literal("Blue"));
                    blueField.setText(String.valueOf(blue));
                    blueField.setChangedListener(s -> updateFromRgbFields());
                    addDrawableChild(blueField);

                    hexField = new TextFieldWidget(textRenderer, cx - 130, 145, 80, 20, Text.literal("Hex"));
                    hexField.setText(toHex(red, green, blue));
                    hexField.setChangedListener(s -> updateFromHexField());
                    addDrawableChild(hexField);
                }

                strengthSlider = new StrengthSlider(cx - 130, 170, 80, 20, strength, () -> strength = strengthSlider.getIntValue());
                addDrawableChild(strengthSlider);

                ButtonWidget rainbowBtn = ButtonWidget.builder(Text.literal(rainbow ? "ON" : "OFF"), btn -> {
                    rainbow = !rainbow;
                    rebuild();
                }).dimensions(cx - 130, 195, 80, 20).build();
                addDrawableChild(rainbowBtn);

                // Main Presets
                int py = 70;
                addDrawableChild(ButtonWidget.builder(Text.literal("Cyan"), btn -> setColor(0, 255, 255))
                    .dimensions(cx + 45, py, 55, 20).build());
                addDrawableChild(ButtonWidget.builder(Text.literal("Red"), btn -> setColor(255, 0, 0))
                    .dimensions(cx + 110, py, 55, 20).build());

                addDrawableChild(ButtonWidget.builder(Text.literal("Green"), btn -> setColor(0, 255, 0))
                    .dimensions(cx + 45, py + 25, 55, 20).build());
                addDrawableChild(ButtonWidget.builder(Text.literal("Blue"), btn -> setColor(0, 0, 255))
                    .dimensions(cx + 110, py + 25, 55, 20).build());

                addDrawableChild(ButtonWidget.builder(Text.literal("White"), btn -> setColor(255, 255, 255))
                    .dimensions(cx + 45, py + 50, 55, 20).build());
                addDrawableChild(ButtonWidget.builder(Text.literal("OG"), btn -> setColor(150, 75, 200))
                    .dimensions(cx + 110, py + 50, 55, 20).build());

            } else if (activeTab == 1) {
                // Item Specific Override Tab
                itemField = new TextFieldWidget(textRenderer, cx - 100, 65, 145, 20, Text.literal("Item ID"));
                itemField.setMaxLength(256);
                itemField.setPlaceholder(Text.literal("e.g. netherite_helmet"));
                if (tempSearchQuery != null && !tempSearchQuery.isEmpty()) {
                    itemField.setText(tempSearchQuery);
                }
                itemField.setChangedListener(s -> {
                    tempSearchQuery = s;
                    updateSearchMatches(s);
                });
                addDrawableChild(itemField);

                addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> addItemColor())
                    .dimensions(cx + 50, 65, 50, 20).build());

                List<GlintConfig.ItemColor> items = GlintConfig.getItemColors();
                int maxPages = (items.size() - 1) / 5;
                if (itemPage > maxPages) itemPage = Math.max(0, maxPages);

                int startIdx = itemPage * 5;
                int endIdx = Math.min(items.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    int idx = i;
                    GlintConfig.ItemColor ic = items.get(i);
                    int ey = 95 + (i - startIdx) * 22;

                    String dispName = ic.getItemId();
                    if (dispName.startsWith("minecraft:")) {
                        dispName = dispName.substring(10);
                    }
                    if (dispName.length() > 16) {
                        dispName = dispName.substring(0, 14) + "..";
                    }

                    addDrawableChild(ButtonWidget.builder(
                        Text.literal(dispName),
                        btn -> {
                            setColor(ic.getRed(), ic.getGreen(), ic.getBlue());
                            itemField.setText(ic.getItemId());
                        }
                    ).dimensions(cx - 80, ey, 125, 20).build());

                    addDrawableChild(ButtonWidget.builder(
                        Text.literal("X"), btn -> {
                            GlintConfig.getItemColors().remove(idx);
                            rebuild();
                        }
                    ).dimensions(cx + 50, ey, 50, 20).build());
                }

                if (items.size() > 5) {
                    addDrawableChild(ButtonWidget.builder(Text.literal("<"), btn -> { if (itemPage > 0) { itemPage--; rebuild(); } })
                        .dimensions(cx - 50, 210, 20, 20).build());
                    addDrawableChild(ButtonWidget.builder(Text.literal(">"), btn -> { if ((itemPage + 1) * 5 < items.size()) { itemPage++; rebuild(); } })
                        .dimensions(cx + 30, 210, 20, 20).build());
                }

            } else if (activeTab == 2) {
                // Named Colors Tab
                nameField = new TextFieldWidget(textRenderer, cx - 100, 65, 145, 20, Text.literal("Name"));
                nameField.setPlaceholder(Text.literal("e.g. Goated Sword"));
                addDrawableChild(nameField);

                addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> addNamed())
                    .dimensions(cx + 50, 65, 50, 20).build());

                List<GlintConfig.NamedColor> named = GlintConfig.getNamedColors();
                int maxPages = (named.size() - 1) / 5;
                if (namedPage > maxPages) namedPage = Math.max(0, maxPages);

                int startIdx = namedPage * 5;
                int endIdx = Math.min(named.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    int idx = i;
                    GlintConfig.NamedColor nc = named.get(i);
                    int ey = 95 + (i - startIdx) * 22;

                    String dispName = nc.getName();
                    if (dispName.length() > 16) {
                        dispName = dispName.substring(0, 14) + "..";
                    }

                    addDrawableChild(ButtonWidget.builder(
                        Text.literal(dispName),
                        btn -> {
                            setColor(nc.getRed(), nc.getGreen(), nc.getBlue());
                            nameField.setText(nc.getName());
                        }
                    ).dimensions(cx - 80, ey, 125, 20).build());

                    addDrawableChild(ButtonWidget.builder(
                        Text.literal("X"), btn -> {
                            GlintConfig.getNamedColors().remove(idx);
                            rebuild();
                        }
                    ).dimensions(cx + 50, ey, 50, 20).build());
                }

                if (named.size() > 5) {
                    addDrawableChild(ButtonWidget.builder(Text.literal("<"), btn -> { if (namedPage > 0) { namedPage--; rebuild(); } })
                        .dimensions(cx - 50, 210, 20, 20).build());
                    addDrawableChild(ButtonWidget.builder(Text.literal(">"), btn -> { if ((namedPage + 1) * 5 < named.size()) { namedPage++; rebuild(); } })
                        .dimensions(cx + 30, 210, 20, 20).build());
                }

            } else if (activeTab == 3) {
                // Pack Configs Tab
                addDrawableChild(ButtonWidget.builder(
                    Text.literal("Master: " + (GlintConfig.isPackOverridesEnabled() ? "ON" : "OFF")),
                    btn -> {
                        GlintConfig.setPackOverridesEnabled(!GlintConfig.isPackOverridesEnabled());
                        rebuild();
                    }
                ).dimensions(cx - 100, 65, 155, 20).build());

                List<GlintConfig.PackData> packs = GlintConfig.getPacksData();
                int maxPages = (packs.size() - 1) / 5;
                if (packPage > maxPages) packPage = Math.max(0, maxPages);

                int startIdx = packPage * 5;
                int endIdx = Math.min(packs.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    int idx = i;
                    GlintConfig.PackData pd = packs.get(i);
                    int ey = 95 + (i - startIdx) * 22;

                    String dispName = pd.getPackId();
                    if (dispName.length() > 18) {
                        dispName = dispName.substring(0, 16) + "..";
                    }

                    boolean enabled = GlintConfig.isPackEnabled(pd.getPackId());
                    addDrawableChild(ButtonWidget.builder(
                        Text.literal(dispName),
                        btn -> {}
                    ).dimensions(cx - 80, ey, 125, 20).build());

                    addDrawableChild(ButtonWidget.builder(
                        Text.literal(enabled ? "ON" : "OFF"),
                        btn -> {
                            GlintConfig.setPackEnabled(pd.getPackId(), !GlintConfig.isPackEnabled(pd.getPackId()));
                            rebuild();
                        }
                    ).dimensions(cx + 50, ey, 50, 20).build());
                }

                if (packs.size() > 5) {
                    addDrawableChild(ButtonWidget.builder(Text.literal("<"), btn -> { if (packPage > 0) { packPage--; rebuild(); } })
                        .dimensions(cx - 50, 210, 20, 20).build());
                    addDrawableChild(ButtonWidget.builder(Text.literal(">"), btn -> { if ((packPage + 1) * 5 < packs.size()) { packPage++; rebuild(); } })
                        .dimensions(cx + 30, 210, 20, 20).build());
                }
            }

            // Bottom controls
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
        popupIsItem = false;
        popupIsEdit = false;
        popupRed = red;
        popupGreen = green;
        popupBlue = blue;
        popupStrength = strength;
        popupRainbow = rainbow;
        popupRainbowSpeed = rainbowSpeed;
        showPopup = true;
        nameField.setText("");
        rebuild();
    }

    private void addItemColor() {
        String itemId = itemField.getText().trim().toLowerCase().replace(" ", "_");
        if (itemId.isEmpty()) return;
        if (!itemId.contains(":")) {
            itemId = "minecraft:" + itemId;
        }
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return;
        popupName = itemId;
        popupIsItem = true;
        popupIsEdit = false;
        popupRed = red;
        popupGreen = green;
        popupBlue = blue;
        popupStrength = strength;
        popupRainbow = rainbow;
        popupRainbowSpeed = rainbowSpeed;
        showPopup = true;
        itemField.setText("");
        tempSearchQuery = "";
        searchMatches.clear();
        rebuild();
    }

    private void updateSearchMatches(String s) {
        searchMatches.clear();
        String query = s.trim().toLowerCase().replace(" ", "_");
        if (!query.isEmpty()) {
            for (net.minecraft.item.Item item : Registries.ITEM) {
                String idStr = Registries.ITEM.getId(item).toString();
                if (idStr.contains(query)) {
                    searchMatches.add(item);
                    if (searchMatches.size() >= 5) break;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean primary) {
        double mx = click.x();
        double my = click.y();
        if (!showPopup && activeTab == 1 && !searchMatches.isEmpty()) {
            int cx = width / 2;
            if (mx >= cx - 100 && mx <= cx + 100) {
                int startY = 85;
                if (my >= startY && my < startY + searchMatches.size() * 20) {
                    int idx = ((int)my - startY) / 20;
                    if (idx >= 0 && idx < searchMatches.size()) {
                        net.minecraft.item.Item clickedItem = searchMatches.get(idx);
                        itemField.setText(Registries.ITEM.getId(clickedItem).toString());
                        tempSearchQuery = itemField.getText();
                        searchMatches.clear();
                        rebuild();
                        return true;
                    }
                }
            }
            searchMatches.clear();
        }
        if (!showPopup) {
            int cx = width / 2;
            if (activeTab == 1) {
                List<GlintConfig.ItemColor> items = GlintConfig.getItemColors();
                int startIdx = itemPage * 5;
                int endIdx = Math.min(items.size(), startIdx + 5);
                for (int i = startIdx; i < endIdx; i++) {
                    int ey = 95 + (i - startIdx) * 22;
                    if (mx >= cx - 101 && mx <= cx - 85 && my >= ey + 2 && my <= ey + 18) {
                        GlintConfig.ItemColor ic = items.get(i);
                        popupName = ic.getItemId();
                        popupIsItem = true;
                        popupIsEdit = true;
                        popupEditIndex = i;
                        popupRed = ic.getRed();
                        popupGreen = ic.getGreen();
                        popupBlue = ic.getBlue();
                        popupStrength = ic.getStrength();
                        popupRainbow = ic.isRainbow();
                        popupRainbowSpeed = ic.getRainbowSpeed();
                        showPopup = true;
                        rebuild();
                        return true;
                    }
                }
            } else if (activeTab == 2) {
                List<GlintConfig.NamedColor> named = GlintConfig.getNamedColors();
                int startIdx = namedPage * 5;
                int endIdx = Math.min(named.size(), startIdx + 5);
                for (int i = startIdx; i < endIdx; i++) {
                    int ey = 95 + (i - startIdx) * 22;
                    if (mx >= cx - 101 && mx <= cx - 85 && my >= ey + 2 && my <= ey + 18) {
                        GlintConfig.NamedColor nc = named.get(i);
                        popupName = nc.getName();
                        popupIsItem = false;
                        popupIsEdit = true;
                        popupEditIndex = i;
                        popupRed = nc.getRed();
                        popupGreen = nc.getGreen();
                        popupBlue = nc.getBlue();
                        popupStrength = nc.getStrength();
                        popupRainbow = nc.isRainbow();
                        popupRainbowSpeed = nc.getRainbowSpeed();
                        showPopup = true;
                        rebuild();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(click, primary);
    }

    private void confirmPopup() {
        if (popupIsEdit) {
            if (popupIsItem) {
                GlintConfig.ItemColor ic = GlintConfig.getItemColors().get(popupEditIndex);
                ic.setRed(popupRed);
                ic.setGreen(popupGreen);
                ic.setBlue(popupBlue);
                ic.setStrength(popupStrength);
                ic.setRainbow(popupRainbow);
                ic.setRainbowSpeed(popupRainbowSpeed);
            } else {
                GlintConfig.NamedColor nc = GlintConfig.getNamedColors().get(popupEditIndex);
                nc.setRed(popupRed);
                nc.setGreen(popupGreen);
                nc.setBlue(popupBlue);
                nc.setStrength(popupStrength);
                nc.setRainbow(popupRainbow);
                nc.setRainbowSpeed(popupRainbowSpeed);
            }
        } else {
            if (popupIsItem) {
                GlintConfig.getItemColors().add(new GlintConfig.ItemColor(popupName, popupRed, popupGreen, popupBlue, popupStrength, popupRainbow, popupRainbowSpeed));
            } else {
                GlintConfig.getNamedColors().add(new GlintConfig.NamedColor(popupName, popupRed, popupGreen, popupBlue, popupStrength, popupRainbow, popupRainbowSpeed));
            }
        }
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
        if (redField != null) redField.setText(String.valueOf(r));
        if (greenField != null) greenField.setText(String.valueOf(g));
        if (blueField != null) blueField.setText(String.valueOf(b));
        if (hexField != null) hexField.setText(toHex(r, g, b));
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
        GlintConfig.setStrength(strength);
        GlintConfig.setRainbow(rainbow);
        GlintConfig.setRainbowSpeed(rainbowSpeed);
        GlintConfig.save();
        MinecraftClient.getInstance().reloadResources();
        close();
    }

    @Override
    public void close() {
        GlintConfig.setRed(red);
        GlintConfig.setGreen(green);
        GlintConfig.setBlue(blue);
        GlintConfig.setStrength(strength);
        GlintConfig.setRainbow(rainbow);
        GlintConfig.setRainbowSpeed(rainbowSpeed);
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
            ctx.fill(cx - 150, cy - 95, cx + 150, cy + 135, 0xFF181818);
            ctx.drawStrokedRectangle(cx - 150, cy - 95, 300, 230, 0xFF666666);

            // Popup Title
            String nameText = popupIsItem && popupName.contains(":") ? popupName.substring(popupName.indexOf(":") + 1) : popupName;
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Pick Color for: " + nameText).formatted(Formatting.YELLOW), cx, cy - 85, 0xFFFFFFFF);

            // Labels for fields
            if (popupRainbow) {
                ctx.drawTextWithShadow(textRenderer, Text.literal("Spd:"), cx - 137, cy - 11, 0xFFFFFFFF);
            } else {
                ctx.drawTextWithShadow(textRenderer, Text.literal("R:"), cx - 130, cy - 61, 0xFFFF5555);
                ctx.drawTextWithShadow(textRenderer, Text.literal("G:"), cx - 130, cy - 36, 0xFF55FF55);
                ctx.drawTextWithShadow(textRenderer, Text.literal("B:"), cx - 130, cy - 11, 0xFF5555FF);
                ctx.drawTextWithShadow(textRenderer, Text.literal("Hex:"), cx - 137, cy + 14, 0xFFFFFFFF);
            }
            ctx.drawTextWithShadow(textRenderer, Text.literal("Str:"), cx - 137, cy + 39, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("Rain:"), cx - 142, cy + 64, 0xFFFFFFFF);

            // Color Preview Box
            int previewColor;
            if (popupRainbow) {
                long time = System.currentTimeMillis();
                float hue = (float) (((time * popupRainbowSpeed) % 100000L) / 100000.0);
                previewColor = 0xFF000000 | hsvToRgbInt(hue, 1.0f, 1.0f);
            } else {
                previewColor = 0xFF000000 | (clamp(popupRed) << 16) | (clamp(popupGreen) << 8) | clamp(popupBlue);
            }
            ctx.fill(cx - 40, cy - 65, cx + 20, cy + 55, previewColor);
            ctx.drawStrokedRectangle(cx - 40, cy - 65, 60, 120, 0xFF888888);

        } else {
            // General Title
            ctx.drawCenteredTextWithShadow(textRenderer, TITLE, cx, 8, 0xFFFFFFFF);

            if (activeTab == 0) {
                // Tab 0: General Color Panel
                ctx.fill(cx - 180, 60, cx + 180, 220, 0x55000000);
                ctx.drawStrokedRectangle(cx - 180, 60, 360, 160, 0xFF555555);

                if (rainbow) {
                    ctx.drawTextWithShadow(textRenderer, Text.literal("Spd:"), cx - 172, 99, 0xFFFFFFFF);
                } else {
                    ctx.drawTextWithShadow(textRenderer, Text.literal("R:"), cx - 170, 74, 0xFFFF5555);
                    ctx.drawTextWithShadow(textRenderer, Text.literal("G:"), cx - 170, 99, 0xFF55FF55);
                    ctx.drawTextWithShadow(textRenderer, Text.literal("B:"), cx - 170, 124, 0xFF5555FF);
                    ctx.drawTextWithShadow(textRenderer, Text.literal("Hex:"), cx - 175, 149, 0xFFFFFFFF);
                }
                ctx.drawTextWithShadow(textRenderer, Text.literal("Str:"), cx - 172, 174, 0xFFFFFFFF);
                ctx.drawTextWithShadow(textRenderer, Text.literal("Rain:"), cx - 172, 199, 0xFFFFFFFF);

                int previewColor;
                if (rainbow) {
                    long time = System.currentTimeMillis();
                    float hue = (float) (((time * rainbowSpeed) % 100000L) / 100000.0);
                    previewColor = 0xFF000000 | hsvToRgbInt(hue, 1.0f, 1.0f);
                } else {
                    previewColor = 0xFF000000 | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
                }
                ctx.fill(cx - 40, 70, cx + 30, 190, previewColor);
                ctx.drawStrokedRectangle(cx - 40, 70, 70, 120, 0xFF555555);

            } else if (activeTab == 1) {
                // Tab 1: Item-Specific Glints Panel
                ctx.fill(cx - 120, 55, cx + 120, 235, 0x55000000);
                ctx.drawStrokedRectangle(cx - 120, 55, 240, 180, 0xFF555555);

                ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Item-Specific Overrides").formatted(Formatting.BOLD, Formatting.GRAY), cx, 48, 0xFFFFFFFF);

                // Colored indicator squares in paginated list
                List<GlintConfig.ItemColor> items = GlintConfig.getItemColors();
                int startIdx = itemPage * 5;
                int endIdx = Math.min(items.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    GlintConfig.ItemColor ic = items.get(i);
                    int ey = 95 + (i - startIdx) * 22;
                    int colorVal;
                    if (ic.isRainbow()) {
                        long time = System.currentTimeMillis();
                        float hue = (float) (((time * ic.getRainbowSpeed()) % 100000L) / 100000.0);
                        colorVal = 0xFF000000 | hsvToRgbInt(hue, 1.0f, 1.0f);
                    } else {
                        colorVal = 0xFF000000 | (ic.getRed() << 16) | (ic.getGreen() << 8) | ic.getBlue();
                    }
                    ctx.drawStrokedRectangle(cx - 100, ey + 3, 14, 14, 0xFF888888);
                    ctx.fill(cx - 99, ey + 4, cx - 87, ey + 16, colorVal);
                }

            } else if (activeTab == 2) {
                // Tab 2: Custom Named Glints Panel
                ctx.fill(cx - 120, 55, cx + 120, 235, 0x55000000);
                ctx.drawStrokedRectangle(cx - 120, 55, 240, 180, 0xFF555555);

                ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Custom Named Overrides").formatted(Formatting.BOLD, Formatting.GRAY), cx, 48, 0xFFFFFFFF);

                // Colored indicator squares in paginated list
                List<GlintConfig.NamedColor> named = GlintConfig.getNamedColors();
                int startIdx = namedPage * 5;
                int endIdx = Math.min(named.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    GlintConfig.NamedColor nc = named.get(i);
                    int ey = 95 + (i - startIdx) * 22;
                    int colorVal;
                    if (nc.isRainbow()) {
                        long time = System.currentTimeMillis();
                        float hue = (float) (((time * nc.getRainbowSpeed()) % 100000L) / 100000.0);
                        colorVal = 0xFF000000 | hsvToRgbInt(hue, 1.0f, 1.0f);
                    } else {
                        colorVal = 0xFF000000 | (nc.getRed() << 16) | (nc.getGreen() << 8) | nc.getBlue();
                    }
                    ctx.drawStrokedRectangle(cx - 100, ey + 3, 14, 14, 0xFF888888);
                    ctx.fill(cx - 99, ey + 4, cx - 87, ey + 16, colorVal);
                }

            } else if (activeTab == 3) {
                ctx.fill(cx - 120, 55, cx + 120, 235, 0x55000000);
                ctx.drawStrokedRectangle(cx - 120, 55, 240, 180, 0xFF555555);
                ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Resource Pack Configs").formatted(Formatting.BOLD, Formatting.GRAY), cx, 48, 0xFFFFFFFF);

                List<GlintConfig.PackData> packs = GlintConfig.getPacksData();
                int startIdx = packPage * 5;
                int endIdx = Math.min(packs.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    GlintConfig.PackData pd = packs.get(i);
                    int ey = 95 + (i - startIdx) * 22;
                    int colorVal = 0xFF000000 | (pd.getRed() << 16) | (pd.getGreen() << 8) | pd.getBlue();
                    ctx.drawStrokedRectangle(cx - 100, ey + 3, 14, 14, 0xFF888888);
                    ctx.fill(cx - 99, ey + 4, cx - 87, ey + 16, colorVal);
                }
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);

        // Draw suggestion dropdown on top of everything!
        if (!showPopup && activeTab == 1 && !searchMatches.isEmpty()) {
            int cx = width / 2;
            int startY = 85;
            int h = searchMatches.size() * 20;

            // Background panel
            ctx.fill(cx - 100, startY, cx + 100, startY + h, 0xEE101010);
            ctx.drawStrokedRectangle(cx - 100, startY, 200, h, 0xFF555555);

            for (int i = 0; i < searchMatches.size(); i++) {
                net.minecraft.item.Item item = searchMatches.get(i);
                int ey = startY + i * 20;

                // Hover highlights
                boolean hover = mx >= cx - 100 && mx <= cx + 100 && my >= ey && my < ey + 20;
                if (hover) {
                    ctx.fill(cx - 100, ey, cx + 100, ey + 20, 0x44FFFFFF);
                }

                // Render item icon texture
                ctx.drawItem(new ItemStack(item), cx - 96, ey + 2);

                // Render item name string
                String dispName = Registries.ITEM.getId(item).toString();
                if (dispName.startsWith("minecraft:")) {
                    dispName = dispName.substring(10);
                }
                ctx.drawTextWithShadow(textRenderer, Text.literal(dispName), cx - 76, ey + 6, 0xFFFFFFFF);
            }
        }
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private class StrengthSlider extends SliderWidget {
        private final Runnable onApply;

        StrengthSlider(int x, int y, int w, int h, int value, Runnable onApply) {
            super(x, y, w, h, Text.literal("Str: " + value), value / 255.0);
            this.onApply = onApply;
        }

        @Override
        public void applyValue() {
            onApply.run();
            updateMessage();
        }

        @Override
        public void updateMessage() {
            setMessage(getMessage());
        }

        @Override
        public Text getMessage() {
            return Text.literal("Str: " + getIntValue());
        }

        int getIntValue() {
            return (int) Math.round(value * 255);
        }

        void setIntValue(int v) {
            value = clamp(v) / 255.0;
            updateMessage();
        }
    }

    private class SpeedSlider extends SliderWidget {
        private final Runnable onApply;

        SpeedSlider(int x, int y, int w, int h, int value, Runnable onApply) {
            super(x, y, w, h, Text.literal("Spd: " + value), (value - 1) / 99.0);
            this.onApply = onApply;
        }

        @Override
        public void applyValue() {
            onApply.run();
            updateMessage();
        }

        @Override
        public void updateMessage() {
            setMessage(getMessage());
        }

        @Override
        public Text getMessage() {
            return Text.literal("Spd: " + getIntValue());
        }

        int getIntValue() {
            return (int) Math.round(value * 99) + 1;
        }

        void setIntValue(int v) {
            value = (clampSpeed(v) - 1) / 99.0;
            updateMessage();
        }
    }

    private static int clampSpeed(int v) { return Math.max(1, Math.min(100, v)); }

    private static int hsvToRgbInt(float hue, float saturation, float value) {
        float r = 0, g = 0, b = 0;
        int i = (int) (hue * 6);
        float f = hue * 6 - i;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);
        switch (i % 6) {
            case 0: r = value; g = t; b = p; break;
            case 1: r = q; g = value; b = p; break;
            case 2: r = p; g = value; b = t; break;
            case 3: r = p; g = q; b = value; break;
            case 4: r = t; g = p; b = value; break;
            case 5: r = value; g = p; b = q; break;
        }
        int ri = Math.round(r * 255);
        int gi = Math.round(g * 255);
        int bi = Math.round(b * 255);
        return (ri << 16) | (gi << 8) | bi;
    }
}
