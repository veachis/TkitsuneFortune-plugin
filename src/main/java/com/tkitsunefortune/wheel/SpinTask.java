package com.tkitsunefortune.wheel;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SpinTask extends BukkitRunnable {

    private final TKitsuneFortunePlugin plugin;
    private final FortuneWheel wheel;
    private final Player player;
    private final String playerName;
    private final WheelSlice winningSlice;
    private final int durationTicks;
    private final float startAngle;
    private final float totalDelta;
    private final float endAngle;

    private int currentTick = 0;
    private int lastSliceIndex = -1;

    public SpinTask(
            TKitsuneFortunePlugin plugin,
            FortuneWheel wheel,
            Player player,
            WheelSlice winningSlice,
            int durationTicks,
            int minRotations,
            int maxRotations
    ) {
        this.plugin = plugin;
        this.wheel = wheel;
        this.player = player;
        this.playerName = player.getName();
        this.winningSlice = winningSlice;
        this.durationTicks = Math.max(20, durationTicks);
        this.startAngle = wheel.getCurrentAngleDegrees();

        float jitter = (ThreadLocalRandom.current().nextFloat() * 20.0f) - 10.0f; // [-10, +10] degrees within the 45-deg slice
        float currentMod = ((startAngle % 360.0f) + 360.0f) % 360.0f;
        float desiredMod = ((winningSlice.getTargetAngleDegrees() + jitter) % 360.0f + 360.0f) % 360.0f;
        float deltaToTarget = (desiredMod - currentMod + 360.0f) % 360.0f;

        int safeMax = Math.max(minRotations, maxRotations);
        int fullTurns = ThreadLocalRandom.current().nextInt(minRotations, safeMax + 1);
        this.totalDelta = (fullTurns * 360.0f) + deltaToTarget;
        this.endAngle = startAngle + totalDelta;
    }

    @Override
    public void run() {
        currentTick++;
        if (currentTick >= durationTicks) {
            finishSpin();
            cancel();
            return;
        }

        double progress = (double) currentTick / (double) durationTicks;
        // Smooth ease-out deceleration with gentle angular step per tick
        double eased = 1.0 - Math.pow(1.0 - progress, 2.65);
        float angleNow = startAngle + (float) (totalDelta * eased);

        wheel.updateWheelRotation(angleNow);

        WheelSlice currentSlice = plugin.getSliceManager().getSliceAtAngle(angleNow);
        if (currentSlice != null && currentSlice.getIndex() != lastSliceIndex) {
            lastSliceIndex = currentSlice.getIndex();
            wheel.updateHologramForSpin(playerName, currentSlice);

            Location pointerLoc = wheel.getPointerWorldLocation();
            if (pointerLoc != null && pointerLoc.getWorld() != null) {
                World world = pointerLoc.getWorld();
                float pitch = (float) (1.65 - (0.75 * progress));
                world.playSound(pointerLoc, Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 0.85f, pitch);
                world.spawnParticle(Particle.CRIT, pointerLoc, 2, 0.03, 0.03, 0.03, 0.02);
            }
        }
    }

    private void finishSpin() {
        float finalNormalized = ((endAngle % 360.0f) + 360.0f) % 360.0f;
        wheel.updateWheelRotation(finalNormalized);
        wheel.updateHologramForSpin(playerName, winningSlice);

        Location centerLoc = wheel.getWheelCenterWorldLocation();
        if (centerLoc != null && centerLoc.getWorld() != null) {
            World world = centerLoc.getWorld();
            world.spawnParticle(Particle.TOTEM_OF_UNDYING, centerLoc, 35, 0.45, 0.45, 0.45, 0.15);
            world.spawnParticle(Particle.END_ROD, centerLoc, 20, 0.4, 0.4, 0.4, 0.05);
            playConfiguredSound(world, centerLoc, winningSlice.getSound());
        }

        deliverRewards();

        // Return hologram to idle state and unlock wheel after 40 ticks (2 seconds)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            wheel.setSpinning(false, null);
            wheel.resetHologramToIdle();
        }, 40L);
    }

    private void deliverRewards() {
        String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&8[&6&lŞansÇarkı&8] &r"));

        // 1. Give physical GUI-configured reward items if present
        if (player != null && player.isOnline() && !winningSlice.getRewardItems().isEmpty()) {
            for (ItemStack rewardItem : winningSlice.getRewardItems()) {
                if (rewardItem == null || rewardItem.getType().isAir()) {
                    continue;
                }
                Map<Integer, ItemStack> leftovers = player.getInventory().addItem(rewardItem.clone());
                for (ItemStack leftover : leftovers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
        }

        // 2. Execute console commands
        for (String rawCmd : winningSlice.getCommands()) {
            if (rawCmd == null || rawCmd.isBlank()) {
                continue;
            }
            String cmd = rawCmd.replace("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        if (player != null && player.isOnline()) {
            String title = winningSlice.getTitle()
                    .replace("%player%", playerName)
                    .replace("%reward%", winningSlice.getDisplayName());
            String subtitle = winningSlice.getSubtitle()
                    .replace("%player%", playerName)
                    .replace("%reward%", winningSlice.getDisplayName());
            player.sendTitle(title, subtitle, 10, 65, 20);

            for (String msg : winningSlice.getMessages()) {
                player.sendMessage(prefix + msg
                        .replace("%player%", playerName)
                        .replace("%reward%", winningSlice.getDisplayName()));
            }
        }

        if (winningSlice.isBroadcast() && winningSlice.getBroadcastMessage() != null && !winningSlice.getBroadcastMessage().isEmpty()) {
            String bcast = winningSlice.getBroadcastMessage()
                    .replace("%player%", playerName)
                    .replace("%reward%", winningSlice.getDisplayName());
            Bukkit.broadcastMessage(bcast);
        }
    }

    private void playConfiguredSound(World world, Location loc, String soundName) {
        if (soundName == null || soundName.isEmpty()) {
            world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            return;
        }
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
            world.playSound(loc, sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
            world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
}

