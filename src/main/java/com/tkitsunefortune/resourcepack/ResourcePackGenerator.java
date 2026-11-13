package com.tkitsunefortune.resourcepack;

import com.tkitsunefortune.TKitsuneFortunePlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackGenerator {

    private final TKitsuneFortunePlugin plugin;

    public ResourcePackGenerator(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    public File generateResourcePack() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File outZip = new File(plugin.getDataFolder(), "TkitsuneFortune-ResourcePack.zip");
        writeZip(outZip);
        return outZip;
    }

    public void writeZip(File targetFile) {
        try (FileOutputStream fos = new FileOutputStream(targetFile);
             ZipOutputStream zos = new ZipOutputStream(fos, StandardCharsets.UTF_8)) {

            // 1. pack.mcmeta supporting 1.21.x through 1.21.11+
            String packMcmeta = "{\n"
                    + "  \"pack\": {\n"
                    + "    \"pack_format\": 46,\n"
                    + "    \"min_format\": 34,\n"
                    + "    \"max_format\": 99,\n"
                    + "    \"supported_formats\": {\n"
                    + "      \"min_inclusive\": 34,\n"
                    + "      \"max_inclusive\": 99\n"
                    + "    },\n"
                    + "    \"description\": \"\\u00a76\\u00a7lTkitsuneFortune \\u00a7e3D Sans Carki + Ozel Bilet (1.21.11)\"\n"
                    + "  }\n"
                    + "}\n";
            addStringEntry(zos, "pack.mcmeta", packMcmeta);

            // 2. Pack icon & textures (Wheel + Custom Ticket)
            byte[] textureBytes = readResourceBytes("models/tkitsunefortune.png");
            if (textureBytes != null) {
                addBytesEntry(zos, "pack.png", textureBytes);
                addBytesEntry(zos, "assets/minecraft/textures/block/tkitsunefortune.png", textureBytes);
                addBytesEntry(zos, "assets/tkitsunefortune/textures/block/tkitsunefortune.png", textureBytes);
            }

            byte[] ticketTexBytes = readResourceBytes("models/tkitsunefortune_ticket.png");
            if (ticketTexBytes != null) {
                addBytesEntry(zos, "assets/minecraft/textures/block/tkitsunefortune_ticket.png", ticketTexBytes);
                addBytesEntry(zos, "assets/tkitsunefortune/textures/block/tkitsunefortune_ticket.png", ticketTexBytes);
            }

            byte[] ticketIconBytes = readResourceBytes("models/tkitsunefortune_ticket_item.png");
            if (ticketIconBytes != null) {
                addBytesEntry(zos, "assets/minecraft/textures/item/tkitsunefortune_ticket.png", ticketIconBytes);
                addBytesEntry(zos, "assets/tkitsunefortune/textures/item/tkitsunefortune_ticket.png", ticketIconBytes);
            }

            // 3. 3D Models (Stand, Wheel, Full, and Custom Ticket)
            byte[] standBytes = readResourceBytes("models/tkitsunefortune_stand.json");
            byte[] wheelBytes = readResourceBytes("models/tkitsunefortune_wheel.json");
            byte[] fullBytes = readResourceBytes("models/tkitsunefortune.json");
            byte[] ticketModelBytes = readResourceBytes("models/tkitsunefortune_ticket.json");

            if (standBytes != null) {
                addBytesEntry(zos, "assets/minecraft/models/item/tkitsunefortune_stand.json", standBytes);
                addBytesEntry(zos, "assets/tkitsunefortune/models/item/stand.json", standBytes);
            }
            if (wheelBytes != null) {
                addBytesEntry(zos, "assets/minecraft/models/item/tkitsunefortune_wheel.json", wheelBytes);
                addBytesEntry(zos, "assets/tkitsunefortune/models/item/wheel.json", wheelBytes);
            }
            if (fullBytes != null) {
                addBytesEntry(zos, "assets/minecraft/models/item/tkitsunefortune_full.json", fullBytes);
                addBytesEntry(zos, "assets/tkitsunefortune/models/item/full.json", fullBytes);
            }
            if (ticketModelBytes != null) {
                addBytesEntry(zos, "assets/minecraft/models/item/tkitsunefortune_ticket.json", ticketModelBytes);
                addBytesEntry(zos, "assets/tkitsunefortune/models/item/ticket.json", ticketModelBytes);
            }

            // 4. 1.21.4 - 1.21.11 Item Model Definitions (assets/tkitsunefortune/items/*.json)
            addStringEntry(zos, "assets/tkitsunefortune/items/stand.json",
                    "{\n  \"model\": {\n    \"type\": \"minecraft:model\",\n    \"model\": \"minecraft:item/tkitsunefortune_stand\"\n  }\n}\n");
            addStringEntry(zos, "assets/tkitsunefortune/items/wheel.json",
                    "{\n  \"model\": {\n    \"type\": \"minecraft:model\",\n    \"model\": \"minecraft:item/tkitsunefortune_wheel\"\n  }\n}\n");
            addStringEntry(zos, "assets/tkitsunefortune/items/full.json",
                    "{\n  \"model\": {\n    \"type\": \"minecraft:model\",\n    \"model\": \"minecraft:item/tkitsunefortune_full\"\n  }\n}\n");
            addStringEntry(zos, "assets/tkitsunefortune/items/ticket.json",
                    "{\n  \"model\": {\n    \"type\": \"minecraft:model\",\n    \"model\": \"minecraft:item/tkitsunefortune_ticket\"\n  }\n}\n");

            // 5. 1.21.4 - 1.21.11 CustomModelData range_dispatch on minecraft:paper
            String paperItems1214 = "{\n"
                    + "  \"model\": {\n"
                    + "    \"type\": \"minecraft:range_dispatch\",\n"
                    + "    \"property\": \"minecraft:custom_model_data\",\n"
                    + "    \"fallback\": {\n"
                    + "      \"type\": \"minecraft:model\",\n"
                    + "      \"model\": \"minecraft:item/paper\"\n"
                    + "    },\n"
                    + "    \"entries\": [\n"
                    + "      {\n"
                    + "        \"threshold\": 1001,\n"
                    + "        \"model\": {\n"
                    + "          \"type\": \"minecraft:model\",\n"
                    + "          \"model\": \"minecraft:item/tkitsunefortune_stand\"\n"
                    + "        }\n"
                    + "      },\n"
                    + "      {\n"
                    + "        \"threshold\": 1002,\n"
                    + "        \"model\": {\n"
                    + "          \"type\": \"minecraft:model\",\n"
                    + "          \"model\": \"minecraft:item/tkitsunefortune_wheel\"\n"
                    + "        }\n"
                    + "      },\n"
                    + "      {\n"
                    + "        \"threshold\": 1003,\n"
                    + "        \"model\": {\n"
                    + "          \"type\": \"minecraft:model\",\n"
                    + "          \"model\": \"minecraft:item/tkitsunefortune_full\"\n"
                    + "        }\n"
                    + "      },\n"
                    + "      {\n"
                    + "        \"threshold\": 1004,\n"
                    + "        \"model\": {\n"
                    + "          \"type\": \"minecraft:model\",\n"
                    + "          \"model\": \"minecraft:item/tkitsunefortune_ticket\"\n"
                    + "        }\n"
                    + "      }\n"
                    + "    ]\n"
                    + "  }\n"
                    + "}\n";
            addStringEntry(zos, "assets/minecraft/items/paper.json", paperItems1214);

            // 6. Legacy 1.21 - 1.21.3 overrides on assets/minecraft/models/item/paper.json
            String paperLegacyModel = "{\n"
                    + "  \"parent\": \"minecraft:item/generated\",\n"
                    + "  \"textures\": {\n"
                    + "    \"layer0\": \"minecraft:item/paper\"\n"
                    + "  },\n"
                    + "  \"overrides\": [\n"
                    + "    { \"predicate\": { \"custom_model_data\": 1001 }, \"model\": \"minecraft:item/tkitsunefortune_stand\" },\n"
                    + "    { \"predicate\": { \"custom_model_data\": 1002 }, \"model\": \"minecraft:item/tkitsunefortune_wheel\" },\n"
                    + "    { \"predicate\": { \"custom_model_data\": 1003 }, \"model\": \"minecraft:item/tkitsunefortune_full\" },\n"
                    + "    { \"predicate\": { \"custom_model_data\": 1004 }, \"model\": \"minecraft:item/tkitsunefortune_ticket\" }\n"
                    + "  ]\n"
                    + "}\n";
            addStringEntry(zos, "assets/minecraft/models/item/paper.json", paperLegacyModel);

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate ResourcePack zip: " + e.getMessage());
        }
    }

    private byte[] readResourceBytes(String path) throws IOException {
        try (InputStream in = plugin.getResource(path)) {
            if (in == null) {
                return null;
            }
            return in.readAllBytes();
        }
    }

    private void addStringEntry(ZipOutputStream zos, String name, String content) throws IOException {
        addBytesEntry(zos, name, content.getBytes(StandardCharsets.UTF_8));
    }

    private void addBytesEntry(ZipOutputStream zos, String name, byte[] bytes) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(bytes);
        zos.closeEntry();
    }
}

