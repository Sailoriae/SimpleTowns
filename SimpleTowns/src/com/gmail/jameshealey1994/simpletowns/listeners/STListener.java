package com.gmail.jameshealey1994.simpletowns.listeners;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.localisation.Localisation;
import com.gmail.jameshealey1994.simpletowns.localisation.LocalisationEntry;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.permissions.STPermission;
import com.gmail.jameshealey1994.simpletowns.utils.TownUtils;
import java.util.Objects;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Lectern;

/**
 * Listener class for SimpleTowns plugin.
 *
 * @author JamesHealey94 <jameshealey1994.gmail.com>
 */
public class STListener implements Listener {

    /**
     * Plugin associated with the Listener.
     */
    private final SimpleTowns plugin;

    /**
     * Constructor - Initialises associated plugin.
     *
     * @param plugin    plugin associated with the listener
     */
    public STListener(SimpleTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks the player is allowed to break the block.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_BREAK_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Checks the player is allowed to place the block.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlockPlaced();
        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_PLACE_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Checks the player is allowed to break the item in an ItemFrame, or the ItemFrame itself, or an ArmorStand.
     *
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if ( (event.getEntityType() != EntityType.ITEM_FRAME) && (event.getEntityType() != EntityType.ARMOR_STAND) ) {
            return;
        }

        final Player player;
        if (event.getDamager() instanceof Arrow) {
            final Arrow arrow = (Arrow) event.getDamager();
            if ((arrow.getShooter() != null) && (arrow.getShooter() instanceof Player)) {
                player = (Player) arrow.getShooter();
            } else {
                return;
            }
        } else if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
        } else {
            return;
        }

        final Block block = event.getEntity().getLocation().getBlock();
        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_BREAK_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Checks the player is allowed to add or remove item in an ItemFrame or an ArmorStand.
     * Note : PlayerInteractEntityEvent is for right-clicking on an entity.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {

        if ( !(event.getRightClicked() instanceof ItemFrame) && !(event.getRightClicked() instanceof ArmorStand) ) {
            return;
        }

        final Player player = (Player) event.getPlayer();
        final Block block = event.getRightClicked().getLocation().getBlock();
        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_BREAK_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Checks the player is allowed to add or remove item in an ArmorStand.
     * Note : PlayerInteractAtEntityEvent is for right-clicking on an entity.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {

        if ( !(event.getRightClicked() instanceof ArmorStand) ) {
            return;
        }

        onPlayerInteractEntityEvent( (PlayerInteractEntityEvent) event );
    }

    /**
     * Checks the player is allowed to right click on a lectern, and item frame, or an armor stand.
     * Note : PlayerInteractEvent is for right-clicking on a block.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        if ( event.getClickedBlock().getType() != Material.LECTERN &&
             event.getClickedBlock().getType() != Material.ITEM_FRAME &&
             event.getClickedBlock().getType() != Material.ARMOR_STAND ) { // This is why "api-version" is important
            return;
        }

        final Player player = (Player) event.getPlayer();
        final Block block = event.getClickedBlock();

        if ( block.getBlockData() instanceof Lectern ) {
            final Lectern lectern = (Lectern) block.getBlockData();
            
            // Allow reading a book in a lectern
            if ( lectern.hasBook() )
                return;
            
            // Allow right-clicking without a book
            if ( event.getMaterial​() != Material.WRITABLE_BOOK )  // This is also why "api-version" is important
                return;
        }

        // Allow right-clicking with nothing on an item frame
        // onPlayerInteractEntityEvent() will cancel rotation
        if ( block.getBlockData() instanceof ItemFrame )
            if ( event.getMaterial​() != Material.AIR )
                return;

        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_BREAK_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Checks the player is allowed to remove/take a book from a lectern.
     *
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTakeLecternBookEvent(PlayerTakeLecternBookEvent event) {

        final Player player = (Player) event.getPlayer();
        final Block block = event.getLectern().getLocation().getBlock();
        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_BREAK_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Checks the player is allowed to break the hanging entity.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreakEvent(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getRemover();
        final Block block = event.getEntity().getLocation().getBlock();
        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_BREAK_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Checks the player is allowed to fill the bucket.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlockClicked();
        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_BREAK_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Checks the player is allowed to empty the bucket.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlockClicked();
        if (!canBuild(player, block)) {
            final Town town = plugin.getTown(block.getChunk());
            if (town == null) {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_CANNOT_BUILD_HERE));
            } else {
                player.sendMessage(plugin.getLocalisation().get(LocalisationEntry.MSG_ONLY_TOWN_MEMBERS_CAN_PLACE_BLOCKS, town.getName()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Sends player a message when moving in / out of Town chunks.
     *
     * @param event     event being handled
     */
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Town exited = plugin.getTown(event.getFrom().getChunk());
        final Town entered = plugin.getTown(event.getTo().getChunk());
        if (!Objects.equals(exited, entered)) {
            final Player player = event.getPlayer();
            final Localisation localisation = plugin.getLocalisation();
            if (exited != null) {
                player.sendMessage(localisation.get(LocalisationEntry.MSG_EXITED_TOWN, exited.getName()));
            }
            if (entered != null) {
                player.sendMessage(localisation.get(LocalisationEntry.MSG_ENTERED_TOWN, entered.getName()));
            }
        }
    }

    /**
     * Returns if a player can break or place a block.
     * (if the chunk the block is in belongs to a town they are a member of)
     *
     * @param player        player being checked
     * @param block         block being checked
     * @return              if the player can build
     */
    private boolean canBuild(Player player, Block block) {
        if (player.hasPermission(STPermission.ADMIN.getPermission())) {
            return true;
        }

        final Town town = plugin.getTown(block.getChunk());
        if (town == null) {
            if (block.getLocation().getBlockY() <= new TownUtils(plugin).getMineRoofY()) {
                return player.hasPermission(STPermission.BUILD_MINES.getPermission());
            } else {
                return player.hasPermission(STPermission.BUILD_WILDERNESS.getPermission());
            }
        } else {
            return town.hasMember(player.getUniqueId()) && player.hasPermission(STPermission.BUILD_TOWNS.getPermission());
        }
    }
}