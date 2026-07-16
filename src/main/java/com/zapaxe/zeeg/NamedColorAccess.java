package com.zapaxe.zeeg;

public interface NamedColorAccess {
    void zeeg$setNamedColor(int r, int g, int b, int strength, boolean rainbow, int speed, int mode, int r2, int g2, int b2);
    int[] zeeg$getNamedColor();
}
