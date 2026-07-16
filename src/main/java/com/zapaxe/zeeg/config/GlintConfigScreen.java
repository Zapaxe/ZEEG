package com.zapaxe.zeeg.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

public class GlintConfigScreen extends Screen {
    private static final Component TITLE = Component.literal("Glint Color Config");
    private final Screen parent;
    
    // Tab State
    private int activeTab = 0;
    private int itemPage = 0;
    private int namedPage = 0;
    private int packPage = 0;

    // Main Tab Widgets
    private EditBox redField;
    private EditBox greenField;
    private EditBox blueField;
    private EditBox hexField;
    private StrengthSlider strengthSlider;

    // Item Tab Widgets
    private EditBox itemField;
    private String tempSearchQuery = "";
    private final List<net.minecraft.world.item.Item> searchMatches = new ArrayList<>();

    // Named Tab Widgets
    private EditBox nameField;
    
    // Config State
    private boolean updating = false;
    private int red = GlintConfig.getFileRed();
    private int green = GlintConfig.getFileGreen();
    private int blue = GlintConfig.getFileBlue();
    private int strength = GlintConfig.getFileStrength();
    private boolean rainbow = GlintConfig.getFileRainbow();
    private int rainbowSpeed = GlintConfig.getFileRainbowSpeed();
    private SpeedSlider speedSlider;
    private int cycleMode = GlintConfig.getFileCycleMode();
    private int red2 = GlintConfig.getFileRed2();
    private int green2 = GlintConfig.getFileGreen2();
    private int blue2 = GlintConfig.getFileBlue2();
    private int editingColorIndex = 0; // 0 for A (primary), 1 for B (secondary)

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
    private int popupCycleMode = 0;
    private int popupRed2 = 255;
    private int popupGreen2 = 255;
    private int popupBlue2 = 255;
    private int popupEditingColorIndex = 0; // 0 for A (primary), 1 for B (secondary)
    private EditBox popupRedField;
    private EditBox popupGreenField;
    private EditBox popupBlueField;
    private EditBox popupHexField;
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
            String modeName = "Static";
            if (popupCycleMode == 1) modeName = "Rainbow";
            else if (popupCycleMode == 2) modeName = "Duo-Tone";

            Button popupModeBtn = Button.builder(Component.literal(modeName), btn -> {
                popupCycleMode = (popupCycleMode + 1) % 3;
                popupRainbow = (popupCycleMode == 1);
                popupEditingColorIndex = 0;
                rebuild();
            }).bounds(cx - 110, cy + 60, 60, 20).build();
            addRenderableWidget(popupModeBtn);

            if (popupCycleMode == 1) {
                popupSpeedSlider = new SpeedSlider(cx - 110, cy - 15, 60, 20, popupRainbowSpeed, () -> popupRainbowSpeed = popupSpeedSlider.getIntValue());
                addRenderableWidget(popupSpeedSlider);
            } else {
                int rVal = (popupEditingColorIndex == 0) ? popupRed : popupRed2;
                int gVal = (popupEditingColorIndex == 0) ? popupGreen : popupGreen2;
                int bVal = (popupEditingColorIndex == 0) ? popupBlue : popupBlue2;

                popupRedField = new EditBox(font, cx - 110, cy - 65, 60, 20, Component.literal("Red"));
                popupRedField.setValue(String.valueOf(rVal));
                popupRedField.setResponder(s -> updatePopupFromRgbFields());
                addRenderableWidget(popupRedField);

                popupGreenField = new EditBox(font, cx - 110, cy - 40, 60, 20, Component.literal("Green"));
                popupGreenField.setValue(String.valueOf(gVal));
                popupGreenField.setResponder(s -> updatePopupFromRgbFields());
                addRenderableWidget(popupGreenField);

                popupBlueField = new EditBox(font, cx - 110, cy - 15, 60, 20, Component.literal("Blue"));
                popupBlueField.setValue(String.valueOf(bVal));
                popupBlueField.setResponder(s -> updatePopupFromRgbFields());
                addRenderableWidget(popupBlueField);

                popupHexField = new EditBox(font, cx - 110, cy + 10, 60, 20, Component.literal("Hex"));
                popupHexField.setValue(toHex(rVal, gVal, bVal));
                popupHexField.setResponder(s -> updatePopupFromHexField());
                addRenderableWidget(popupHexField);

                if (popupCycleMode == 2) {
                    Button colorToggleBtn = Button.builder(
                        Component.literal(popupEditingColorIndex == 0 ? "Color: Primary" : "Color: Secondary"),
                        btn -> {
                            popupEditingColorIndex = 1 - popupEditingColorIndex;
                            rebuild();
                        }
                    ).bounds(cx + 30, cy + 10, 115, 20).build();
                    addRenderableWidget(colorToggleBtn);

                    popupSpeedSlider = new SpeedSlider(cx + 30, cy + 35, 115, 20, popupRainbowSpeed, () -> popupRainbowSpeed = popupSpeedSlider.getIntValue());
                    addRenderableWidget(popupSpeedSlider);
                }
            }

            popupStrSlider = new StrengthSlider(cx - 110, cy + 35, 60, 20, popupStrength, () -> popupStrength = popupStrSlider.getIntValue());
            addRenderableWidget(popupStrSlider);

            // Popup Preset Buttons
            int py = cy - 65;
            addRenderableWidget(Button.builder(Component.literal("Cyan"), btn -> setPopupColor(0, 255, 255))
                .bounds(cx + 30, py, 55, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Red"), btn -> setPopupColor(255, 0, 0))
                .bounds(cx + 90, py, 55, 20).build());

            addRenderableWidget(Button.builder(Component.literal("Green"), btn -> setPopupColor(0, 255, 0))
                .bounds(cx + 30, py + 25, 55, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Blue"), btn -> setPopupColor(0, 0, 255))
                .bounds(cx + 90, py + 25, 55, 20).build());

            addRenderableWidget(Button.builder(Component.literal("White"), btn -> setPopupColor(255, 255, 255))
                .bounds(cx + 30, py + 50, 55, 20).build());
            addRenderableWidget(Button.builder(Component.literal("OG"), btn -> setPopupColor(150, 75, 200))
                .bounds(cx + 90, py + 50, 55, 20).build());

            // OK / Cancel Buttons
            addRenderableWidget(Button.builder(Component.literal("OK"), btn -> confirmPopup())
                .bounds(cx - 90, cy + 90, 80, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> cancelPopup())
                .bounds(cx + 10, cy + 90, 80, 20).build());

        } else {
            // Tab Selection Header
            Button tab0 = Button.builder(Component.literal("General"), btn -> { activeTab = 0; tempSearchQuery = ""; searchMatches.clear(); rebuild(); })
                .bounds(cx - 155, 22, 100, 20).build();
            Button tab1 = Button.builder(Component.literal("Item Colors"), btn -> { activeTab = 1; tempSearchQuery = ""; searchMatches.clear(); rebuild(); })
                .bounds(cx - 50, 22, 100, 20).build();
            Button tab2 = Button.builder(Component.literal("Named Colors"), btn -> { activeTab = 2; tempSearchQuery = ""; searchMatches.clear(); rebuild(); })
                .bounds(cx + 55, 22, 100, 20).build();

            tab0.active = (activeTab != 0);
            tab1.active = (activeTab != 1);
            tab2.active = (activeTab != 2);
            Button tab3 = Button.builder(Component.literal("Pack Configs"), btn -> { activeTab = 3; rebuild(); })
                .bounds(cx + 160, 22, 100, 20).build();
            tab3.active = (activeTab != 3);

            addRenderableWidget(tab0);
            addRenderableWidget(tab1);
            addRenderableWidget(tab2);
            addRenderableWidget(tab3);

            if (activeTab == 0) {
                String modeName = "Static";
                if (cycleMode == 1) modeName = "Rainbow";
                else if (cycleMode == 2) modeName = "Duo-Tone";

                Button modeBtn = Button.builder(Component.literal(modeName), btn -> {
                    cycleMode = (cycleMode + 1) % 3;
                    rainbow = (cycleMode == 1);
                    editingColorIndex = 0;
                    rebuild();
                }).bounds(cx - 130, 195, 80, 20).build();
                addRenderableWidget(modeBtn);

                if (cycleMode == 1) {
                    speedSlider = new SpeedSlider(cx - 130, 95, 80, 20, rainbowSpeed, () -> rainbowSpeed = speedSlider.getIntValue());
                    addRenderableWidget(speedSlider);
                } else {
                    // General Settings Layout
                    int rVal = (editingColorIndex == 0) ? red : red2;
                    int gVal = (editingColorIndex == 0) ? green : green2;
                    int bVal = (editingColorIndex == 0) ? blue : blue2;

                    redField = new EditBox(font, cx - 130, 70, 80, 20, Component.literal("Red"));
                    redField.setValue(String.valueOf(rVal));
                    redField.setResponder(s -> updateFromRgbFields());
                    addRenderableWidget(redField);

                    greenField = new EditBox(font, cx - 130, 95, 80, 20, Component.literal("Green"));
                    greenField.setValue(String.valueOf(gVal));
                    greenField.setResponder(s -> updateFromRgbFields());
                    addRenderableWidget(greenField);

                    blueField = new EditBox(font, cx - 130, 120, 80, 20, Component.literal("Blue"));
                    blueField.setValue(String.valueOf(bVal));
                    blueField.setResponder(s -> updateFromRgbFields());
                    addRenderableWidget(blueField);

                    hexField = new EditBox(font, cx - 130, 145, 80, 20, Component.literal("Hex"));
                    hexField.setValue(toHex(rVal, gVal, bVal));
                    hexField.setResponder(s -> updateFromHexField());
                    addRenderableWidget(hexField);

                    if (cycleMode == 2) {
                        Button colorToggleBtn = Button.builder(
                            Component.literal(editingColorIndex == 0 ? "Color: Primary" : "Color: Secondary"),
                            btn -> {
                                editingColorIndex = 1 - editingColorIndex;
                                rebuild();
                            }
                        ).bounds(cx + 45, 145, 120, 20).build();
                        addRenderableWidget(colorToggleBtn);

                        speedSlider = new SpeedSlider(cx + 45, 170, 120, 20, rainbowSpeed, () -> rainbowSpeed = speedSlider.getIntValue());
                        addRenderableWidget(speedSlider);
                    }
                }

                strengthSlider = new StrengthSlider(cx - 130, 170, 80, 20, strength, () -> strength = strengthSlider.getIntValue());
                addRenderableWidget(strengthSlider);

                // Main Presets
                int py = 70;
                addRenderableWidget(Button.builder(Component.literal("Cyan"), btn -> setColor(0, 255, 255))
                    .bounds(cx + 45, py, 55, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Red"), btn -> setColor(255, 0, 0))
                    .bounds(cx + 110, py, 55, 20).build());

                addRenderableWidget(Button.builder(Component.literal("Green"), btn -> setColor(0, 255, 0))
                    .bounds(cx + 45, py + 25, 55, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Blue"), btn -> setColor(0, 0, 255))
                    .bounds(cx + 110, py + 25, 55, 20).build());

                addRenderableWidget(Button.builder(Component.literal("White"), btn -> setColor(255, 255, 255))
                    .bounds(cx + 45, py + 50, 55, 20).build());
                addRenderableWidget(Button.builder(Component.literal("OG"), btn -> setColor(150, 75, 200))
                    .bounds(cx + 110, py + 50, 55, 20).build());

            } else if (activeTab == 1) {
                // Item Specific Override Tab
                itemField = new EditBox(font, cx - 100, 65, 145, 20, Component.literal("Item ID"));
                itemField.setMaxLength(256);
                itemField.setHint(Component.literal("e.g. netherite_helmet"));
                if (tempSearchQuery != null && !tempSearchQuery.isEmpty()) {
                    itemField.setValue(tempSearchQuery);
                }
                itemField.setResponder(s -> {
                    tempSearchQuery = s;
                    updateSearchMatches(s);
                });
                addRenderableWidget(itemField);

                addRenderableWidget(Button.builder(Component.literal("Add"), btn -> addItemColor())
                    .bounds(cx + 50, 65, 50, 20).build());

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

                    addRenderableWidget(Button.builder(
                        Component.literal(dispName),
                        btn -> {
                            setColor(ic.getRed(), ic.getGreen(), ic.getBlue());
                            itemField.setValue(ic.getItemId());
                        }
                    ).bounds(cx - 80, ey, 125, 20).build());

                    addRenderableWidget(Button.builder(
                        Component.literal("X"), btn -> {
                            GlintConfig.getItemColors().remove(idx);
                            rebuild();
                        }
                    ).bounds(cx + 50, ey, 50, 20).build());
                }

                if (items.size() > 5) {
                    addRenderableWidget(Button.builder(Component.literal("<"), btn -> { if (itemPage > 0) { itemPage--; rebuild(); } })
                        .bounds(cx - 50, 210, 20, 20).build());
                    addRenderableWidget(Button.builder(Component.literal(">"), btn -> { if ((itemPage + 1) * 5 < items.size()) { itemPage++; rebuild(); } })
                        .bounds(cx + 30, 210, 20, 20).build());
                }

            } else if (activeTab == 2) {
                // Named Colors Tab
                nameField = new EditBox(font, cx - 100, 65, 145, 20, Component.literal("Name"));
                nameField.setHint(Component.literal("e.g. Goated Sword"));
                addRenderableWidget(nameField);

                addRenderableWidget(Button.builder(Component.literal("Add"), btn -> addNamed())
                    .bounds(cx + 50, 65, 50, 20).build());

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

                    addRenderableWidget(Button.builder(
                        Component.literal(dispName),
                        btn -> {
                            setColor(nc.getRed(), nc.getGreen(), nc.getBlue());
                            nameField.setValue(nc.getName());
                        }
                    ).bounds(cx - 80, ey, 125, 20).build());

                    addRenderableWidget(Button.builder(
                        Component.literal("X"), btn -> {
                            GlintConfig.getNamedColors().remove(idx);
                            rebuild();
                        }
                    ).bounds(cx + 50, ey, 50, 20).build());
                }

                if (named.size() > 5) {
                    addRenderableWidget(Button.builder(Component.literal("<"), btn -> { if (namedPage > 0) { namedPage--; rebuild(); } })
                        .bounds(cx - 50, 210, 20, 20).build());
                    addRenderableWidget(Button.builder(Component.literal(">"), btn -> { if ((namedPage + 1) * 5 < named.size()) { namedPage++; rebuild(); } })
                        .bounds(cx + 30, 210, 20, 20).build());
                }

            } else if (activeTab == 3) {
                // Pack Configs Tab
                addRenderableWidget(Button.builder(
                    Component.literal("Master: " + (GlintConfig.isPackOverridesEnabled() ? "ON" : "OFF")),
                    btn -> {
                        GlintConfig.setPackOverridesEnabled(!GlintConfig.isPackOverridesEnabled());
                        rebuild();
                    }
                ).bounds(cx - 100, 65, 155, 20).build());

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
                    addRenderableWidget(Button.builder(
                        Component.literal(dispName),
                        btn -> {}
                    ).bounds(cx - 80, ey, 125, 20).build());

                    addRenderableWidget(Button.builder(
                        Component.literal(enabled ? "ON" : "OFF"),
                        btn -> {
                            GlintConfig.setPackEnabled(pd.getPackId(), !GlintConfig.isPackEnabled(pd.getPackId()));
                            rebuild();
                        }
                    ).bounds(cx + 50, ey, 50, 20).build());
                }

                if (packs.size() > 5) {
                    addRenderableWidget(Button.builder(Component.literal("<"), btn -> { if (packPage > 0) { packPage--; rebuild(); } })
                        .bounds(cx - 50, 210, 20, 20).build());
                    addRenderableWidget(Button.builder(Component.literal(">"), btn -> { if ((packPage + 1) * 5 < packs.size()) { packPage++; rebuild(); } })
                        .bounds(cx + 30, 210, 20, 20).build());
                }
            }

            // Bottom controls
            addRenderableWidget(Button.builder(Component.literal("Save & Reload"), btn -> saveAndReload())
                .bounds(cx - 100, height - 55, 200, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Back"), btn -> onClose())
                .bounds(cx - 100, height - 30, 200, 20).build());
        }
    }

    private void rebuild() {
        clearWidgets();
        init();
    }

    private void addNamed() {
        String name = nameField.getValue().trim();
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
        popupCycleMode = cycleMode;
        popupRed2 = red2;
        popupGreen2 = green2;
        popupBlue2 = blue2;
        popupEditingColorIndex = 0;
        showPopup = true;
        nameField.setValue("");
        rebuild();
    }

    private void addItemColor() {
        String itemId = itemField.getValue().trim().toLowerCase().replace(" ", "_");
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
        popupCycleMode = cycleMode;
        popupRed2 = red2;
        popupGreen2 = green2;
        popupBlue2 = blue2;
        popupEditingColorIndex = 0;
        showPopup = true;
        itemField.setValue("");
        tempSearchQuery = "";
        searchMatches.clear();
        rebuild();
    }

    private void updateSearchMatches(String s) {
        searchMatches.clear();
        String query = s.trim().toLowerCase().replace(" ", "_");
        if (!query.isEmpty()) {
            for (net.minecraft.world.item.Item item : BuiltInRegistries.ITEM) {
                String idStr = BuiltInRegistries.ITEM.getKey(item).toString();
                if (idStr.contains(query)) {
                    searchMatches.add(item);
                    if (searchMatches.size() >= 5) break;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean primary) {
        double mx = click.x();
        double my = click.y();
        if (!showPopup && activeTab == 1 && !searchMatches.isEmpty()) {
            int cx = width / 2;
            if (mx >= cx - 100 && mx <= cx + 100) {
                int startY = 85;
                if (my >= startY && my < startY + searchMatches.size() * 20) {
                    int idx = ((int)my - startY) / 20;
                    if (idx >= 0 && idx < searchMatches.size()) {
                        net.minecraft.world.item.Item clickedItem = searchMatches.get(idx);
                        itemField.setValue(BuiltInRegistries.ITEM.getKey(clickedItem).toString());
                        tempSearchQuery = itemField.getValue();
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
                        popupCycleMode = ic.getCycleMode();
                        popupRed2 = ic.getRed2();
                        popupGreen2 = ic.getGreen2();
                        popupBlue2 = ic.getBlue2();
                        popupEditingColorIndex = 0;
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
                        popupCycleMode = nc.getCycleMode();
                        popupRed2 = nc.getRed2();
                        popupGreen2 = nc.getGreen2();
                        popupBlue2 = nc.getBlue2();
                        popupEditingColorIndex = 0;
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
                ic.setCycleMode(popupCycleMode);
                ic.setRed2(popupRed2);
                ic.setGreen2(popupGreen2);
                ic.setBlue2(popupBlue2);
            } else {
                GlintConfig.NamedColor nc = GlintConfig.getNamedColors().get(popupEditIndex);
                nc.setRed(popupRed);
                nc.setGreen(popupGreen);
                nc.setBlue(popupBlue);
                nc.setStrength(popupStrength);
                nc.setRainbow(popupRainbow);
                nc.setRainbowSpeed(popupRainbowSpeed);
                nc.setCycleMode(popupCycleMode);
                nc.setRed2(popupRed2);
                nc.setGreen2(popupGreen2);
                nc.setBlue2(popupBlue2);
            }
        } else {
            if (popupIsItem) {
                GlintConfig.getItemColors().add(new GlintConfig.ItemColor(popupName, popupRed, popupGreen, popupBlue, popupStrength, popupRainbow, popupRainbowSpeed, popupCycleMode, popupRed2, popupGreen2, popupBlue2));
            } else {
                GlintConfig.getNamedColors().add(new GlintConfig.NamedColor(popupName, popupRed, popupGreen, popupBlue, popupStrength, popupRainbow, popupRainbowSpeed, popupCycleMode, popupRed2, popupGreen2, popupBlue2));
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
        if (editingColorIndex == 0) {
            red = r; green = g; blue = b;
        } else {
            red2 = r; green2 = g; blue2 = b;
        }
        if (redField != null) redField.setValue(String.valueOf(r));
        if (greenField != null) greenField.setValue(String.valueOf(g));
        if (blueField != null) blueField.setValue(String.valueOf(b));
        if (hexField != null) hexField.setValue(toHex(r, g, b));
        updating = false;
    }

    private void setPopupColor(int r, int g, int b) {
        if (updating) return;
        updating = true;
        if (popupEditingColorIndex == 0) {
            popupRed = r; popupGreen = g; popupBlue = b;
        } else {
            popupRed2 = r; popupGreen2 = g; popupBlue2 = b;
        }
        if (popupRedField != null) popupRedField.setValue(String.valueOf(r));
        if (popupGreenField != null) popupGreenField.setValue(String.valueOf(g));
        if (popupBlueField != null) popupBlueField.setValue(String.valueOf(b));
        if (popupHexField != null) popupHexField.setValue(toHex(r, g, b));
        updating = false;
    }

    private static String toHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private void updateFromRgbFields() {
        if (updating) return;
        updating = true;
        try {
            int r = clamp(Integer.parseInt(redField.getValue()));
            int g = clamp(Integer.parseInt(greenField.getValue()));
            int b = clamp(Integer.parseInt(blueField.getValue()));
            if (editingColorIndex == 0) {
                red = r; green = g; blue = b;
            } else {
                red2 = r; green2 = g; blue2 = b;
            }
            hexField.setValue(toHex(r, g, b));
        } catch (NumberFormatException ignored) {}
        updating = false;
    }

    private void updateFromHexField() {
        if (updating) return;
        updating = true;
        String t = hexField.getValue().trim();
        if (t.startsWith("#") || t.startsWith("0x")) {
            t = t.replaceFirst("^[#0][xX]?", "");
        }
        if (t.length() == 6) {
            try {
                int color = Integer.parseInt(t, 16);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                if (editingColorIndex == 0) {
                    red = r; green = g; blue = b;
                } else {
                    red2 = r; green2 = g; blue2 = b;
                }
                redField.setValue(String.valueOf(r));
                greenField.setValue(String.valueOf(g));
                blueField.setValue(String.valueOf(b));
            } catch (NumberFormatException ignored) {}
        }
        updating = false;
    }

    private void updatePopupFromRgbFields() {
        if (updating) return;
        updating = true;
        try {
            int r = clamp(Integer.parseInt(popupRedField.getValue()));
            int g = clamp(Integer.parseInt(popupGreenField.getValue()));
            int b = clamp(Integer.parseInt(popupBlueField.getValue()));
            if (popupEditingColorIndex == 0) {
                popupRed = r; popupGreen = g; popupBlue = b;
            } else {
                popupRed2 = r; popupGreen2 = g; popupBlue2 = b;
            }
            popupHexField.setValue(toHex(r, g, b));
        } catch (NumberFormatException ignored) {}
        updating = false;
    }

    private void updatePopupFromHexField() {
        if (updating) return;
        updating = true;
        String t = popupHexField.getValue().trim();
        if (t.startsWith("#") || t.startsWith("0x")) {
            t = t.replaceFirst("^[#0][xX]?", "");
        }
        if (t.length() == 6) {
            try {
                int color = Integer.parseInt(t, 16);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                if (popupEditingColorIndex == 0) {
                    popupRed = r; popupGreen = g; popupBlue = b;
                } else {
                    popupRed2 = r; popupGreen2 = g; popupBlue2 = b;
                }
                popupRedField.setValue(String.valueOf(r));
                popupGreenField.setValue(String.valueOf(g));
                popupBlueField.setValue(String.valueOf(b));
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
        GlintConfig.setCycleMode(cycleMode);
        GlintConfig.setRed2(red2);
        GlintConfig.setGreen2(green2);
        GlintConfig.setBlue2(blue2);
        GlintConfig.save();
        Minecraft.getInstance().reloadResourcePacks();
        onClose();
    }

    @Override
    public void onClose() {
        GlintConfig.setRed(red);
        GlintConfig.setGreen(green);
        GlintConfig.setBlue(blue);
        GlintConfig.setStrength(strength);
        GlintConfig.setRainbow(rainbow);
        GlintConfig.setRainbowSpeed(rainbowSpeed);
        GlintConfig.setCycleMode(cycleMode);
        GlintConfig.setRed2(red2);
        GlintConfig.setGreen2(green2);
        GlintConfig.setBlue2(blue2);
        GlintConfig.save();
        minecraft.setScreenAndShow(parent);
    }

    private int getCycleColor(int mode, int r1, int g1, int b1, int r2, int g2, int b2, int speed) {
        long time = System.currentTimeMillis();
        if (mode == 1) {
            float hue = (float) (((time * speed) % 100000L) / 100000.0);
            return hsvToRgbInt(hue, 1.0f, 1.0f);
        } else if (mode == 2) {
            double factor = (time * speed / 100000.0) * 2.0 * Math.PI;
            double t = 0.5 + 0.5 * Math.sin(factor);
            int r = (int) Math.round(r1 * (1 - t) + r2 * t);
            int g = (int) Math.round(g1 * (1 - t) + g2 * t);
            int b = (int) Math.round(b1 * (1 - t) + b2 * t);
            return (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
        }
        return (clamp(r1) << 16) | (clamp(g1) << 8) | clamp(b1);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        super.extractBackground(ctx, mx, my, delta);
        int cx = width / 2;
        int cy = height / 2;

        if (showPopup) {
            // Dark overlay
            ctx.fill(0, 0, width, height, 0x55000000);

            // Popup container
            ctx.fill(cx - 150, cy - 95, cx + 150, cy + 135, 0xFF181818);
            ctx.outline(cx - 150, cy - 95, 300, 230, 0xFF666666);

            // Popup Title
            String nameText = popupIsItem && popupName.contains(":") ? popupName.substring(popupName.indexOf(":") + 1) : popupName;
            ctx.centeredText(font, Component.literal("Pick Color for: " + nameText).withStyle(ChatFormatting.YELLOW), cx, cy - 85, 0xFFFFFFFF);

            // Labels for fields
            if (popupCycleMode == 1) {
                ctx.text(font, Component.literal("Spd:"), cx - 137, cy - 11, 0xFFFFFFFF, true);
            } else {
                ctx.text(font, Component.literal("R:"), cx - 130, cy - 61, 0xFFFF5555, true);
                ctx.text(font, Component.literal("G:"), cx - 130, cy - 36, 0xFF55FF55, true);
                ctx.text(font, Component.literal("B:"), cx - 130, cy - 11, 0xFF5555FF, true);
                ctx.text(font, Component.literal("Hex:"), cx - 137, cy + 14, 0xFFFFFFFF, true);
            }
            ctx.text(font, Component.literal("Str:"), cx - 137, cy + 39, 0xFFFFFFFF, true);
            ctx.text(font, Component.literal("Mode:"), cx - 142, cy + 64, 0xFFFFFFFF, true);

            // Color Preview Box
            int previewColor = 0xFF000000 | getCycleColor(popupCycleMode, popupRed, popupGreen, popupBlue, popupRed2, popupGreen2, popupBlue2, popupRainbowSpeed);
            ctx.fill(cx - 40, cy - 65, cx + 20, cy + 55, previewColor);
            ctx.outline(cx - 40, cy - 65, 60, 120, 0xFF888888);

        } else {
            // General Title
            ctx.centeredText(font, TITLE, cx, 8, 0xFFFFFFFF);

            if (activeTab == 0) {
                // Tab 0: General Color Panel
                ctx.fill(cx - 180, 60, cx + 180, 220, 0x55000000);
                ctx.outline(cx - 180, 60, 360, 160, 0xFF555555);

                if (cycleMode == 1) {
                    ctx.text(font, Component.literal("Spd:"), cx - 172, 99, 0xFFFFFFFF, true);
                } else {
                    ctx.text(font, Component.literal("R:"), cx - 170, 74, 0xFFFF5555, true);
                    ctx.text(font, Component.literal("G:"), cx - 170, 99, 0xFF55FF55, true);
                    ctx.text(font, Component.literal("B:"), cx - 170, 124, 0xFF5555FF, true);
                    ctx.text(font, Component.literal("Hex:"), cx - 175, 149, 0xFFFFFFFF, true);
                }
                ctx.text(font, Component.literal("Str:"), cx - 172, 174, 0xFFFFFFFF, true);
                ctx.text(font, Component.literal("Mode:"), cx - 172, 199, 0xFFFFFFFF, true);

                int previewColor = 0xFF000000 | getCycleColor(cycleMode, red, green, blue, red2, green2, blue2, rainbowSpeed);
                ctx.fill(cx - 40, 70, cx + 30, 190, previewColor);
                ctx.outline(cx - 40, 70, 70, 120, 0xFF555555);

            } else if (activeTab == 1) {
                // Tab 1: Item-Specific Glints Panel
                ctx.fill(cx - 120, 55, cx + 120, 235, 0x55000000);
                ctx.outline(cx - 120, 55, 240, 180, 0xFF555555);

                ctx.centeredText(font, Component.literal("Item-Specific Overrides").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY), cx, 48, 0xFFFFFFFF);

                // Colored indicator squares in paginated list
                List<GlintConfig.ItemColor> items = GlintConfig.getItemColors();
                int startIdx = itemPage * 5;
                int endIdx = Math.min(items.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    GlintConfig.ItemColor ic = items.get(i);
                    int ey = 95 + (i - startIdx) * 22;
                    int colorVal = 0xFF000000 | getCycleColor(ic.getCycleMode(), ic.getRed(), ic.getGreen(), ic.getBlue(), ic.getRed2(), ic.getGreen2(), ic.getBlue2(), ic.getRainbowSpeed());
                    ctx.outline(cx - 100, ey + 3, 14, 14, 0xFF888888);
                    ctx.fill(cx - 99, ey + 4, cx - 87, ey + 16, colorVal);
                }

            } else if (activeTab == 2) {
                // Tab 2: Custom Named Glints Panel
                ctx.fill(cx - 120, 55, cx + 120, 235, 0x55000000);
                ctx.outline(cx - 120, 55, 240, 180, 0xFF555555);

                ctx.centeredText(font, Component.literal("Custom Named Overrides").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY), cx, 48, 0xFFFFFFFF);

                // Colored indicator squares in paginated list
                List<GlintConfig.NamedColor> named = GlintConfig.getNamedColors();
                int startIdx = namedPage * 5;
                int endIdx = Math.min(named.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    GlintConfig.NamedColor nc = named.get(i);
                    int ey = 95 + (i - startIdx) * 22;
                    int colorVal = 0xFF000000 | getCycleColor(nc.getCycleMode(), nc.getRed(), nc.getGreen(), nc.getBlue(), nc.getRed2(), nc.getGreen2(), nc.getBlue2(), nc.getRainbowSpeed());
                    ctx.outline(cx - 100, ey + 3, 14, 14, 0xFF888888);
                    ctx.fill(cx - 99, ey + 4, cx - 87, ey + 16, colorVal);
                }

            } else if (activeTab == 3) {
                ctx.fill(cx - 120, 55, cx + 120, 235, 0x55000000);
                ctx.outline(cx - 120, 55, 240, 180, 0xFF555555);
                ctx.centeredText(font, Component.literal("Resource Pack Configs").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY), cx, 48, 0xFFFFFFFF);

                List<GlintConfig.PackData> packs = GlintConfig.getPacksData();
                int startIdx = packPage * 5;
                int endIdx = Math.min(packs.size(), startIdx + 5);

                for (int i = startIdx; i < endIdx; i++) {
                    GlintConfig.PackData pd = packs.get(i);
                    int ey = 95 + (i - startIdx) * 22;
                    int colorVal = 0xFF000000 | (pd.getRed() << 16) | (pd.getGreen() << 8) | pd.getBlue();
                    ctx.outline(cx - 100, ey + 3, 14, 14, 0xFF888888);
                    ctx.fill(cx - 99, ey + 4, cx - 87, ey + 16, colorVal);
                }
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        super.extractRenderState(ctx, mx, my, delta);

        // Draw suggestion dropdown on top of everything!
        if (!showPopup && activeTab == 1 && !searchMatches.isEmpty()) {
            int cx = width / 2;
            int startY = 85;
            int h = searchMatches.size() * 20;

            // Background panel
            ctx.fill(cx - 100, startY, cx + 100, startY + h, 0xEE101010);
            ctx.outline(cx - 100, startY, 200, h, 0xFF555555);

            for (int i = 0; i < searchMatches.size(); i++) {
                net.minecraft.world.item.Item item = searchMatches.get(i);
                int ey = startY + i * 20;

                // Hover highlights
                boolean hover = mx >= cx - 100 && mx <= cx + 100 && my >= ey && my < ey + 20;
                if (hover) {
                    ctx.fill(cx - 100, ey, cx + 100, ey + 20, 0x44FFFFFF);
                }

                // Render item icon texture
                ctx.item(new ItemStack(item), cx - 96, ey + 2);

                // Render item name string
                String dispName = BuiltInRegistries.ITEM.getKey(item).toString();
                if (dispName.startsWith("minecraft:")) {
                    dispName = dispName.substring(10);
                }
                ctx.text(font, Component.literal(dispName), cx - 76, ey + 6, 0xFFFFFFFF, true);
            }
        }
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private class StrengthSlider extends AbstractSliderButton {
        private final Runnable onApply;

        StrengthSlider(int x, int y, int w, int h, int value, Runnable onApply) {
            super(x, y, w, h, Component.literal("Str: " + value), value / 255.0);
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
        public Component getMessage() {
            return Component.literal("Str: " + getIntValue());
        }

        int getIntValue() {
            return (int) Math.round(value * 255);
        }

        void setIntValue(int v) {
            value = clamp(v) / 255.0;
            updateMessage();
        }
    }

    private class SpeedSlider extends AbstractSliderButton {
        private final Runnable onApply;

        SpeedSlider(int x, int y, int w, int h, int value, Runnable onApply) {
            super(x, y, w, h, Component.literal("Spd: " + value), (value - 1) / 99.0);
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
        public Component getMessage() {
            return Component.literal("Spd: " + getIntValue());
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
