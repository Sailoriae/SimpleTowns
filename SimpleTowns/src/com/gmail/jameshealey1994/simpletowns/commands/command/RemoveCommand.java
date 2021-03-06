package com.gmail.jameshealey1994.simpletowns.commands.command;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.events.TownRemoveEvent;
import com.gmail.jameshealey1994.simpletowns.localisation.Localisation;
import com.gmail.jameshealey1994.simpletowns.localisation.LocalisationEntry;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.permissions.STPermission;
import com.gmail.jameshealey1994.simpletowns.utils.Logger;
import com.gmail.jameshealey1994.simpletowns.utils.PlayernameUUID;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class representing an Add command.
 * /... remove <user>               Claims current chunk for sender's current town
 * /... remove <user> <town>        Claims current chunk for <town>
 *
 * @author JamesHealey94 <jameshealey1994.gmail.com>
 */
public class RemoveCommand extends STCommand {

    /**
     * Constructor to add aliases and permissions.
     */
    public RemoveCommand() {
        this.aliases.add("remove");
        this.aliases.add("removeplayer");
        this.aliases.add("removemember");
    }

    @Override
    public boolean execute(SimpleTowns plugin, CommandSender sender, String commandLabel, String[] args) {
        final Localisation localisation = plugin.getLocalisation();

        final String playername;
        final String townname;

        switch (args.length) {
            case 0: {
                sender.sendMessage(localisation.get(LocalisationEntry.ERR_SPECIFY_PLAYER));
                return false;
            }
            case 1: {
                // ... username
                playername = args[0];

                if (sender instanceof Player) {
                    final Player player = (Player) sender;
                    final Town town = plugin.getTown(player.getLocation().getChunk());

                    if (town == null) {
                        sender.sendMessage(localisation.get(LocalisationEntry.ERR_SPECIFY_TOWN_NAME));
                        return false;
                    }

                    townname = town.getName();
                } else {
                    sender.sendMessage(localisation.get(LocalisationEntry.ERR_SPECIFY_TOWN_NAME));
                    return false;
                }
                break;
            }
            case 2: {
                // ... username townname
                playername = args[0];
                townname = args[1];
                break;
            }
            default: {
                return false;
            }
        }

        final Town town = plugin.getTown(townname);

        // Check town exists
        if (town == null) {
            sender.sendMessage(localisation.get(LocalisationEntry.ERR_TOWN_NOT_FOUND, townname));
            return true;
        }

        // Check sender is a leader of that town.
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!town.getLeaders().contains(player.getUniqueId()) && !sender.hasPermission(STPermission.ADMIN.getPermission())) {
                sender.sendMessage(localisation.get(LocalisationEntry.ERR_NOT_LEADER, town.getName()));
                return true;
            }
        }

        // Validate playername UUID
        UUID playerUUID;
        try {
            playerUUID = UUID.fromString( playername ); // Can give UUID instead of playername in order to remove "ghosts" members
        } catch (IllegalArgumentException e) {
            playerUUID = PlayernameUUID.getPlayerUUID( playername );
            if (playerUUID == null) {
                sender.sendMessage(localisation.get(LocalisationEntry.ERR_CANNOT_FIND_PLAYER_UUID, playername));
                return true;
            }
        }

        // Check player is a member of town (citizen or leader)
        if (!town.hasMember(playerUUID)) {
            sender.sendMessage(localisation.get(LocalisationEntry.ERR_PLAYER_NOT_MEMBER, playername, town.getName()));
            return true;
        }

        //Create and call event
        final TownRemoveEvent event = new TownRemoveEvent(town, sender, playername, playerUUID);
        plugin.getServer().getPluginManager().callEvent(event);

        // Check event has not been cancelled by event listeners
        if (event.isCancelled()) {
            return true;
        }

        // Remove member from town if citizen
        final String citizensPath = "Towns." + town.getName() + ".Citizens";
        final List<String> citizens = plugin.getConfig().getStringList(citizensPath);
        citizens.remove(playerUUID.toString());
        plugin.getConfig().set(citizensPath, citizens);
        plugin.getTown(town.getName()).getCitizens().remove(playerUUID);

        // Remove member from town if leader
        final String leadersPath = "Towns." + town.getName() + ".Leaders";
        final List<String> leaders = plugin.getConfig().getStringList(leadersPath);
        leaders.remove(playerUUID.toString());
        plugin.getConfig().set(leadersPath, leaders);
        plugin.getTown(town.getName()).getLeaders().remove(playerUUID);

        // Remove citizen from WorldGuard regions
        plugin.getWorldGuardHook().removeMemberFromRegions(town, playerUUID);

        // Log to file
        new Logger(plugin).log(localisation.get(LocalisationEntry.LOG_TOWN_MEMBER_REMOVED, town.getName(), sender.getName(), playername));

        // Save config
        plugin.saveConfig();

        // Send confimation message to sender
        sender.sendMessage(localisation.get(LocalisationEntry.MSG_MEMBER_REMOVED, town.getName(), playername));

        // Send message to removed player, if they're online
        final Player removed = plugin.getServer().getPlayerExact(playername);
        if (removed != null) {
            removed.sendMessage(localisation.get(LocalisationEntry.MSG_REMOVED_MEMBER, town.getName(), sender.getName()));
        }
        return true;
    }

    @Override
    public String getDescription(Localisation localisation) {
        return localisation.get(LocalisationEntry.DESCRIPTION_REMOVE);
    }
}