package com.tkitsunefortune.command;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.util.ColorUtil;
import com.tkitsunefortune.wheel.FortuneWheel;
import com.tkitsunefortune.wheel.RenderMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FortuneWheelCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "menu", "place", "remove", "list", "ticket", "ticketname", "biletisim", "name", "isim", "hologram", "sound", "ses", "lang", "dil", "mode", "scale", "resourcepack", "reload", "help"
    );

    private final TKitsuneFortunePlugin plugin;

    public FortuneWheelCommand(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    private String tr(String trText, String enText) {
        return plugin.tr(trText, enText);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));

        if (!sender.hasPermission("tkitsunefortune.admin")) {
            sender.sendMessage(prefix + ColorUtil.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Default "/tkf" opens the interactive Admin Settings & Prize Menu!
        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.getAdminMenuManager().openMainMenu(player);
            } else {
                sendHelp(sender, "tkf");
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "menu", "gui", "ayarlar", "settings" -> {
                if (sender instanceof Player player) {
                    plugin.getAdminMenuManager().openMainMenu(player);
                } else {
                    sender.sendMessage(prefix + ColorUtil.colorize(tr(
                            "&cMenü sadece oyun içinden açılabilir!",
                            "&cThe GUI menu can only be opened in-game!"
                    )));
                }
            }
            case "place", "create", "kur" -> handlePlace(sender, args, prefix);
            case "remove", "delete", "sil" -> handleRemove(sender, args, prefix);
            case "list", "liste" -> handleList(sender, prefix);
            case "ticket", "bilet", "give" -> handleTicket(sender, args, prefix);
            case "ticketname", "biletisim", "biletadi" -> handleTicketName(sender, args, prefix);
            case "name", "isim", "hologram", "baslik", "title" -> handleName(sender, args, prefix);
            case "sound", "sounds", "ses", "sesler" -> handleSound(sender, args, prefix);
            case "lang", "language", "dil" -> handleLang(sender, args);
            case "mode", "mod" -> handleMode(sender, args, prefix);
            case "scale", "boyut" -> handleScale(sender, args, prefix);
            case "resourcepack", "rp" -> handleResourcePack(sender, prefix);
            case "reload", "yenile" -> handleReload(sender, prefix);
            default -> sendHelp(sender, "tkf");
        }
        return true;
    }

    private void handleTicketName(CommandSender sender, String[] args, String prefix) {
        if (args.length < 2) {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cKullanım: &e/tkf biletisim <yeni bilet ismi>",
                    "&cUsage: &e/tkf ticketname <new ticket name>"
            )));
            return;
        }
        String newName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getTicketManager().setTicketName(newName);
        sender.sendMessage(prefix + ColorUtil.colorize(tr(
                "&aBilet ismi güncellendi: &r",
                "&aTicket name updated: &r"
        ) + ColorUtil.colorize(plugin.getTicketManager().getTicketName())));
    }

    private void handleSound(CommandSender sender, String[] args, String prefix) {
        if (args.length < 2) {
            if (sender instanceof Player player) {
                plugin.getAdminMenuManager().openSoundEditor(player);
            } else {
                sender.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&cKullanım: /tkf ses <ac|kapat|toggle>",
                        "&cUsage: /tkf sound <on|off|toggle>"
                )));
            }
            return;
        }
        String arg1 = args[1].toLowerCase(Locale.ROOT);
        if (arg1.equals("menu") && sender instanceof Player player) {
            plugin.getAdminMenuManager().openSoundEditor(player);
            return;
        }
        boolean current = plugin.getConfig().getBoolean("settings.sounds.enabled", true);
        boolean next = switch (arg1) {
            case "ac", "aç", "on" -> true;
            case "kapat", "off", "mute" -> false;
            default -> !current;
        };
        plugin.getConfig().set("settings.sounds.enabled", next);
        plugin.saveConfig();
        sender.sendMessage(prefix + ColorUtil.colorize(next
                ? tr("&aÇark sesleri &2AÇILDI&a!", "&aWheel sounds &2ENABLED&a!")
                : tr("&cÇark sesleri &4KAPATILDI&c!", "&cWheel sounds &4MUTED&c!")));
    }

    private void handleLang(CommandSender sender, String[] args) {
        String targetLang;
        if (args.length >= 2) {
            targetLang = args[1].toUpperCase(Locale.ROOT);
        } else {
            targetLang = plugin.isEnglish() ? "TR" : "EN";
        }
        boolean translateAll = !(args.length >= 3 && args[2].equalsIgnoreCase("ui"));
        plugin.getLanguageManager().setLanguage(targetLang, translateAll);
        String newPrefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));
        sender.sendMessage(newPrefix + ColorUtil.colorize(tr(
                "&aEklenti dili &eTürkçe (TR) &aolarak değiştirildi ve tüm çarklar güncellendi!",
                "&aPlugin language switched to &eEnglish (EN) &aand all wheels updated!"
        )));
    }

    private void handlePlace(CommandSender sender, String[] args, String prefix) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cBu komut sadece oyun içinden kullanılabilir!",
                    "&cThis command can only be used in-game!"
            )));
            return;
        }

        String id = args.length >= 2 ? args[1] : plugin.getWheelManager().generateNextWheelId();

        Block targetBlock = player.getTargetBlockExact(5);
        Location spawnLoc;
        if (targetBlock != null && !targetBlock.getType().isAir()) {
            spawnLoc = targetBlock.getLocation().add(0.5, 1.0, 0.5);
        } else {
            spawnLoc = player.getLocation().getBlock().getLocation().add(0.5, 0.0, 0.5);
        }

        float rawYaw = player.getLocation().getYaw();
        float snappedYaw = Math.round(rawYaw / 22.5f) * 22.5f;

        plugin.getWheelManager().createWheel(id, spawnLoc, snappedYaw);
        String mode = plugin.getConfig().getString("settings.render-mode", "RESOURCE_PACK");
        String msg = plugin.getConfig().getString("messages.wheel-placed",
                        tr("&aŞans Çarkı (#%id%) yerleştirildi!", "&aWheel of Fortune (#%id%) placed!"))
                .replace("%id%", id)
                .replace("%mode%", mode);
        player.sendMessage(prefix + ColorUtil.colorize(msg));
    }

    private void handleRemove(CommandSender sender, String[] args, String prefix) {
        String id;
        if (args.length >= 2 && !args[1].equalsIgnoreCase("nearest")) {
            id = args[1];
        } else if (sender instanceof Player player) {
            FortuneWheel nearest = plugin.getWheelManager().findNearestWheel(player.getLocation(), 8.0);
            if (nearest == null) {
                sender.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&cYakınınızda (8 blok içinde) herhangi bir Şans Çarkı bulunamadı!",
                        "&cNo Wheel of Fortune found within 8 blocks!"
                )));
                return;
            }
            id = nearest.getId();
        } else {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cKullanım: /tkf remove <id>",
                    "&cUsage: /tkf remove <id>"
            )));
            return;
        }

        if (plugin.getWheelManager().removeWheel(id)) {
            String msg = plugin.getConfig().getString("messages.wheel-removed",
                            tr("&cŞans Çarkı (#%id%) kaldırıldı.", "&cWheel of Fortune (#%id%) removed."))
                    .replace("%id%", id);
            sender.sendMessage(prefix + ColorUtil.colorize(msg));
        } else {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cBelirtilen ID ile bir Şans Çarkı bulunamadı: &e",
                    "&cNo Wheel of Fortune found with ID: &e"
            ) + id));
        }
    }

    private void handleList(CommandSender sender, String prefix) {
        var wheels = plugin.getWheelManager().getWheels();
        if (wheels.isEmpty()) {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&7Henüz yerleştirilmiş bir Şans Çarkı bulunmuyor.",
                    "&7There are no placed Fortune Wheels yet."
            )));
            return;
        }
        sender.sendMessage(prefix + ColorUtil.colorize(tr(
                "&6&lAktif Şans Çarkları (" + wheels.size() + "):",
                "&6&lActive Fortune Wheels (" + wheels.size() + "):"
        )));
        for (FortuneWheel w : wheels) {
            sender.sendMessage(ColorUtil.colorize(String.format(
                    "&8- &e#%s &7(%s: %.1f, %.1f, %.1f) &8[%s: %s&8]",
                    w.getId(),
                    w.getWorldName(),
                    w.getX(), w.getY(), w.getZ(),
                    tr("Durum", "Status"),
                    w.isSpinning() ? tr("&cDönüyor", "&cSpinning") : tr("&aHazır", "&aIdle")
            )));
        }
    }

    private void handleTicket(CommandSender sender, String[] args, String prefix) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(prefix + ColorUtil.colorize(tr(
                        "&cOyuncu bulunamadı: &e",
                        "&cPlayer not found: &e"
                ) + args[1]));
                return;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cKullanım: /tkf ticket <oyuncu> [miktar]",
                    "&cUsage: /tkf ticket <player> [amount]"
            )));
            return;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException ignored) {
                amount = 1;
            }
        }

        ItemStack ticketItem = plugin.getTicketManager().createTicket(amount);
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(ticketItem);
        for (ItemStack leftover : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), leftover);
        }

        String givenMsg = plugin.getConfig().getString("messages.ticket-given",
                        tr("&a%player% adlı oyuncuya %amount%x bilet verildi!", "&aGave %amount%x ticket to %player%!"))
                .replace("%player%", target.getName())
                .replace("%amount%", String.valueOf(amount));
        sender.sendMessage(prefix + ColorUtil.colorize(givenMsg));

        if (!sender.equals(target)) {
            String recMsg = plugin.getConfig().getString("messages.ticket-received",
                            tr("&aSana %amount%x Şans Çarkı Bileti verildi!", "&aYou received %amount%x Fortune Wheel Ticket!"))
                    .replace("%amount%", String.valueOf(amount));
            target.sendMessage(prefix + ColorUtil.colorize(recMsg));
        }
    }

    private void handleName(CommandSender sender, String[] args, String prefix) {
        if (args.length < 2) {
            if (sender instanceof Player player && args[0].equalsIgnoreCase("hologram")) {
                plugin.getAdminMenuManager().openHologramEditor(player);
                return;
            }
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cKullanım: &e/tkf hologram &7(Tasarım Menüsü) &cveya &e/tkf isim <yeni çark ismi>",
                    "&cUsage: &e/tkf hologram &7(Design Menu) &cor &e/tkf name <new wheel title>"
            )));
            return;
        }
        String arg1 = args[1].toLowerCase(Locale.ROOT);
        if (args.length == 2 && (arg1.equals("toggle") || arg1.equals("ac") || arg1.equals("aç") || arg1.equals("kapat") || arg1.equals("on") || arg1.equals("off"))) {
            boolean current = plugin.getConfig().getBoolean("settings.hologram.enabled", true);
            boolean next = switch (arg1) {
                case "ac", "aç", "on" -> true;
                case "kapat", "off" -> false;
                default -> !current;
            };
            plugin.getConfig().set("settings.hologram.enabled", next);
            plugin.saveConfig();
            plugin.getWheelManager().respawnAll();
            sender.sendMessage(prefix + ColorUtil.colorize(next
                    ? tr("&aÇark üstü hologram &2AÇILDI&a!", "&aWheel hologram &2ENABLED&a!")
                    : tr("&cÇark üstü hologram &4KAPATILDI&c!", "&cWheel hologram &4DISABLED&c!")));
            return;
        }
        if (args.length == 2 && (arg1.equals("menu") || arg1.equals("tasarim") || arg1.equals("tasarım") || arg1.equals("editor")) && sender instanceof Player player) {
            plugin.getAdminMenuManager().openHologramEditor(player);
            return;
        }
        String newTitle = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getWheelManager().setHologramTitle(newTitle);
        sender.sendMessage(prefix + ColorUtil.colorize(tr(
                "&aÇark hologram ismi güncellendi: &r",
                "&aWheel hologram title updated: &r"
        ) + plugin.getWheelManager().getHologramTitle()));
    }

    private void handleMode(CommandSender sender, String[] args, String prefix) {
        if (args.length < 2) {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cKullanım: /tkf mode <BLOCK_DISPLAY|RESOURCE_PACK>",
                    "&cUsage: /tkf mode <BLOCK_DISPLAY|RESOURCE_PACK>"
            )));
            return;
        }
        RenderMode mode = RenderMode.fromString(args[1]);
        plugin.getConfig().set("settings.render-mode", mode.name());
        plugin.saveConfig();
        plugin.getWheelManager().respawnAll();

        String msg = plugin.getConfig().getString("messages.mode-changed",
                        tr("&aGörünüm modu %mode% olarak değiştirildi!", "&aRender mode changed to %mode%!"))
                .replace("%mode%", mode.name());
        sender.sendMessage(prefix + ColorUtil.colorize(msg));
    }

    private void handleScale(CommandSender sender, String[] args, String prefix) {
        if (args.length < 2) {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cKullanım: /tkf scale <0.5 - 4.0>",
                    "&cUsage: /tkf scale <0.5 - 4.0>"
            )));
            return;
        }
        double scale;
        try {
            scale = Math.max(0.25, Math.min(5.0, Double.parseDouble(args[1])));
        } catch (NumberFormatException e) {
            sender.sendMessage(prefix + ColorUtil.colorize(tr(
                    "&cGeçersiz sayı! Örnek: /tkf scale 1.7",
                    "&cInvalid number! Example: /tkf scale 1.7"
            )));
            return;
        }

        plugin.getConfig().set("settings.scale", scale);
        plugin.saveConfig();
        plugin.getWheelManager().respawnAll();

        String msg = plugin.getConfig().getString("messages.scale-changed",
                        tr("&aÇark boyutu %scale% olarak değiştirildi!", "&aWheel scale changed to %scale%!"))
                .replace("%scale%", String.valueOf(scale));
        sender.sendMessage(prefix + ColorUtil.colorize(msg));
    }

    private void handleResourcePack(CommandSender sender, String prefix) {
        File zip = plugin.getResourcePackGenerator().generateResourcePack();
        plugin.getResourcePackHostServer().reloadPackCache(zip);
        plugin.getResourcePackHostServer().sendToAllOnlinePlayers();
        sender.sendMessage(prefix + ColorUtil.colorize(tr(
                "&aResource Pack oluşturuldu ve aktif oyunculara gönderildi: &e",
                "&aResource Pack generated and sent to online players: &e"
        ) + zip.getPath()));
    }

    private void handleReload(CommandSender sender, String prefix) {
        plugin.reloadPluginState();
        sender.sendMessage(prefix + ColorUtil.colorize(plugin.getConfig().getString("messages.reloaded")));
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ColorUtil.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ColorUtil.colorize(tr(
                "&6&l✦ TkitsuneFortune &e3D Şans Çarkı (1.21.11) &6&l✦",
                "&6&l✦ TkitsuneFortune &e3D Wheel of Fortune (1.21.11) &6&l✦"
        )));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " &8- " + tr("&aÖdül, Şans Oranı ve Ayar Menüsünü (GUI) açar", "&aOpens the Prize, Chance & Settings GUI Menu")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " place [id] &8- " + tr("&7Baktığınız yere 3D Şans Çarkı kurar", "&7Places a 3D Wheel of Fortune where you look")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " remove [id|nearest] &8- " + tr("&7Şans Çarkını kaldırır", "&7Removes a Wheel of Fortune")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " list &8- " + tr("&7Aktif Şans Çarklarını listeler", "&7Lists active Fortune Wheels")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " ticket <player> [amount] &8- " + tr("&7Özel Şans Çarkı Bileti verir", "&7Gives Custom Fortune Wheel Tickets")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " biletisim <yeni isim> &8- " + tr("&7Bilet eşyasının ismini değiştirir", "&7Changes the ticket item name")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " hologram [on|off|menu] &8- " + tr("&7Hologram Tasarım Menüsünü açar veya açıp kapatır", "&7Opens the Hologram Design Menu or toggles it")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " ses [ac|kapat|menu] &8- " + tr("&7Çark Ses Ayarları Menüsünü açar", "&7Opens the Wheel Sound Settings Menu")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " name <title> &8- " + tr("&7Hologramdaki çark ismini değiştirir", "&7Changes the wheel hologram title")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " lang <tr|en> &8- " + tr("&7Eklenti dilini değiştirir (TR / EN)", "&7Switches plugin language (TR / EN)")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " mode <BLOCK_DISPLAY|RESOURCE_PACK> &8- " + tr("&73D modu değiştirir", "&7Changes 3D render mode")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " scale <size> &8- " + tr("&73D Çark boyutunu ayarlar", "&7Sets the 3D Wheel scale")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " resourcepack &8- " + tr("&7ResourcePack .zip dosyasını gönderir", "&7Sends the ResourcePack .zip to players")));
        sender.sendMessage(ColorUtil.colorize("&e/" + label + " reload &8- " + tr("&7Ayarları ve çarkları yeniden yükler", "&7Reloads config and all wheels")));
        sender.sendMessage(ColorUtil.colorize("&8&m----------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("tkitsunefortune.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filterPrefix(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("remove") || sub.equals("delete") || sub.equals("sil")) {
                List<String> ids = new ArrayList<>();
                ids.add("nearest");
                for (FortuneWheel w : plugin.getWheelManager().getWheels()) {
                    ids.add(w.getId());
                }
                return filterPrefix(ids, args[1]);
            }
            if (sub.equals("hologram") || sub.equals("sound") || sub.equals("ses")) {
                return filterPrefix(Arrays.asList("menu", "toggle", "on", "off", "ac", "kapat"), args[1]);
            }
            if (sub.equals("lang") || sub.equals("language") || sub.equals("dil")) {
                return filterPrefix(Arrays.asList("tr", "en"), args[1]);
            }
            if (sub.equals("mode") || sub.equals("mod")) {
                return filterPrefix(Arrays.asList("BLOCK_DISPLAY", "RESOURCE_PACK"), args[1]);
            }
            if (sub.equals("scale") || sub.equals("boyut")) {
                return filterPrefix(Arrays.asList("1.0", "1.25", "1.5", "1.7", "2.0"), args[1]);
            }
            if (sub.equals("ticket") || sub.equals("bilet") || sub.equals("give")) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    names.add(p.getName());
                }
                return filterPrefix(names, args[1]);
            }
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("ticket") || args[0].equalsIgnoreCase("bilet") || args[0].equalsIgnoreCase("give"))) {
            return filterPrefix(Arrays.asList("1", "3", "5", "10", "32", "64"), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> options, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String opt : options) {
            if (opt.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(opt);
            }
        }
        return matches;
    }
}
