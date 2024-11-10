package org.funty.startelytra.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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

    private final ItemStack elytra = new ItemStack(Material.ELYTRA);
    private final ItemMeta elytraMeta = this.elytra.getItemMeta();
    private static final ArrayList<UUID> glider = new ArrayList<>();

    public ElytraListener() {
        if (elytraMeta != null) {
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
        Location centerLocation = new Location(
            player.getWorld(),
            Double.parseDouble(Main.getPlugin().getConfig().getString("Center.X")),
            Double.parseDouble(Main.getPlugin().getConfig().getString("Center.Y")),
            Double.parseDouble(Main.getPlugin().getConfig().getString("Center.Z"))
        );
        int radiusSquared = Integer.parseInt(Main.getPlugin().getConfig().getString("Radius"));
        radiusSquared *= radiusSquared;

        // Check if the player is within the defined area and is in survival mode
        if (centerLocation.distanceSquared(player.getLocation()) <= radiusSquared && player.getGameMode() == GameMode.SURVIVAL) {
            // Check if the player is falling down (negative Y velocity)
            if (player.getVelocity().getY() < -0.5) {
                Material belowBlockType = player.getLocation().add(0, -1, 0).getBlock().getType();

                // Ensure the block below is solid (indicating the player jumped from a surface)
                if (belowBlockType != Material.AIR && !glider.contains(uuid)) {
                    glider.add(uuid);
                    if (player.getName().startsWith(Main.getPlugin().getConfig().getString("Geysermc.Prefix"))) {
                        if (player.getInventory().getChestplate() == null) {
                            String displayName = Main.getPlugin().getConfig().getString("Geysermc.Elytra.DisplayName");
                            String lore = Main.getPlugin().getConfig().getString("Geysermc.Elytra.Lore");
                            elytraMeta.setDisplayName(displayName);
                            elytraMeta.setLore(Collections.singletonList(lore));
                            elytra.setItemMeta(elytraMeta);
                            player.getInventory().setChestplate(elytra);
                        } else {
                            player.sendMessage(Main.getPlugin().getConfig().getString("Geysermc.Messages.ChestOccupied"));
                        }
                    }
                    player.setGliding(true);
                    player.setAllowFlight(true);
                }
            }
        } else {
            // Reset the player's ability to fly and remove their elytra if they leave the zone or land
            glider.remove(uuid);
            if (player.getGameMode() == GameMode.SURVIVAL) {
                player.setAllowFlight(false);
                if (player.getName().startsWith(Main.getPlugin().getConfig().getString("Geysermc.Prefix"))) {
                    ItemStack chestplate = player.getInventory().getChestplate();
                    if (chestplate != null && chestplate.equals(elytra)) {
                        player.getInventory().setChestplate(null);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (glider.contains(player.getUniqueId())) {
            event.setCancelled(true);
            Vector velocity = player.getLocation().getDirection().multiply(2).add(new Vector(0, Double.parseDouble(Main.getPlugin().getConfig().getString("Boost")), 0));
            player.setVelocity(velocity);
        }
    }

    @EventHandler
    public void onSneakItems(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (glider.contains(player.getUniqueId()) && player.getName().startsWith(Main.getPlugin().getConfig().getString("Geysermc.Prefix"))) {
            event.setCancelled(true);
            Vector velocity = player.getLocation().getDirection().multiply(2).add(new Vector(0, Double.parseDouble(Main.getPlugin().getConfig().getString("Boost")), 0));
            player.setVelocity(velocity);
        }
    }

    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL && glider.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGlideToggle(EntityToggleGlideEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            if (glider.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
