package com.tkitsunefortune.util;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;

public class LanguageManager {

    private final TKitsuneFortunePlugin plugin;

    public LanguageManager(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnglish() {
        String lang = plugin.getConfig().getString("settings.language", "TR");
        return lang != null && (lang.equalsIgnoreCase("EN")
                || lang.equalsIgnoreCase("ENG")
                || lang.equalsIgnoreCase("ENGLISH")
                || lang.toUpperCase(Locale.ROOT).startsWith("EN"));
    }

    public String getLanguageCode() {
        return isEnglish() ? "EN" : "TR";
    }

    public String tr(String trText, String enText) {
        return isEnglish() ? enText : trText;
    }

    public void setLanguage(String langCode, boolean translateConfigContent) {
        boolean toEnglish = langCode != null && (langCode.equalsIgnoreCase("EN")
                || langCode.equalsIgnoreCase("ENG")
                || langCode.equalsIgnoreCase("ENGLISH")
                || langCode.toUpperCase(Locale.ROOT).startsWith("EN"));
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("settings.language", toEnglish ? "EN" : "TR");

        if (translateConfigContent) {
            if (toEnglish) {
                applyEnglishDefaults(cfg);
            } else {
                applyTurkishDefaults(cfg);
            }
        }

        plugin.saveConfig();
        plugin.getSliceManager().loadSlices();
        plugin.getTicketManager().refreshAllOnlinePlayerTickets();
        plugin.getWheelManager().respawnAll();
    }

    private void applyEnglishDefaults(FileConfiguration cfg) {
        // Hologram
        cfg.set("settings.hologram.title", "&6&l✦ &e&lTkitsune Wheel of Fortune &6&l✦");
        cfg.set("settings.hologram.idle-lines", List.of(
                "%title%",
                "&7Right-Click with your &e&lFortune Wheel Ticket &7in hand!"
        ));
        cfg.set("settings.hologram.spinning-lines", List.of(
                "%title%",
                "&7Spinner: &b%player%",
                "&e▸ &f%slice% &e◂"
        ));

        // Ticket
        cfg.set("ticket.name", "&6&l✦ &e&lTkitsune Fortune Wheel Ticket &6&l✦");
        cfg.set("ticket.lore", List.of(
                "&8Custom VIP Fortune Wheel Ticket",
                "",
                "&7Hold this golden ticket in your hand and",
                "&7right-click the &6Tkitsune Wheel of Fortune &7to spin!",
                "",
                "&e▸ &aHold in hand & Right-Click the Wheel!"
        ));

        // Messages
        cfg.set("messages.prefix", "&8[&6&lFortuneWheel&8] &r");
        cfg.set("messages.no-permission", "&cYou do not have permission to do this!");
        cfg.set("messages.wheel-busy", "&cThis Wheel of Fortune is currently being spun by &e%spinner%&c! Please wait.");
        cfg.set("messages.no-ticket", "&cYou need a &eFortune Wheel Ticket &cto spin the wheel!");
        cfg.set("messages.no-ticket-in-hand", "&cYou must hold a &eFortune Wheel Ticket &cin your hand to spin the wheel!");
        cfg.set("messages.spin-started", "&aThe Wheel of Fortune is spinning! Good luck...");
        cfg.set("messages.ticket-given", "&aGave &e%amount%x Fortune Wheel Ticket &ato &e%player%&a!");
        cfg.set("messages.ticket-received", "&aYou received &e%amount%x Fortune Wheel Ticket&a!");
        cfg.set("messages.wheel-placed", "&aWheel of Fortune (&e#%id%&a) placed! Mode: &e%mode%");
        cfg.set("messages.wheel-removed", "&cWheel of Fortune (&e#%id%&c) removed.");
        cfg.set("messages.mode-changed", "&aRender mode changed to &e%mode% &aand all wheels updated!");
        cfg.set("messages.scale-changed", "&aWheel scale changed to &e%scale% &aand all wheels updated!");
        cfg.set("messages.reloaded", "&aTkitsuneFortune configuration and all 3D wheels reloaded!");

        // Slices
        String enSub = "&fReward: &r%reward%";
        List<String> enMsgs = List.of("&aCongratulations! You won &r%reward% &afrom &r%slice%&a!");
        String enBcast = "&6&l✦ &e%player% &7won &r%slice% &8(&r%reward%&8) &7from the Wheel of Fortune! &6&l✦";

        setSliceText(cfg, "red",
                "&c&lRed Slice &8(&6&lJACKPOT&8)",
                "&c&l16x Diamond\n&a&l+2 Emerald Block",
                "&c&l✦ JACKPOT! ✦",
                enSub, enMsgs, enBcast);

        setSliceText(cfg, "orange",
                "&6&lOrange Slice &8(&eGold Treasure&8)",
                "&6&l32x Gold",
                "&6&lCONGRATULATIONS!",
                enSub, enMsgs, enBcast);

        setSliceText(cfg, "yellow",
                "&e&lYellow Slice &8(&eEnchanted Golden Apple&8)",
                "&e&lEnchanted Apple",
                "&e&lLUCKY SPIN!",
                enSub, enMsgs, enBcast);

        setSliceText(cfg, "green",
                "&a&lGreen Slice &8(&aNetherite Sword&8)",
                "&a&l1x Netherite Sword",
                "&a&lYOU WON!",
                enSub, enMsgs, enBcast);

        setSliceText(cfg, "cyan",
                "&b&lCyan Slice &8(&bDiamond Pack&8)",
                "&b&l8x Diamond",
                "&b&lAWESOME!",
                enSub, enMsgs, enBcast);

        setSliceText(cfg, "blue",
                "&9&lBlue Slice &8(&9XP Bottle Pack&8)",
                "&9&l32x XP Bottle",
                "&9&lCONGRATULATIONS!",
                enSub, enMsgs, enBcast);

        setSliceText(cfg, "purple",
                "&5&lPurple Slice &8(&5Netherite Scrap&8)",
                "&5&l2x Netherite",
                "&5&lLEGENDARY!",
                enSub, enMsgs, enBcast);

        setSliceText(cfg, "magenta",
                "&d&lMagenta Slice &8(&dFree Respin Ticket&8)",
                "&d&l1x Ticket\n&f&l+16x Iron",
                "&d&lSPIN AGAIN!",
                enSub, enMsgs, enBcast);
    }

    private void applyTurkishDefaults(FileConfiguration cfg) {
        // Hologram
        cfg.set("settings.hologram.title", "&6&l✦ &e&lTkitsune Şans Çarkı &6&l✦");
        cfg.set("settings.hologram.idle-lines", List.of(
                "%title%",
                "&7Elindeki &e&lÖzel Şans Çarkı Bileti &7ile &aSağ Tıkla!"
        ));
        cfg.set("settings.hologram.spinning-lines", List.of(
                "%title%",
                "&7Çeviren: &b%player%",
                "&e▸ &f%slice% &e◂"
        ));

        // Ticket
        cfg.set("ticket.name", "&6&l✦ &e&lTkitsune Şans Çarkı Bileti &6&l✦");
        cfg.set("ticket.lore", List.of(
                "&8Özel Tasarım VIP Çark Bileti",
                "",
                "&7Bu özel işlemeli altın bileti elinde tutarak",
                "&6Tkitsune Şans Çarkına &7sağ tıkla ve çevir!",
                "",
                "&e▸ &aElinde tutarak Şans Çarkına sağ tıkla!"
        ));

        // Messages
        cfg.set("messages.prefix", "&8[&6&lŞansÇarkı&8] &r");
        cfg.set("messages.no-permission", "&cBu işlemi yapmak için yetkiniz yok!");
        cfg.set("messages.wheel-busy", "&cBu Şans Çarkı şu anda &e%spinner% &ctarafından çevriliyor! Lütfen bitmesini bekle.");
        cfg.set("messages.no-ticket", "&cŞans Çarkını çevirmek için &eŞans Çarkı Biletine &cihtiyacın var!");
        cfg.set("messages.no-ticket-in-hand", "&cŞans Çarkını çevirmek için &eŞans Çarkı Biletini &celinde tutmalısın!");
        cfg.set("messages.spin-started", "&aŞans Çarkı dönmeye başladı! Bol şans...");
        cfg.set("messages.ticket-given", "&a%player% &7adlı oyuncuya &e%amount%x Şans Çarkı Bileti &7verildi!");
        cfg.set("messages.ticket-received", "&aSana &e%amount%x Şans Çarkı Bileti &averildi!");
        cfg.set("messages.wheel-placed", "&aŞans Çarkı (&e#%id%&a) başarıyla yerleştirildi! Mod: &e%mode%");
        cfg.set("messages.wheel-removed", "&cŞans Çarkı (&e#%id%&c) kaldırıldı.");
        cfg.set("messages.mode-changed", "&aGörünüm modu &e%mode% &aolarak değiştirildi ve tüm çarklar güncellendi!");
        cfg.set("messages.scale-changed", "&aÇark boyutu &e%scale% &aolarak değiştirildi ve tüm çarklar güncellendi!");
        cfg.set("messages.reloaded", "&aTkitsuneFortune yapılandırması ve tüm 3D çarklar başarıyla yeniden yüklendi!");

        // Slices
        String trSub = "&fKazandığın ödül: &r%reward%";
        List<String> trMsgs = List.of("&aTebrikler! &r%slice% &aödülü olarak &r%reward% &akazandın!");
        String trBcast = "&6&l✦ &e%player% &7Şans Çarkından &r%slice% &8(&r%reward%&8) &7ödülünü kazandı! &6&l✦";

        setSliceText(cfg, "red",
                "&c&lKırmızı Dilim &8(&6&lJACKPOT&8)",
                "&c&l16x Elmas\n&a&l+2 Zümrüt Blok",
                "&c&l✦ JACKPOT! ✦",
                trSub, trMsgs, trBcast);

        setSliceText(cfg, "orange",
                "&6&lTuruncu Dilim &8(&eAltın Hazinesi&8)",
                "&6&l32x Altın",
                "&6&lTEBRİKLER!",
                trSub, trMsgs, trBcast);

        setSliceText(cfg, "yellow",
                "&e&lSarı Dilim &8(&eBüyülü Altın Elma&8)",
                "&e&lBüyülü Elma",
                "&e&lŞANSLISIN!",
                trSub, trMsgs, trBcast);

        setSliceText(cfg, "green",
                "&a&lYeşil Dilim &8(&aNetherite Kılıcı&8)",
                "&a&l1x Netherite Kılıcı",
                "&a&lKAZANDIN!",
                trSub, trMsgs, trBcast);

        setSliceText(cfg, "cyan",
                "&b&lTurkuaz Dilim &8(&bElmas Paketi&8)",
                "&b&l8x Elmas",
                "&b&lHARİKA!",
                trSub, trMsgs, trBcast);

        setSliceText(cfg, "blue",
                "&9&lMavi Dilim &8(&9Tecrübe İksiri&8)",
                "&9&l32x XP Şişesi",
                "&9&lTEBRİKLER!",
                trSub, trMsgs, trBcast);

        setSliceText(cfg, "purple",
                "&5&lMor Dilim &8(&5Netherite Hurdası&8)",
                "&5&l2x Netherite",
                "&5&lEFSANEVİ!",
                trSub, trMsgs, trBcast);

        setSliceText(cfg, "magenta",
                "&d&lPembe Dilim &8(&dÜcretsiz Tekrar Bileti&8)",
                "&d&l1x Bilet\n&f&l+16x Demir",
                "&d&lTEKRAR ÇEVİR!",
                trSub, trMsgs, trBcast);
    }

    private void setSliceText(
            FileConfiguration cfg,
            String key,
            String displayName,
            String sliceLabel,
            String title,
            String subtitle,
            List<String> messages,
            String broadcastMsg
    ) {
        String base = "slices." + key + ".";
        cfg.set(base + "display-name", displayName);
        cfg.set(base + "slice-label", sliceLabel);
        cfg.set(base + "title", title);
        cfg.set(base + "subtitle", subtitle);
        cfg.set(base + "messages", messages);
        if (broadcastMsg != null) {
            cfg.set(base + "broadcast-message", broadcastMsg);
        }
    }
}

