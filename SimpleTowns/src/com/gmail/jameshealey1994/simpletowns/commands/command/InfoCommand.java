package com.gmail.jameshealey1994.simpletowns.commands.command;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.localisation.Localisation;
import com.gmail.jameshealey1994.simpletowns.localisation.LocalisationEntry;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.permissions.STPermission;
import com.gmail.jameshealey1994.simpletowns.utils.PlayernameUUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Class representing an Info command.
 * Allows you to view information about a town
 * /... info            Display information about current town
 * /... info <town>     Display information about a town
 *
 * @author JamesHealey94 <jameshealey1994.gmail.com>
 */
public class InfoCommand extends STCommand {

    /**
     * Constructor to add aliases and permissions.
     */
    public InfoCommand() {
        this.aliases.add("info");
        this.aliases.add("information");

        this.permissions.add(STPermission.INFO.getPermission());
    }

    @Override
    public boolean execute(SimpleTowns plugin, CommandSender sender, String commandLabel, String[] args) {
        final Localisation localisation = plugin.getLocalisation();

        final Town town;

        if (args.length == 0) {
            if (sender instanceof Player) {
                final Player player = (Player) sender;
                town = plugin.getTown(player.getLocation().getChunk());

                if (town == null) {
                    sender.sendMessage(localisation.get(LocalisationEntry.ERR_SPECIFY_TOWN_NAME));
                    return true;
                }
            } else {
                sender.sendMessage(localisation.get(LocalisationEntry.ERR_SPECIFY_TOWN_NAME));
                return true;
            }
        } else {
            town = plugin.getTown(args[0]);
        }

        if (town == null) {
            sender.sendMessage(localisation.get(LocalisationEntry.ERR_TOWN_NOT_FOUND, args[0]));
            return true;
        }

        // Display town information
        // TODO paginate - see ChatPaginator bukkit utility class
        sender.sendMessage(localisation.get(LocalisationEntry.INFO_HEADER, town.getName()));
        String playername;
        if (!(town.getLeaders().isEmpty())) {
            StringJoiner toSend = new StringJoiner(", ");
            ArrayList<String> ghosts = new ArrayList<>(); // Display at the end players whose UUID couldn't be converted to names
            for (UUID uuid : town.getLeaders()) {
                playername = PlayernameUUID.getPlayerName(uuid);
                if (playername != null) {
                    toSend.add(playername);
                } else 
                    ghosts.add( uuid.toString() );
            }
            sender.sendMessage(localisation.get(LocalisationEntry.INFO_TOWN_LEADERS_HEADER) + " " + toSend.toString());
            for (String uuid : ghosts)
                sender.sendMessage(localisation.get(LocalisationEntry.INFO_TOWN_LEADERS_ENTRY, ghosts));
        }
        if (!(town.getCitizens().isEmpty())) {
            StringJoiner toSend = new StringJoiner(", ");
            ArrayList<String> ghosts = new ArrayList<>(); // Display at the end players whose UUID couldn't be converted to names
            for (UUID uuid : town.getCitizens()) {
                playername = PlayernameUUID.getPlayerName(uuid);
                if (playername != null) {
                    toSend.add(playername);
                } else 
                    ghosts.add( uuid.toString() );
            }
            sender.sendMessage(localisation.get(LocalisationEntry.INFO_TOWN_CITIZENS_HEADER) + " " + toSend.toString());
            for (String uuid : ghosts)
                sender.sendMessage(localisation.get(LocalisationEntry.INFO_TOWN_CITIZENS_ENTRY, ghosts));
        }
        sender.sendMessage(localisation.get(LocalisationEntry.INFO_TOWN_CHUNKS, town.getTownChunks().size()));
        sender.sendMessage(localisation.get(LocalisationEntry.INFO_FOOTER));
        return true;
    }

    @Override
    public String getDescription(Localisation localisation) {
        return localisation.get(LocalisationEntry.DESCRIPTION_INFO);
    }
}