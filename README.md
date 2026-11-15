# 🎡 TkitsuneFortune — 3D Wheel of Fortune (Spigot & Paper 1.21.11)

<p align="center">
  <img src="assets/banner.jpg" alt="TkitsuneFortune Banner" width="100%" />
</p>

<p align="center">
  <a href="#-english-documentation"><b>🇬🇧 English Documentation</b></a> •
  <a href="#-türkçe-dokümantasyon"><b>🇹🇷 Türkçe Dokümantasyon</b></a>
</p>

<p align="center">
  <b>EN:</b> A lightweight, fast, and fully customizable <b>3D Wheel of Fortune</b> plugin for <b>Spigot & Paper 1.21.11</b> featuring smooth 3D quaternion spin physics, an in-game Drag & Drop Prize Editor, Hologram Designer, Sound Editor, Ticket Customizer, built-in Resource Pack HTTP Host, and instant <b>English / Turkish (EN/TR)</b> language switching.<br><br>
  <b>TR:</b> <b>Spigot & Paper 1.21.11</b> için geliştirilmiş, pürüzsüz 3D dönüş fiziğine, oyun içi Sürükle-Bırak Ödül & Şans Oranı Menüsüne, Hologram Tasarımcısına, Ses Düzenleyicisine, Bilet Özelleştiricisine, dahili Otomatik Resource Pack Sunucusuna ve anlık <b>Türkçe / İngilizce (TR/EN)</b> dil desteğine sahip 3D Şans Çarkı eklentisi.
</p>

---

# 🇬🇧 English Documentation

## ✨ Features

- **Smooth 3D Spin Physics (`Display` Entities):**
  - 8-slice 3D wheel and ornate stand powered by Minecraft `1.21.11` `ItemDisplay`, `BlockDisplay`, `TextDisplay`, and `Interaction` entities with cubic ease-out deceleration, sound effects, and particles.
- **Dual 3D Render Modes (`RESOURCE_PACK` & `BLOCK_DISPLAY`):**
  - **`RESOURCE_PACK` Mode (Default):** Uses custom Blockbench 3D models and a built-in HTTP server that automatically hosts and sends the resource pack to players when they join.
  - **`BLOCK_DISPLAY` Mode:** Builds the 111-element 3D model purely out of vanilla `BlockDisplay` blocks—no resource pack required.
- **Rotating 3D Slice Prizes & Dynamic Broadcasts:**
  - Each of the 8 slices displays its 3D reward icon and prize label right on the wheel face, rotating in real time.
  - Changing a slice's prize items, label, or title in the GUI automatically updates all win messages, titles/subtitles, and server-wide broadcasts (`%slice%` & `%reward%`).
- **In-Game Drag & Drop Prize & Chance GUI (`/tkf`):**
  - Edit slice win weights/probabilities, drag & drop up to 9 physical reward items per slice (supports custom NBT, enchants, and items), or configure console command rewards in-game.
- **Custom 3D Ticket Name, Lore & Glow Editor (`/tkf ticketname` or `/tkf` ➔ Slot 50):**
  - Customize the ticket's display name, multi-line lore, and enchantment glow directly from the GUI or via command; existing tickets in online players' inventories update automatically.
- **Full 3D Hologram Designer & Toggle (`/tkf hologram` or `/tkf` ➔ Slot 47):**
  - Toggle the hologram ON/OFF, edit the title (`%title%`), idle lines, spinning lines, height offset, text scale, background style, billboard mode, or apply **6 one-click preset themes**.
- **Wheel Sound Customizer (`/tkf sound` or `/tkf` ➔ Slot 48):**
  - Toggle all wheel sounds ON/OFF, adjust master volume, and customize or preview the **Spin Start**, **Spin Tick/Click**, **Default Win**, **Error**, and **8 individual Per-Slice Win Sounds**.
- **Built-In Bilingual Support (`EN` & `TR`):**
  - Switch the entire plugin (menus, messages, ticket, hologram, and slice labels) between **English (`EN`)** and **Turkish (`TR`)** with a single click in `/tkf` (Slot 46) or via `/tkf lang <en|tr>`.

---

## 📦 Installation

1. Download [`releases/TkitsuneFortune-1.0.0.jar`](releases/TkitsuneFortune-1.0.0.jar) (or build from source) and place it into your server's `plugins/` directory.
2. Start your **Spigot** or **Paper 1.21.11** server (**Java 21+**).
3. Run `/tkf lang en` in-game to switch all menus and wheel texts to English.
4. Run `/tkf` to open the Admin GUI and click **Place Wheel Here** (Slot 37)!

---

## ⌨ Commands & Permissions

**Main Command:** `/tkf` *(Aliases: `/fw`, `/fortunewheel`, `/tkitsunefortune`, `/cark`, `/sanscarki`)*

| Command | Description |
| :--- | :--- |
| `/tkf` | Opens the interactive Admin Settings, Slice Prize, Sound & Hologram GUI. |
| `/tkf place [id]` | Places a 3D Wheel of Fortune on the block you are looking at. |
| `/tkf remove [id\|nearest]` | Removes a Wheel of Fortune by ID or the nearest one within 8 blocks. |
| `/tkf list` | Lists all active Fortune Wheels and their coordinates. |
| `/tkf ticket <player> [amount]` | Gives custom 3D **Fortune Wheel Tickets** to a player. |
| `/tkf ticketname <new name>` | Changes the custom Ticket item's display name (`&` and `&#RRGGBB` supported). |
| `/tkf hologram [on\|off\|menu]` | Opens the Hologram Design GUI or toggles the hologram ON/OFF. |
| `/tkf name <new title>` | Updates the wheel hologram title (`&` and `&#RRGGBB` supported). |
| `/tkf sound [on\|off\|menu]` | Opens the Wheel Sound Settings GUI or mutes/unmutes wheel sounds. |
| `/tkf lang <en\|tr>` | Switches the plugin language between English (`EN`) and Turkish (`TR`). |
| `/tkf mode <RESOURCE_PACK\|BLOCK_DISPLAY>` | Switches the 3D rendering mode on the fly. |
| `/tkf scale <0.5 - 4.0>` | Adjusts the 3D wheel size multiplier (Default: `1.7`). |
| `/tkf resourcepack` | Regenerates and sends the Resource Pack `.zip` to online players. |
| `/tkf reload` | Reloads `config.yml`, `wheels.yml`, and respawns all active wheels. |

### Permissions
- `tkitsunefortune.use` *(Default: `true`)* — Allows players to spin the Wheel of Fortune using a ticket.
- `tkitsunefortune.admin` *(Default: `op`)* — Grants access to `/tkf` admin menus and all management commands.

---

## 🛠 Building from Source

This project uses **Java 21** and includes the **Gradle Wrapper**:

```bash
# Linux / macOS
chmod +x gradlew
./gradlew clean build

# Windows (PowerShell / CMD)
.\gradlew.bat clean build
```

The compiled JAR file will be generated at:
```text
build/libs/TkitsuneFortune-1.0.0.jar
```

---

# 🇹🇷 Türkçe Dokümantasyon

## ✨ Özellikler

- **Gerçek 3D Dönüş Fizikleri (`Display` Varlıkları):**
  - 8 renkli dilime sahip 3D çark gövdesi ve sabit standı, Minecraft `1.21.11` `ItemDisplay`, `BlockDisplay`, `TextDisplay` ve `Interaction` varlıkları ile pürüzsüz hızlanma ve yavaşlama (ease-out) fiziğiyle döner.
- **Çift 3D Görünüm Modu (`RESOURCE_PACK` & `BLOCK_DISPLAY`):**
  - **`RESOURCE_PACK` Modu (Varsayılan):** Özel Blockbench 3D çark modelini, standını ve 3D altın biletini yerleşik HTTP sunucusu üzerinden oyunculara otomatik gönderir.
  - **`BLOCK_DISPLAY` Modu:** Hiçbir doku paketi gerektirmeden 111 parçalı 3D modeli doğrudan `BlockDisplay` bloklarıyla oluşturur.
- **Çark Üstü 3D Ödül İkonları & Dinamik Duyuru Sistemi:**
  - Her dilimin dış kenarında o dilimin ödül ikonu (`ItemDisplay`) ve ödül adı (`TextDisplay`) çarkla birlikte gerçek zamanlı olarak döner.
  - Bir dilimin ödülünü, çark üstü yazısını veya başlığını değiştirdiğinizde ekrana gelen **Title/Subtitle**, **sohbet ödül mesajı** ve **sunucu duyuruları (Broadcast)** otomatik olarak yeni ödül ismiyle (`%slice%` ve `%reward%`) güncellenir.
- **Sürükle & Bırak (Drag & Drop) Ödül ve Şans Oranı Menüsü (`/tkf`):**
  - Oyun içinden her dilimin **şans ağırlığını (yüzdesini)** ayarlayabilir, envanterinizden **9 ödül slotuna** eşya sürükleyip bırakabilir veya konsol komut ödülleri ekleyebilirsiniz.
- **Bilet İsmi, Açıklaması (Lore) ve Parlama Düzenleyici (`/tkf biletisim` veya `/tkf` ➔ 50. Slot):**
  - Özel çark biletinin ismini, açıklama satırlarını ve büyü parlama (Glow) efektini doğrudan menüden veya komutla değiştirebilirsiniz.
- **Hologram Tasarım & Aç/Kapat Menüsü (`/tkf hologram` veya `/tkf` ➔ 47. Slot):**
  - Çarkın üstündeki 3D hologramı tamamen açıp kapatabilir, başlığını (`%title%`), bekleme (`idle-lines`) ve dönüş (`spinning-lines`) satırlarını, yüksekliğini, yazı boyutunu, arka plan rengini ve **6 hazır tasarım şablonunu** tek tıkla değiştirebilirsiniz.
- **Çark Ses Ayarları Menüsü (`/tkf ses` veya `/tkf` ➔ 48. Slot):**
  - Tüm çark seslerini açıp kapatabilir, ses seviyesini (`volume`) ayarlayabilir; **Çark Başlangıç**, **Dönüş Tıkırtı (Tick)**, **Genel Kazanma**, **Uyarı/Hata** ve **8 dilimin her birine özel kazanma seslerini** oyun içinden dinleyerek değiştirebilirsiniz.
- **Çift Dil Desteği (`TR` / `EN`):**
  - `/tkf lang <tr|en>` komutuyla veya `/tkf` menüsündeki **Dil / Language (46. Slot)** butonuyla tüm menüleri, mesajları, biletleri, hologramı ve dilim yazılarını tek tıkla Türkçe veya İngilizceye çevirebilirsiniz.

---

## 📦 Kurulum

1. Hazır derlenmiş [`releases/TkitsuneFortune-1.0.0.jar`](releases/TkitsuneFortune-1.0.0.jar) dosyasını sunucunuzun `plugins/` klasörüne atın.
2. Sunucunuzu başlatın (`Spigot` veya `Paper 1.21.11`, **Java 21+**).
3. Oyun içinden `/tkf` yazarak yönetim menüsünü açın ve **Baktığın Yere Çark Kur (37. Slot)** butonuna tıklayın!

---

## ⌨ Komutlar ve Yetkiler

**Ana Komut:** `/tkf` *(Alternatifler: `/cark`, `/sanscarki`, `/fw`, `/fortunewheel`, `/tkitsunefortune`)*

| Komut | Açıklama |
| :--- | :--- |
| `/tkf` | Ödül, Şans Oranı, Ses, Bilet, Hologram ve Çark Yönetim Menüsünü (GUI) açar. |
| `/tkf place [id]` | Baktığınız bloğun üstüne yeni bir 3D Şans Çarkı kurar. |
| `/tkf remove [id\|nearest]` | Belirtilen ID'deki veya en yakındaki (8 blok) Şans Çarkını kaldırır. |
| `/tkf list` | Dünyadaki aktif Şans Çarklarını ve koordinatlarını listeler. |
| `/tkf ticket <oyuncu> [adet]` | Oyuncuya özel 3D tasarımlı **Şans Çarkı Bileti** verir. |
| `/tkf biletisim <yeni isim>` | Bilet eşyasının ismini değiştirir (`&` ve `&#RRGGBB` HEX destekli). |
| `/tkf hologram [ac\|kapat\|menu]` | Hologram Tasarım Menüsünü açar veya hologramı hızlıca açıp kapatır. |
| `/tkf isim <yeni başlık>` | Çarkın üstündeki hologram başlığını değiştirir. |
| `/tkf ses [ac\|kapat\|menu]` | Çark Ses Ayarları Menüsünü açar veya sesleri açıp kapatır. |
| `/tkf lang <tr\|en>` | Eklenti dilini Türkçe (`TR`) veya İngilizce (`EN`) olarak değiştirir. |
| `/tkf mode <RESOURCE_PACK\|BLOCK_DISPLAY>` | 3D görünüm modunu anlık olarak değiştirir. |
| `/tkf scale <0.5 - 4.0>` | 3D Şans Çarkının boyut çarpanını değiştirir (Varsayılan: `1.7`). |
| `/tkf resourcepack` | Resource Pack `.zip` paketini yeniden oluşturur ve aktif oyunculara gönderir. |
| `/tkf reload` | `config.yml` ve `wheels.yml` dosyalarını yeniden yükler, tüm çarkları yeniler. |

### Yetkiler (Permissions)
- `tkitsunefortune.use` *(Varsayılan: `true`)* — Oyuncuların biletle Şans Çarkını çevirmesine izin verir.
- `tkitsunefortune.admin` *(Varsayılan: `op`)* — `/tkf` yönetim menüsüne ve tüm yönetici komutlarına erişim verir.

---

## 🛠 Kaynak Koddan Derleme (Build)

Bilgisayarınızda **JDK 21+** kurulu olması yeterlidir:

```powershell
# Windows (PowerShell / CMD)
.\gradlew.bat clean build
```
```bash
# Linux / macOS
chmod +x gradlew
./gradlew clean build
```

Derlenen `.jar` dosyası `build/libs/TkitsuneFortune-1.0.0.jar` konumunda oluşturulur.

---

## 📂 Proje Yapısı / Project Structure

```text
TkitsuneFortune/
├── assets/                                # Banner, preview renders & Blockbench (.bbmodel) files
│   ├── banner.jpg
│   ├── preview_render.png
│   ├── ticket_preview_render.png
│   ├── tkitsunefortune.bbmodel
│   └── tkitsunefortune_ticket.bbmodel
├── releases/                              # Pre-built plugin JAR & ResourcePack ZIP
│   ├── TkitsuneFortune-1.0.0.jar
│   └── TKitsuneFortune-ResourcePack.zip
├── src/main/
│   ├── java/com/tkitsunefortune/
│   │   ├── TKitsuneFortunePlugin.java
│   │   ├── command/FortuneWheelCommand.java
│   │   ├── gui/                           # Main, Slice, Hologram & Sound GUI Managers/Listeners
│   │   ├── listener/WheelInteractionListener.java
│   │   ├── model/                         # Blockbench JSON -> 3D BlockDisplay parser
│   │   ├── resourcepack/                  # Auto ResourcePack generator & embedded HTTP server
│   │   ├── ticket/TicketManager.java
│   │   ├── util/                          # ColorUtil & LanguageManager (TR/EN)
│   │   └── wheel/                         # FortuneWheel, SpinTask, SliceManager, WheelManager
│   └── resources/
│       ├── config.yml
│       ├── plugin.yml
│       ├── models/                        # 3D JSON models (stand, wheel, ticket)
│       └── textures/                      # 64x64 textures (tkitsunefortune.png, ticket.png)
├── build.gradle
└── settings.gradle
```

---

## 📄 License / Lisans

Distributed under the [MIT License](LICENSE).
