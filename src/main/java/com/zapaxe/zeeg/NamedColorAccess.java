package com.zapaxe.zeeg;

public interface NamedColorAccess {
    void zeeg$setNamedColor(int r, int g, int b, int strength, boolean rainbow, int speed);
    int[] zeeg$getNamedColor();
}
