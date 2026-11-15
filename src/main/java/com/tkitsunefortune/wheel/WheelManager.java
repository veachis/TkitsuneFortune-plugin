package com.tkitsunefortune.wheel;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.util.ColorUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WheelManager {

    private final TKitsuneFortunePlugin plugin;
    private final File wheelsFile;
    private final Map<String, FortuneWheel> wheels = new LinkedHashMap<>();

    public WheelManager(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
        this.wheelsFile = new File(plugin.getDataFolder(), "wheels.yml");
    }

    public void loadWheels() {
        despawnAll();
        wheels.clear();

        if (!wheelsFile.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(wheelsFile);
        ConfigurationSection section = yaml.getConfigurationSection("wheels");
        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection wSec = section.getConfigurationSection(id);
            if (wSec == null) {
                continue;
            }
            String worldName = wSec.getString("world", "world");
            double x = wSec.getDouble("x");
            double y = wSec.getDouble("y");
            double z = wSec.getDouble("z");
            float yaw = (float) wSec.getDouble("yaw", 0.0);

            FortuneWheel wheel = new FortuneWheel(plugin, id, worldName, x, y, z, yaw);
            wheels.put(id.toLowerCase(), wheel);
            wheel.spawn();
        }
    }

    public void saveWheels() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection section = yaml.createSection("wheels");

        for (FortuneWheel wheel : wheels.values()) {
            ConfigurationSection wSec = section.createSection(wheel.getId());
            wSec.set("world", wheel.getWorldName());
            wSec.set("x", wheel.getX());
            wSec.set("y", wheel.getY());
            wSec.set("z", wheel.getZ());
            wSec.set("yaw", wheel.getYaw());
        }

        try {
            yaml.save(wheelsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save wheels.yml: " + e.getMessage());
        }
    }

    public FortuneWheel createWheel(String id, Location baseLoc, float yaw) {
        String key = id.toLowerCase();
        if (wheels.containsKey(key)) {
            wheels.get(key).despawn();
        }
        FortuneWheel wheel = new FortuneWheel(
                plugin,
                id,
                baseLoc.getWorld().getName(),
                baseLoc.getX(),
                baseLoc.getY(),
                baseLoc.getZ(),
                yaw
        );
        wheels.put(key, wheel);
        wheel.spawn();
        saveWheels();
        return wheel;
    }

    public boolean removeWheel(String id) {
        FortuneWheel wheel = wheels.remove(id.toLowerCase());
        if (wheel == null) {
            return false;
        }
        wheel.despawn();
        saveWheels();
        return true;
    }

    public FortuneWheel findNearestWheel(Location loc, double maxDistance) {
        if (loc == null || loc.getWorld() == null) {
            return null;
        }
        FortuneWheel nearest = null;
        double bestDistSq = maxDistance * maxDistance;
        for (FortuneWheel wheel : wheels.values()) {
            Location wLoc = wheel.getBaseLocation();
            if (wLoc == null || !loc.getWorld().equals(wLoc.getWorld())) {
                continue;
            }
            double dSq = loc.distanceSquared(wLoc);
            if (dSq <= bestDistSq) {
                bestDistSq = dSq;
                nearest = wheel;
            }
        }
        return nearest;
    }

    public FortuneWheel getWheelByEntity(Entity entity) {
        if (entity == null) {
            return null;
        }
        for (FortuneWheel wheel : wheels.values()) {
            if (wheel.ownsEntity(entity)) {
                return wheel;
            }
        }
        return null;
    }

    public void respawnAll() {
        for (FortuneWheel wheel : wheels.values()) {
            wheel.spawn();
        }
    }

    public void despawnAll() {
        for (FortuneWheel wheel : wheels.values()) {
            wheel.despawn();
        }
    }

    public void handleChunkLoad(Chunk chunk) {
        for (FortuneWheel wheel : wheels.values()) {
            if (!wheel.getWorldName().equals(chunk.getWorld().getName())) {
                continue;
            }
            int cx = ((int) Math.floor(wheel.getX())) >> 4;
            int cz = ((int) Math.floor(wheel.getZ())) >> 4;
            if (chunk.getX() == cx && chunk.getZ() == cz) {
                wheel.spawn();
            }
        }
    }

    public void attemptSpin(Player player, FortuneWheel wheel) {
        String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));

        if (!player.hasPermission("tkitsunefortune.use")) {
            player.sendMessage(prefix + ColorUtil.colorize(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (wheel.isSpinning()) {
            String msg = plugin.getConfig().getString("messages.wheel-busy", "&cBu Şans Çarkı şu anda dönüyor!")
                    .replace("%spinner%", wheel.getCurrentSpinnerName());
            player.sendMessage(prefix + ColorUtil.colorize(msg));
            playPlayerSound(player, plugin.getConfig().getString("settings.sounds.error", "BLOCK_NOTE_BLOCK_BASS"), 0.8f, 0.7f);
            return;
        }

        boolean requireInHand = plugin.getConfig().getBoolean("settings.require-ticket-in-hand", true);
        if (!plugin.getTicketManager().consumeTicket(player, requireInHand)) {
            String key = requireInHand ? "messages.no-ticket-in-hand" : "messages.no-ticket";
            player.sendMessage(prefix + ColorUtil.colorize(plugin.getConfig().getString(key)));
            playPlayerSound(player, plugin.getConfig().getString("settings.sounds.error", "BLOCK_NOTE_BLOCK_BASS"), 0.8f, 0.7f);
            return;
        }

        WheelSlice winningSlice = plugin.getSliceManager().pickWeightedRandomSlice();
        if (winningSlice == null) {
            return;
        }

        wheel.setSpinning(true, player.getName());
        player.sendMessage(prefix + ColorUtil.colorize(plugin.getConfig().getString("messages.spin-started")));
        playPlayerSound(player, plugin.getConfig().getString("settings.sounds.spin-start", "BLOCK_AMETHYST_BLOCK_CHIME"), 1.0f, 1.3f);

        int durationTicks = plugin.getConfig().getInt("settings.spin-duration-ticks", 120);
        int minRot = plugin.getConfig().getInt("settings.min-full-rotations", 5);
        int maxRot = plugin.getConfig().getInt("settings.max-full-rotations", 8);

        playTicketInsertAnimationAndStartSpin(player, wheel, winningSlice, durationTicks, minRot, maxRot);
    }

    private void playTicketInsertAnimationAndStartSpin(
            Player player,
            FortuneWheel wheel,
            WheelSlice winningSlice,
            int durationTicks,
            int minRot,
            int maxRot
    ) {
        Location centerLoc = wheel.getWheelCenterWorldLocation();
        if (centerLoc == null || centerLoc.getWorld() == null) {
            SpinTask task = new SpinTask(plugin, wheel, player, winningSlice, durationTicks, minRot, maxRot);
            task.runTaskTimer(plugin, 1L, 1L);
            return;
        }

        World world = centerLoc.getWorld();
        float scale = (float) plugin.getConfig().getDouble("settings.scale", 1.0);
        double radYaw = Math.toRadians(wheel.getYaw());
        // Spawn the floating 3D ticket slightly in front of the wheel (-Z in local space)
        double frontOffset = -0.65 * scale;
        double startDx = -frontOffset * Math.sin(radYaw);
        double startDz = frontOffset * Math.cos(radYaw);
        Location startLoc = centerLoc.clone().add(startDx, -0.15 * scale, startDz);

        ItemDisplay ticketDisplay = world.spawn(startLoc, ItemDisplay.class, display -> {
            display.setPersistent(false);
            display.setRotation(wheel.getYaw(), 0.0f);
            display.setItemStack(plugin.getTicketManager().createTicket(1));
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(1);
            display.setTransformation(new Transformation(
                    new Vector3f(0.0f, 0.0f, 0.0f),
                    new Quaternionf(),
                    new Vector3f(0.65f * scale, 0.65f * scale, 0.65f * scale),
                    new Quaternionf()
            ));
        });

        new BukkitRunnable() {
            private int tick = 0;
            private final int totalInsertTicks = 12;

            @Override
            public void run() {
                tick++;
                if (tick >= totalInsertTicks || !ticketDisplay.isValid()) {
                    if (ticketDisplay.isValid()) {
                        ticketDisplay.remove();
                    }
                    world.spawnParticle(Particle.END_ROD, centerLoc, 15, 0.25, 0.25, 0.25, 0.06);
                    if (plugin.getConfig().getBoolean("settings.sounds.enabled", true)) {
                        String startSnd = plugin.getConfig().getString("settings.sounds.spin-start", "BLOCK_BEACON_ACTIVATE");
                        playWorldSound(world, centerLoc, startSnd, 0.9f, 1.45f);
                    }
                    SpinTask task = new SpinTask(plugin, wheel, player, winningSlice, durationTicks, minRot, maxRot);
                    task.runTaskTimer(plugin, 1L, 1L);
                    cancel();
                    return;
                }

                float progress = (float) tick / (float) totalInsertTicks;
                float s = (0.65f * (1.0f - 0.55f * progress)) * scale;
                float zMove = (0.45f * progress) * scale;
                float yMove = (0.15f * progress) * scale;
                Quaternionf spinRot = new Quaternionf().rotateY((float) Math.toRadians(progress * 360.0f));

                ticketDisplay.setInterpolationDelay(0);
                ticketDisplay.setInterpolationDuration(1);
                ticketDisplay.setTransformation(new Transformation(
                        new Vector3f(0.0f, yMove, zMove),
                        spinRot,
                        new Vector3f(s, s, s),
                        new Quaternionf()
                ));
                world.spawnParticle(Particle.WAX_ON, ticketDisplay.getLocation().add(0, 0.1, 0), 2, 0.1, 0.1, 0.1, 0.01);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public String getHologramTitle() {
        String configured = plugin.getConfig().getString("settings.hologram.title");
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        List<String> idleLines = plugin.getConfig().getStringList("settings.hologram.idle-lines");
        if (!idleLines.isEmpty() && !idleLines.get(0).equals("%title%")) {
            return idleLines.get(0);
        }
        return plugin.tr("&6&l✦ &e&lTkitsune Şans Çarkı &6&l✦", "&6&l✦ &e&lTkitsune Wheel of Fortune &6&l✦");
    }

    public void setHologramTitle(String rawInput) {
        String trimmed = rawInput == null ? "" : rawInput.trim();
        if (trimmed.isEmpty()) {
            trimmed = plugin.tr("&6&l✦ &e&lTkitsune Şans Çarkı &6&l✦", "&6&l✦ &e&lTkitsune Wheel of Fortune &6&l✦");
        } else if (!trimmed.contains("&") && !trimmed.contains("§")) {
            trimmed = "&6&l✦ &e&l" + trimmed + " &6&l✦";
        }

        plugin.getConfig().set("settings.hologram.title", trimmed);

        List<String> idleLines = new ArrayList<>(plugin.getConfig().getStringList("settings.hologram.idle-lines"));
        if (idleLines.isEmpty()) {
            idleLines.add("%title%");
            idleLines.add(plugin.tr(
                    "&7Elindeki &e&lÖzel Şans Çarkı Bileti &7ile &aSağ Tıkla!",
                    "&7Right-Click with your &e&lFortune Wheel Ticket &7in hand!"
            ));
        } else if (!idleLines.get(0).equals("%title%")) {
            idleLines.set(0, "%title%");
        }
        plugin.getConfig().set("settings.hologram.idle-lines", idleLines);

        List<String> spinLines = new ArrayList<>(plugin.getConfig().getStringList("settings.hologram.spinning-lines"));
        if (spinLines.isEmpty()) {
            spinLines.add("%title%");
            spinLines.add(plugin.tr("&7Çeviren: &b%player%", "&7Spinner: &b%player%"));
            spinLines.add("&e▸ &f%slice% &e◂");
        } else if (!spinLines.get(0).equals("%title%")) {
            spinLines.set(0, "%title%");
        }
        plugin.getConfig().set("settings.hologram.spinning-lines", spinLines);

        plugin.saveConfig();
        respawnAll();
    }

    public String generateNextWheelId() {
        int i = 1;
        while (wheels.containsKey("cark" + i)) {
            i++;
        }
        return "cark" + i;
    }

    public Collection<FortuneWheel> getWheels() {
        return Collections.unmodifiableCollection(wheels.values());
    }

    public void playPlayerSound(Player player, String soundName, float baseVol, float pitch) {
        if (player == null || !plugin.getConfig().getBoolean("settings.sounds.enabled", true)) {
            return;
        }
        if (soundName == null || soundName.isBlank() || soundName.equalsIgnoreCase("NONE")) {
            return;
        }
        float volMult = (float) plugin.getConfig().getDouble("settings.sounds.volume", 1.0);
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, Math.max(0.0f, baseVol * volMult), pitch);
        } catch (IllegalArgumentException ignored) {
            try {
                player.playSound(player.getLocation(), soundName.toLowerCase(), Math.max(0.0f, baseVol * volMult), pitch);
            } catch (Throwable ignored2) {
            }
        }
    }

    public void playWorldSound(World world, Location loc, String soundName, float baseVol, float pitch) {
        if (world == null || loc == null || !plugin.getConfig().getBoolean("settings.sounds.enabled", true)) {
            return;
        }
        if (soundName == null || soundName.isBlank() || soundName.equalsIgnoreCase("NONE")) {
            return;
        }
        float volMult = (float) plugin.getConfig().getDouble("settings.sounds.volume", 1.0);
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            world.playSound(loc, sound, Math.max(0.0f, baseVol * volMult), pitch);
        } catch (IllegalArgumentException ignored) {
            try {
                world.playSound(loc, soundName.toLowerCase(), Math.max(0.0f, baseVol * volMult), pitch);
            } catch (Throwable ignored2) {
            }
        }
    }
}

