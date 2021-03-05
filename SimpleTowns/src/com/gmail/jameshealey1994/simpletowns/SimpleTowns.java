package com.gmail.jameshealey1994.simpletowns;

import com.gmail.jameshealey1994.simpletowns.commands.DefaultSTCommandEnvironment;
import com.gmail.jameshealey1994.simpletowns.commands.STCommandEnvironment;
import com.gmail.jameshealey1994.simpletowns.commands.STCommandExecutor;
import com.gmail.jameshealey1994.simpletowns.commands.command.HelpCommand;
import com.gmail.jameshealey1994.simpletowns.commands.command.STCommand;
import com.gmail.jameshealey1994.simpletowns.listeners.STListener;
import com.gmail.jameshealey1994.simpletowns.localisation.Localisable;
import com.gmail.jameshealey1994.simpletowns.localisation.Localisation;
import com.gmail.jameshealey1994.simpletowns.localisation.LocalisationEntry;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.object.TownChunk;
import com.gmail.jameshealey1994.simpletowns.utils.TownUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerSet;

/**
 * Simple, chunk-based protection plugin for Bukkit.
 * Further description at http://dev.bukkit.org/bukkit-plugins/simpletowns/
 *
 * @author JamesHealey94 <jameshealey1994.gmail.com>
 */
public class SimpleTowns extends JavaPlugin implements Localisable {

    /**
     * The current command environment for the plugin (subset of commands
     * accessible from the current state of the plugin).
     */
    private STCommandEnvironment commandEnvironment = new DefaultSTCommandEnvironment();

    /**
     * The current localisation for the plugin.
     */
    private Localisation localisation = new Localisation(this);

    /**
     * Towns from config.
     */
    private Map<String, Town> towns = new HashMap<>();

    /**
     * Our Dynmap marketset (Or null).
     */
    private MarkerSet markerset = null;

    @Override
    public void onEnable() {

        // Save a copy of the default config.yml if one is not there
        saveDefaultConfig();

        // Get Dynmap API (Or null)
        DynmapAPI dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");

        // Create Dynmap markerset if it doesn't exists
        if (dynmap != null) {
            this.getLogger().log(Level.INFO, "Hooked into Dynmap");
            this.markerset = dynmap.getMarkerAPI().getMarkerSet("simpletowns.markerset");
            if (this.markerset == null) {
                this.markerset = dynmap.getMarkerAPI().createMarkerSet("simpletowns.markerset", this.localisation.get(LocalisationEntry.DYNMAP_LAYER), null, false);
                this.markerset.setHideByDefault(false);
            }
        }

        // Load towns from config
        this.towns = new TownUtils(this).getTownsFromConfig();

        // Register events
        getServer().getPluginManager().registerEvents(new STListener(this), this);

        // Set command executors and default command
        getCommand("towns").setExecutor(new STCommandExecutor(this, new HelpCommand()));
    }

    @Override
    public void onDisable() {
        // Delete our Dynmap markerset (Like Dynmap-WorldGuard plugin)
        if (this.markerset != null)
            this.markerset.deleteMarkerSet();
    }

    /**
     * Returns an array of commands belonging to the plugin.
     *
     * @return      commands belonging to the plugin
     */
    public STCommand[] getCommands() {
        return commandEnvironment.getCommands();
    }

    /**
     * Gets the current command environment for the plugin.
     *
     * @return      the current command environment for the plugin
     */
    public STCommandEnvironment getCommandEnvironment() {
        return commandEnvironment;
    }

    /**
     * Sets the current command environment for the plugin.
     *
     * @param commandEnvironment    the new command environment for the plugin
     */
    public void setCommandEnvironment(STCommandEnvironment commandEnvironment) {
        this.commandEnvironment = commandEnvironment;
    }

    @Override
    public Localisation getLocalisation() {
        return localisation;
    }

    @Override
    public void setLocalisation(Localisation localisation) {
        this.localisation = localisation;
    }

    /**
     * Returns the Towns in the server.
     *
     * @return      the Towns in the server
     */
    public Map<String, Town> getTowns() {
        return towns;
    }

    /**
     * Sets the Towns in the server.
     *
     * @param towns     the new Towns in the server
     */
    public void setTowns(Map<String, Town> towns) {
        this.towns = towns;
    }

    /**
     * Returns Town with name equal to passed String.
     * If a town is not found, null is returned.
     *
     * @param townname      possible town name
     * @return              town with name equal to passed string, or null, if
     *                      no such town is found
     */
    public Town getTown(String townname) {
        return towns.get(townname.toLowerCase());
    }

    /**
     * Returns Town that owns the passed Chunk.
     * If a town is not found, null is returned.
     *
     * @param chunk     chunk possibly owned by a Town
     * @return          town that owns the passed Chunk, or null, if no such
     *                  town is found
     */
    public Town getTown(Chunk chunk) {
        for (Town t : towns.values()) {
            for (TownChunk tc : t.getTownChunks()) {
                if (tc.equalsChunk(chunk)) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * Returns our Dynmap markerset if this plugin is installed.
     *
     * @return      MarkerSet or null
     */
    public MarkerSet getMarketset() {
        return markerset;
    }
}