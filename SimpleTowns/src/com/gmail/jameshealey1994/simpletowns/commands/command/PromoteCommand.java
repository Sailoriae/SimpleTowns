package com.gmail.jameshealey1994.simpletowns.commands.command;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
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
 * Class representing a Promote command.
 * /... promote <user>              Promote <username> to a town leader in current town
 * /... promote <user> <town>       Promote <username> to a town leader in [town]
 *
 * @author JamesHealey94 <jameshealey1994.gmail.com>
 */
public class PromoteCommand extends STCommand {

    /**
     * Constructor to add aliases and permissions.
     */
    public PromoteCommand() {
        this.aliases.add("promote");
        this.aliases.add("promoteplayer");
        this.aliases.add("promotemember");
        this.aliases.add("promotecitizen");
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

        // Get player's full name
        final Player player = plugin.getServer().getPlayer(playername);
        String fullPlayerName;
        if (player == null) {
            fullPlayerName = playername;
        } else {
            fullPlayerName = player.getName();
        }

        // Validate playername UUID
        UUID playerUUID = PlayernameUUID.getPlayerUUID( fullPlayerName );
        if (playerUUID == null) {
            sender.sendMessage(localisation.get(LocalisationEntry.ERR_CANNOT_FIND_PLAYER_UUID, fullPlayerName));
            return true;
        }

        // Check player is a member of the town
        if (!town.hasMember(playerUUID)) {
            sender.sendMessage(localisation.get(LocalisationEntry.ERR_PLAYER_NOT_MEMBER, fullPlayerName, town.getName()));
            return true;
        }

        // Check player isn't already a leader of town (cannot be promoted)
        if (town.getLeaders().contains(fullPlayerName)) {
            sender.sendMessage(localisation.get(LocalisationEntry.ERR_PLAYER_ALREADY_LEADER, fullPlayerName, town.getName()));
            return true;
        }

        // Promote player from citizen to leader in town
        // (remove from citizens, add to leaders)
        final String basePath = "Towns." + town.getName();
        final String citizensPath = basePath + ".Citizens";
        final List<String> citizens = plugin.getConfig().getStringList(citizensPath);
        citizens.remove(playerUUID.toString());
        plugin.getConfig().set(citizensPath, citizens);
        plugin.getTown(town.getName()).getCitizens().remove(playerUUID);

        final String leadersPath = basePath + ".Leaders";
        final List<String> leaders = plugin.getConfig().getStringList(leadersPath);
        leaders.add(playerUUID.toString());
        plugin.getConfig().set(leadersPath, leaders);
        plugin.getTown(town.getName()).getLeaders().add(playerUUID);

        // Log to file
        new Logger(plugin).log(localisation.get(LocalisationEntry.LOG_CITIZEN_PROMOTED, town.getName(), sender.getName(), fullPlayerName));

        // Save config
        plugin.saveConfig();

        // Send confimation message to sender
        sender.sendMessage(localisation.get(LocalisationEntry.MSG_CITIZEN_PROMOTED, town.getName(), fullPlayerName));

        // Send message to citizen, if they're online
        final Player citizen = plugin.getServer().getPlayer(fullPlayerName);
        if (citizen != null) {
            citizen.sendMessage(localisation.get(LocalisationEntry.MSG_PROMOTED, town.getName(), sender.getName()));
        }
        return true;
    }

    @Override
    public String getDescription(Localisation localisation) {
        return localisation.get(LocalisationEntry.DESCRIPTION_PROMOTE);
    }
}