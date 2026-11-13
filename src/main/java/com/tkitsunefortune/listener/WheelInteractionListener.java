package com.tkitsunefortune.listener;

import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.wheel.FortuneWheel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WheelInteractionListener implements Listener {

    private final TKitsuneFortunePlugin plugin;
    private final Map<UUID, Long> interactionDebounce = new HashMap<>();

    public WheelInteractionListener(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        handleEntityClick(event.getPlayer(), event.getRightClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        handleEntityClick(event.getPlayer(), event.getRightClicked(), event);
    }

    private void handleEntityClick(Player player, Entity clicked, PlayerInteractEntityEvent event) {
        FortuneWheel wheel = plugin.getWheelManager().getWheelByEntity(clicked);
        if (wheel == null) {
            return;
        }
        event.setCancelled(true);
        if (isDebounced(player)) {
            return;
        }
        plugin.getWheelManager().attemptSpin(player, wheel);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        FortuneWheel wheel = plugin.getWheelManager().getWheelByEntity(event.getEntity());
        if (wheel == null) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Prevent accidental vanilla placement/consumption of the Ticket item
        if (event.getItem() != null && plugin.getTicketManager().isTicket(event.getItem())) {
            event.setCancelled(true);
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            Location clickLoc = clickedBlock.getLocation().add(0.5, 1.0, 0.5);
            FortuneWheel wheel = plugin.getWheelManager().findNearestWheel(clickLoc, 1.35);
            if (wheel != null) {
                event.setCancelled(true);
                if (!isDebounced(player)) {
                    plugin.getWheelManager().attemptSpin(player, wheel);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getTicketManager().refreshPlayerTickets(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getResourcePackHostServer().sendResourcePackToPlayer(player);
            }
        }, 15L);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.getWheelManager().handleChunkLoad(event.getChunk());
    }

    private boolean isDebounced(Player player) {
        long now = System.currentTimeMillis();
        Long last = interactionDebounce.get(player.getUniqueId());
        if (last != null && (now - last) < 250L) {
            return true;
        }
        interactionDebounce.put(player.getUniqueId(), now);
        return false;
    }
}

