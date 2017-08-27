package net.yzimroni.bukkitanimations.utils;

import org.bukkit.Location;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;

public class Utils {

	public static final NPCRegistry NPCREGISTRY = CitizensAPI.createAnonymousNPCRegistry(new EmptyNPCDataStore());

	private Utils() {

	}

	public static boolean isInside(Location l, Location min, Location max) {
		return l.getWorld().equals(min.getWorld()) && l.getX() >= min.getX() && l.getX() <= max.getX()
				&& l.getY() >= min.getY() && l.getY() <= max.getY() && l.getZ() >= min.getZ() && l.getZ() <= max.getZ();
	}

}
