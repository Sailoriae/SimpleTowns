package com.gmail.jameshealey1994.simpletowns.commands.command;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.localisation.Localisation;
import com.gmail.jameshealey1994.simpletowns.localisation.LocalisationEntry;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.permissions.STPermission;
import org.bukkit.command.CommandSender;
import java.util.StringJoiner;

/**
 * Class representing a list command.
 * Allows you to view a list of towns
 *
 * /... list        Display a list of towns
 *
 * @author JamesHealey94 <jameshealey1994.gmail.com>
 */
public class ListCommand extends STCommand {

    /**
     * Constructor to add aliases and permissions.
     */
    public ListCommand() {
        this.aliases.add("list");

        this.permissions.add(STPermission.LIST.getPermission());
    }

    @Override
    public boolean execute(SimpleTowns plugin, CommandSender sender, String commandLabel, String[] args) {
        final Localisation localisation = plugin.getLocalisation();

        if (plugin.getTowns().isEmpty()) {
            sender.sendMessage(localisation.get(LocalisationEntry.MSG_NO_TOWNS));
            return true;
        }

        StringJoiner toSend = new StringJoiner(", ");
        for (Town t : plugin.getTowns().values()) {
            toSend.add(t.getName());
        }
        sender.sendMessage(localisation.get(LocalisationEntry.LIST_HEADER) + " " + toSend.toString());
        return true;
    }

    @Override
    public String getDescription(Localisation localisation) {
        return localisation.get(LocalisationEntry.DESCRIPTION_LIST);
    }
}