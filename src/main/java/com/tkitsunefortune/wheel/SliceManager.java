package com.tkitsunefortune.wheel;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SliceManager {

    private static final String[] DEFAULT_SLICE_ORDER = {
            "red", "orange", "yellow", "green", "cyan", "blue", "purple", "magenta"
    };

    private static final String[] DEFAULT_ICONS = {
            "DIAMOND_BLOCK", "GOLD_INGOT", "ENCHANTED_GOLDEN_APPLE", "NETHERITE_SWORD",
            "DIAMOND", "EXPERIENCE_BOTTLE", "NETHERITE_SCRAP", "TICKET"
    };

    private static final String[] DEFAULT_LABELS = {
            "&c&l16x Elmas", "&6&l32x Altın", "&e&lBüyülü Elma", "&a&l1x Netherite Kılıcı",
            "&b&l8x Elmas", "&9&l32x XP", "&5&l2x Netherite", "&d&l1x Bilet"
    };

    private final TKitsuneFortunePlugin plugin;
    private final List<WheelSlice> slices = new ArrayList<>();

    public SliceManager(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadSlices() {
        slices.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("slices");
        if (section == null) {
            plugin.getLogger().warning("No 'slices' section found in config.yml!");
            return;
        }

        for (int i = 0; i < DEFAULT_SLICE_ORDER.length; i++) {
            String key = DEFAULT_SLICE_ORDER[i];
            ConfigurationSection s = section.getConfigurationSection(key);
            if (s == null) {
                continue;
            }
            int index = s.getInt("index", i);
            String displayName = ColorUtil.colorize(s.getString("display-name", "&e" + key));
            String iconMaterial = s.getString("icon", DEFAULT_ICONS[i]);
            ItemStack customIconItem = s.getItemStack("custom-icon-item");
            String sliceLabel = ColorUtil.colorize(s.getString("slice-label", DEFAULT_LABELS[i]));
            double chance = s.getDouble("chance", 12.5);
            boolean broadcast = s.getBoolean("broadcast", false);

            String rawBroadcast = s.getString("broadcast-message", "");
            if (rawBroadcast == null || rawBroadcast.isBlank() || (!rawBroadcast.contains("%reward%") && !rawBroadcast.contains("%slice%"))) {
                rawBroadcast = plugin.tr(
                        "&6&l✦ &e%player% &7Şans Çarkından &r%slice% &8(&r%reward%&8) &7ödülünü kazandı! &6&l✦",
                        "&6&l✦ &e%player% &7won &r%slice% &8(&r%reward%&8) &7from the Wheel of Fortune! &6&l✦"
                );
            }
            String broadcastMsg = ColorUtil.colorize(rawBroadcast);

            String title = ColorUtil.colorize(s.getString("title", plugin.tr("&6&lTEBRİKLER!", "&6&lCONGRATULATIONS!")));

            String rawSubtitle = s.getString("subtitle", "");
            if (rawSubtitle == null || rawSubtitle.isBlank() || (!rawSubtitle.contains("%reward%") && !rawSubtitle.contains("%slice%"))) {
                rawSubtitle = plugin.tr("&fKazandığın ödül: &r%reward%", "&fReward: &r%reward%");
            }
            String subtitle = ColorUtil.colorize(rawSubtitle);

            String sound = s.getString("sound", "ENTITY_PLAYER_LEVELUP");

            List<String> rawMessages = s.getStringList("messages");
            boolean hasDynamicPlaceholder = false;
            for (String m : rawMessages) {
                if (m.contains("%reward%") || m.contains("%slice%")) {
                    hasDynamicPlaceholder = true;
                    break;
                }
            }
            if (rawMessages.isEmpty() || !hasDynamicPlaceholder) {
                rawMessages = List.of(plugin.tr(
                        "&aTebrikler! &r%slice% &aödülü olarak &r%reward% &akazandın!",
                        "&aCongratulations! You won &r%reward% &afrom &r%slice%&a!"
                ));
            }
            List<String> messages = ColorUtil.colorize(rawMessages);
            List<String> commands = s.getStringList("commands");

            List<ItemStack> rewardItems = new ArrayList<>();
            List<?> rawItems = s.getList("reward-items");
            if (rawItems != null) {
                for (Object obj : rawItems) {
                    if (obj instanceof ItemStack stack && !stack.getType().isAir()) {
                        rewardItems.add(stack.clone());
                    }
                }
            }

            slices.add(new WheelSlice(
                    key, index, displayName, iconMaterial, customIconItem, sliceLabel, chance,
                    broadcast, broadcastMsg, title, subtitle, sound, messages, commands, rewardItems
            ));
        }

        slices.sort(Comparator.comparingInt(WheelSlice::getIndex));
    }

    public void syncDynamicRewardTexts(WheelSlice slice) {
        if (slice == null) {
            return;
        }
        slice.setSubtitle(ColorUtil.colorize(plugin.tr(
                "&fKazandığın ödül: &r%reward%",
                "&fReward: &r%reward%"
        )));
        slice.setMessages(ColorUtil.colorize(List.of(plugin.tr(
                "&aTebrikler! &r%slice% &aödülü olarak &r%reward% &akazandın!",
                "&aCongratulations! You won &r%reward% &afrom &r%slice%&a!"
        ))));
        slice.setBroadcastMessage(ColorUtil.colorize(plugin.tr(
                "&6&l✦ &e%player% &7Şans Çarkından &r%slice% &8(&r%reward%&8) &7ödülünü kazandı! &6&l✦",
                "&6&l✦ &e%player% &7won &r%slice% &8(&r%reward%&8) &7from the Wheel of Fortune! &6&l✦"
        )));
    }

    public void saveSliceToConfig(WheelSlice slice) {
        if (slice == null) {
            return;
        }
        String path = "slices." + slice.getId();
        plugin.getConfig().set(path + ".chance", slice.getChance());
        plugin.getConfig().set(path + ".icon", slice.getIconMaterial());
        plugin.getConfig().set(path + ".custom-icon-item", slice.getCustomIconItem());
        plugin.getConfig().set(path + ".slice-label", slice.getSliceLabel());
        plugin.getConfig().set(path + ".display-name", slice.getDisplayName());
        plugin.getConfig().set(path + ".broadcast", slice.isBroadcast());
        plugin.getConfig().set(path + ".broadcast-message", slice.getBroadcastMessage());
        plugin.getConfig().set(path + ".title", slice.getTitle());
        plugin.getConfig().set(path + ".subtitle", slice.getSubtitle());
        plugin.getConfig().set(path + ".sound", slice.getSound());
        plugin.getConfig().set(path + ".messages", new ArrayList<>(slice.getMessages()));
        plugin.getConfig().set(path + ".commands", new ArrayList<>(slice.getCommands()));
        plugin.getConfig().set(path + ".reward-items", new ArrayList<>(slice.getRewardItems()));
        plugin.saveConfig();
    }

    public ItemStack createSliceIconItem(WheelSlice slice) {
        if (slice == null) {
            return new ItemStack(Material.DIAMOND);
        }
        ItemStack custom = slice.getCustomIconItem();
        if (custom != null && !custom.getType().isAir()) {
            ItemStack single = custom.clone();
            single.setAmount(1);
            return single;
        }
        String matStr = slice.getIconMaterial();
        if (matStr != null && matStr.equalsIgnoreCase("TICKET")) {
            return plugin.getTicketManager().createTicket(1);
        }
        Material mat = Material.matchMaterial(matStr != null ? matStr : "DIAMOND");
        if (mat == null || mat.isAir()) {
            mat = Material.DIAMOND;
        }
        return new ItemStack(mat, 1);
    }

    public double getTotalWeight() {
        double total = 0.0;
        for (WheelSlice slice : slices) {
            total += slice.getChance();
        }
        return total;
    }

    public double getChancePercentage(WheelSlice slice) {
        double total = getTotalWeight();
        if (total <= 0.0 || slice == null) {
            return 0.0;
        }
        return (slice.getChance() / total) * 100.0;
    }

    public WheelSlice getSliceById(String id) {
        if (id == null) {
            return null;
        }
        for (WheelSlice slice : slices) {
            if (slice.getId().equalsIgnoreCase(id)) {
                return slice;
            }
        }
        return null;
    }

    public WheelSlice pickWeightedRandomSlice() {
        if (slices.isEmpty()) {
            return null;
        }
        double totalWeight = getTotalWeight();
        if (totalWeight <= 0.0) {
            return slices.get(ThreadLocalRandom.current().nextInt(slices.size()));
        }

        double roll = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulative = 0.0;
        for (WheelSlice slice : slices) {
            cumulative += slice.getChance();
            if (roll <= cumulative) {
                return slice;
            }
        }
        return slices.get(slices.size() - 1);
    }

    public WheelSlice getSliceAtAngle(float angleDegrees) {
        if (slices.isEmpty()) {
            return null;
        }
        float norm = ((angleDegrees % 360.0f) + 360.0f) % 360.0f;
        int sliceIndex = ((int) Math.floor((norm + 22.5f) / 45.0f)) % 8;
        for (WheelSlice slice : slices) {
            if (slice.getIndex() == sliceIndex) {
                return slice;
            }
        }
        return slices.get(sliceIndex % slices.size());
    }

    public List<WheelSlice> getSlices() {
        return Collections.unmodifiableList(slices);
    }
}
