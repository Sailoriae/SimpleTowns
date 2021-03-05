package com.gmail.jameshealey1994.simpletowns.utils;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.object.TownChunk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.dynmap.markers.AreaMarker;

/**
 * Utility methods that interact with the Dynmap API.
 */
public class DynmapUtils {

    /**
     * Plugin with associated config file.
     */
    private final SimpleTowns plugin;

    /**
     * Constructor - Sets plugin.
     *
     * @param plugin    plugin with config and logger
     */
    public DynmapUtils(SimpleTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Add a chunk to our Dynmap marketset.
     */
    public void addMarkersetChunk( String townname, String world, int chunkX, int chunkZ ) {
        if (plugin.getMarketset() != null) {
            String markerID = world + "_" + String.valueOf(chunkX) + "_" + String.valueOf(chunkZ);
            AreaMarker marker = plugin.getMarketset().findAreaMarker( markerID );
            if (marker != null)
                marker.deleteMarker();
            double[] cornersX = {chunkX*16, chunkX*16+16};
            double[] cornersZ = {chunkZ*16, chunkZ*16+16};
            marker = plugin.getMarketset().createAreaMarker(markerID, townname, false, world, cornersX, cornersZ, false);
            marker.setLineStyle(0, 0.0, 0xFFFFFF);
            marker.setFillStyle(0.35, 0xFFFFFF);
            marker.setLabel(townname);
        }
    }

    /**
     * Remove a chunk from our Dynmap marketset.
     */
    public void removeMarkersetChunk( String world, int chunkX, int chunkZ ) {
        if (plugin.getMarketset() != null) {
            AreaMarker marker = plugin.getMarketset().findAreaMarker( world + "_" + String.valueOf(chunkX) + "_" + String.valueOf(chunkZ) );
            if (marker != null)
                marker.deleteMarker();
        }
    }
}