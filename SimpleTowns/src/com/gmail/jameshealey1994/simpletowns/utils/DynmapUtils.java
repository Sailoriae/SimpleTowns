package com.gmail.jameshealey1994.simpletowns.utils;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.localisation.LocalisationEntry;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.object.TownChunk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
     * Add a chunk to our Dynmap marketset.
     * Does nothing if Dynmap isn't installed.
     */
    public void addMarkersetChunk( Town town, TownChunk chunk ) {
        if (markerset == null) return;

        String markerID = chunk.getWorldname() + "_" + String.valueOf(chunk.getX()) + "_" + String.valueOf(chunk.getZ());
        AreaMarker marker = markerset.findAreaMarker( markerID );
        if (marker != null)
            marker.deleteMarker();
        double[] cornersX = {chunk.getX()*16, chunk.getX()*16+16};
        double[] cornersZ = {chunk.getZ()*16, chunk.getZ()*16+16};
        marker = markerset.createAreaMarker(markerID, town.getName(), false, chunk.getWorldname(), cornersX, cornersZ, false);
        marker.setLineStyle(0, 0.0, 0xFFFFFF);
        marker.setFillStyle(0.35, 0xFFFFFF);
        marker.setLabel(town.getName());
    }

    /**
     * Remove a chunk from our Dynmap marketset.
     * Does nothing if Dynmap isn't installed.
     */
    public void removeMarkersetChunk( Town town, TownChunk chunk ) {
        if (markerset == null) return;

        AreaMarker marker = markerset.findAreaMarker( chunk.getWorldname() + "_" + String.valueOf(chunk.getX()) + "_" + String.valueOf(chunk.getZ()) );
        if (marker != null)
            marker.deleteMarker();
        else {
            removeOptimizedMarkersetTown(town);
            fastAddMarkersetTown(town); // The player will maybe remove other chunks, so we add all of them in a non-optimized way, but faster
        }
    }

    /**
     * Add all chunks of a Town to our Dynmap marketset, without merging chunks.
     * Does nothing if Dynmap isn't installed.
     */
    private void fastAddMarkersetTown( final Town town ) {
        if (markerset == null) return;

        for (TownChunk chunk : town.getTownChunks()) {
            addMarkersetChunk( town, chunk );
        }
    }

    /**
     * Remove all non-merged chunks of a Town from our Dynmap marketset.
     * Does nothing if Dynmap isn't installed.
     */
    public void removeFastMarkersetTown( final Town town ) {
        if (markerset == null) return;

        for (TownChunk chunk : town.getTownChunks()) {
            AreaMarker marker = markerset.findAreaMarker( chunk.getWorldname() + "_" + String.valueOf(chunk.getX()) + "_" + String.valueOf(chunk.getZ()) );
            if (marker != null)
                marker.deleteMarker();
        }
    }

    /**
     * Represents a point in the XZ plan.
     * This class is only used by the continuous-chunks merging algorithm.
     */
    private class Point {
        public int x;
        public int z;

        public Point(final int x, final int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point) {
                final Point other = (Point) obj;
                return ( x == other.x && z == other.z);
            }
            return false;
        }
    }

    /**
     * Represents an edge of a chunk in the XZ plan.
     * This class is only used by the continuous-chunks merging algorithm.
     */
    private class ChunkEdge {
        public Point p1;
        public Point p2;

        public ChunkEdge connectedEdgeAtP1 = null;
        public ChunkEdge connectedEdgeAtP2 = null;

        public boolean runThrough = false;

        public ChunkEdge (final Point p1, final Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ChunkEdge) {
                final ChunkEdge other = (ChunkEdge) obj;
                return ( p1.equals(other.p1) && p2.equals(other.p2) ||
                         p2.equals(other.p1) && p1.equals(other.p2) );
            }
            return false;
        }

        @Override
        public int hashCode() {
            return p1.x*p2.x*1000000000 + p1.z*p2.z;
        }
    }

    /**
     * Remove continuous chunks merges for a town from our Dynmap marketset.
     * Does nothing if Dynmap isn't installed.
     */
    public void removeOptimizedMarkersetTown( final Town town ) {
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
    public void optimizedAddMarkersetTown( final Town town ) {
        if (markerset == null) return;

        // Remove already existant areas
        removeOptimizedMarkersetTown(town);

        // Init main data containers
        HashMap<String, HashSet<ChunkEdge>> externalEdgesPerWorld = new HashMap<>();
        HashSet<ChunkEdge> externalEdgesInWorld;
        HashMap<String, ArrayList<Point>> areas = new HashMap<>();
        HashMap<String, String> areasWorld = new HashMap<>();

        // Get all external edges per world
        Point p1;
        Point p2;
        Point p3;
        Point p4;
        ChunkEdge e1;
        ChunkEdge e2;
        ChunkEdge e3;
        ChunkEdge e4;
        for (TownChunk chunk : town.getTownChunks()) {
            if (!externalEdgesPerWorld.containsKey(chunk.getWorldname()))
                externalEdgesPerWorld.put(chunk.getWorldname(), new HashSet<ChunkEdge>());

            p1 = new Point( chunk.getX()*16, chunk.getZ()*16 );
            p2 = new Point( chunk.getX()*16+16, chunk.getZ()*16 );
            p3 = new Point( chunk.getX()*16+16, chunk.getZ()*16+16 );
            p4 = new Point( chunk.getX()*16, chunk.getZ()*16+16 );
            e1 = new ChunkEdge(p1,p2);
            e2 = new ChunkEdge(p2,p3);
            e3 = new ChunkEdge(p3,p4);
            e4 = new ChunkEdge(p4,p1);

            externalEdgesInWorld = externalEdgesPerWorld.get(chunk.getWorldname());

            // There are two edges if it's an internal edge, so we remove it if we added it once
            if (!externalEdgesInWorld.add( e1 ))   // add() uses equals() to test if already added, then hashCode()
                externalEdgesInWorld.remove( e1 ); // remove() uses equals(), then hashCode()
            if (!externalEdgesInWorld.add( e2 ))
                externalEdgesInWorld.remove( e2 );
            if (!externalEdgesInWorld.add( e3 ))
                externalEdgesInWorld.remove( e3 );
            if (!externalEdgesInWorld.add( e4 ))
                externalEdgesInWorld.remove( e4 );
        }

        // Connect edges between them
        for (String world : externalEdgesPerWorld.keySet()) {
            externalEdgesInWorld = externalEdgesPerWorld.get(world);
            for (ChunkEdge edge1 : externalEdgesInWorld) {
                if (edge1.connectedEdgeAtP1 != null && edge1.connectedEdgeAtP2 != null )
                    continue; // We can do this optimization only in one loop, NOT IN BOTH LOOPS
                for (ChunkEdge edge2 : externalEdgesInWorld) {
                    if (edge1.equals(edge2))
                        continue;
                    if (edge1.p1.equals(edge2.p1)) {
                        if (edge1.connectedEdgeAtP1 == null && edge2.connectedEdgeAtP1 == null) {
                            edge1.connectedEdgeAtP1 = edge2;
                            edge2.connectedEdgeAtP1 = edge1;
                        }
                    }
                    if (edge1.p2.equals(edge2.p2)) {
                        if (edge1.connectedEdgeAtP2 == null && edge2.connectedEdgeAtP2 == null) {
                            edge1.connectedEdgeAtP2 = edge2;
                            edge2.connectedEdgeAtP2 = edge1;
                        }
                    }
                    if (edge1.p1.equals(edge2.p2)) {
                        if (edge1.connectedEdgeAtP1 == null && edge2.connectedEdgeAtP2 == null) {
                            edge1.connectedEdgeAtP1 = edge2;
                            edge2.connectedEdgeAtP2 = edge1;
                        }
                    }
                    if (edge1.p2.equals(edge2.p1)) {
                        if (edge1.connectedEdgeAtP2 == null && edge2.connectedEdgeAtP1 == null) {
                            edge1.connectedEdgeAtP2 = edge2;
                            edge2.connectedEdgeAtP1 = edge1;
                        }
                    }
                }
            }
        }

        // Create areas by running through edges
        ArrayList<Point> area;
        String areaName;
        Integer areaNumber = 0;
        for (String world : externalEdgesPerWorld.keySet()) {
            externalEdgesInWorld = externalEdgesPerWorld.get(world);
            for (ChunkEdge startEdge : externalEdgesInWorld) {
                if (startEdge.runThrough) continue;
                area = new ArrayList<Point>();
                runThroughEdges(startEdge, startEdge, area);
                areaName = town.getName() + "_" + areaNumber.toString();
                areas.put( areaName, area );
                areasWorld.put( areaName, world );
                areaNumber++;
            }
        }

        // Add to our Dynmap marketset
        ArrayList<Double> cornersX = new ArrayList<>();
        ArrayList<Double> cornersZ = new ArrayList<>();
        double[] cornersX_converted;
        double[] cornersZ_converted;
        int i;
        Point currentPoint;
        Point previousPoint;
        Point nextPoint;
        for (String nameOfArea : areas.keySet()) {
            cornersX.clear();
            cornersZ.clear();
            area = areas.get(nameOfArea);

            // Remove aligned points
            for (i = 0; i < area.size(); i++) {
                currentPoint = area.get(i);
                if (i==0) previousPoint = area.get(area.size()-1);
                else previousPoint = area.get(i-1);
                if (i==area.size()-1) nextPoint = area.get(0);
                else nextPoint = area.get(i+1);
                if (currentPoint.x == previousPoint.x && currentPoint.x == nextPoint.x || currentPoint.z == previousPoint.z && currentPoint.z == nextPoint.z)
                    continue; // Then point is aligned with the previous and the next one, we can skip it
                cornersX.add( (double)currentPoint.x );
                cornersZ.add( (double)currentPoint.z );
            }

            cornersX_converted = new double[cornersX.size()];
            cornersZ_converted = new double[cornersZ.size()];
            for (i = 0; i < cornersX_converted.length; i++) {
                cornersX_converted[i] = cornersX.get(i);
                cornersZ_converted[i] = cornersZ.get(i);
            }

            AreaMarker marker = markerset.createAreaMarker(nameOfArea, town.getName(), false, areasWorld.get(nameOfArea), cornersX_converted, cornersZ_converted, false);
            marker.setLineStyle(0, 0.0, 0xFFFFFF);
            marker.setFillStyle(0.35, 0xFFFFFF);
            marker.setLabel(town.getName());
        }
    }

    /**
     * Recursively run through edges.
     */
    private void runThroughEdges(ChunkEdge previousEdge, ChunkEdge currentEdge, ArrayList<Point> area) {
        currentEdge.runThrough = true;
        
        // Run through the next edge without going back to the previous
        if (!currentEdge.connectedEdgeAtP1.equals(previousEdge) ) {
            area.add(currentEdge.p1);
            if (currentEdge.connectedEdgeAtP1.runThrough)
                return;
            runThroughEdges(currentEdge, currentEdge.connectedEdgeAtP1, area);
        }
        else if (!currentEdge.connectedEdgeAtP2.equals(previousEdge) ) {
            area.add(currentEdge.p2);
            if (currentEdge.connectedEdgeAtP2.runThrough)
                return;
            runThroughEdges(currentEdge, currentEdge.connectedEdgeAtP2, area);
        }
    }
}