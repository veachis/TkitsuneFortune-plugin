package com.tkitsunefortune.gui;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.util.ColorUtil;
import com.tkitsunefortune.wheel.WheelSlice;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AdminMenuManager {

    public enum ChatEditType {
        CHANCE,
        SLICE_LABEL,
        DISPLAY_NAME,
        ADD_COMMAND,
        HOLOGRAM_TITLE,
        HOLOGRAM_ADD_IDLE_LINE,
        HOLOGRAM_SET_IDLE_LINES,
        HOLOGRAM_ADD_SPIN_LINE,
        HOLOGRAM_SET_SPIN_LINES
    }

    public record PendingChatEdit(String sliceId, ChatEditType type) {
    }

    public static final int[] MAIN_SLICE_SLOTS = {10, 11, 12, 13, 14, 15, 16, 22};
    public static final int PRIZE_SLOT_START = 27;
    public static final int PRIZE_SLOT_END = 35;

    private final TKitsuneFortunePlugin plugin;
    private final Map<UUID, PendingChatEdit> pendingChatEdits = new HashMap<>();

    public AdminMenuManager(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    private String tr(String trText, String enText) {
        return plugin.tr(trText, enText);
    }

    public void openMainMenu(Player player) {
        AdminMenuHolder holder = new AdminMenuHolder(AdminMenuHolder.MenuType.MAIN, null);
        Inventory inv = Bukkit.createInventory(holder, 54, ColorUtil.colorize(
                tr("&8✦ &6&lTkitsune Çark Yönetimi &8✦", "&8✦ &6&lTkitsune Wheel Admin &8✦")
        ));
        holder.setInventory(inv);

        ItemStack glass = createButton(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }

        // Top header info
        inv.setItem(4, createButton(
                Material.NETHER_STAR,
                tr("&6&l✦ &e&lTkitsune Şans Çarkı Ayarları &6&l✦", "&6&l✦ &e&lTkitsune Wheel of Fortune Settings &6&l✦"),
                tr("&7Dilimlerin &eşans oranlarını&7, &aödül eşyalarını&7,", "&7Manage slice &echance weights&7, &aprize items&7,"),
                tr("&7çark üzerindeki &b3D ikon/yazıları &7ve genel", "&b3D slice icons/labels &7and global wheel"),
                tr("&7çark ayarlarını bu menüden yönetebilirsiniz!", "&7settings directly from this menu!"),
                "",
                tr("&e▸ &fBir dilimi düzenlemek için üzerine &aSol Tıklayın!", "&e▸ &aLeft-Click &fany slice to edit its prizes & chance!")
        ));

        // 8 Wheel Slices
        List<WheelSlice> slices = plugin.getSliceManager().getSlices();
        for (int i = 0; i < slices.size() && i < MAIN_SLICE_SLOTS.length; i++) {
            WheelSlice slice = slices.get(i);
            int slot = MAIN_SLICE_SLOTS[i];
            inv.setItem(slot, createMainSliceItem(slice));
        }

        // Bottom control buttons
        double scale = plugin.getConfig().getDouble("settings.scale", 1.7);
        int spinTicks = plugin.getConfig().getInt("settings.spin-duration-ticks", 120);
        String renderMode = plugin.getConfig().getString("settings.render-mode", "RESOURCE_PACK");
        boolean showPrizes = plugin.getConfig().getBoolean("settings.show-slice-prizes", true);
        boolean requireHand = plugin.getConfig().getBoolean("settings.require-ticket-in-hand", true);
        boolean en = plugin.isEnglish();

        inv.setItem(37, createButton(
                Material.GOLD_BLOCK,
                tr("&a&l✚ Baktığın Yere Çark Kur", "&a&l✚ Place Wheel Here"),
                tr("&7Bulunduğunuz/baktığınız bloğun üstüne", "&7Spawns a new 3D Tkitsune Wheel of Fortune"),
                tr("&7yeni bir 3D Tkitsune Şans Çarkı kurar.", "&7on the block you are looking at."),
                "",
                tr("&e▸ &aKurmak için Tıkla!", "&e▸ &aClick to Place!")
        ));

        inv.setItem(38, createButton(
                Material.BARRIER,
                tr("&c&l✖ Yakındaki Çarkı Kaldır", "&c&l✖ Remove Nearest Wheel"),
                tr("&7Yakınınızdaki (8 blok) en yakın", "&7Removes the nearest 3D Wheel of Fortune"),
                tr("&73D Şans Çarkını siler.", "&7within 8 blocks."),
                "",
                tr("&e▸ &cKaldırmak için Tıkla!", "&e▸ &cClick to Remove!")
        ));

        ItemStack ticketBtn = plugin.getTicketManager().createTicket(1);
        ItemMeta tMeta = ticketBtn.getItemMeta();
        if (tMeta != null) {
            tMeta.setDisplayName(ColorUtil.colorize(tr(
                    "&6&l✦ &e&lÖzel Çark Bileti Al &6&l✦",
                    "&6&l✦ &e&lGet Custom Wheel Ticket &6&l✦"
            )));
            tMeta.setLore(ColorUtil.colorize(Arrays.asList(
                    tr("&7Kendinize özel tasarımlı 3D", "&7Gives you the custom 3D"),
                    tr("&eTkitsune Şans Çarkı Bileti &7verir.", "&eTkitsune Fortune Wheel Ticket&7."),
                    "",
                    tr("&e▸ Sol Tık: &a1x Bilet Al", "&e▸ Left-Click: &aGet 1x Ticket"),
                    tr("&e▸ Sağ Tık: &a16x Bilet Al", "&e▸ Right-Click: &aGet 16x Tickets"),
                    tr("&e▸ Shift + Tık: &a64x Bilet Al", "&e▸ Shift-Click: &aGet 64x Tickets")
            )));
            ticketBtn.setItemMeta(tMeta);
        }
        inv.setItem(39, ticketBtn);

        inv.setItem(40, createButton(
                Material.SPYGLASS,
                tr("&b&l⇕ Çark Boyutu (Scale): &e", "&b&l⇕ Wheel Scale: &e") + String.format(Locale.US, "%.1f", scale),
                tr("&73D Şans Çarklarının boyutunu büyütür", "&7Increases or decreases the 3D size"),
                tr("&7veya küçültür.", "&7of all Fortune Wheels."),
                "",
                tr("&e▸ Sol Tık: &a+0.1 Büyüt", "&e▸ Left-Click: &a+0.1 Increase"),
                tr("&e▸ Sağ Tık: &c-0.1 Küçült", "&e▸ Right-Click: &c-0.1 Decrease")
        ));

        inv.setItem(41, createButton(
                Material.CLOCK,
                tr("&e&l⏱ Dönüş Süresi: &f", "&e&l⏱ Spin Duration: &f") + spinTicks + " Tick &7(" + String.format(Locale.US, "%.1f", spinTicks / 20.0) + tr(" sn)", " s)"),
                tr("&7Çarkın çevrildiğinde kaç saniye", "&7Adjusts how many seconds the wheel"),
                tr("&7boyunca döneceğini ayarlar.", "&7spins when activated."),
                "",
                tr("&e▸ Sol Tık: &a+10 Tick (+0.5 sn)", "&e▸ Left-Click: &a+10 Ticks (+0.5 s)"),
                tr("&e▸ Sağ Tık: &c-10 Tick (-0.5 sn)", "&e▸ Right-Click: &c-10 Ticks (-0.5 s)")
        ));

        inv.setItem(42, createButton(
                Material.ARMOR_STAND,
                tr("&d&l✦ 3D Görünüm Modu: &e", "&d&l✦ 3D Render Mode: &e") + renderMode,
                tr("&7BLOCK_DISPLAY: Doku paketsiz 111 parça 3D model", "&7BLOCK_DISPLAY: No-pack 111-part 3D BlockDisplay"),
                tr("&7RESOURCE_PACK: Özel doku paketli ItemDisplay model", "&7RESOURCE_PACK: Custom textured 3D ItemDisplay"),
                "",
                tr("&e▸ Değiştirmek için Tıkla!", "&e▸ Click to Toggle!")
        ));

        inv.setItem(43, createButton(
                showPrizes ? Material.LIME_DYE : Material.GRAY_DYE,
                tr("&6&l✦ Çark Üstü Ödüller: ", "&6&l✦ On-Wheel 3D Prizes: ") + (showPrizes ? tr("&aAçık", "&aEnabled") : tr("&cKapalı", "&cDisabled")),
                tr("&7Çark dilimlerinin üzerinde 3D ödül ikonları", "&7Toggles 3D prize icons and prize text labels"),
                tr("&7ve ödül isimlerinin görünmesini açıp kapatır.", "&7on the 8 slices of the wheel."),
                "",
                tr("&e▸ Değiştirmek için Tıkla!", "&e▸ Click to Toggle!")
        ));

        inv.setItem(46, createButton(
                Material.RECOVERY_COMPASS,
                "&b&l🌐 Dil / Language: " + (en ? "&a&lEnglish (EN)" : "&e&lTürkçe (TR)"),
                tr("&7Eklentinin menü, mesaj, bilet, hologram ve", "&7Switches the plugin's menus, messages, ticket,"),
                tr("&7çark dilimlerinin dilini değiştirir.", "&7hologram, and wheel slice labels between TR & EN."),
                "",
                tr("&e▸ Sol Tık: &aİngilizceye (EN) Çevir (Tüm Metinlerle)", "&e▸ Left-Click: &aSwitch to Turkish (TR) (Translate All)"),
                tr("&e▸ Sağ Tık: &bSadece Menü/Komut Dilini Değiştir", "&e▸ Right-Click: &bSwitch Menu/Command Language Only")
        ));

        boolean holoEnabled = plugin.getConfig().getBoolean("settings.hologram.enabled", true);
        String holoTitle = plugin.getWheelManager().getHologramTitle();

        inv.setItem(47, createButton(
                Material.NAME_TAG,
                tr("&6&l🏷 Hologram Ayarları & Tasarımı", "&6&l🏷 Hologram Settings & Design"),
                tr("&7Hologram Durumu: ", "&7Hologram Status: ") + (holoEnabled ? tr("&aAçık", "&aEnabled") : tr("&cKapalı", "&cDisabled")),
                tr("&7Mevcut Başlık: &r", "&7Current Title: &r") + holoTitle,
                tr("&7Hologramı açıp kapatabilir, başlığını, satırlarını,", "&7Toggle hologram on/off, customize its title, lines,"),
                tr("&7arka planını, boyutunu ve hazır temalarını", "&7background, size, and apply 1-click preset themes"),
                tr("&7özel tasarım menüsünden değiştirebilirsiniz!", "&7from the dedicated Hologram Design menu!"),
                "",
                tr("&e▸ Sol Tık: &aHologram Tasarım Menüsünü Aç", "&e▸ Left-Click: &aOpen Hologram Design Menu"),
                tr("&e▸ Sağ Tık: &eHologramı Hızlı Aç / Kapat", "&e▸ Right-Click: &eQuick Toggle Hologram On / Off")
        ));

        inv.setItem(48, createButton(
                requireHand ? Material.TRIPWIRE_HOOK : Material.CHEST,
                tr("&e&l✦ Bilet Kullanım Şekli: ", "&e&l✦ Ticket Requirement: ")
                        + (requireHand ? tr("&6Elinde Tutmalı", "&6Must Hold in Hand") : tr("&aEnvanterde Yeterli", "&aAnywhere in Inventory")),
                tr("&7Oyuncunun çarkı çevirirken bileti elinde", "&7Determines whether players must hold the ticket"),
                tr("&7tutmasının zorunlu olup olmadığını belirler.", "&7in their hand or just have it in their inventory."),
                "",
                tr("&e▸ Değiştirmek için Tıkla!", "&e▸ Click to Toggle!")
        ));

        inv.setItem(49, createButton(
                Material.EMERALD,
                tr("&a&l✔ Kaydet ve Tüm Çarkları Yenile", "&a&l✔ Save & Reload All Wheels"),
                tr("&7Tüm ayarları kaydeder ve dünyadaki", "&7Saves all settings and refreshes all"),
                tr("&7aktif 3D Şans Çarklarını yeniler.", "&7active 3D Fortune Wheels in the world."),
                "",
                tr("&e▸ Yenilemek için Tıkla!", "&e▸ Click to Reload!")
        ));

        inv.setItem(50, createButton(
                Material.BOOK,
                tr("&b&l📦 Resource Pack Gönder", "&b&l📦 Send Resource Pack"),
                tr("&7Özel 3D bilet ve çark doku paketini", "&7Regenerates the custom 3D wheel & ticket"),
                tr("&7üretir ve sunucudaki oyunculara gönderir.", "&7resource pack and sends it to online players."),
                "",
                tr("&e▸ Göndermek için Tıkla!", "&e▸ Click to Send!")
        ));

        player.openInventory(inv);
    }

    public void openHologramEditor(Player player) {
        AdminMenuHolder holder = new AdminMenuHolder(AdminMenuHolder.MenuType.HOLOGRAM_EDITOR, null);
        Inventory inv = Bukkit.createInventory(holder, 54, ColorUtil.colorize(
                tr("&8✦ &6&lHOLOGRAM TASARIM & AYARLAR &8✦", "&8✦ &6&lHOLOGRAM DESIGN & SETTINGS &8✦")
        ));
        holder.setInventory(inv);

        ItemStack glass = createButton(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }

        boolean enabled = plugin.getConfig().getBoolean("settings.hologram.enabled", true);
        String title = plugin.getWheelManager().getHologramTitle();
        double heightOffset = plugin.getConfig().getDouble("settings.hologram.height-offset", 2.25);
        double holoScale = plugin.getConfig().getDouble("settings.hologram.scale", 1.0);
        boolean shadowed = plugin.getConfig().getBoolean("settings.hologram.shadowed", true);
        String billboard = plugin.getConfig().getString("settings.hologram.billboard", "CENTER");
        String bg = plugin.getConfig().getString("settings.hologram.background", "TRANSPARENT");

        inv.setItem(4, createButton(
                enabled ? Material.LIME_DYE : Material.GRAY_DYE,
                tr("&6&l✦ Hologram Görünümü: ", "&6&l✦ Hologram Visibility: ") + (enabled ? tr("&a&lAÇIK", "&a&lENABLED") : tr("&c&lKAPALI", "&c&lDISABLED")),
                tr("&7Çarkın üstündeki 3D hologram yazısını", "&7Turns the 3D hologram above the wheel"),
                tr("&7tamamen açar veya kapatır.", "&7completely ON or OFF."),
                "",
                tr("&e▸ Tıkla: ", "&e▸ Click: ") + (enabled ? tr("&cHologramı Kapat", "&cDisable Hologram") : tr("&aHologramı Aç", "&aEnable Hologram"))
        ));

        inv.setItem(10, createButton(
                Material.NAME_TAG,
                tr("&e&l🏷 Çark Başlığı (%title%)", "&e&l🏷 Wheel Title (%title%)"),
                tr("&7Mevcut Başlık:", "&7Current Title:"),
                "  &r" + title,
                "",
                tr("&7Hologram satırlarında &e%title% &7kullandığınızda", "&7Displayed wherever &e%title% &7is used"),
                tr("&7veya ilk satırda bu başlık gösterilir.", "&7in your hologram lines."),
                "",
                tr("&e▸ Sol Tık: &aYeni Başlık Yaz (Sohbetten)", "&e▸ Left-Click: &aSet New Title (via Chat)"),
                tr("&e▸ Sağ Tık: &cVarsayılan Başlığa Sıfırla", "&e▸ Right-Click: &cReset to Default Title")
        ));

        List<String> idleLines = plugin.getConfig().getStringList("settings.hologram.idle-lines");
        List<String> idleLore = new ArrayList<>();
        idleLore.add(tr("&7Çark boşta dururken görünen satırlar:", "&7Lines shown when the wheel is idle:"));
        for (int i = 0; i < idleLines.size(); i++) {
            idleLore.add("  &8" + (i + 1) + ". &r" + idleLines.get(i).replace("%title%", title));
        }
        idleLore.add("");
        idleLore.add(tr("&7Desteklenen değişken: &e%title%", "&7Supported placeholder: &e%title%"));
        idleLore.add(tr("&e▸ Sol Tık: &aYeni Satır Ekle (Sohbetten)", "&e▸ Left-Click: &aAdd New Line (via Chat)"));
        idleLore.add(tr("&e▸ Sağ Tık: &cSon Satırı Sil", "&e▸ Right-Click: &cRemove Last Line"));
        idleLore.add(tr("&e▸ Shift + Sol Tık: &bTüm Satırları Baştan Yaz (&f| &bile ayırarak)", "&e▸ Shift + Left-Click: &bRewrite All Lines (separate with &f|&b)"));
        idleLore.add(tr("&e▸ Shift + Sağ Tık: &eVarsayılana Sıfırla", "&e▸ Shift + Right-Click: &eReset to Default"));
        inv.setItem(11, createButton(
                Material.WRITABLE_BOOK,
                tr("&a&l📝 Bekleme Tasarımı (Idle Satırları)", "&a&l📝 Idle Hologram Lines"),
                idleLore
        ));

        List<String> spinLines = plugin.getConfig().getStringList("settings.hologram.spinning-lines");
        List<String> spinLore = new ArrayList<>();
        spinLore.add(tr("&7Çark dönerken görünen satırlar:", "&7Lines shown while the wheel is spinning:"));
        for (int i = 0; i < spinLines.size(); i++) {
            spinLore.add("  &8" + (i + 1) + ". &r" + spinLines.get(i).replace("%title%", title));
        }
        spinLore.add("");
        spinLore.add(tr("&7Değişkenler: &e%title%&7, &b%player%&7, &d%slice%", "&7Placeholders: &e%title%&7, &b%player%&7, &d%slice%"));
        spinLore.add(tr("&e▸ Sol Tık: &aYeni Satır Ekle (Sohbetten)", "&e▸ Left-Click: &aAdd New Line (via Chat)"));
        spinLore.add(tr("&e▸ Sağ Tık: &cSon Satırı Sil", "&e▸ Right-Click: &cRemove Last Line"));
        spinLore.add(tr("&e▸ Shift + Sol Tık: &bTüm Satırları Baştan Yaz (&f| &bile ayırarak)", "&e▸ Shift + Left-Click: &bRewrite All Lines (separate with &f|&b)"));
        spinLore.add(tr("&e▸ Shift + Sağ Tık: &eVarsayılana Sıfırla", "&e▸ Shift + Right-Click: &eReset to Default"));
        inv.setItem(12, createButton(
                Material.ENCHANTED_BOOK,
                tr("&d&l🌀 Dönüş Tasarımı (Spin Satırları)", "&d&l🌀 Spinning Hologram Lines"),
                spinLore
        ));

        inv.setItem(13, createButton(
                Material.FEATHER,
                tr("&b&l⇕ Hologram Yüksekliği: &e", "&b&l⇕ Hologram Height: &e") + String.format(Locale.US, "%.2f", heightOffset),
                tr("&7Hologramın çarkın ne kadar üstünde", "&7Adjusts how high above the wheel"),
                tr("&7duracağını ayarlar.", "&7the hologram floats."),
                "",
                tr("&e▸ Sol Tık: &a+0.10 Yükselt", "&e▸ Left-Click: &a+0.10 Higher"),
                tr("&e▸ Sağ Tık: &c-0.10 Alçalt", "&e▸ Right-Click: &c-0.10 Lower"),
                tr("&e▸ Shift + Sağ Tık: &e2.25 (Varsayılan)", "&e▸ Shift + Right-Click: &e2.25 (Default)")
        ));

        inv.setItem(14, createButton(
                Material.SPYGLASS,
                tr("&6&l🔍 Hologram Yazı Boyutu: &e", "&6&l🔍 Hologram Text Scale: &e") + String.format(Locale.US, "%.1f", holoScale) + "x",
                tr("&7Hologram yazılarının büyüklüğünü", "&7Increases or decreases the text size"),
                tr("&7artırır veya küçültür.", "&7of the hologram."),
                "",
                tr("&e▸ Sol Tık: &a+0.1 Büyüt", "&e▸ Left-Click: &a+0.1 Larger"),
                tr("&e▸ Sağ Tık: &c-0.1 Küçült", "&e▸ Right-Click: &c-0.1 Smaller"),
                tr("&e▸ Shift + Sağ Tık: &e1.0x (Varsayılan)", "&e▸ Shift + Right-Click: &e1.0x (Default)")
        ));

        inv.setItem(15, createButton(
                Material.PAINTING,
                tr("&d&l🎨 Arka Plan Stili: &e", "&d&l🎨 Background Style: &e") + bg,
                tr("&7Hologramın arkasındaki kutu rengini", "&7Changes the background box color or"),
                tr("&7veya şeffaflığını değiştirir.", "&7transparency behind the hologram."),
                "&8(TRANSPARENT, DARK, CRIMSON, GOLD, PURPLE, AQUA)",
                "",
                tr("&e▸ Değiştirmek için Tıkla!", "&e▸ Click to Cycle!")
        ));

        inv.setItem(16, createButton(
                Material.ENDER_EYE,
                tr("&b&l👁 Bakış Yönü & Gölge: &e", "&b&l👁 Billboard & Shadow: &e") + billboard + " &8| " + (shadowed ? tr("&aGölgeli", "&aShadowed") : tr("&7Gölgesiz", "&7No Shadow")),
                tr("&7CENTER: Her açıdan oyuncuya döner", "&7CENTER: Always faces the player"),
                tr("&7VERTICAL: Sadece sağa/sola döner", "&7VERTICAL: Rotates horizontally only"),
                tr("&7FIXED: Sabit çarkın baktığı yöne bakar", "&7FIXED: Faces the wheel's front direction"),
                "",
                tr("&e▸ Sol Tık: &aBakış Yönünü Değiştir", "&e▸ Left-Click: &aCycle Billboard Mode"),
                tr("&e▸ Sağ Tık: &eYazı Gölgesini Aç / Kapat", "&e▸ Right-Click: &eToggle Text Shadow")
        ));

        inv.setItem(22, createButton(
                Material.NETHER_STAR,
                tr("&6&l✦ Hazır Hologram Tasarım Şablonları ✦", "&6&l✦ One-Click Hologram Preset Themes ✦"),
                tr("&7Aşağıdaki hazır tasarımlardan birine tıklayarak", "&7Click any preset below to instantly apply"),
                tr("&7hologramın tüm görünümünü tek tıkla değiştirebilirsiniz!", "&7a complete hologram theme!")
        ));

        inv.setItem(28, createButton(
                Material.GOLD_BLOCK,
                tr("&6&l1. Klasik Altın Tasarım &7(Varsayılan)", "&6&l1. Classic Gold Theme &7(Default)"),
                tr("&7Önizleme:", "&7Preview:"),
                "  &6&l✦ &e&l" + ColorUtil.stripColor(title) + " &6&l✦",
                tr("  &7Elindeki &e&lÖzel Şans Çarkı Bileti &7ile &aSağ Tıkla!", "  &7Right-Click with your &e&lFortune Wheel Ticket &7in hand!"),
                "",
                tr("&e▸ Uygulamak için Tıkla!", "&e▸ Click to Apply!")
        ));

        inv.setItem(29, createButton(
                Material.CRIMSON_NYLIUM,
                tr("&c&l2. Kitsune Tapınağı Tasarımı", "&c&l2. Kitsune Shrine Theme"),
                tr("&7Önizleme:", "&7Preview:"),
                "  &8&m━━━&4&m━━━&6&l ⛩ &4&m━━━&8&m━━━",
                "  &4&l❖ &c&lKITSUNE &6&l" + tr("ŞANS ÇARKI", "FORTUNE WHEEL") + " &4&l❖",
                tr("  &7Biletinle &e&lSağ Tıkla &7ve Kadere Meydan Oku!", "  &e&lRight-Click &7with a Ticket & Challenge Fate!"),
                "",
                tr("&e▸ Uygulamak için Tıkla!", "&e▸ Click to Apply!")
        ));

        inv.setItem(30, createButton(
                Material.AMETHYST_BLOCK,
                tr("&d&l3. Siber Neon Tasarım", "&d&l3. Cyber Neon Theme"),
                tr("&7Önizleme:", "&7Preview:"),
                "  &b&l⚡ &3&l" + tr("SİBER &d&lŞANS ÇARKI", "CYBER &d&lFORTUNE WHEEL") + " &b&l⚡",
                tr("  &d▸ &fBilet ile &b&lSAĞ TIKLA &fve Çevir! &d◂", "  &d▸ &b&lRIGHT-CLICK &fwith a Ticket to Spin! &d◂"),
                "",
                tr("&e▸ Uygulamak için Tıkla!", "&e▸ Click to Apply!")
        ));

        inv.setItem(32, createButton(
                Material.DIAMOND_BLOCK,
                tr("&6&l4. Kraliyet VIP Tasarımı", "&6&l4. Royal VIP Jackpot Theme"),
                tr("&7Önizleme:", "&7Preview:"),
                "  &e&l✦ &6&m──────────────&e&l ✦",
                "  &6&l👑 &e&l" + tr("KRALİYET &f&lJACKPOT ÇARKI", "ROYAL &f&lJACKPOT WHEEL") + " &6&l👑",
                tr("  &fÖzel Bilet ile &a&lSağ Tıkla &fve Kazan!", "  &a&lRight-Click &fwith a VIP Ticket to Win!"),
                "",
                tr("&e▸ Uygulamak için Tıkla!", "&e▸ Click to Apply!")
        ));

        inv.setItem(33, createButton(
                Material.QUARTZ_BLOCK,
                tr("&f&l5. Minimalist Sade Tasarım", "&f&l5. Minimalist Clean Theme"),
                tr("&7Önizleme:", "&7Preview:"),
                "  &e&l" + tr("Tkitsune Şans Çarkı", "Tkitsune Wheel of Fortune"),
                tr("  &7Bilet ile &aSağ Tıkla", "  &aRight-Click &7with Ticket"),
                "",
                tr("&e▸ Uygulamak için Tıkla!", "&e▸ Click to Apply!")
        ));

        inv.setItem(34, createButton(
                Material.BLAZE_POWDER,
                tr("&4&l6. Ateş & Alev Tasarımı", "&4&l6. Hellfire & Flame Theme"),
                tr("&7Önizleme:", "&7Preview:"),
                "  &4&l🔥 &c&l" + tr("CEHENNEM &6&lATEŞİ ÇARKI", "HELLFIRE &6&lFORTUNE WHEEL") + " &4&l🔥",
                tr("  &c▸ &7Ateş Biletinle &6&lSağ Tıkla! &c◂", "  &c▸ &6&lRight-Click &7with your Ticket! &c◂"),
                "",
                tr("&e▸ Uygulamak için Tıkla!", "&e▸ Click to Apply!")
        ));

        inv.setItem(49, createButton(
                Material.ARROW,
                tr("&e&l« Ana Menüye Dön", "&e&l« Back to Main Menu"),
                tr("&7Çark yönetim menüsüne geri döner.", "&7Return to the main wheel admin menu.")
        ));

        player.openInventory(inv);
    }

    private ItemStack createMainSliceItem(WheelSlice slice) {
        ItemStack icon = plugin.getSliceManager().createSliceIconItem(slice);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            double pct = plugin.getSliceManager().getChancePercentage(slice);
            meta.setDisplayName(ColorUtil.colorize(slice.getDisplayName()));

            List<String> lore = new ArrayList<>();
            lore.add(tr("&8Dilim #", "&8Slice #") + (slice.getIndex() + 1) + " (" + slice.getId() + ")");
            lore.add("");
            lore.add(tr("&7Şans Ağırlığı: &e", "&7Chance Weight: &e") + String.format(Locale.US, "%.1f", slice.getChance())
                    + " &8(&a%" + String.format(Locale.US, "%.1f", pct) + tr(" İhtimal&8)", " Probability&8)"));
            lore.add(tr("&7Çark Üstü Yazı: &f", "&7Wheel Label: &f") + slice.getSliceLabel().replace("\n", " "));
            lore.add(tr("&7Duyuru (Broadcast): ", "&7Broadcast: ") + (slice.isBroadcast() ? tr("&aAçık", "&aEnabled") : tr("&cKapalı", "&cDisabled")));
            lore.add(tr("&7Eşya Ödülü Sayısı: &b", "&7Item Rewards: &b") + slice.getRewardItems().size() + tr(" Eşya", " Items"));
            lore.add(tr("&7Komut Ödülü Sayısı: &d", "&7Command Rewards: &d") + slice.getCommands().size() + tr(" Komut", " Commands"));
            if (!slice.getCommands().isEmpty()) {
                for (String cmd : slice.getCommands()) {
                    lore.add("  &8• &7/" + cmd);
                }
            }
            lore.add("");
            lore.add(tr("&e▸ Sol Tık: &aÖdülleri & Detayları Düzenle", "&e▸ Left-Click: &aEdit Prizes & Details"));
            lore.add(tr("&e▸ Sağ Tık: &a+1.0 Şans Artır", "&e▸ Right-Click: &a+1.0 Chance Weight"));
            lore.add(tr("&e▸ Shift + Sağ Tık: &c-1.0 Şans Azalt", "&e▸ Shift + Right-Click: &c-1.0 Chance Weight"));

            meta.setLore(ColorUtil.colorize(lore));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    public void openSliceEditor(Player player, WheelSlice slice) {
        if (slice == null) {
            openMainMenu(player);
            return;
        }
        AdminMenuHolder holder = new AdminMenuHolder(AdminMenuHolder.MenuType.SLICE_EDITOR, slice.getId());
        Inventory inv = Bukkit.createInventory(
                holder,
                54,
                ColorUtil.colorize(tr("&8✦ &6&lDİLİM DÜZENLE: &e", "&8✦ &6&lEDIT SLICE: &e") + slice.getId().toUpperCase(Locale.ROOT) + " &8✦")
        );
        holder.setInventory(inv);

        ItemStack borderGlass = createButton(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, borderGlass);
        }

        // Highlight row above & below the 9 drag-and-drop prize slots (slots 27..35)
        ItemStack goldPane = createButton(
                Material.YELLOW_STAINED_GLASS_PANE,
                tr("&6&l⬇ Fiziksel Eşya Ödülü Slotları (Sürükle & Bırak) ⬇", "&6&l⬇ Physical Prize Item Slots (Drag & Drop) ⬇"),
                tr("&7Envanterinizden istediğiniz eşyaları", "&7Drag and drop any items from your inventory"),
                tr("&7ortadaki 9 boş slota koyabilirsiniz!", "&7into the 9 empty slots below!")
        );
        for (int s = 18; s <= 26; s++) {
            inv.setItem(s, goldPane);
        }
        for (int s = 36; s <= 44; s++) {
            inv.setItem(s, createButton(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Clear the 9 drag-and-drop prize slots (27..35) and populate existing rewardItems
        for (int s = PRIZE_SLOT_START; s <= PRIZE_SLOT_END; s++) {
            inv.setItem(s, null);
        }
        List<ItemStack> rewardItems = slice.getRewardItems();
        for (int i = 0; i < rewardItems.size() && (PRIZE_SLOT_START + i) <= PRIZE_SLOT_END; i++) {
            ItemStack rItem = rewardItems.get(i);
            if (rItem != null && !rItem.getType().isAir()) {
                inv.setItem(PRIZE_SLOT_START + i, rItem.clone());
            }
        }

        // Row 1: Chance controls (Slots 10..16)
        double pct = plugin.getSliceManager().getChancePercentage(slice);
        inv.setItem(10, createButton(Material.RED_STAINED_GLASS_PANE, tr("&c&l-10.0 Şans Oranı", "&c&l-10.0 Chance"), tr("&7Şans ağırlığını 10 azaltır.", "&7Decreases chance weight by 10.")));
        inv.setItem(11, createButton(Material.ORANGE_STAINED_GLASS_PANE, tr("&6&l-5.0 Şans Oranı", "&6&l-5.0 Chance"), tr("&7Şans ağırlığını 5 azaltır.", "&7Decreases chance weight by 5.")));
        inv.setItem(12, createButton(
                Material.PINK_STAINED_GLASS_PANE,
                tr("&c&l-1.0 Şans Oranı", "&c&l-1.0 Chance"),
                tr("&e▸ Sol Tık: &c-1.0 Azalt", "&e▸ Left-Click: &c-1.0"),
                tr("&e▸ Sağ Tık: &c-0.5 Azalt", "&e▸ Right-Click: &c-0.5")
        ));

        inv.setItem(13, createButton(
                Material.GOLD_INGOT,
                tr("&6&l✦ Şans Oranı: &e", "&6&l✦ Chance Weight: &e") + String.format(Locale.US, "%.1f", slice.getChance())
                        + " &8(&a%" + String.format(Locale.US, "%.1f", pct) + "&8)",
                tr("&7Bu dilimin çarkta çıkma ağırlığı ve", "&7The weight of this slice and its"),
                tr("&7diğer dilimlere göre gerçek yüzde ihtimali.", "&7actual win percentage."),
                "",
                tr("&e▸ Sohbetten özel sayı girmek için Tıkla!", "&e▸ Click to enter an exact number in chat!")
        ));

        inv.setItem(14, createButton(
                Material.LIME_STAINED_GLASS_PANE,
                tr("&a&l+1.0 Şans Oranı", "&a&l+1.0 Chance"),
                tr("&e▸ Sol Tık: &a+1.0 Artır", "&e▸ Left-Click: &a+1.0"),
                tr("&e▸ Sağ Tık: &a+0.5 Artır", "&e▸ Right-Click: &a+0.5")
        ));
        inv.setItem(15, createButton(Material.GREEN_STAINED_GLASS_PANE, tr("&2&l+5.0 Şans Oranı", "&2&l+5.0 Chance"), tr("&7Şans ağırlığını 5 artırır.", "&7Increases chance weight by 5.")));
        inv.setItem(16, createButton(Material.EMERALD_BLOCK, tr("&a&l+10.0 Şans Oranı", "&a&l+10.0 Chance"), tr("&7Şans ağırlığını 10 artırır.", "&7Increases chance weight by 10.")));

        // Row 0: Slice Icon, Label, Name, Commands, Broadcast
        ItemStack currentIcon = plugin.getSliceManager().createSliceIconItem(slice);
        ItemMeta iconMeta = currentIcon.getItemMeta();
        if (iconMeta != null) {
            iconMeta.setDisplayName(ColorUtil.colorize(tr("&b&l✦ Çark Üstü 3D İkon: &e", "&b&l✦ On-Wheel 3D Icon: &e") + slice.getIconMaterial()));
            iconMeta.setLore(ColorUtil.colorize(Arrays.asList(
                    tr("&7Çarkın bu dilimi üzerinde dönen 3D eşya ikonu.", "&7The 3D item icon rotating on this slice."),
                    "",
                    tr("&e▸ İkonu değiştirmek için envanterinizden", "&e▸ Pick up an item from your inventory with your"),
                    tr("&e  bir eşyayı imlecinize alıp buraya Tıklayın!", "&e  cursor and click here to change the icon!"),
                    tr("&e▸ Sağ Tık: &dÖzel Çark Bileti İkonu Yap", "&e▸ Right-Click: &dUse Custom Wheel Ticket Icon")
            )));
            currentIcon.setItemMeta(iconMeta);
        }
        inv.setItem(2, currentIcon);

        inv.setItem(3, createButton(
                Material.NAME_TAG,
                tr("&e&l✎ Çark Üstü Yazıyı Değiştir", "&e&l✎ Change On-Wheel Label"),
                tr("&7Şu anki yazı: &f", "&7Current label: &f") + slice.getSliceLabel().replace("\n", " "),
                "",
                tr("&e▸ Sohbetten yeni yazı girmek için Tıkla!", "&e▸ Click to type a new label in chat!")
        ));

        inv.setItem(4, createButton(
                Material.OAK_SIGN,
                tr("&6&l✎ Dilim Başlığını Değiştir", "&6&l✎ Change Slice Title"),
                tr("&7Şu anki başlık: &f", "&7Current title: &f") + slice.getDisplayName(),
                "",
                tr("&e▸ Sohbetten yeni başlık girmek için Tıkla!", "&e▸ Click to type a new title in chat!")
        ));

        List<String> cmdLore = new ArrayList<>();
        cmdLore.add(tr("&7Bu dilim çıktığında konsoldan çalışacak komutlar:", "&7Console commands executed when this slice wins:"));
        if (slice.getCommands().isEmpty()) {
            cmdLore.add(tr("  &8(Kayıtlı komut yok - Sadece aşağıdaki eşyalar verilir)", "  &8(No commands - Only the items below are given)"));
        } else {
            for (String cmd : slice.getCommands()) {
                cmdLore.add("  &8• &e/" + cmd);
            }
        }
        cmdLore.add("");
        cmdLore.add(tr("&e▸ Sol Tık: &aYeni Komut Ekle (%player% destekli)", "&e▸ Left-Click: &aAdd Command (supports %player%)"));
        cmdLore.add(tr("&e▸ Sağ Tık: &cSon Komutu Sil", "&e▸ Right-Click: &cRemove Last Command"));
        cmdLore.add(tr("&e▸ Shift + Sağ Tık: &4Tüm Komutları Temizle", "&e▸ Shift + Right-Click: &4Clear All Commands"));
        inv.setItem(5, createButton(Material.COMMAND_BLOCK, tr("&d&l⚡ Komut Ödülleri (", "&d&l⚡ Command Rewards (") + slice.getCommands().size() + ")", cmdLore));

        inv.setItem(6, createButton(
                slice.isBroadcast() ? Material.BELL : Material.GRAY_DYE,
                tr("&6&l📢 Sunucu Duyurusu: ", "&6&l📢 Server Broadcast: ") + (slice.isBroadcast() ? tr("&aAçık", "&aEnabled") : tr("&cKapalı", "&cDisabled")),
                tr("&7Bu dilim kazanıldığında tüm sunucuya", "&7Whether to broadcast a message to the"),
                tr("&7sohbetten duyuru geçilip geçilmeyeceği.", "&7entire server when this slice is won."),
                "",
                tr("&e▸ Değiştirmek için Tıkla!", "&e▸ Click to Toggle!")
        ));

        // Bottom buttons: Back / Save
        inv.setItem(48, createButton(
                Material.ARROW,
                tr("&e&l« Kaydet ve Ana Menüye Dön", "&e&l« Save & Return to Main Menu"),
                tr("&7Yaptığınız değişiklikleri ve koyduğunuz", "&7Saves your changes and prize items"),
                tr("&7ödül eşyalarını kaydedip ana menüye döner.", "&7and returns to the main menu.")
        ));

        inv.setItem(49, createButton(
                Material.EMERALD_BLOCK,
                tr("&a&l✔ Ödülleri Kaydet ve Çarkı Güncelle", "&a&l✔ Save Prizes & Update Wheel"),
                tr("&7Ortadaki 9 slota koyduğunuz eşyaları", "&7Saves the items placed in the 9 slots"),
                tr("&7bu dilimin ödülü olarak kaydeder ve", "&7as this slice's rewards and updates"),
                tr("&73D çarkı anında günceller!", "&7the 3D wheel immediately!")
        ));

        inv.setItem(50, createButton(
                Material.REDSTONE_BLOCK,
                tr("&c&l✖ Komut Ödüllerini Temizle", "&c&l✖ Clear Command Rewards"),
                tr("&7Bu dilimdeki tüm komutları temizler,", "&7Clears all console commands on this slice"),
                tr("&7sadece 9 slota koyduğunuz eşyaların", "&7so only the items placed in the 9 slots"),
                tr("&7verilmesini sağlar.", "&7are awarded.")
        ));

        player.openInventory(inv);
    }

    public ItemStack createButton(Material material, String name, String... loreLines) {
        return createButton(material, name, Arrays.asList(loreLines));
    }

    public ItemStack createButton(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(name));
            if (loreLines != null && !loreLines.isEmpty()) {
                meta.setLore(ColorUtil.colorize(loreLines));
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void setPendingChatEdit(UUID playerUuid, String sliceId, ChatEditType type) {
        pendingChatEdits.put(playerUuid, new PendingChatEdit(sliceId, type));
    }

    public PendingChatEdit consumePendingChatEdit(UUID playerUuid) {
        return pendingChatEdits.remove(playerUuid);
    }

    public boolean hasPendingChatEdit(UUID playerUuid) {
        return pendingChatEdits.containsKey(playerUuid);
    }
}
