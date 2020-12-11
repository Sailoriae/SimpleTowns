package com.gmail.jameshealey1994.simpletowns.utils;

import com.google.common.base.Charsets;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

/**
 * Convert UUID and playernames.
 */
public abstract class PlayernameUUID {

    /**
     * https://github.com/kellerkindt/ShowCaseStandalone/blob/2e123063f7c3dd99178c8f26f2490d9a9e763c7d/src/com/kellerkindt/scs/ShowCaseStandalone.java#L353
     * @param name      name of the player
     * @return          the UUID of the player with the given name or null
     */
    public static UUID getPlayerUUID (String name) {
        // try to get the online player with the given UUID
        Player          playerOnline     = Bukkit.getServer().getPlayer(name);
        OfflinePlayer   playerOffline    = null;

        // if player is online, return its name
        if (playerOnline != null) {
            return playerOnline.getUniqueId();
        }

        // get the offline instance
        playerOffline = Bukkit.getServer().getOfflinePlayer(name);

        // return the UUID if available
        if (playerOffline != null) {
            UUID uuid = playerOffline.getUniqueId();

            if (uuid != null) {
                return uuid;
            }
        }

        return null;
    }

    /**
     * https://github.com/kellerkindt/ShowCaseStandalone/blob/2e123063f7c3dd99178c8f26f2490d9a9e763c7d/src/com/kellerkindt/scs/ShowCaseStandalone.java#L415
     * @param uuid      UUID of the player
     * @return          the name of the player of the given UUID or null
     */
    public static String getPlayerName (UUID uuid) {
        // try to get the online player with the given UUID
        Player          playerOnline     = Bukkit.getServer().getPlayer(uuid);
        OfflinePlayer   playerOffline    = null;

        // if player is online, return its name
        if (playerOnline != null) {
            return playerOnline.getName();
        }

        // get the offline instance
        playerOffline = Bukkit.getServer().getOfflinePlayer(uuid);

        // return the name if available
        if (playerOffline != null) {
            return playerOffline.getName();
        }

        return null;
    }
}