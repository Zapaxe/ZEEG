package com.zapaxe.zeeg;

import java.util.Map;
import java.util.WeakHashMap;

public class ZeegRenderHooks {
    public static final ThreadLocal<int[]> GLINT_COLOR = new ThreadLocal<>();
    public static final Map<Object, int[]> ITEM_SUBMIT_COLORS = java.util.Collections.synchronizedMap(new WeakHashMap<>());
}
