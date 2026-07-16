package com.zapaxe.zeeg;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;

public class ZeegRenderHooks {
    public static final ThreadLocal<int[]> GLINT_COLOR = new ThreadLocal<>();

    private static final Map<BatchingRenderCommandQueue, List<int[]>> COMMAND_COLORS = new WeakHashMap<>();

    public static List<int[]> getCommandColors(BatchingRenderCommandQueue queue) {
        return COMMAND_COLORS.get(queue);
    }

    public static void putCommandColors(BatchingRenderCommandQueue queue, List<int[]> colors) {
        COMMAND_COLORS.put(queue, colors);
    }

    public static void removeCommandColors(BatchingRenderCommandQueue queue) {
        COMMAND_COLORS.remove(queue);
    }
}
