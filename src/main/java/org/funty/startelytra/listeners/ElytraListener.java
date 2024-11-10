package org.funty.startelytra.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.funty.startelytra.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ElytraListener implements Listener {

    private static final List<UUID> glider = new ArrayList<>();
    private static final List<Material> allowedLaunchBlocks = List.of(Material.STONE, Material.GRASS_BLOCK, Material.DIRT); // List of blocks to start gliding from

    private final ItemStack Elytra = new ItemStack(Material.ELYTRA);
    private final ItemMeta ElytraMeta = this.Elytra.getItemMeta();

    public ElytraListener() {
        // Initialize Elytra metadata if necessary
        ElytraMeta.setDisplayName(ChatColor.GOLD + "Special Elytra");
        ElytraMeta.setUnbreakable(true);
        ElytraMeta.addEnchant(Enchantment.DURABILITY, 1000, true);
        ElytraMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        Elytra.setItemMeta(ElytraMeta);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Location playerLocation = player.getLocation();
        Material blockBelow = playerLocation.add(0, -1, 0).getBlock().getType();

        if (allowedLaunchBlocks.contains(blockBelow) && playerLocation.getY() > 100) { // Check block type and height
            if (!glider.contains(uuid)) {
                glider.add(uuid);
                player.setGliding(true);
                player.setAllowFlight(true);
            }
        } else {
            // Stop gliding if not on an allowed block or below the height threshold
            glider.remove(uuid);
            player.setAllowFlight(false);
            if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().equals(Elytra)) {
                player.getInventory().setChestplate(null);
            }
        }
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (glider.contains(uuid)) {
            event.setCancelled(true);
            Vector velocity = player.getLocation().getDirection().multiply(2)
                    .add(new Vector(0, Double.parseDouble(Main.getPlugin().getConfig().getString("Boost")), 0));
            player.setVelocity(velocity);
        }
    }

    @EventHandler
    public void onSneakItems(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (glider.contains(uuid)) {
            event.setCancelled(true);
            Vector velocity = player.getLocation().getDirection().multiply(2)
                    .add(new Vector(0, Double.parseDouble(Main.getPlugin().getConfig().getString("Boost")), 0));
            player.setVelocity(velocity);
        }
    }

    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL && glider.contains(uuid)) {
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
