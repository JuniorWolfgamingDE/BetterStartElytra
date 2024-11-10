package org.funty.startelytra.listeners;

import org.bukkit.ChatColor;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ElytraListener implements Listener {

    private final ItemStack elytra;
    private static final Set<UUID> glider = new HashSet<>();

    public ElytraListener() {
        // Initialize the Elytra item with custom properties
        elytra = new ItemStack(Material.ELYTRA);
        ItemMeta elytraMeta = elytra.getItemMeta();
        if (elytraMeta != null) {
            elytraMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Main.getPlugin().getConfig().getString("Geysermc.Elytra.DisplayName")));
            elytraMeta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', Main.getPlugin().getConfig().getString("Geysermc.Elytra.Lore"))));
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
        Location center = new Location(
                player.getWorld(),
                Double.parseDouble(Main.getPlugin().getConfig().getString("Center.X")),
                Double.parseDouble(Main.getPlugin().getConfig().getString("Center.Y")),
                Double.parseDouble(Main.getPlugin().getConfig().getString("Center.Z"))
        );
        int radiusSquared = Integer.parseInt(Main.getPlugin().getConfig().getString("Radius"));
        radiusSquared *= radiusSquared;

        // Check if player is within the specified area
        if (center.distanceSquared(player.getLocation()) <= radiusSquared && player.getGameMode() == GameMode.SURVIVAL) {
            if (isAirOrPassableBelow(player)) {
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
            removeGliderAndResetFlight(player);
        }
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (glider.contains(uuid)) {
            event.setCancelled(true);
            Vector boost = player.getLocation().getDirection().multiply(2).add(new Vector(0, Double.parseDouble(Main.getPlugin().getConfig().getString("Boost")), 0));
            player.setVelocity(boost);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (glider.contains(uuid) && player.getName().startsWith(Main.getPlugin().getConfig().getString("Geysermc.Prefix"))) {
            event.setCancelled(true);
            Vector boost = player.getLocation().getDirection().multiply(2).add(new Vector(0, Double.parseDouble(Main.getPlugin().getConfig().getString("Boost")), 0));
            player.setVelocity(boost);
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

    private boolean isAirOrPassableBelow(Player player) {
        Material blockTypeBelow = player.getLocation().add(0, -1, 0).getBlock().getType();
        return blockTypeBelow == Material.AIR || blockTypeBelow.isTransparent();
    }

    private void removeGliderAndResetFlight(Player player) {
        UUID uuid = player.getUniqueId();
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
