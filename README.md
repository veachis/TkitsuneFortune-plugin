# 🎡 TkitsuneFortune — 3D Wheel of Fortune (Spigot & Paper 1.21.11)

<p align="center">
  <img src="assets/banner.jpg" alt="TkitsuneFortune Banner" width="100%" />
</p>

<p align="center">
  <b>Minecraft 1.21.11 (Spigot & Paper)</b> için geliştirilmiş, <b>BlockDisplay & ItemDisplay</b> tabanlı, oyun içi <b>Sürükle-Bırak Ödül & Hologram Tasarım Menüsü (GUI)</b>, dahili <b>Otomatik Resource Pack Sunucusu</b> ve <b>Türkçe / İngilizce (TR/EN)</b> dil desteğine sahip 3D Şans Çarkı eklentisi.
</p>

---

## 🌍 İçindekiler / Table of Contents
- [🇹🇷 Türkçe Dokümantasyon](#-türkçe-dokümantasyon)
  - [Özellikler](#-özellikler)
  - [Kurulum](#-kurulum)
  - [Komutlar ve Yetkiler](#-komutlar-ve-yetkiler)
  - [Oyun İçi Yönetim ve Tasarım Menüsü (GUI)](#-oyun-içi-yönetim-ve-tasarım-menüsü-gui)
  - [Kaynak Koddan Derleme (Build)](#-kaynak-koddan-derleme-build)
  - [Proje Yapısı ve Geliştirici Rehberi](#-proje-yapısı-ve-geliştirici-rehberi)
- [🇬🇧 English Documentation](#-english-documentation)
  - [Features](#-features)
  - [Installation](#-installation)
  - [Commands & Permissions](#-commands--permissions)
  - [In-Game Admin & Design GUI](#-in-game-admin--design-gui)
  - [Building from Source](#-building-from-source)

---

# 🇹🇷 Türkçe Dokümantasyon

## ✨ Özellikler

- **Gerçek 3D Dönüş Fizikleri (`60 FPS` Quaternion İnterpolasyonu):**
  - 8 renkli dilime sahip 3D çark gövdesi ve sabit standı, Minecraft `1.21.11` `Display` varlıkları ile pürüzsüz hızlanma ve yavaşlama (ease-out) fiziğiyle döner.
- **Çift 3D Görünüm Modu (`RESOURCE_PACK` & `BLOCK_DISPLAY`):**
  - **`RESOURCE_PACK` Modu (Varsayılan):** Özel Blockbench 3D çark modelini, standını ve 3D altın biletini yerleşik HTTP sunucusu üzerinden oyunculara otomatik gönderir.
  - **`BLOCK_DISPLAY` Modu:** Hiçbir doku paketi gerektirmeden 111 parçalı 3D modeli doğrudan `BlockDisplay` bloklarıyla oluşturur.
- **Çark Üstü 3D Ödül İkonları & Yazıları:**
  - Her dilimin dış kenarında o dilimin ödül ikonu (`ItemDisplay`) ve ödül adı (`TextDisplay`) çarkla birlikte gerçek zamanlı olarak döner.
- **Sürükle & Bırak (Drag & Drop) Ödül ve Şans Oranı Menüsü (`/tkf`):**
  - Dosya düzenlemeye gerek kalmadan oyun içinden her dilimin **şans ağırlığını (yüzdesini)** ayarlayabilir, envanterinizden **9 ödül slotuna** eşya sürükleyip bırakabilir (büyülü/özel NBT'li eşyalar dahil) veya konsol komut ödülleri ekleyebilirsiniz.
- **Tam Kapsamlı Hologram Tasarım & Aç/Kapat Menüsü (`/tkf hologram`):**
  - Çarkın üstündeki 3D hologramı tamamen açıp kapatabilir, başlığını (`%title%`), bekleme (`idle-lines`) ve dönüş (`spinning-lines`) satırlarını, yüksekliğini, yazı boyutunu, arka plan rengini/şeffaflığını ve **6 hazır tasarım şablonunu** tek tıkla değiştirebilirsiniz.
- **Çift Dil Desteği (`TR` / `EN`):**
  - `/tkf lang <tr|en>` komutuyla veya `/tkf` menüsündeki **Dil / Language** butonuyla tüm menüleri, mesajları, biletleri, hologramı ve dilim yazılarını tek tıkla Türkçe veya İngilizceye çevirebilirsiniz.

---

## 📦 Kurulum

1. Hazır derlenmiş [`releases/TkitsuneFortune-1.0.0.jar`](releases/TkitsuneFortune-1.0.0.jar) dosyasını (veya `gradlew build` ile derlediğiniz `.jar` dosyasını) sunucunuzun `plugins/` klasörüne atın.
2. Sunucunuzu başlatın (`Spigot` veya `Paper 1.21.11`, **Java 21+**).
3. Oyun içinden `/tkf` yazarak yönetim menüsünü açın ve **Baktığın Yere Çark Kur** butonuna tıklayın!
4. *(İsteğe Bağlı)* Eklenti, `plugins/TkitsuneFortune/TkitsuneFortune-ResourcePack.zip` dosyasını otomatik üretir ve `config.yml` içindeki `settings.resource-pack.port` (varsayılan `8765`) üzerinden oyunculara otomatik gönderir.

---

## ⌨ Komutlar ve Yetkiler

**Ana Komut:** `/tkf` *(Alternatifler: `/cark`, `/sanscarki`, `/fw`, `/fortunewheel`, `/tkitsunefortune`)*

| Komut | Açıklama |
| :--- | :--- |
| `/tkf` | Ödül, Şans Oranı, Hologram ve Çark Yönetim Menüsünü (GUI) açar. |
| `/tkf place [id]` | Baktığınız bloğun üstüne yeni bir 3D Şans Çarkı kurar. |
| `/tkf remove [id\|nearest]` | Belirtilen ID'deki veya en yakındaki (8 blok) Şans Çarkını kaldırır. |
| `/tkf list` | Dünyadaki aktif Şans Çarklarını ve koordinatlarını listeler. |
| `/tkf ticket <oyuncu> [adet]` | Oyuncuya özel 3D tasarımlı **Tkitsune Şans Çarkı Bileti** verir. |
| `/tkf hologram [ac\|kapat\|menu]` | Hologram Tasarım Menüsünü açar veya hologramı hızlıca açıp kapatır. |
| `/tkf isim <yeni başlık>` | Çarkın üstündeki hologram başlığını değiştirir (`&` ve `&#RRGGBB` destekli). |
| `/tkf lang <tr\|en>` | Eklenti dilini Türkçe (`TR`) veya İngilizce (`EN`) olarak değiştirir. |
| `/tkf mode <RESOURCE_PACK\|BLOCK_DISPLAY>` | 3D görünüm modunu anlık olarak değiştirir. |
| `/tkf scale <0.5 - 4.0>` | 3D Şans Çarkının boyut çarpanını değiştirir (Varsayılan: `1.7`). |
| `/tkf resourcepack` | Resource Pack `.zip` paketini yeniden oluşturur ve aktif oyunculara gönderir. |
| `/tkf reload` | `config.yml` ve `wheels.yml` dosyalarını yeniden yükler, tüm çarkları yeniler. |

### Yetkiler (Permissions)
- `tkitsunefortune.use` *(Varsayılan: `true`)* — Oyuncuların biletle Şans Çarkını çevirmesine izin verir.
- `tkitsunefortune.admin` *(Varsayılan: `op`)* — `/tkf` yönetim menüsüne ve tüm yönetici komutlarına erişim verir.

---

## 🖥 Oyun İçi Yönetim ve Tasarım Menüsü (GUI)

1. **Ana Menü (`/tkf`):**
   - **8 Dilim Slotu:** Sol tıklayarak dilimin ödül/şans düzenleyicisini açın; Sağ/Shift-Sağ tıklayarak şans ağırlığını hızlıca `+1.0` / `-1.0` değiştirin.
   - **Alt Kontrol Butonları:** Çark kurma/kaldırma, bilet alma, çark boyutu (`scale`), dönüş süresi (`ticks`), 3D görünüm modu, dil seçimi (`TR`/`EN`), hologram tasarım menüsü ve doku paketi gönderme.
2. **Dilim Ödül & Şans Düzenleyici:**
   - Ortadaki **9 boş slota** envanterinizden istediğiniz eşyaları sürükleyip bırakın.
   - İlk slota koyduğunuz eşya otomatik olarak çarkın o dilimindeki **3D dönen ikon** ve **dilim yazısı** olur (veya sol üstteki ikon slotuna istediğiniz eşyayla tıklayarak özel ikon belirleyebilirsiniz).
3. **Hologram Tasarım Menüsü (`/tkf hologram`):**
   - Hologramı açıp kapatma, başlık (`%title%`), bekleme ve dönüş satırlarını tek tek veya `|` ile toplu düzenleme, yükseklik, yazı boyutu, arka plan stili ve **6 hazır tema şablonu**.

---

## 🛠 Kaynak Koddan Derleme (Build)

Bu proje **Java 21** ve **Gradle Wrapper** kullanır. Bilgisayarınızda **JDK 21** kurulu olması yeterlidir:

### Windows (PowerShell / CMD)
```powershell
.\gradlew.bat clean build
```

### Linux / macOS
```bash
chmod +x gradlew
./gradlew clean build
```

Derleme tamamlandığında kullanıma hazır eklenti dosyası şu konumda oluşturulur:
```text
build/libs/TkitsuneFortune-1.0.0.jar
```

---

## 📂 Proje Yapısı ve Geliştirici Rehberi

```text
TkitsuneFortune/
├── assets/                                # Önizleme görselleri ve Blockbench kaynak dosyaları (.bbmodel)
│   ├── preview_render.png
│   ├── ticket_preview_render.png
│   ├── tkitsunefortune.bbmodel            # 3D Çark + Stand Blockbench projesi
│   └── tkitsunefortune_ticket.bbmodel     # 3D Özel Bilet Blockbench projesi
├── releases/                              # Hazır derlenmiş .jar ve ResourcePack .zip
│   ├── TkitsuneFortune-1.0.0.jar
│   └── TKitsuneFortune-ResourcePack.zip
├── src/main/
│   ├── java/com/tkitsunefortune/
│   │   ├── TKitsuneFortunePlugin.java     # Ana eklenti sınıfı
│   │   ├── command/FortuneWheelCommand.java
│   │   ├── gui/                           # Oyun içi Admin, Dilim ve Hologram GUI sınıfları
│   │   ├── listener/                      # Sağ tık etkileşim ve koruma dinleyicileri
│   │   ├── model/                         # Blockbench JSON -> 3D BlockDisplay çözümleyici
│   │   ├── resourcepack/                  # Otomatik Resource Pack üretici & HTTP sunucusu
│   │   ├── ticket/TicketManager.java      # Özel PDC etiketli 3D Bilet yöneticisi
│   │   ├── util/                          # ColorUtil (HEX & renk) ve LanguageManager (TR/EN)
│   │   └── wheel/                         # 3D FortuneWheel, SpinTask, SliceManager, WheelManager
│   └── resources/
│       ├── config.yml
│       ├── plugin.yml
│       ├── models/                        # 3D JSON modelleri (stand, wheel, ticket)
│       └── textures/                      # 64x64 dokular (tkitsunefortune.png, ticket.png)
├── build.gradle
└── settings.gradle
```

### 🎨 3D Modeli veya Dokuyu Özelleştirme (Blockbench)
1. `assets/tkitsunefortune.bbmodel` veya `assets/tkitsunefortune_ticket.bbmodel` dosyasını [Blockbench](https://www.blockbench.net/) ile açın.
2. Model veya doku üzerinde değişiklik yaptıktan sonra:
   - Stand elemanlarını `src/main/resources/models/tkitsunefortune_stand.json`
   - Dönen çark elemanlarını `src/main/resources/models/tkitsunefortune_wheel.json`
   - Ana dokuyu `src/main/resources/textures/tkitsunefortune.png` olarak dışa aktarın.
3. `./gradlew build` komutunu çalıştırın; eklenti yeni modeli ve doku paketini otomatik olarak paketleyecektir.

---

# 🇬🇧 English Documentation

## ✨ Features

- **Smooth 3D Spin Physics:** Real-time `Display` entity quaternion interpolation with cubic ease-out deceleration and tick-by-tick sound/particle effects.
- **Dual 3D Render Modes (`RESOURCE_PACK` & `BLOCK_DISPLAY`):**
  - `RESOURCE_PACK`: Uses custom Blockbench 3D models (`ItemDisplay`) with a built-in HTTP server that automatically hosts and sends the resource pack to players.
  - `BLOCK_DISPLAY`: Builds the 111-element 3D model purely out of vanilla `BlockDisplay` entities—no resource pack required.
- **Rotating 3D Slice Prizes & Labels:** Each of the 8 wheel slices displays a rotating 3D item icon and customizable text label on the outer ring.
- **In-Game Drag & Drop Admin GUI (`/tkf`):** Edit slice win chances, drag and drop up to 9 physical reward items per slice (supports custom NBT/enchants), manage console commands, and customize the 3D hologram without touching config files.
- **Full Hologram Design Editor (`/tkf hologram`):** Toggle the hologram ON/OFF, edit the title (`%title%`), idle lines, spinning lines, height offset, text scale, background style, billboard mode, or apply **6 one-click preset themes**.
- **Built-In Bilingual Support (`TR` & `EN`):** Switch the entire plugin (menus, messages, ticket, hologram, and slice labels) between **Turkish (`TR`)** and **English (`EN`)** anytime via `/tkf lang en` or the in-game GUI button.

## 📦 Installation

1. Drop [`releases/TkitsuneFortune-1.0.0.jar`](releases/TkitsuneFortune-1.0.0.jar) into your server's `plugins/` folder.
2. Start your **Spigot** or **Paper 1.21.11** server (**Java 21+**).
3. Run `/tkf lang en` in-game to switch all menus and wheel texts to English, then run `/tkf` and click **Place Wheel Here**!

## ⌨ Commands & Permissions

| Command | Description |
| :--- | :--- |
| `/tkf` | Opens the interactive Admin Settings, Slice Prize & Hologram GUI. |
| `/tkf place [id]` | Places a 3D Wheel of Fortune on the block you are looking at. |
| `/tkf remove [id\|nearest]` | Removes a Wheel of Fortune by ID or the nearest one within 8 blocks. |
| `/tkf list` | Lists all active Fortune Wheels and their coordinates. |
| `/tkf ticket <player> [amount]` | Gives custom 3D **Fortune Wheel Tickets** to a player. |
| `/tkf hologram [on\|off\|menu]` | Opens the Hologram Design GUI or toggles the hologram ON/OFF. |
| `/tkf name <new title>` | Updates the wheel hologram title (supports `&` and `&#RRGGBB` hex colors). |
| `/tkf lang <tr\|en>` | Switches plugin language between Turkish (`TR`) and English (`EN`). |
| `/tkf mode <RESOURCE_PACK\|BLOCK_DISPLAY>` | Switches the 3D rendering mode on the fly. |
| `/tkf scale <0.5 - 4.0>` | Adjusts the 3D wheel size multiplier (Default: `1.7`). |
| `/tkf resourcepack` | Regenerates and sends the Resource Pack `.zip` to online players. |
| `/tkf reload` | Reloads `config.yml`, `wheels.yml`, and respawns all active wheels. |

## 🛠 Building from Source

Requires **JDK 21+**:

```bash
# Linux / macOS
./gradlew clean build

# Windows
.\gradlew.bat clean build
```

The compiled plugin JAR will be output to `build/libs/TkitsuneFortune-1.0.0.jar`.

---

## 📄 License

Distributed under the [MIT License](LICENSE).
