package com.tkitsunefortune.wheel;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WheelSlice {

    private final String id;
    private final int index;
    private String displayName;
    private String iconMaterial;
    private ItemStack customIconItem;
    private String sliceLabel;
    private double chance;
    private boolean broadcast;
    private String broadcastMessage;
    private String title;
    private String subtitle;
    private String sound;
    private List<String> messages;
    private List<String> commands;
    private List<ItemStack> rewardItems;

    public WheelSlice(
            String id,
            int index,
            String displayName,
            String iconMaterial,
            ItemStack customIconItem,
            String sliceLabel,
            double chance,
            boolean broadcast,
            String broadcastMessage,
            String title,
            String subtitle,
            String sound,
            List<String> messages,
            List<String> commands,
            List<ItemStack> rewardItems
    ) {
        this.id = id;
        this.index = index;
        this.displayName = displayName;
        this.iconMaterial = iconMaterial != null ? iconMaterial : "DIAMOND";
        this.customIconItem = customIconItem;
        this.sliceLabel = sliceLabel != null ? sliceLabel : displayName;
        this.chance = Math.max(0.0, chance);
        this.broadcast = broadcast;
        this.broadcastMessage = broadcastMessage;
        this.title = title;
        this.subtitle = subtitle;
        this.sound = sound;
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        this.commands = commands != null ? new ArrayList<>(commands) : new ArrayList<>();
        this.rewardItems = rewardItems != null ? new ArrayList<>(rewardItems) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public float getTargetAngleDegrees() {
        return index * 45.0f;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public void setIconMaterial(String iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    public ItemStack getCustomIconItem() {
        return customIconItem != null ? customIconItem.clone() : null;
    }

    public void setCustomIconItem(ItemStack customIconItem) {
        this.customIconItem = customIconItem != null ? customIconItem.clone() : null;
    }

    public String getSliceLabel() {
        return sliceLabel;
    }

    public void setSliceLabel(String sliceLabel) {
        this.sliceLabel = sliceLabel;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = Math.max(0.0, Math.round(chance * 100.0) / 100.0);
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSound() {
        return sound;
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public List<String> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public void setCommands(List<String> commands) {
        this.commands = new ArrayList<>(commands);
    }

    public List<ItemStack> getRewardItems() {
        return Collections.unmodifiableList(rewardItems);
    }

    public void setRewardItems(List<ItemStack> rewardItems) {
        this.rewardItems = new ArrayList<>(rewardItems);
    }
}
