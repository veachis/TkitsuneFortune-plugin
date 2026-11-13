package com.tkitsunefortune.wheel;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.model.ModelElement;
import com.tkitsunefortune.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FortuneWheel {

    private static final Quaternionf TEXT_FRONT_ROTATION = new Quaternionf().rotateY((float) Math.PI);

    private final TKitsuneFortunePlugin plugin;
    private final String id;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;

    private float currentAngleDegrees = 0.0f;
    private boolean spinning = false;
    private String currentSpinnerName = null;

    private final List<UUID> spawnedEntityIds = new ArrayList<>();
    private UUID resourcePackWheelEntityId;
    private UUID hologramEntityId;
    private UUID interactionEntityId;
    private final Map<UUID, ModelElement> blockDisplayWheelElements = new HashMap<>();
    private final Map<UUID, WheelSlice> slicePrizeIcons = new HashMap<>();
    private final Map<UUID, WheelSlice> slicePrizeLabels = new HashMap<>();

    public FortuneWheel(
            TKitsuneFortunePlugin plugin,
            String id,
            String worldName,
            double x,
            double y,
            double z,
            float yaw
    ) {
        this.plugin = plugin;
        this.id = id;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    public Location getBaseLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, 0.0f);
    }

    public void spawn() {
        despawn();
        Location baseLoc = getBaseLocation();
        if (baseLoc == null || baseLoc.getWorld() == null) {
            return;
        }
        World world = baseLoc.getWorld();
        if (!baseLoc.getChunk().isLoaded()) {
            return;
        }

        cleanupOrphanEntities(baseLoc);

        float scale = (float) plugin.getConfig().getDouble("settings.scale", 1.7);
        RenderMode mode = RenderMode.fromString(plugin.getConfig().getString("settings.render-mode", "BLOCK_DISPLAY"));

        if (mode == RenderMode.RESOURCE_PACK) {
            spawnResourcePackMode(world, baseLoc, scale);
        } else {
            spawnBlockDisplayMode(world, baseLoc, scale);
        }

        spawnSlicePrizeDisplays(world, baseLoc, scale);
        spawnInteractionHitbox(world, baseLoc, scale);
        spawnHologram(world, baseLoc, scale);
    }

    private void spawnResourcePackMode(World world, Location baseLoc, float scale) {
        Quaternionf standRot = new Quaternionf().rotateY((float) Math.PI);
        ItemDisplay stand = world.spawn(baseLoc, ItemDisplay.class, display -> {
            tagEntity(display);
            display.setRotation(yaw, 0.0f);
            display.setItemStack(plugin.getTicketManager().createStandModelItem());
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setViewRange(2.0f);
            display.setInterpolationDuration(0);
            display.setTransformation(new Transformation(
                    new Vector3f(0.0f, 0.5f * scale, 0.0f),
                    standRot,
                    new Vector3f(scale, scale, scale),
                    new Quaternionf()
            ));
        });
        spawnedEntityIds.add(stand.getUniqueId());

        float rad = (float) Math.toRadians(currentAngleDegrees);
        Quaternionf wheelRot = new Quaternionf().rotateZ(rad).rotateY((float) Math.PI);
        ItemDisplay wheel = world.spawn(baseLoc, ItemDisplay.class, display -> {
            tagEntity(display);
            display.setRotation(yaw, 0.0f);
            display.setItemStack(plugin.getTicketManager().createWheelModelItem());
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setViewRange(2.0f);
            display.setInterpolationDuration(0);
            display.setTransformation(new Transformation(
                    new Vector3f(0.0f, 1.0625f * scale, 0.0f),
                    wheelRot,
                    new Vector3f(scale, scale, scale),
                    new Quaternionf()
            ));
        });
        resourcePackWheelEntityId = wheel.getUniqueId();
        spawnedEntityIds.add(wheel.getUniqueId());
    }

    private void spawnBlockDisplayMode(World world, Location baseLoc, float scale) {
        // 1. Spawn Stand BlockDisplays
        for (ModelElement el : plugin.getModelLoader().getStandElements()) {
            Transformation transformation = computeStandBlockTransformation(el, scale);
            BlockDisplay bd = world.spawn(baseLoc, BlockDisplay.class, display -> {
                tagEntity(display);
                display.setRotation(yaw, 0.0f);
                display.setBlock(Bukkit.createBlockData(el.getMaterial()));
                display.setViewRange(2.0f);
                display.setInterpolationDuration(0);
                display.setBrightness(el.isEmissive()
                        ? new Display.Brightness(15, 15)
                        : new Display.Brightness(13, 15));
                display.setTransformation(transformation);
            });
            spawnedEntityIds.add(bd.getUniqueId());
        }

        // 2. Spawn Wheel BlockDisplays (interpolationDuration=0 prevents chord-lerp shrinking!)
        for (ModelElement el : plugin.getModelLoader().getWheelElements()) {
            Transformation transformation = computeWheelBlockTransformation(el, scale, currentAngleDegrees);
            BlockDisplay bd = world.spawn(baseLoc, BlockDisplay.class, display -> {
                tagEntity(display);
                display.setRotation(yaw, 0.0f);
                display.setBlock(Bukkit.createBlockData(el.getMaterial()));
                display.setViewRange(2.0f);
                display.setInterpolationDuration(0);
                display.setBrightness(el.isEmissive()
                        ? new Display.Brightness(15, 15)
                        : new Display.Brightness(13, 15));
                display.setTransformation(transformation);
            });
            spawnedEntityIds.add(bd.getUniqueId());
            blockDisplayWheelElements.put(bd.getUniqueId(), el);
        }
    }

    private void spawnSlicePrizeDisplays(World world, Location baseLoc, float scale) {
        if (!plugin.getConfig().getBoolean("settings.show-slice-prizes", true)) {
            return;
        }
        for (WheelSlice slice : plugin.getSliceManager().getSlices()) {
            Transformation iconTrans = computeSliceIconTransformation(slice, scale, currentAngleDegrees);
            ItemDisplay iconDisplay = world.spawn(baseLoc, ItemDisplay.class, display -> {
                tagEntity(display);
                display.setRotation(yaw, 0.0f);
                display.setItemStack(plugin.getSliceManager().createSliceIconItem(slice));
                display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GUI);
                display.setBrightness(new Display.Brightness(15, 15));
                display.setViewRange(1.5f);
                display.setInterpolationDuration(0);
                display.setTransformation(iconTrans);
            });
            spawnedEntityIds.add(iconDisplay.getUniqueId());
            slicePrizeIcons.put(iconDisplay.getUniqueId(), slice);

            Transformation labelTrans = computeSliceLabelTransformation(slice, scale, currentAngleDegrees);
            TextDisplay labelDisplay = world.spawn(baseLoc, TextDisplay.class, td -> {
                tagEntity(td);
                td.setRotation(yaw, 0.0f);
                td.setText(slice.getSliceLabel());
                td.setBillboard(Display.Billboard.FIXED);
                td.setAlignment(TextDisplay.TextAlignment.CENTER);
                td.setShadowed(true);
                td.setBrightness(new Display.Brightness(15, 15));
                td.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                td.setViewRange(1.2f);
                td.setInterpolationDuration(0);
                td.setTransformation(labelTrans);
            });
            spawnedEntityIds.add(labelDisplay.getUniqueId());
            slicePrizeLabels.put(labelDisplay.getUniqueId(), slice);
        }
    }

    private Transformation computeSliceIconTransformation(WheelSlice slice, float scale, float wheelAngleDeg) {
        float betaDeg = wheelAngleDeg - (slice.getIndex() * 45.0f);
        double betaRad = Math.toRadians(betaDeg);
        float radius = 0.495f * scale;

        float ix = (float) (-radius * Math.sin(betaRad));
        float iy = (1.0625f * scale) + (float) (radius * Math.cos(betaRad)) + (0.022f * scale);
        float iz = -0.115f * scale;
        float iconScale = 0.16f * scale;

        return new Transformation(
                new Vector3f(ix, iy, iz),
                new Quaternionf(),
                new Vector3f(iconScale, iconScale, iconScale),
                new Quaternionf()
        );
    }

    private Transformation computeSliceLabelTransformation(WheelSlice slice, float scale, float wheelAngleDeg) {
        float betaDeg = wheelAngleDeg - (slice.getIndex() * 45.0f);
        double betaRad = Math.toRadians(betaDeg);
        float radius = 0.495f * scale;

        float tx = (float) (-radius * Math.sin(betaRad));
        float ty = (1.0625f * scale) + (float) (radius * Math.cos(betaRad)) - (0.052f * scale);
        float tz = -0.135f * scale;
        float textScale = 0.145f * scale;

        return new Transformation(
                new Vector3f(tx, ty, tz),
                new Quaternionf(TEXT_FRONT_ROTATION),
                new Vector3f(textScale, textScale, textScale),
                new Quaternionf()
        );
    }

    private Transformation computeStandBlockTransformation(ModelElement el, float scale) {
        float sx = ((el.getToX() - el.getFromX()) / 16.0f) * scale;
        float sy = ((el.getToY() - el.getFromY()) / 16.0f) * scale;
        float sz = ((el.getToZ() - el.getFromZ()) / 16.0f) * scale;

        Vector3f origin = new Vector3f(
                ((el.getOriginX() - 8.0f) / 16.0f) * scale,
                (el.getOriginY() / 16.0f) * scale,
                ((el.getOriginZ() - 8.0f) / 16.0f) * scale
        );
        Vector3f offset = new Vector3f(
                ((el.getFromX() - el.getOriginX()) / 16.0f) * scale,
                ((el.getFromY() - el.getOriginY()) / 16.0f) * scale,
                ((el.getFromZ() - el.getOriginZ()) / 16.0f) * scale
        );

        Quaternionf rot = createAxisQuaternion(el.getRotationAxis(), el.getRotationAngleDegrees());
        rot.transform(offset);
        Vector3f translation = origin.add(offset);

        return new Transformation(
                translation,
                rot,
                new Vector3f(sx, sy, sz),
                new Quaternionf()
        );
    }

    private Transformation computeWheelBlockTransformation(ModelElement el, float scale, float wheelAngleDeg) {
        float sx = ((el.getToX() - el.getFromX()) / 16.0f) * scale;
        float sy = ((el.getToY() - el.getFromY()) / 16.0f) * scale;
        float sz = ((el.getToZ() - el.getFromZ()) / 16.0f) * scale;

        float totalAngleDeg = el.getRotationAngleDegrees() + wheelAngleDeg;
        Quaternionf rot = new Quaternionf().rotateZ((float) Math.toRadians(totalAngleDeg));

        Vector3f offsetFromHub = new Vector3f(
                ((el.getFromX() - 8.0f) / 16.0f) * scale,
                ((el.getFromY() - 8.0f) / 16.0f) * scale,
                ((el.getFromZ() - 8.0f) / 16.0f) * scale
        );
        rot.transform(offsetFromHub);

        Vector3f translation = new Vector3f(
                offsetFromHub.x,
                (1.0625f * scale) + offsetFromHub.y,
                offsetFromHub.z
        );

        return new Transformation(
                translation,
                rot,
                new Vector3f(sx, sy, sz),
                new Quaternionf()
        );
    }

    private Quaternionf createAxisQuaternion(String axis, float angleDeg) {
        float rad = (float) Math.toRadians(angleDeg);
        if ("x".equalsIgnoreCase(axis)) {
            return new Quaternionf().rotateX(rad);
        } else if ("y".equalsIgnoreCase(axis)) {
            return new Quaternionf().rotateY(rad);
        }
        return new Quaternionf().rotateZ(rad);
    }

    private void spawnInteractionHitbox(World world, Location baseLoc, float scale) {
        Interaction interaction = world.spawn(baseLoc, Interaction.class, inter -> {
            tagEntity(inter);
            inter.setInteractionWidth(1.45f * scale);
            inter.setInteractionHeight(2.05f * scale);
            inter.setResponsive(true);
        });
        interactionEntityId = interaction.getUniqueId();
        spawnedEntityIds.add(interaction.getUniqueId());
    }

    private void spawnHologram(World world, Location baseLoc, float scale) {
        if (!plugin.getConfig().getBoolean("settings.hologram.enabled", true)) {
            return;
        }
        double heightOffset = plugin.getConfig().getDouble("settings.hologram.height-offset", 2.25) * scale;
        Location holoLoc = baseLoc.clone().add(0.0, heightOffset, 0.0);

        String title = plugin.getWheelManager().getHologramTitle();
        List<String> rawLines = plugin.getConfig().getStringList("settings.hologram.idle-lines");
        List<String> formatted = new ArrayList<>(rawLines.size());
        for (String rawLine : rawLines) {
            formatted.add(ColorUtil.colorize(rawLine.replace("%title%", title)));
        }
        String text = String.join("\n", formatted);

        float holoScale = (float) plugin.getConfig().getDouble("settings.hologram.scale", 1.0);
        boolean shadowed = plugin.getConfig().getBoolean("settings.hologram.shadowed", true);
        String billboardStr = plugin.getConfig().getString("settings.hologram.billboard", "CENTER");
        Display.Billboard billboard = switch (billboardStr == null ? "CENTER" : billboardStr.toUpperCase()) {
            case "VERTICAL" -> Display.Billboard.VERTICAL;
            case "FIXED" -> Display.Billboard.FIXED;
            default -> Display.Billboard.CENTER;
        };
        Color bgColor = resolveHologramBackgroundColor(plugin.getConfig().getString("settings.hologram.background", "TRANSPARENT"));

        TextDisplay hologram = world.spawn(holoLoc, TextDisplay.class, td -> {
            tagEntity(td);
            td.setRotation(yaw, 0.0f);
            td.setText(text);
            td.setBillboard(billboard);
            td.setAlignment(TextDisplay.TextAlignment.CENTER);
            td.setShadowed(shadowed);
            td.setBackgroundColor(bgColor);
            td.setViewRange(1.5f);
            Quaternionf leftRot = (billboard == Display.Billboard.FIXED)
                    ? new Quaternionf(TEXT_FRONT_ROTATION)
                    : new Quaternionf();
            td.setTransformation(new Transformation(
                    new Vector3f(0.0f, 0.0f, 0.0f),
                    leftRot,
                    new Vector3f(holoScale, holoScale, holoScale),
                    new Quaternionf()
            ));
        });
        hologramEntityId = hologram.getUniqueId();
        spawnedEntityIds.add(hologram.getUniqueId());
    }

    private Color resolveHologramBackgroundColor(String bgKey) {
        if (bgKey == null) {
            return Color.fromARGB(0, 0, 0, 0);
        }
        return switch (bgKey.toUpperCase()) {
            case "DARK", "BLACK" -> Color.fromARGB(110, 0, 0, 0);
            case "CRIMSON", "RED" -> Color.fromARGB(145, 90, 10, 18);
            case "GOLD", "YELLOW" -> Color.fromARGB(145, 95, 65, 8);
            case "PURPLE", "MYSTIC" -> Color.fromARGB(145, 58, 12, 85);
            case "AQUA", "DIAMOND" -> Color.fromARGB(145, 10, 68, 88);
            default -> Color.fromARGB(0, 0, 0, 0);
        };
    }

    public void updateWheelRotation(float newAngleDegrees) {
        this.currentAngleDegrees = newAngleDegrees;
        float scale = (float) plugin.getConfig().getDouble("settings.scale", 1.7);

        // 1. Update ResourcePack Wheel ItemDisplay if active
        if (resourcePackWheelEntityId != null) {
            Entity entity = Bukkit.getEntity(resourcePackWheelEntityId);
            if (entity instanceof ItemDisplay display && display.isValid()) {
                float rad = (float) Math.toRadians(newAngleDegrees);
                Quaternionf wheelRot = new Quaternionf().rotateZ(rad).rotateY((float) Math.PI);
                display.setInterpolationDuration(0);
                display.setTransformation(new Transformation(
                        new Vector3f(0.0f, 1.0625f * scale, 0.0f),
                        wheelRot,
                        new Vector3f(scale, scale, scale),
                        new Quaternionf()
                ));
            }
        }

        // 2. Update BlockDisplay Wheel elements if active (interpolationDuration=0 keeps exact constant radius!)
        if (!blockDisplayWheelElements.isEmpty()) {
            for (Map.Entry<UUID, ModelElement> entry : blockDisplayWheelElements.entrySet()) {
                Entity entity = Bukkit.getEntity(entry.getKey());
                if (entity instanceof BlockDisplay bd && bd.isValid()) {
                    Transformation t = computeWheelBlockTransformation(entry.getValue(), scale, newAngleDegrees);
                    bd.setInterpolationDuration(0);
                    bd.setTransformation(t);
                }
            }
        }

        // 3. Update 8 Slice Prize Icons
        if (!slicePrizeIcons.isEmpty()) {
            for (Map.Entry<UUID, WheelSlice> entry : slicePrizeIcons.entrySet()) {
                Entity entity = Bukkit.getEntity(entry.getKey());
                if (entity instanceof ItemDisplay idisp && idisp.isValid()) {
                    idisp.setInterpolationDuration(0);
                    idisp.setTransformation(computeSliceIconTransformation(entry.getValue(), scale, newAngleDegrees));
                }
            }
        }

        // 4. Update 8 Slice Prize Labels
        if (!slicePrizeLabels.isEmpty()) {
            for (Map.Entry<UUID, WheelSlice> entry : slicePrizeLabels.entrySet()) {
                Entity entity = Bukkit.getEntity(entry.getKey());
                if (entity instanceof TextDisplay tdisp && tdisp.isValid()) {
                    tdisp.setInterpolationDuration(0);
                    tdisp.setTransformation(computeSliceLabelTransformation(entry.getValue(), scale, newAngleDegrees));
                }
            }
        }
    }

    public void updateHologramForSpin(String playerName, WheelSlice currentSlice) {
        if (hologramEntityId == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(hologramEntityId);
        if (!(entity instanceof TextDisplay td) || !td.isValid()) {
            return;
        }
        String title = plugin.getWheelManager().getHologramTitle();
        List<String> rawLines = plugin.getConfig().getStringList("settings.hologram.spinning-lines");
        String sliceName = currentSlice != null ? currentSlice.getDisplayName() : "";
        List<String> formatted = new ArrayList<>(rawLines.size());
        for (String baseLine : rawLines) {
            String replaced = baseLine
                    .replace("%title%", title)
                    .replace("%player%", playerName)
                    .replace("%slice%", sliceName);
            formatted.add(ColorUtil.colorize(replaced));
        }
        td.setText(String.join("\n", formatted));
    }

    public void resetHologramToIdle() {
        if (hologramEntityId == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(hologramEntityId);
        if (!(entity instanceof TextDisplay td) || !td.isValid()) {
            return;
        }
        String title = plugin.getWheelManager().getHologramTitle();
        List<String> rawLines = plugin.getConfig().getStringList("settings.hologram.idle-lines");
        List<String> formatted = new ArrayList<>(rawLines.size());
        for (String rawLine : rawLines) {
            formatted.add(ColorUtil.colorize(rawLine.replace("%title%", title)));
        }
        td.setText(String.join("\n", formatted));
    }

    public Location getPointerWorldLocation() {
        Location base = getBaseLocation();
        if (base == null) {
            return null;
        }
        float scale = (float) plugin.getConfig().getDouble("settings.scale", 1.7);
        double localZ = (-2.6 / 16.0) * scale;
        double radYaw = Math.toRadians(yaw);
        double dx = -localZ * Math.sin(radYaw);
        double dz = localZ * Math.cos(radYaw);
        return base.clone().add(dx, (25.0 / 16.0) * scale, dz);
    }

    public Location getWheelCenterWorldLocation() {
        Location base = getBaseLocation();
        if (base == null) {
            return null;
        }
        float scale = (float) plugin.getConfig().getDouble("settings.scale", 1.7);
        return base.clone().add(0.0, 1.0625 * scale, 0.0);
    }

    public void despawn() {
        for (UUID uuid : spawnedEntityIds) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
        spawnedEntityIds.clear();
        blockDisplayWheelElements.clear();
        slicePrizeIcons.clear();
        slicePrizeLabels.clear();
        resourcePackWheelEntityId = null;
        hologramEntityId = null;
        interactionEntityId = null;

        Location baseLoc = getBaseLocation();
        if (baseLoc != null && baseLoc.getWorld() != null && baseLoc.getChunk().isLoaded()) {
            cleanupOrphanEntities(baseLoc);
        }
    }

    private void cleanupOrphanEntities(Location baseLoc) {
        World world = baseLoc.getWorld();
        if (world == null) {
            return;
        }
        NamespacedKey key = plugin.getWheelEntityKey();
        for (Entity entity : world.getNearbyEntities(baseLoc, 8.0, 8.0, 8.0)) {
            String taggedId = entity.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (id.equalsIgnoreCase(taggedId)) {
                entity.remove();
            }
        }
    }

    private void tagEntity(Entity entity) {
        entity.setPersistent(false);
        entity.getPersistentDataContainer().set(plugin.getWheelEntityKey(), PersistentDataType.STRING, id);
    }

    public boolean ownsEntity(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (spawnedEntityIds.contains(entity.getUniqueId())) {
            return true;
        }
        String taggedId = entity.getPersistentDataContainer().get(plugin.getWheelEntityKey(), PersistentDataType.STRING);
        return id.equalsIgnoreCase(taggedId);
    }

    public String getId() {
        return id;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getCurrentAngleDegrees() {
        return currentAngleDegrees;
    }

    public boolean isSpinning() {
        return spinning;
    }

    public void setSpinning(boolean spinning, String spinnerName) {
        this.spinning = spinning;
        this.currentSpinnerName = spinnerName;
    }

    public String getCurrentSpinnerName() {
        return currentSpinnerName != null ? currentSpinnerName : "";
    }

    public UUID getInteractionEntityId() {
        return interactionEntityId;
    }
}
