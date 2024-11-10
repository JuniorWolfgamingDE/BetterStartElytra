package org.funty.startelytra.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.funty.startelytra.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ElytraListener implements Listener {

    private static final ArrayList<UUID> glider = new ArrayList<>();
    private static final List<Material> allowedBlocks = List.of(
        Material.STONE, Material.GRASS_BLOCK, Material.DIRT, // Add more allowed block types here
        Material.SAND, Material.GRAVEL
    );

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.getGameMode() == GameMode.SURVIVAL && !glider.contains(uuid)) {
            Location playerLocation = player.getLocation();
            double jumpHeight = playerLocation.getY();
            Material blockBelow = playerLocation.add(0, -1, 0).getBlock().getType();

            // Check if the player is jumping from an allowed block type and at a sufficient height
            if (jumpHeight > Main.getPlugin().getConfig().getDouble("MinimumHeight") &&
                allowedBlocks.contains(blockBelow)) {

                glider.add(uuid);
                player.setGliding(true);
                player.setAllowFlight(true);
            }
        }

        // Remove flight and gliding capability when player is on solid ground
        if (!player.getLocation().add(0, -1, 0).getBlock().getType().equals(Material.AIR)) {
            glider.remove(uuid);
            if (player.getGameMode() == GameMode.SURVIVAL) {
                player.setAllowFlight(false);
            }
        }
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (glider.contains(uuid)) {
            event.setCancelled(true);
            player.setVelocity(player.getLocation().getDirection().multiply(2).add(
                new Vector(0, Main.getPlugin().getConfig().getDouble("Boost"), 0)
            ));
        }
    }

    @EventHandler
    public void onSneakItems(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (glider.contains(uuid)) {
            event.setCancelled(true);
            player.setVelocity(player.getLocation().getDirection().multiply(2).add(
                new Vector(0, Main.getPlugin().getConfig().getDouble("Boost"), 0)
            ));
        }
    }

    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.getGameMode() == GameMode.SURVIVAL && glider.contains(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGlideToggle(EntityToggleGlideEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            UUID uuid = player.getUniqueId();

            if (glider.contains(uuid)) {
                event.setCancelled(true);
            }
        }
    }
}
