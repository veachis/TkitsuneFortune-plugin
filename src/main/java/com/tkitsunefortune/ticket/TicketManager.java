package com.tkitsunefortune.ticket;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.List;

public class TicketManager {

    private final TKitsuneFortunePlugin plugin;
    private final NamespacedKey ticketKey;

    public TicketManager(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
        this.ticketKey = new NamespacedKey(plugin, "ticket");
    }

    public ItemStack createTicket(int amount) {
        FileConfiguration cfg = plugin.getConfig();
        String matName = cfg.getString("ticket.material", "PAPER");
        Material material = Material.matchMaterial(matName != null ? matName : "PAPER");
        if (material == null) {
            material = Material.PAPER;
        }

        ItemStack item = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = cfg.getString("ticket.name", "&6&l✦ &e&lTkitsune Şans Çarkı Bileti &6&l✦");
            meta.setDisplayName(ColorUtil.colorize(name));

            List<String> lore = cfg.getStringList("ticket.lore");
            if (!lore.isEmpty()) {
                meta.setLore(ColorUtil.colorize(lore));
            }

            // Use CustomModelData (1004) on PAPER so that:
            // - With ResourcePack loaded: 1.21.11 range_dispatch renders the 3D Ticket model!
            // - Without ResourcePack loaded: falls back to Paper texture (never purple/black cube!)
            int cmd = cfg.getInt("ticket.custom-model-data", 1004);
            if (cmd > 0) {
                meta.setCustomModelData(cmd);
            }

            boolean glow = cfg.getBoolean("ticket.glow", false);
            if (glow) {
                applyGlow(meta);
            }

            meta.getPersistentDataContainer().set(ticketKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createStandModelItem() {
        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(1001);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWheelModelItem() {
        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(1002);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void refreshPlayerTickets(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isTicket(item)) {
                inv.setItem(i, createTicket(item.getAmount()));
            }
        }
    }

    public void refreshAllOnlinePlayerTickets() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayerTickets(player);
        }
    }

    public String getTicketName() {
        return plugin.getConfig().getString("ticket.name",
                plugin.tr("&6&l✦ &e&lTkitsune Şans Çarkı Bileti &6&l✦", "&6&l✦ &e&lTkitsune Fortune Wheel Ticket &6&l✦"));
    }

    public void setTicketName(String rawName) {
        String trimmed = rawName == null ? "" : rawName.trim();
        if (trimmed.isEmpty()) {
            trimmed = plugin.tr("&6&l✦ &e&lTkitsune Şans Çarkı Bileti &6&l✦", "&6&l✦ &e&lTkitsune Fortune Wheel Ticket &6&l✦");
        } else if (!trimmed.contains("&") && !trimmed.contains("§")) {
            trimmed = "&6&l✦ &e&l" + trimmed + " &6&l✦";
        }
        plugin.getConfig().set("ticket.name", trimmed);
        plugin.saveConfig();
        refreshAllOnlinePlayerTickets();
        plugin.getWheelManager().respawnAll();
    }

    public void setTicketLore(List<String> rawLore) {
        plugin.getConfig().set("ticket.lore", rawLore);
        plugin.saveConfig();
        refreshAllOnlinePlayerTickets();
    }

    private void applyGlow(ItemMeta meta) {
        try {
            Method setGlint = ItemMeta.class.getMethod("setEnchantmentGlintOverride", Boolean.class);
            setGlint.invoke(meta, Boolean.TRUE);
        } catch (Throwable ignored) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    public boolean isTicket(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte flag = meta.getPersistentDataContainer().get(ticketKey, PersistentDataType.BYTE);
        return flag != null && flag == (byte) 1;
    }

    public boolean isHoldingTicket(Player player) {
        PlayerInventory inv = player.getInventory();
        return isTicket(inv.getItemInMainHand()) || isTicket(inv.getItemInOffHand());
    }

    public boolean consumeTicket(Player player, boolean requireInHand) {
        PlayerInventory inv = player.getInventory();

        // 1. Check main hand
        ItemStack mainHand = inv.getItemInMainHand();
        if (isTicket(mainHand)) {
            if (mainHand.getAmount() > 1) {
                mainHand.setAmount(mainHand.getAmount() - 1);
            } else {
                inv.setItemInMainHand(null);
            }
            return true;
        }

        // 2. Check off hand
        ItemStack offHand = inv.getItemInOffHand();
        if (isTicket(offHand)) {
            if (offHand.getAmount() > 1) {
                offHand.setAmount(offHand.getAmount() - 1);
            } else {
                inv.setItemInOffHand(null);
            }
            return true;
        }

        if (requireInHand) {
            return false;
        }

        // 3. Check player inventory storage slots
        ItemStack[] contents = inv.getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack slot = contents[i];
            if (isTicket(slot)) {
                if (slot.getAmount() > 1) {
                    slot.setAmount(slot.getAmount() - 1);
                    inv.setItem(i, slot);
                } else {
                    inv.setItem(i, null);
                }
                return true;
            }
        }

        return false;
    }
}
