package com.tkitsunefortune.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tkitsunefortune.TKitsuneFortunePlugin;
import org.bukkit.Material;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ModelLoader {

    private final TKitsuneFortunePlugin plugin;
    private final List<ModelElement> standElements = new ArrayList<>();
    private final List<ModelElement> wheelElements = new ArrayList<>();

    public ModelLoader(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadModels() {
        standElements.clear();
        wheelElements.clear();

        standElements.addAll(parseModelResource("models/tkitsunefortune_stand.json"));
        wheelElements.addAll(parseModelResource("models/tkitsunefortune_wheel.json"));

        plugin.getLogger().info("Loaded 3D Fortune Wheel geometry: "
                + standElements.size() + " stand parts, "
                + wheelElements.size() + " wheel parts.");
    }

    private List<ModelElement> parseModelResource(String resourcePath) {
        List<ModelElement> result = new ArrayList<>();
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                plugin.getLogger().warning("Model resource not found: " + resourcePath);
                return result;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray elements = root.getAsJsonArray("elements");
            if (elements == null) {
                return result;
            }

            for (JsonElement el : elements) {
                JsonObject obj = el.getAsJsonObject();
                String name = obj.has("name") ? obj.get("name").getAsString() : "part";
                JsonArray from = obj.getAsJsonArray("from");
                JsonArray to = obj.getAsJsonArray("to");

                float fx = from.get(0).getAsFloat();
                float fy = from.get(1).getAsFloat();
                float fz = from.get(2).getAsFloat();

                float tx = to.get(0).getAsFloat();
                float ty = to.get(1).getAsFloat();
                float tz = to.get(2).getAsFloat();

                String axis = "z";
                float angle = 0.0f;
                float ox = 8.0f;
                float oy = 8.0f;
                float oz = 8.0f;

                if (obj.has("rotation") && obj.get("rotation").isJsonObject()) {
                    JsonObject rot = obj.getAsJsonObject("rotation");
                    if (rot.has("axis")) {
                        axis = rot.get("axis").getAsString();
                    }
                    if (rot.has("angle")) {
                        angle = rot.get("angle").getAsFloat();
                    }
                    if (rot.has("origin") && rot.get("origin").isJsonArray()) {
                        JsonArray orig = rot.getAsJsonArray("origin");
                        ox = orig.get(0).getAsFloat();
                        oy = orig.get(1).getAsFloat();
                        oz = orig.get(2).getAsFloat();
                    }
                }

                Material material = resolveMaterialForPart(name);
                boolean emissive = isEmissivePart(name);

                result.add(new ModelElement(
                        name,
                        fx, fy, fz,
                        tx, ty, tz,
                        axis, angle,
                        ox, oy, oz,
                        material,
                        emissive
                ));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to parse model resource " + resourcePath + ": " + e.getMessage());
        }
        return result;
    }

    private Material resolveMaterialForPart(String partName) {
        String lower = partName.toLowerCase(Locale.ROOT);

        // 8 colored wheel slices matching preview_render.png
        if (lower.startsWith("slice_1_red")) {
            return Material.RED_CONCRETE;
        }
        if (lower.startsWith("slice_2_orange")) {
            return Material.ORANGE_CONCRETE;
        }
        if (lower.startsWith("slice_3_yellow")) {
            return Material.YELLOW_CONCRETE;
        }
        if (lower.startsWith("slice_4_green")) {
            return Material.LIME_CONCRETE;
        }
        if (lower.startsWith("slice_5_cyan")) {
            return Material.LIGHT_BLUE_CONCRETE;
        }
        if (lower.startsWith("slice_6_blue")) {
            return Material.BLUE_CONCRETE;
        }
        if (lower.startsWith("slice_7_purple")) {
            return Material.PURPLE_CONCRETE;
        }
        if (lower.startsWith("slice_8_magenta")) {
            return Material.MAGENTA_CONCRETE;
        }

        // Gems (Center hub cyan gem + top pointer cyan gem)
        if (lower.contains("gem")) {
            return Material.DIAMOND_BLOCK;
        }

        // Rim light bulbs & pegs
        if (lower.startsWith("rim_bulb")) {
            return Material.SEA_LANTERN;
        }
        if (lower.startsWith("peg_")) {
            return Material.QUARTZ_BLOCK;
        }

        // Red velvet pad & red pointer housing
        if (lower.equals("stand_velvet_pad")
                || lower.equals("pointer_arrow_top")
                || lower.equals("pointer_arrow_mid")) {
            return Material.RED_CONCRETE;
        }

        // Dark wood stand structure & axle
        if (lower.equals("stand_base_bottom")
                || lower.equals("stand_pedestal_riser")
                || lower.equals("stand_rear_pillar_core")
                || lower.startsWith("stand_strut_")
                || lower.equals("hub_axle_rear")) {
            return Material.DARK_OAK_WOOD;
        }

        // Gold trim, feet, bands, spokes, rim segments, pointer crown & tip, hub octagon
        return Material.GOLD_BLOCK;
    }

    private boolean isEmissivePart(String partName) {
        String lower = partName.toLowerCase(Locale.ROOT);
        return lower.startsWith("rim_bulb")
                || lower.contains("gem")
                || lower.startsWith("slice_");
    }

    public List<ModelElement> getStandElements() {
        return Collections.unmodifiableList(standElements);
    }

    public List<ModelElement> getWheelElements() {
        return Collections.unmodifiableList(wheelElements);
    }
}

