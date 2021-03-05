package com.gmail.jameshealey1994.simpletowns.utils;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.object.TownChunk;
import com.gmail.jameshealey1994.simpletowns.utils.PlayernameUUID;
import com.gmail.jameshealey1994.simpletowns.utils.DynmapUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.dynmap.markers.AreaMarker;

/**
 * Utility methods that interact with a configuration file for town values.
 *
 * @author JamesHealey94 <jameshealey1994.gmail.com>
 */
public class TownUtils {

    /**
     * The path to the values in the config this class is interacting with.
     */
    public static final String PATH = "Towns";

    /**
     * Plugin with associated config file.
     */
    private final SimpleTowns plugin;

    /**
     * Constructor - Sets plugin.
     *
     * @param plugin    plugin with config and logger
     */
    public TownUtils(SimpleTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns if a string is a valid town name.
     * Valid town names are between 1 and 16 characters long, and contain
     * only letters, numbers, and underscores.
     *
     * @param possibleName      possible town name to be checked for validity
     * @return                  if the town name is valid
     */
    public static boolean isValidName(String possibleName) {
        return new NameValidityChecker(possibleName).isValidName();
    }

    /**
     * Returns the Y value of the mine roof.
     *
     * @return  Y value of the mine roof,
     *          or -1 if no value is specified in the config
     */
    public int getMineRoofY() {
        return plugin.getConfig().getInt("Mine Roof Y Value", -1);
    }

    /**
     * Returns towns from config.
     *
     * @return  towns from config
     */
    public Map<String, Town> getTownsFromConfig() {
        final ConfigurationSection townConfigSection = new ConfigUtils(plugin).getConfigSection(PATH);
        final Map<String, Town> townsFromConfig = new HashMap<>();
        final Set<String> townKeys = new HashSet<>(townConfigSection.getKeys(false));

        final DynmapUtils dynmap = new DynmapUtils(plugin);

        for (String townname : townKeys) {
            try {
                UUID playerUUID;
                boolean hasUUIDconversion = false;

                final Set<UUID> leaders = new HashSet<>();
                final ArrayList<String> unknownLeaders = new ArrayList<>(); // Only for UUID conversion
                for (String player: plugin.getConfig().getStringList(PATH + "." + townname + ".Leaders")) {
                    try {
                        leaders.add(UUID.fromString(player));
                    } catch (IllegalArgumentException e) {
                        playerUUID = PlayernameUUID.getPlayerUUID(player);
                        if (playerUUID != null) leaders.add(playerUUID);
                        else unknownLeaders.add(player);
                        hasUUIDconversion = true;
                    }
                }

                // Save UUID conversion
                if (hasUUIDconversion) {
                    final String path = "Towns." + townname + ".Leaders";
                    for (UUID leader : leaders){
                        unknownLeaders.add(leader.toString()); // Now contains unconverted to UUID and converted to UUID leaders
                    }
                    plugin.getConfig().set(path, unknownLeaders);
                }

                hasUUIDconversion = false;

                final Set<UUID> citizens = new HashSet<>();
                final ArrayList<String> unknownCitizens = new ArrayList<>(); // Only for UUID conversion
                for (String player: plugin.getConfig().getStringList(PATH + "." + townname + ".Citizens")) {
                    try {
                        citizens.add(UUID.fromString(player));
                    } catch (IllegalArgumentException e) {
                        playerUUID = PlayernameUUID.getPlayerUUID(player);
                        if (playerUUID != null) citizens.add(playerUUID);
                        else unknownCitizens.add(player);
                        hasUUIDconversion = true;
                    }
                }

                // Save UUID conversion
                if (hasUUIDconversion) {
                    final String path = "Towns." + townname + ".Citizens";
                    for (UUID citizen : citizens){
                        unknownCitizens.add(citizen.toString()); // Now contains unconverted to UUID and converted to UUID citizens
                    }
                    plugin.getConfig().set(path, unknownCitizens);
                }


                final Set<String> chunkWorlds = new HashSet<>(plugin.getConfig().getConfigurationSection(PATH + "." + townname + ".Chunks").getKeys(false));
                final Set<TownChunk> chunks = new HashSet<>();
                for (String world : chunkWorlds) {
                    final Set<String> chunkKeys = new HashSet<>(plugin.getConfig().getStringList(PATH + "." + townname + ".Chunks." + world));
                    for (String chunk : chunkKeys) {
                        final int chunkX = Integer.parseInt(chunk.substring(0, chunk.indexOf(',')));
                        final int chunkZ = Integer.parseInt(chunk.substring(chunk.indexOf(',') + 1));
                        final TownChunk townchunk = new TownChunk(chunkX, chunkZ, world);
                        chunks.add(townchunk);

                        // Add the chunk to our Dynmap markerset
                        dynmap.addMarkersetChunk(townname, world, chunkX, chunkZ);
                    }
                }
                final Town town = new Town(townname, leaders, citizens, chunks);
                townsFromConfig.put(townname.toLowerCase(), town);
            } catch (NumberFormatException | NullPointerException ex) {
                plugin.getLogger().log(Level.WARNING, "{0} getting towns from config: {1}", new Object[] {ex.getClass().getName(), ex.getMessage()});
            }
        }
        return townsFromConfig;
    }
}