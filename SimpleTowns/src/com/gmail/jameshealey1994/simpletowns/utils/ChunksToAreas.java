package com.gmail.jameshealey1994.simpletowns.utils;

import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.object.TownChunk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Convert the town's chunks to areas.
 */
public class ChunksToAreas {

    /**
     * First container of the output of chunksToAreas().
     * Area name -> List of points in this area.
     */
    public HashMap<String, ArrayList<Point>> areas = new HashMap<>();

    /**
     * Second container of the output of chunksToAreas().
     * Area name -> World name of this area.
     */
    public HashMap<String, String> areasWorld = new HashMap<>();

    /**
     * Current Town.
     */
    private Town town;

    public ChunksToAreas( Town town ) {
        this.town = town;
        update();
    }

    /**
     * Represents a point in the XZ plan.
     * This class is only used by the continuous-chunks merging algorithm.
     */
    public class Point {
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
     * Merge continuous chunks into areas.
     */
    public void update() {
        // Init main data containers
        HashMap<String, HashSet<ChunkEdge>> externalEdgesPerWorld = new HashMap<>();
        HashSet<ChunkEdge> externalEdgesInWorld;
        areas.clear();
        areasWorld.clear();

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

        // Remove aligned points
        ArrayList<Point> toRemove = new ArrayList<>();
        int i;
        Point currentPoint;
        Point previousPoint;
        Point nextPoint;
        for (String nameOfArea : areas.keySet()) {
            toRemove.clear();
            area = areas.get(nameOfArea);

            for (i = 0; i < area.size(); i++) {
                currentPoint = area.get(i);
                if (i==0) previousPoint = area.get(area.size()-1);
                else previousPoint = area.get(i-1);
                if (i==area.size()-1) nextPoint = area.get(0);
                else nextPoint = area.get(i+1);
                if (currentPoint.x == previousPoint.x && currentPoint.x == nextPoint.x || currentPoint.z == previousPoint.z && currentPoint.z == nextPoint.z)
                    toRemove.add(currentPoint); // Then point is aligned with the previous and the next one, we can remove it
            }
            for (Point point : toRemove) {
                area.remove(point);
            }
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