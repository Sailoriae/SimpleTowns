package com.gmail.jameshealey1994.simpletowns.utils;

import com.gmail.jameshealey1994.simpletowns.SimpleTowns;
import com.gmail.jameshealey1994.simpletowns.localisation.LocalisationEntry;
import com.gmail.jameshealey1994.simpletowns.object.Town;
import com.gmail.jameshealey1994.simpletowns.permissions.STPermission;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.internal.platform.StringMatcher;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.permission.Permission;
import java.lang.NoClassDefFoundError;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.text.Normalizer;

/**
 * Utility methods that interact with the WorldGuard API.
 * Because SimpleTowns doesn't handle the security, so we create regions on WorldGuard.
 */
public class WorldGuardUtils {

    /**
     * Disable this system, it will enable our internal security system.
     * It is better to use WorldGuard for land protection, since it's more up to date.
     */
    public final boolean LAND_PROTECTION_BY_WORLDGUARD = true;

    /**
     * SimpleTowns plugin.
     */
    private SimpleTowns plugin;

    /**
     * WorldGuard region container.
     */
    private RegionContainer container;

    /**
     * WorldGuard string matcher.
     */
    private StringMatcher matcher;

    /**
     * Our WorldGuard flag to mark our regions.
     */
    private static StateFlag IS_SIMPLETOWN_REGION;

    /**
     * Permissions through Vault.
     */
    private Permission vaultPermissions;

    public void onLoad( SimpleTowns plugin ) {
        this.plugin = plugin;
        if (!LAND_PROTECTION_BY_WORLDGUARD) return;

        // Create a flag with the name "towns-autogenerated-region", defaulting to true
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        StateFlag flag = new StateFlag("towns-autogenerated-region", false);
        registry.register(flag);
        IS_SIMPLETOWN_REGION = flag;
    }

    public void onEnable() {
        if (!LAND_PROTECTION_BY_WORLDGUARD) return;

        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        matcher = WorldGuard.getInstance().getPlatform().getMatcher();
        plugin.getLogger().log(Level.INFO, "Hooked into WorldGuard");

        try {
            RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
            vaultPermissions = rsp.getProvider();
            plugin.getLogger().log(Level.INFO, "Hooked into Vault");
        } catch ( NoClassDefFoundError e ) {
            vaultPermissions = null;
        }
    }

    private String normalizeName( String name ) {
        return "towns_autogenerated_" + Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public void addMemberToRegions( final Town town, UUID player ) {
        if (!LAND_PROTECTION_BY_WORLDGUARD) return;

        String world;
        RegionManager regions;
        ProtectedRegion region;
        for (String nameOfArea : town.getChunksToAreas().areas.keySet()) {
            world = town.getChunksToAreas().areasWorld.get(nameOfArea);
            regions = container.get(matcher.getWorldByName(world));
            if (regions != null) {
                region = regions.getRegion(normalizeName(nameOfArea));
                if (region != null)
                    region.getMembers().addPlayer(player);
            }
        }
    }

    public void removeMemberFromRegions( final Town town, UUID player ) {
        if (!LAND_PROTECTION_BY_WORLDGUARD) return;

        String world;
        RegionManager regions;
        ProtectedRegion region;
        for (String nameOfArea : town.getChunksToAreas().areas.keySet()) {
            world = town.getChunksToAreas().areasWorld.get(nameOfArea);
            regions = container.get(matcher.getWorldByName(world));
            if (regions != null) {
                region = regions.getRegion(normalizeName(nameOfArea));
                if (region != null)
                    region.getMembers().removePlayer(player);
            }
        }
    }


    public void clearWorldGuard() {
        if (!LAND_PROTECTION_BY_WORLDGUARD) return;

        StateFlag.State flag;
        for (RegionManager regions : container.getLoaded()) {
            for (String regionName : regions.getRegions().keySet()) {
                flag = regions.getRegion(regionName).getFlag(IS_SIMPLETOWN_REGION);
                if (flag != null)
                    regions.removeRegion(regionName, RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
            }
        }
    }

    /**
     * Remove WorldGuard regions for a Town.
     * HAS TO BE DONE BEFORE ANY CHUNKSTOAREAS UPDATE !
     */
    public void removeTownRegions( final Town town ) {
        if (!LAND_PROTECTION_BY_WORLDGUARD) return;

        String world;
        RegionManager regions;
        for (String nameOfArea : town.getChunksToAreas().areas.keySet()) {
            world = town.getChunksToAreas().areasWorld.get(nameOfArea);
            regions = container.get(matcher.getWorldByName(world));
            if (regions != null)
                regions.removeRegion(normalizeName(nameOfArea), RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
        }
    }

    /**
     * Add WorldGuard regions for a Town.
     */
    public void addTownRegions( final Town town ) {
        if (!LAND_PROTECTION_BY_WORLDGUARD) return;

        ArrayList<BlockVector2> points;
        int size;
        int i;
        int minY = 0;
        int maxY = 256;
        ProtectedRegion region;
        String world;
        RegionManager regions;
        for (String nameOfArea : town.getChunksToAreas().areas.keySet()) {
            points = new ArrayList();
            size = town.getChunksToAreas().areas.get(nameOfArea).size();

            for (i = 0; i < size; i++) {
                points.add(BlockVector2.at(town.getChunksToAreas().areas.get(nameOfArea).get(i).x, town.getChunksToAreas().areas.get(nameOfArea).get(i).z));
            }
            region = new ProtectedPolygonalRegion(normalizeName(nameOfArea), points, minY, maxY);
            for (UUID leader : town.getLeaders())
                region.getMembers().addPlayer(leader);
            for (UUID citizen : town.getCitizens())
                region.getMembers().addPlayer(citizen);
            region.setFlag(Flags.GREET_MESSAGE, plugin.getLocalisation().get(LocalisationEntry.MSG_ENTERED_TOWN, town.getName()));
            region.setFlag(Flags.FAREWELL_MESSAGE, plugin.getLocalisation().get(LocalisationEntry.MSG_EXITED_TOWN, town.getName()));
            region.setFlag(IS_SIMPLETOWN_REGION, StateFlag.State.ALLOW);

            world = town.getChunksToAreas().areasWorld.get(nameOfArea);
            if (vaultPermissions != null)
                for (String groupName : vaultPermissions.getGroups())
                    if (vaultPermissions.groupHas(world, groupName, STPermission.ADMIN.getPermission().getName()))
                        region.getMembers().addGroup(groupName);

            regions = container.get(matcher.getWorldByName(world));
            if (regions != null)
                regions.addRegion(region); // If the region already exists, it will be overwritten
        }
    }
}