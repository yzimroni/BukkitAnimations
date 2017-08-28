package net.yzimroni.bukkitanimations.utils;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.citizensnpcs.util.NMS;

public class NMSUtils {

	private static boolean init = false;

	private static Method reciveItem;

	public static Class<?> getNMSClass(String classname) {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
		String name = "net.minecraft.server." + version + classname;
		Class<?> nmsClass = null;
		try {
			nmsClass = Class.forName(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nmsClass;
	}

	public static void pickUp(Entity entity, Player player) {
		init(player);
		try {
			Object handle = NMS.getHandle(player);
			reciveItem.invoke(handle, NMS.getHandle(entity), 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static synchronized void init(Player player) {
		if (init) {
			return;
		}
		init = true;
		try {
			Object handle = NMS.getHandle(player);
			reciveItem = handle.getClass().getMethod("receive", getNMSClass("Entity"), int.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
