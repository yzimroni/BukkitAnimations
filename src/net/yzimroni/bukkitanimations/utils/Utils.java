package net.yzimroni.bukkitanimations.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;

public class Utils {

	public static final NPCRegistry NPCREGISTRY = CitizensAPI.createAnonymousNPCRegistry(new EmptyNPCDataStore());
	public static final List<EntityType> SPECIAL_ENTITIES = Collections.unmodifiableList(Arrays
			.asList(EntityType.EXPERIENCE_ORB, EntityType.PAINTING, EntityType.ITEM_FRAME, EntityType.ARMOR_STAND));
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

	private Utils() {

	}

	public static boolean isInside(Location l, Location min, Location max) {
		return l.getWorld().equals(min.getWorld()) && l.getX() >= min.getX() && l.getX() <= max.getX()
				&& l.getY() >= min.getY() && l.getY() <= max.getY() && l.getZ() >= min.getZ() && l.getZ() <= max.getZ();
	}

	public static boolean isSpecialEntity(EntityType type) {
		return SPECIAL_ENTITIES.contains(type);
	}

}
