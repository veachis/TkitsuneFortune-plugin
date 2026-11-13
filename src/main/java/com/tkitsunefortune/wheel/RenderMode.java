package com.tkitsunefortune.wheel;

public enum RenderMode {
    BLOCK_DISPLAY,
    RESOURCE_PACK;

    public static RenderMode fromString(String value) {
        if (value == null) {
            return BLOCK_DISPLAY;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.equals("RESOURCE_PACK") || normalized.equals("ITEM_DISPLAY") || normalized.equals("RP")) {
            return RESOURCE_PACK;
        }
        return BLOCK_DISPLAY;
    }
}

