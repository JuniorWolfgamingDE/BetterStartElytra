package org.funty.startelytra.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
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
import java.util.UUID;

public class ElytraListener implements Listener {

    private final ItemStack elytra;
    private final ArrayList<UUID> glider = new ArrayList<>();

    public ElytraListener() {
        elytra = new ItemStack(Material.ELYTRA);
        ItemMeta elytraMeta = elytra.getItemMeta();
        if (elytraMeta != null) {
            elytraMeta.setDisplayName(ChatColor.RESET + "Special Elytra");
            elytraMeta.setUnbreakable(true);
            elytraMeta.addEnchant(Enchantment.DURABILITY, 1000, true);
            elytraMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            elytraMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            elytraMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            elytra.setItemMeta(elytraMeta);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location location = event.getPlayer().getWorld().getSpawnLocation();
        location.setX(Double.parseDouble(Main.getPlugin().getConfig().getString("Center.X")));
        location.setY(Double.parseDouble(Main.getPlugin().getConfig().getString("Center.Y")));
        location.setZ(Double.parseDouble(Main.getPlugin().getConfig().getString("Center.Z")));
        int radiusSquared = (int) Math.pow(Integer.parseInt(Main.getPlugin().getConfig().getString("Radius")), 2);

        if (location.distanceSquared(player.getLocation()) <= radiusSquared && player.getGameMode() == GameMode.SURVIVAL) {
            Block blockBelow = player.getLocation().add(0, -3, 0).getBlock();
            if (blockBelow.getType().isSolid() && !blockBelow.getType().name().contains("SLAB") && !blockBelow.getType().name().contains("STAIRS")) {
                if (!glider.contains(uuid)) {
                    glider.add(uuid);
                    if (player.getName().startsWith(Main.getPlugin().getConfig().getString("Geysermc.Prefix"))) {
                        if (player.getInventory().getChestplate() == null) {
                            player.getInventory().setChestplate(elytra);
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.getPlugin().getConfig().getString("Geysermc.Messages.ChestOccupied")));
                        }
                    }
                    player.setGliding(true);
                    player.setAllowFlight(true);
                }
            }
        } else {
            glider.remove(uuid);
            if (player.getGameMode() == GameMode.SURVIVAL) {
                player.setAllowFlight(false);
            }
            if (player.getName().startsWith(Main.getPlugin().getConfig().getString("Geysermc.Prefix"))) {
                if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().equals(elytra)) {
                    player.getInventory().setChestplate(null);
                }
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

        if (player.getName().startsWith(Main.getPlugin().getConfig().getString("Geysermc.Prefix"))) {
            if (glider.contains(uuid)) {
                event.setCancelled(true);
                Vector velocity = player.getLocation().getDirection().multiply(2)
                        .add(new Vector(0, Double.parseDouble(Main.getPlugin().getConfig().getString("Boost")), 0));
                player.setVelocity(velocity);
            }
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
