package com.tkitsunefortune.gui;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.util.ColorUtil;
import com.tkitsunefortune.wheel.FortuneWheel;
import com.tkitsunefortune.wheel.RenderMode;
import com.tkitsunefortune.wheel.WheelSlice;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminMenuListener implements Listener {

    private final TKitsuneFortunePlugin plugin;

    public AdminMenuListener(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    private String tr(String trText, String enText) {
        return plugin.tr(trText, enText);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory topInv = event.getView().getTopInventory();
        if (!(topInv.getHolder() instanceof AdminMenuHolder holder)) {
            return;
        }

        if (holder.getMenuType() == AdminMenuHolder.MenuType.MAIN) {
            event.setCancelled(true);
            if (event.getClickedInventory() != topInv) {
                return;
            }
            handleMainMenuClick(player, event.getSlot(), event.getClick());
        } else if (holder.getMenuType() == AdminMenuHolder.MenuType.HOLOGRAM_EDITOR) {
            event.setCancelled(true);
            if (event.getClickedInventory() != topInv) {
                return;
            }
            handleHologramEditorClick(player, event.getSlot(), event.getClick());
        } else if (holder.getMenuType() == AdminMenuHolder.MenuType.SLICE_EDITOR) {
            handleSliceEditorClick(player, holder, topInv, event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (!(topInv.getHolder() instanceof AdminMenuHolder holder)) {
            return;
        }
        if (holder.getMenuType() == AdminMenuHolder.MenuType.MAIN
                || holder.getMenuType() == AdminMenuHolder.MenuType.HOLOGRAM_EDITOR) {
            event.setCancelled(true);
            return;
        }
        // In SLICE_EDITOR, only allow dragging into slots 27..35 (or bottom player inventory)
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topInv.getSize() && (rawSlot < AdminMenuManager.PRIZE_SLOT_START || rawSlot > AdminMenuManager.PRIZE_SLOT_END)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        Inventory topInv = event.getView().getTopInventory();
        if (!(topInv.getHolder() instanceof AdminMenuHolder holder)) {
            return;
        }
        if (holder.getMenuType() == AdminMenuHolder.MenuType.SLICE_EDITOR) {
            WheelSlice slice = plugin.getSliceManager().getSliceById(holder.getSliceId());
            if (slice != null) {
                savePrizeSlotsFromInventory(slice, topInv, false);
            }
        }
    }

    private void handleMainMenuClick(Player player, int slot, ClickType click) {
        String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));

        // Check if one of the 8 slice slots was clicked
        List<WheelSlice> slices = plugin.getSliceManager().getSlices();
        for (int i = 0; i < slices.size() && i < AdminMenuManager.MAIN_SLICE_SLOTS.length; i++) {
            if (slot == AdminMenuManager.MAIN_SLICE_SLOTS[i]) {
                WheelSlice slice = slices.get(i);
                if (click.isLeftClick()) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                    plugin.getAdminMenuManager().openSliceEditor(player, slice);
                } else if (click == ClickType.RIGHT) {
                    slice.setChance(slice.getChance() + 1.0);
                    plugin.getSliceManager().saveSliceToConfig(slice);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.4f);
                    plugin.getAdminMenuManager().openMainMenu(player);
                } else if (click == ClickType.SHIFT_RIGHT) {
                    slice.setChance(Math.max(0.0, slice.getChance() - 1.0));
                    plugin.getSliceManager().saveSliceToConfig(slice);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.9f);
                    plugin.getAdminMenuManager().openMainMenu(player);
                }
                return;
            }
        }

        switch (slot) {
            case 37 -> { // Place wheel
                player.closeInventory();
                String id = plugin.getWheelManager().generateNextWheelId();
                Block targetBlock = player.getTargetBlockExact(5);
                Location spawnLoc = (targetBlock != null && !targetBlock.getType().isAir())
                        ? targetBlock.getLocation().add(0.5, 1.0, 0.5)
                        : player.getLocation().getBlock().getLocation().add(0.5, 0.0, 0.5);
                float snappedYaw = Math.round(player.getLocation().getYaw() / 22.5f) * 22.5f;
                plugin.getWheelManager().createWheel(id, spawnLoc, snappedYaw);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.2f);
                player.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&a3D Tkitsune Şans Çarkı (&e#" + id + "&a) başarıyla kuruldu!",
                        "&a3D Tkitsune Wheel of Fortune (&e#" + id + "&a) placed successfully!"
                )));
            }
            case 38 -> { // Remove nearest wheel
                FortuneWheel nearest = plugin.getWheelManager().findNearestWheel(player.getLocation(), 8.0);
                if (nearest == null) {
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&cYakınınızda (8 blok içinde) kaldırılacak bir Şans Çarkı bulunamadı!",
                            "&cNo Wheel of Fortune found within 8 blocks!"
                    )));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.7f);
                } else {
                    plugin.getWheelManager().removeWheel(nearest.getId());
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&cŞans Çarkı (&e#" + nearest.getId() + "&c) kaldırıldı.",
                            "&cWheel of Fortune (&e#" + nearest.getId() + "&c) removed."
                    )));
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1.0f);
                }
            }
            case 39 -> { // Give custom ticket
                int amount = click.isShiftClick() ? 64 : (click.isRightClick() ? 16 : 1);
                ItemStack ticket = plugin.getTicketManager().createTicket(amount);
                Map<Integer, ItemStack> leftovers = player.getInventory().addItem(ticket);
                for (ItemStack leftover : leftovers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.9f, 1.2f);
                player.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&aEnvanterinize &e" + amount + "x Tkitsune Şans Çarkı Bileti &aeklendi!",
                        "&aAdded &e" + amount + "x Tkitsune Fortune Wheel Ticket &ato your inventory!"
                )));
            }
            case 40 -> { // Scale +/- 0.1
                double current = plugin.getConfig().getDouble("settings.scale", 1.7);
                double delta = click.isRightClick() ? -0.1 : 0.1;
                double next = Math.max(0.5, Math.min(4.0, Math.round((current + delta) * 10.0) / 10.0));
                plugin.getConfig().set("settings.scale", next);
                plugin.saveConfig();
                plugin.getWheelManager().respawnAll();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openMainMenu(player);
            }
            case 41 -> { // Spin duration +/- 10 ticks
                int current = plugin.getConfig().getInt("settings.spin-duration-ticks", 120);
                int delta = click.isRightClick() ? -10 : 10;
                int next = Math.max(20, Math.min(400, current + delta));
                plugin.getConfig().set("settings.spin-duration-ticks", next);
                plugin.saveConfig();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openMainMenu(player);
            }
            case 42 -> { // Toggle RenderMode
                RenderMode current = RenderMode.fromString(plugin.getConfig().getString("settings.render-mode", "RESOURCE_PACK"));
                RenderMode next = (current == RenderMode.BLOCK_DISPLAY) ? RenderMode.RESOURCE_PACK : RenderMode.BLOCK_DISPLAY;
                plugin.getConfig().set("settings.render-mode", next.name());
                plugin.saveConfig();
                plugin.getWheelManager().respawnAll();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.9f, 1.2f);
                plugin.getAdminMenuManager().openMainMenu(player);
            }
            case 43 -> { // Toggle show-slice-prizes
                boolean current = plugin.getConfig().getBoolean("settings.show-slice-prizes", true);
                plugin.getConfig().set("settings.show-slice-prizes", !current);
                plugin.saveConfig();
                plugin.getWheelManager().respawnAll();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openMainMenu(player);
            }
            case 46 -> { // Toggle Language (TR <-> EN)
                String nextLang = plugin.isEnglish() ? "TR" : "EN";
                boolean translateAll = !click.isRightClick();
                plugin.getLanguageManager().setLanguage(nextLang, translateAll);
                String newPrefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
                player.sendMessage(newPrefix + ColorUtil.colorize(tr(
                        "&aDil &eTürkçe (TR) &aolarak değiştirildi!",
                        "&aLanguage switched to &eEnglish (EN)&a!"
                )));
                plugin.getAdminMenuManager().openMainMenu(player);
            }
            case 47 -> { // Hologram design editor / toggle
                if (click.isRightClick()) {
                    boolean current = plugin.getConfig().getBoolean("settings.hologram.enabled", true);
                    plugin.getConfig().set("settings.hologram.enabled", !current);
                    plugin.saveConfig();
                    plugin.getWheelManager().respawnAll();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                    plugin.getAdminMenuManager().openMainMenu(player);
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                    plugin.getAdminMenuManager().openHologramEditor(player);
                }
            }
            case 48 -> { // Toggle require-ticket-in-hand
                boolean current = plugin.getConfig().getBoolean("settings.require-ticket-in-hand", true);
                plugin.getConfig().set("settings.require-ticket-in-hand", !current);
                plugin.saveConfig();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openMainMenu(player);
            }
            case 49 -> { // Reload all
                plugin.reloadPluginState();
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
                player.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&aTüm ayarlar kaydedildi ve 3D Şans Çarkları yenilendi!",
                        "&aAll settings saved and 3D Fortune Wheels reloaded!"
                )));
                plugin.getAdminMenuManager().openMainMenu(player);
            }
            case 50 -> { // Regenerate & send Resource Pack
                File zip = plugin.getResourcePackGenerator().generateResourcePack();
                plugin.getResourcePackHostServer().reloadPackCache(zip);
                plugin.getResourcePackHostServer().sendToAllOnlinePlayers();
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.9f, 1.2f);
                player.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&aResource Pack tüm oyunculara gönderildi!",
                        "&aResource Pack sent to all online players!"
                )));
            }
        }
    }

    private void handleHologramEditorClick(Player player, int slot, ClickType click) {
        String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));
        switch (slot) {
            case 4 -> { // Toggle hologram enabled
                boolean current = plugin.getConfig().getBoolean("settings.hologram.enabled", true);
                plugin.getConfig().set("settings.hologram.enabled", !current);
                plugin.saveConfig();
                plugin.getWheelManager().respawnAll();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, !current ? 1.3f : 0.8f);
                player.sendMessage(prefix + ColorUtil.colorize(!current
                        ? tr("&aÇark üstü hologram &2AÇILDI&a!", "&aWheel hologram &2ENABLED&a!")
                        : tr("&cÇark üstü hologram &4KAPATILDI&c!", "&cWheel hologram &4DISABLED&c!")));
                plugin.getAdminMenuManager().openHologramEditor(player);
            }
            case 10 -> { // Hologram title
                if (click.isRightClick()) {
                    plugin.getWheelManager().setHologramTitle(tr(
                            "&6&l✦ &e&lTkitsune Şans Çarkı &6&l✦",
                            "&6&l✦ &e&lTkitsune Wheel of Fortune &6&l✦"
                    ));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 1.3f);
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&aÇark hologram başlığı varsayılana sıfırlandı!",
                            "&aWheel hologram title reset to default!"
                    )));
                    plugin.getAdminMenuManager().openHologramEditor(player);
                } else {
                    plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), "HOLOGRAM", AdminMenuManager.ChatEditType.HOLOGRAM_TITLE);
                    player.closeInventory();
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&eLütfen çarkın hologramda görünecek yeni başlığını sohbete yazın &7(Örn: &6&l✦ &e&lTkitsune Şans Çarkı &6&l✦ &7veya &ciptal&7):",
                            "&ePlease type the new hologram title in chat &7(e.g. &6&l✦ &e&lTkitsune Wheel of Fortune &6&l✦ &7or &ccancel&7):"
                    )));
                }
            }
            case 11 -> { // Idle lines
                List<String> lines = new ArrayList<>(plugin.getConfig().getStringList("settings.hologram.idle-lines"));
                if (lines.isEmpty()) {
                    lines.add("%title%");
                    lines.add(tr(
                            "&7Elindeki &e&lÖzel Şans Çarkı Bileti &7ile &aSağ Tıkla!",
                            "&7Right-Click with your &e&lFortune Wheel Ticket &7in hand!"
                    ));
                }
                if (click == ClickType.SHIFT_LEFT) {
                    plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), "HOLOGRAM", AdminMenuManager.ChatEditType.HOLOGRAM_SET_IDLE_LINES);
                    player.closeInventory();
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&eBekleme hologramının tüm satırlarını aralarına &f| &ekoyarak sohbete yazın:",
                            "&eType all idle hologram lines in chat separated by &f|&e:"
                    )));
                    player.sendMessage(ColorUtil.colorize(tr(
                            "&7Örnek: &f%title% | &7Elindeki &e&lBilet &7ile &aSağ Tıkla! &7(İptal: &ciptal&7)",
                            "&7Example: &f%title% | &7Right-Click with your &e&lTicket&7! &7(Cancel: &ccancel&7)"
                    )));
                } else if (click == ClickType.SHIFT_RIGHT) {
                    plugin.getConfig().set("settings.hologram.idle-lines", List.of(
                            "%title%",
                            tr(
                                    "&7Elindeki &e&lÖzel Şans Çarkı Bileti &7ile &aSağ Tıkla!",
                                    "&7Right-Click with your &e&lFortune Wheel Ticket &7in hand!"
                            )
                    ));
                    plugin.saveConfig();
                    plugin.getWheelManager().respawnAll();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 1.3f);
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&aBekleme hologramı satırları varsayılana sıfırlandı!",
                            "&aIdle hologram lines reset to default!"
                    )));
                    plugin.getAdminMenuManager().openHologramEditor(player);
                } else if (click.isRightClick()) {
                    if (lines.size() > 1) {
                        lines.remove(lines.size() - 1);
                        plugin.getConfig().set("settings.hologram.idle-lines", lines);
                        plugin.saveConfig();
                        plugin.getWheelManager().respawnAll();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.9f);
                        plugin.getAdminMenuManager().openHologramEditor(player);
                    } else {
                        player.sendMessage(prefix + ColorUtil.colorize(tr(
                                "&cEn az 1 satır kalmalıdır!",
                                "&cAt least 1 line must remain!"
                        )));
                    }
                } else {
                    plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), "HOLOGRAM", AdminMenuManager.ChatEditType.HOLOGRAM_ADD_IDLE_LINE);
                    player.closeInventory();
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&eBekleme hologramına eklenecek yeni satırı sohbete yazın &7(Başlık için &f%title% &7kullanabilirsiniz, iptal için &ciptal&7):",
                            "&eType the new line to add to the idle hologram &7(You can use &f%title%&7, type &ccancel &7to abort):"
                    )));
                }
            }
            case 12 -> { // Spinning lines
                List<String> lines = new ArrayList<>(plugin.getConfig().getStringList("settings.hologram.spinning-lines"));
                if (lines.isEmpty()) {
                    lines.add("%title%");
                    lines.add(tr("&7Çeviren: &b%player%", "&7Spinner: &b%player%"));
                    lines.add("&e▸ &f%slice% &e◂");
                }
                if (click == ClickType.SHIFT_LEFT) {
                    plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), "HOLOGRAM", AdminMenuManager.ChatEditType.HOLOGRAM_SET_SPIN_LINES);
                    player.closeInventory();
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&eDönüş hologramının tüm satırlarını aralarına &f| &ekoyarak sohbete yazın:",
                            "&eType all spinning hologram lines in chat separated by &f|&e:"
                    )));
                    player.sendMessage(ColorUtil.colorize(tr(
                            "&7Örnek: &f%title% | &7Çeviren: &b%player% | &e▸ &f%slice% &e◂ &7(İptal: &ciptal&7)",
                            "&7Example: &f%title% | &7Spinner: &b%player% | &e▸ &f%slice% &e◂ &7(Cancel: &ccancel&7)"
                    )));
                } else if (click == ClickType.SHIFT_RIGHT) {
                    plugin.getConfig().set("settings.hologram.spinning-lines", List.of(
                            "%title%",
                            tr("&7Çeviren: &b%player%", "&7Spinner: &b%player%"),
                            "&e▸ &f%slice% &e◂"
                    ));
                    plugin.saveConfig();
                    plugin.getWheelManager().respawnAll();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 1.3f);
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&aDönüş hologramı satırları varsayılana sıfırlandı!",
                            "&aSpinning hologram lines reset to default!"
                    )));
                    plugin.getAdminMenuManager().openHologramEditor(player);
                } else if (click.isRightClick()) {
                    if (lines.size() > 1) {
                        lines.remove(lines.size() - 1);
                        plugin.getConfig().set("settings.hologram.spinning-lines", lines);
                        plugin.saveConfig();
                        plugin.getWheelManager().respawnAll();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.9f);
                        plugin.getAdminMenuManager().openHologramEditor(player);
                    } else {
                        player.sendMessage(prefix + ColorUtil.colorize(tr(
                                "&cEn az 1 satır kalmalıdır!",
                                "&cAt least 1 line must remain!"
                        )));
                    }
                } else {
                    plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), "HOLOGRAM", AdminMenuManager.ChatEditType.HOLOGRAM_ADD_SPIN_LINE);
                    player.closeInventory();
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&eDönüş hologramına eklenecek yeni satırı sohbete yazın &7(&f%title%&7, &f%player%&7, &f%slice% &7kullanabilirsiniz, iptal: &ciptal&7):",
                            "&eType the new line to add to the spinning hologram &7(Placeholders: &f%title%&7, &f%player%&7, &f%slice%&7, or &ccancel&7):"
                    )));
                }
            }
            case 13 -> { // Height offset
                double current = plugin.getConfig().getDouble("settings.hologram.height-offset", 2.25);
                double next;
                if (click == ClickType.SHIFT_RIGHT) {
                    next = 2.25;
                } else {
                    double delta = click.isRightClick() ? -0.10 : 0.10;
                    next = Math.max(0.50, Math.min(5.00, Math.round((current + delta) * 100.0) / 100.0));
                }
                plugin.getConfig().set("settings.hologram.height-offset", next);
                plugin.saveConfig();
                plugin.getWheelManager().respawnAll();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openHologramEditor(player);
            }
            case 14 -> { // Scale
                double current = plugin.getConfig().getDouble("settings.hologram.scale", 1.0);
                double next;
                if (click == ClickType.SHIFT_RIGHT) {
                    next = 1.0;
                } else {
                    double delta = click.isRightClick() ? -0.1 : 0.1;
                    next = Math.max(0.4, Math.min(3.0, Math.round((current + delta) * 10.0) / 10.0));
                }
                plugin.getConfig().set("settings.hologram.scale", next);
                plugin.saveConfig();
                plugin.getWheelManager().respawnAll();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openHologramEditor(player);
            }
            case 15 -> { // Background cycle
                List<String> styles = List.of("TRANSPARENT", "DARK", "CRIMSON", "GOLD", "PURPLE", "AQUA");
                String current = plugin.getConfig().getString("settings.hologram.background", "TRANSPARENT").toUpperCase(Locale.ROOT);
                int idx = styles.indexOf(current);
                if (idx < 0) idx = 0;
                int nextIdx = click.isRightClick()
                        ? (idx - 1 + styles.size()) % styles.size()
                        : (idx + 1) % styles.size();
                plugin.getConfig().set("settings.hologram.background", styles.get(nextIdx));
                plugin.saveConfig();
                plugin.getWheelManager().respawnAll();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openHologramEditor(player);
            }
            case 16 -> { // Billboard & Shadow
                if (click.isRightClick()) {
                    boolean shadowed = plugin.getConfig().getBoolean("settings.hologram.shadowed", true);
                    plugin.getConfig().set("settings.hologram.shadowed", !shadowed);
                } else {
                    List<String> modes = List.of("CENTER", "VERTICAL", "FIXED");
                    String current = plugin.getConfig().getString("settings.hologram.billboard", "CENTER").toUpperCase(Locale.ROOT);
                    int idx = modes.indexOf(current);
                    int nextIdx = (idx + 1) % modes.size();
                    plugin.getConfig().set("settings.hologram.billboard", modes.get(nextIdx));
                }
                plugin.saveConfig();
                plugin.getWheelManager().respawnAll();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openHologramEditor(player);
            }
            case 28 -> applyHologramPreset(player,
                    tr("&6&l✦ &e&lTkitsune Şans Çarkı &6&l✦", "&6&l✦ &e&lTkitsune Wheel of Fortune &6&l✦"),
                    List.of("%title%", tr("&7Elindeki &e&lÖzel Şans Çarkı Bileti &7ile &aSağ Tıkla!", "&7Right-Click with your &e&lFortune Wheel Ticket &7in hand!")),
                    List.of("%title%", tr("&7Çeviren: &b%player%", "&7Spinner: &b%player%"), "&e▸ &f%slice% &e◂"),
                    "TRANSPARENT", tr("Klasik Altın", "Classic Gold"));
            case 29 -> applyHologramPreset(player,
                    tr("&4&l❖ &c&lKITSUNE &6&lŞANS ÇARKI &4&l❖", "&4&l❖ &c&lKITSUNE &6&lFORTUNE WHEEL &4&l❖"),
                    List.of("&8&m━━━&4&m━━━&6&l ⛩ &4&m━━━&8&m━━━", "%title%", tr("&7Biletinle &e&lSağ Tıkla &7ve Kadere Meydan Oku!", "&e&lRight-Click &7with a Ticket & Challenge Fate!")),
                    List.of("&8&m━━━&4&m━━━&6&l ⛩ &4&m━━━&8&m━━━", "%title%", tr("&7Çeviren: &e%player%", "&7Spinner: &e%player%"), "&c&l⚡ &f%slice% &c&l⚡"),
                    "TRANSPARENT", tr("Kitsune Tapınağı", "Kitsune Shrine"));
            case 30 -> applyHologramPreset(player,
                    tr("&b&l⚡ &3&lSİBER &d&lŞANS ÇARKI &b&l⚡", "&b&l⚡ &3&lCYBER &d&lFORTUNE WHEEL &b&l⚡"),
                    List.of("%title%", tr("&d▸ &fBilet ile &b&lSAĞ TIKLA &fve Çevir! &d◂", "&d▸ &b&lRIGHT-CLICK &fwith a Ticket to Spin! &d◂")),
                    List.of("%title%", tr("&bOyuncu: &f%player%", "&bPlayer: &f%player%"), "&d&l» &e%slice% &d&l«"),
                    "TRANSPARENT", tr("Siber Neon", "Cyber Neon"));
            case 32 -> applyHologramPreset(player,
                    tr("&6&l👑 &e&lKRALİYET &f&lJACKPOT ÇARKI &6&l👑", "&6&l👑 &e&lROYAL &f&lJACKPOT WHEEL &6&l👑"),
                    List.of("&e&l✦ &6&m──────────────&e&l ✦", "%title%", tr("&fÖzel Bilet ile &a&lSağ Tıkla &fve Kazan!", "&a&lRight-Click &fwith a VIP Ticket to Win!"), "&e&l✦ &6&m──────────────&e&l ✦"),
                    List.of("&e&l✦ &6&m──────────────&e&l ✦", "%title%", tr("&7Şansını Deneyen: &e%player%", "&7Challenger: &e%player%"), "&6&l👑 &f%slice% &6&l👑"),
                    "TRANSPARENT", tr("Kraliyet VIP", "Royal VIP"));
            case 33 -> applyHologramPreset(player,
                    tr("&e&lTkitsune Şans Çarkı", "&e&lTkitsune Wheel of Fortune"),
                    List.of("%title%", tr("&7Bilet ile &aSağ Tıkla", "&aRight-Click &7with Ticket")),
                    List.of(tr("&e%player% &7çeviriyor...", "&e%player% &7is spinning..."), "&f%slice%"),
                    "TRANSPARENT", tr("Minimalist Sade", "Minimalist Clean"));
            case 34 -> applyHologramPreset(player,
                    tr("&4&l🔥 &c&lCEHENNEM &6&lATEŞİ ÇARKI &4&l🔥", "&4&l🔥 &c&lHELLFIRE &6&lFORTUNE WHEEL &4&l🔥"),
                    List.of("%title%", tr("&c▸ &7Ateş Biletinle &6&lSağ Tıkla! &c◂", "&c▸ &6&lRight-Click &7with your Ticket! &c◂")),
                    List.of("%title%", tr("&c🔥 &7Çeviren: &6%player% &c🔥", "&c🔥 &7Spinner: &6%player% &c🔥"), "&4&l» &e%slice% &4&l«"),
                    "TRANSPARENT", tr("Ateş & Alev", "Hellfire & Flame"));
            case 49 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.1f);
                plugin.getAdminMenuManager().openMainMenu(player);
            }
        }
    }

    private void applyHologramPreset(Player player, String title, List<String> idleLines, List<String> spinLines, String bg, String presetName) {
        String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));
        plugin.getConfig().set("settings.hologram.enabled", true);
        plugin.getConfig().set("settings.hologram.title", title);
        plugin.getConfig().set("settings.hologram.idle-lines", idleLines);
        plugin.getConfig().set("settings.hologram.spinning-lines", spinLines);
        plugin.getConfig().set("settings.hologram.background", bg);
        plugin.saveConfig();
        plugin.getWheelManager().respawnAll();
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.3f);
        player.sendMessage(prefix + ColorUtil.colorize(tr(
                "&aHologram tasarımı &e" + presetName + " &aşablonu olarak güncellendi!",
                "&aHologram theme updated to &e" + presetName + "&a!"
        )));
        plugin.getAdminMenuManager().openHologramEditor(player);
    }

    private void handleSliceEditorClick(Player player, AdminMenuHolder holder, Inventory topInv, InventoryClickEvent event) {
        WheelSlice slice = plugin.getSliceManager().getSliceById(holder.getSliceId());
        if (slice == null) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot >= 54) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && !clicked.getType().isAir()) {
                    for (int s = AdminMenuManager.PRIZE_SLOT_START; s <= AdminMenuManager.PRIZE_SLOT_END; s++) {
                        ItemStack existing = topInv.getItem(s);
                        if (existing == null || existing.getType().isAir()) {
                            topInv.setItem(s, clicked.clone());
                            event.setCurrentItem(null);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
                            break;
                        }
                    }
                }
            }
            return;
        }

        if (rawSlot >= AdminMenuManager.PRIZE_SLOT_START && rawSlot <= AdminMenuManager.PRIZE_SLOT_END) {
            return;
        }

        event.setCancelled(true);
        ClickType click = event.getClick();
        String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));

        switch (rawSlot) {
            case 2 -> { // Change 3D Slice Icon
                savePrizeSlotsFromInventory(slice, topInv, false);
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    if (plugin.getTicketManager().isTicket(cursor)) {
                        slice.setIconMaterial("TICKET");
                        slice.setCustomIconItem(null);
                    } else {
                        slice.setIconMaterial(cursor.getType().name());
                        ItemStack customClone = cursor.clone();
                        customClone.setAmount(1);
                        slice.setCustomIconItem(customClone);
                    }
                    plugin.getSliceManager().saveSliceToConfig(slice);
                    plugin.getWheelManager().respawnAll();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 1.4f);
                    plugin.getAdminMenuManager().openSliceEditor(player, slice);
                } else if (click.isRightClick()) {
                    slice.setIconMaterial("TICKET");
                    slice.setCustomIconItem(null);
                    plugin.getSliceManager().saveSliceToConfig(slice);
                    plugin.getWheelManager().respawnAll();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 1.4f);
                    plugin.getAdminMenuManager().openSliceEditor(player, slice);
                } else {
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&eİpucu: &7Envanterinizden bir eşyayı fareyle tutup &b[Çark Üstü 3D İkon] &7slotuna tıklayarak ikonu değiştirebilirsiniz!",
                            "&eTip: &7Pick up an item from your inventory with your cursor and click the &b[On-Wheel 3D Icon] &7slot to change the icon!"
                    )));
                }
            }
            case 3 -> { // Edit slice-label via chat
                savePrizeSlotsFromInventory(slice, topInv, false);
                plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), slice.getId(), AdminMenuManager.ChatEditType.SLICE_LABEL);
                player.closeInventory();
                player.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&eLütfen sohbete bu dilimin çark üzerinde görünecek yeni yazısını yazın &7(İptal için &ciptal &7yazın):",
                        "&ePlease type the new on-wheel label for this slice in chat &7(Type &ccancel &7to abort):"
                )));
            }
            case 4 -> { // Edit display-name via chat
                savePrizeSlotsFromInventory(slice, topInv, false);
                plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), slice.getId(), AdminMenuManager.ChatEditType.DISPLAY_NAME);
                player.closeInventory();
                player.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&eLütfen sohbete bu dilimin yeni başlığını yazın &7(Örn: &c&lKırmızı Dilim (Jackpot)&7, iptal için &ciptal&7):",
                        "&ePlease type the new display name for this slice in chat &7(e.g. &c&lRed Slice (Jackpot)&7, or &ccancel&7):"
                )));
            }
            case 5 -> { // Commands management
                savePrizeSlotsFromInventory(slice, topInv, false);
                if (click.isLeftClick()) {
                    plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), slice.getId(), AdminMenuManager.ChatEditType.ADD_COMMAND);
                    player.closeInventory();
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&eLütfen eklenecek konsol komutunu başında / olmadan yazın &7(Örn: &fgive %player% diamond 16 &7veya &ciptal&7):",
                            "&ePlease type the console command to add without a leading / &7(e.g. &fgive %player% diamond 16 &7or &ccancel&7):"
                    )));
                } else if (click == ClickType.SHIFT_RIGHT) {
                    slice.setCommands(new ArrayList<>());
                    plugin.getSliceManager().saveSliceToConfig(slice);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1.0f);
                    plugin.getAdminMenuManager().openSliceEditor(player, slice);
                } else if (click.isRightClick()) {
                    List<String> cmds = new ArrayList<>(slice.getCommands());
                    if (!cmds.isEmpty()) {
                        cmds.remove(cmds.size() - 1);
                        slice.setCommands(cmds);
                        plugin.getSliceManager().saveSliceToConfig(slice);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f);
                        plugin.getAdminMenuManager().openSliceEditor(player, slice);
                    }
                }
            }
            case 6 -> { // Toggle broadcast
                savePrizeSlotsFromInventory(slice, topInv, false);
                slice.setBroadcast(!slice.isBroadcast());
                plugin.getSliceManager().saveSliceToConfig(slice);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                plugin.getAdminMenuManager().openSliceEditor(player, slice);
            }
            case 10 -> adjustChance(player, slice, topInv, -10.0);
            case 11 -> adjustChance(player, slice, topInv, -5.0);
            case 12 -> adjustChance(player, slice, topInv, click.isRightClick() ? -0.5 : -1.0);
            case 13 -> { // Exact chance via chat
                savePrizeSlotsFromInventory(slice, topInv, false);
                plugin.getAdminMenuManager().setPendingChatEdit(player.getUniqueId(), slice.getId(), AdminMenuManager.ChatEditType.CHANCE);
                player.closeInventory();
                player.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&eLütfen bu dilim için yeni şans oranını sayı olarak yazın &7(Örn: &a15.5 &7veya &ciptal&7):",
                        "&ePlease type the new chance weight as a number &7(e.g. &a15.5 &7or &ccancel&7):"
                )));
            }
            case 14 -> adjustChance(player, slice, topInv, click.isRightClick() ? 0.5 : 1.0);
            case 15 -> adjustChance(player, slice, topInv, 5.0);
            case 16 -> adjustChance(player, slice, topInv, 10.0);
            case 48 -> { // Save & back to Main Menu
                savePrizeSlotsFromInventory(slice, topInv, true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.1f);
                plugin.getAdminMenuManager().openMainMenu(player);
            }
            case 49 -> { // Save prizes & update wheel
                savePrizeSlotsFromInventory(slice, topInv, true);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.3f);
                player.sendMessage(prefix + ColorUtil.colorize("&a" + slice.getDisplayName() + tr(
                        " &aödülleri kaydedildi ve 3D çark güncellendi!",
                        " &arewards saved and 3D wheel updated!"
                )));
                plugin.getAdminMenuManager().openSliceEditor(player, slice);
            }
            case 50 -> { // Clear commands so only drag-and-drop items are given
                savePrizeSlotsFromInventory(slice, topInv, false);
                slice.setCommands(new ArrayList<>());
                plugin.getSliceManager().saveSliceToConfig(slice);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.7f, 1.2f);
                player.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&eKomut ödülleri temizlendi; artık sadece kutuya koyduğunuz eşyalar verilecek.",
                        "&eCommand rewards cleared; only items placed in the prize slots will be awarded."
                )));
                plugin.getAdminMenuManager().openSliceEditor(player, slice);
            }
        }
    }

    private void adjustChance(Player player, WheelSlice slice, Inventory topInv, double delta) {
        savePrizeSlotsFromInventory(slice, topInv, false);
        slice.setChance(Math.max(0.0, slice.getChance() + delta));
        plugin.getSliceManager().saveSliceToConfig(slice);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, delta > 0 ? 1.3f : 0.9f);
        plugin.getAdminMenuManager().openSliceEditor(player, slice);
    }

    private void savePrizeSlotsFromInventory(WheelSlice slice, Inventory topInv, boolean autoUpdateWheelVisuals) {
        List<ItemStack> items = new ArrayList<>();
        for (int s = AdminMenuManager.PRIZE_SLOT_START; s <= AdminMenuManager.PRIZE_SLOT_END; s++) {
            ItemStack item = topInv.getItem(s);
            if (item != null && !item.getType().isAir()) {
                items.add(item.clone());
            }
        }

        boolean hadItemsBefore = !slice.getRewardItems().isEmpty();
        slice.setRewardItems(items);

        if (!items.isEmpty()) {
            ItemStack first = items.get(0);
            if (plugin.getTicketManager().isTicket(first)) {
                slice.setIconMaterial("TICKET");
                slice.setCustomIconItem(null);
            } else {
                slice.setIconMaterial(first.getType().name());
                ItemStack single = first.clone();
                single.setAmount(1);
                slice.setCustomIconItem(single);
            }
            if (!hadItemsBefore || autoUpdateWheelVisuals) {
                String itemName = (first.hasItemMeta() && first.getItemMeta() != null && first.getItemMeta().hasDisplayName())
                        ? first.getItemMeta().getDisplayName()
                        : formatMaterialName(first.getType());
                slice.setSliceLabel(ColorUtil.colorize("&e&l" + first.getAmount() + "x &f" + itemName));
            }
        }

        plugin.getSliceManager().saveSliceToConfig(slice);
        if (autoUpdateWheelVisuals || !items.isEmpty()) {
            plugin.getWheelManager().respawnAll();
        }
    }

    private String formatMaterialName(Material mat) {
        String raw = mat.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] parts = raw.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAdminChatInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getAdminMenuManager().hasPendingChatEdit(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        AdminMenuManager.PendingChatEdit edit = plugin.getAdminMenuManager().consumePendingChatEdit(player.getUniqueId());
        String msg = event.getMessage().trim();

        Bukkit.getScheduler().runTask(plugin, () -> {
            String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));
            if (edit.type() == AdminMenuManager.ChatEditType.HOLOGRAM_TITLE
                    || edit.type() == AdminMenuManager.ChatEditType.HOLOGRAM_ADD_IDLE_LINE
                    || edit.type() == AdminMenuManager.ChatEditType.HOLOGRAM_SET_IDLE_LINES
                    || edit.type() == AdminMenuManager.ChatEditType.HOLOGRAM_ADD_SPIN_LINE
                    || edit.type() == AdminMenuManager.ChatEditType.HOLOGRAM_SET_SPIN_LINES) {
                if (msg.equalsIgnoreCase("iptal") || msg.equalsIgnoreCase("cancel")) {
                    player.sendMessage(prefix + ColorUtil.colorize(tr("&cİşlem iptal edildi.", "&cAction cancelled.")));
                    plugin.getAdminMenuManager().openHologramEditor(player);
                    return;
                }
                switch (edit.type()) {
                    case HOLOGRAM_TITLE -> {
                        plugin.getWheelManager().setHologramTitle(msg);
                        player.sendMessage(prefix + ColorUtil.colorize(tr(
                                "&aÇark hologram başlığı güncellendi: &r",
                                "&aWheel hologram title updated: &r"
                        ) + plugin.getWheelManager().getHologramTitle()));
                    }
                    case HOLOGRAM_ADD_IDLE_LINE -> {
                        List<String> lines = new ArrayList<>(plugin.getConfig().getStringList("settings.hologram.idle-lines"));
                        if (lines.isEmpty()) {
                            lines.add("%title%");
                        }
                        lines.add(msg);
                        plugin.getConfig().set("settings.hologram.idle-lines", lines);
                        plugin.saveConfig();
                        plugin.getWheelManager().respawnAll();
                        player.sendMessage(prefix + ColorUtil.colorize(tr(
                                "&aBekleme hologramına yeni satır eklendi: &r",
                                "&aAdded new idle hologram line: &r"
                        ) + ColorUtil.colorize(msg)));
                    }
                    case HOLOGRAM_SET_IDLE_LINES -> {
                        String[] parts = msg.split("\\|");
                        List<String> lines = new ArrayList<>();
                        for (String p : parts) {
                            lines.add(p.trim());
                        }
                        plugin.getConfig().set("settings.hologram.idle-lines", lines);
                        plugin.saveConfig();
                        plugin.getWheelManager().respawnAll();
                        player.sendMessage(prefix + ColorUtil.colorize(tr(
                                "&aBekleme hologramı satırları komple güncellendi (&e" + lines.size() + " satır&a)!",
                                "&aIdle hologram lines updated (&e" + lines.size() + " lines&a)!"
                        )));
                    }
                    case HOLOGRAM_ADD_SPIN_LINE -> {
                        List<String> lines = new ArrayList<>(plugin.getConfig().getStringList("settings.hologram.spinning-lines"));
                        if (lines.isEmpty()) {
                            lines.add("%title%");
                        }
                        lines.add(msg);
                        plugin.getConfig().set("settings.hologram.spinning-lines", lines);
                        plugin.saveConfig();
                        plugin.getWheelManager().respawnAll();
                        player.sendMessage(prefix + ColorUtil.colorize(tr(
                                "&aDönüş hologramına yeni satır eklendi: &r",
                                "&aAdded new spinning hologram line: &r"
                        ) + ColorUtil.colorize(msg)));
                    }
                    case HOLOGRAM_SET_SPIN_LINES -> {
                        String[] parts = msg.split("\\|");
                        List<String> lines = new ArrayList<>();
                        for (String p : parts) {
                            lines.add(p.trim());
                        }
                        plugin.getConfig().set("settings.hologram.spinning-lines", lines);
                        plugin.saveConfig();
                        plugin.getWheelManager().respawnAll();
                        player.sendMessage(prefix + ColorUtil.colorize(tr(
                                "&aDönüş hologramı satırları komple güncellendi (&e" + lines.size() + " satır&a)!",
                                "&aSpinning hologram lines updated (&e" + lines.size() + " lines&a)!"
                        )));
                    }
                }
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9f, 1.2f);
                plugin.getAdminMenuManager().openHologramEditor(player);
                return;
            }

            WheelSlice slice = plugin.getSliceManager().getSliceById(edit.sliceId());
            if (slice == null) {
                return;
            }
            if (msg.equalsIgnoreCase("iptal") || msg.equalsIgnoreCase("cancel")) {
                player.sendMessage(prefix + ColorUtil.colorize(tr("&cİşlem iptal edildi.", "&cAction cancelled.")));
                plugin.getAdminMenuManager().openSliceEditor(player, slice);
                return;
            }

            switch (edit.type()) {
                case CHANCE -> {
                    try {
                        double val = Double.parseDouble(msg.replace(',', '.'));
                        slice.setChance(Math.max(0.0, val));
                        plugin.getSliceManager().saveSliceToConfig(slice);
                        player.sendMessage(prefix + ColorUtil.colorize(tr(
                                "&aŞans oranı &e" + slice.getChance() + " &aolarak güncellendi!",
                                "&aChance weight updated to &e" + slice.getChance() + "&a!"
                        )));
                    } catch (NumberFormatException e) {
                        player.sendMessage(prefix + ColorUtil.colorize(tr("&cGeçersiz sayı girdiniz!", "&cInvalid number!")));
                    }
                }
                case SLICE_LABEL -> {
                    slice.setSliceLabel(ColorUtil.colorize(msg));
                    plugin.getSliceManager().saveSliceToConfig(slice);
                    plugin.getWheelManager().respawnAll();
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&aÇark üstü yazı güncellendi: &r",
                            "&aOn-wheel label updated: &r"
                    ) + slice.getSliceLabel()));
                }
                case DISPLAY_NAME -> {
                    slice.setDisplayName(ColorUtil.colorize(msg));
                    plugin.getSliceManager().saveSliceToConfig(slice);
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&aDilim başlığı güncellendi: &r",
                            "&aSlice display name updated: &r"
                    ) + slice.getDisplayName()));
                }
                case ADD_COMMAND -> {
                    String cleanCmd = msg.startsWith("/") ? msg.substring(1) : msg;
                    List<String> cmds = new ArrayList<>(slice.getCommands());
                    cmds.add(cleanCmd);
                    slice.setCommands(cmds);
                    plugin.getSliceManager().saveSliceToConfig(slice);
                    player.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&aYeni ödül komutu eklendi: &e/",
                            "&aAdded new reward command: &e/"
                    ) + cleanCmd));
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9f, 1.2f);
            plugin.getAdminMenuManager().openSliceEditor(player, slice);
        });
    }
}
