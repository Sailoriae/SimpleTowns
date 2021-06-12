package com.gmail.jameshealey1994.simpletowns.utils;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.localisation.LocalisationEntry;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.AreaMarker;

/**
 * Utility methods that interact with the Dynmap API.
 * This class has to be instantiated ONCE by the plugin.
 */
public class DynmapUtils {

    /**
     * Our Dynmap markerset.
     */
    private MarkerSet markerset;

    /**
     * Create our Dynmap markerset if it doesn't exists.
     * 
     * @param plugin    plugin with localisation
     * @return      true if Dynmap in install, false otherwise
     */
    public boolean checkDynmapAndCreateMarkerset( SimpleTowns plugin ) {
        // Get Dynmap API (Or null)
        DynmapAPI dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");

        // Check if Dynmap is installed
        if (dynmap == null)
            return false;
        
        // Create Dynmap markerset if it doesn't exists
        this.markerset = dynmap.getMarkerAPI().getMarkerSet("simpletowns.markerset");
        if (this.markerset == null) {
            this.markerset = dynmap.getMarkerAPI().createMarkerSet("simpletowns.markerset", plugin.getLocalisation().get(LocalisationEntry.DYNMAP_LAYER), null, false);
            this.markerset.setHideByDefault(false);
        }
        return true;
    }

    /**
     * Delete our Dynmap markerset if it exists.
     * Does nothing if Dynmap isn't installed.
     */
    public void deleteMarkerset() {
        if (markerset == null) return;

        markerset.deleteMarkerSet();
        markerset = null;
    }

    /**
     * Remove continuous chunks merges for a town from our Dynmap marketset.
     * Does nothing if Dynmap isn't installed.
     */
    public void removeTownFromMarkerset( final Town town ) {
        if (markerset == null) return;

        Integer areaNumber = 0;
        while (true) {
            AreaMarker marker = markerset.findAreaMarker( town.getName() + "_" + areaNumber.toString() );
            if (marker != null)
                marker.deleteMarker();
            else
                break;
            areaNumber++;
        }
    }

    /**
     * Add all chunks of a Town to our Dynmap marketset.
     * Continuous chunks are merged into one area.
     * Does nothing if Dynmap isn't installed.
     */
    public void addTownToMarkerset( final Town town ) {
        if (markerset == null) return;

        // Add to our Dynmap marketset
        int size;
        double[] cornersX_converted;
        double[] cornersZ_converted;
        int i;
        for (String nameOfArea : town.getChunksToAreas().areas.keySet()) {
            size = town.getChunksToAreas().areas.get(nameOfArea).size();

            cornersX_converted = new double[size];
            cornersZ_converted = new double[size];
            for (i = 0; i < size; i++) {
                cornersX_converted[i] = town.getChunksToAreas().areas.get(nameOfArea).get(i).x;
                cornersZ_converted[i] = town.getChunksToAreas().areas.get(nameOfArea).get(i).z;
            }

            AreaMarker marker = markerset.createAreaMarker(nameOfArea, town.getName(), false, town.getChunksToAreas().areasWorld.get(nameOfArea), cornersX_converted, cornersZ_converted, false);
            marker.setLineStyle(0, 0.0, 0xFFFFFF);
            marker.setFillStyle(0.35, 0xFFFFFF);
            marker.setLabel(town.getName());
        }
    }
}