package com.tkitsunefortune.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AdminMenuHolder implements InventoryHolder {

    public enum MenuType {
        MAIN,
        SLICE_EDITOR,
        HOLOGRAM_EDITOR,
        SOUND_EDITOR
    }

    private final MenuType menuType;
    private final String sliceId;
    private Inventory inventory;

    public AdminMenuHolder(MenuType menuType, String sliceId) {
        this.menuType = menuType;
        this.sliceId = sliceId;
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public String getSliceId() {
        return sliceId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

