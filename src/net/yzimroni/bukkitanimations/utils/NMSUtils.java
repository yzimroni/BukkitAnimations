package net.yzimroni.bukkitanimations.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.util.NMS;

public class NMSUtils {

	private static boolean init = false;

	private static Method reciveItem;
	private static Method getProfile;

	public static Class<?> getNMSClass(String classname) {
		String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		String className = "net.minecraft.server." + nmsVersion + "." + classname;
		try {
			return Class.forName(className);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

	public static void updateEntityUseItem(Entity entity, boolean useItem) {
		try {
			Object handle = NMS.getHandle(entity);
			Field dataWatcherField = getNMSClass("Entity").getDeclaredField("datawatcher");
			dataWatcherField.setAccessible(true);
			Object dataWatcher = dataWatcherField.get(handle);
			Method getByteMethod = dataWatcher.getClass().getMethod("getByte", int.class);
			byte data = (byte) getByteMethod.invoke(dataWatcher, 0);
			if (useItem) {
				data = (byte) (data | 1 << 4);
			} else {
				data = (byte) (data & ~(1 << 4));
			}
			Method watchMethod = dataWatcher.getClass().getMethod("watch", int.class, Object.class);
			watchMethod.invoke(dataWatcher, 0, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static GameProfile getGameProfile(Player player) {
		init(player);
		try {
			Object handle = NMS.getHandle(player);
			return (GameProfile) getProfile.invoke(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static synchronized void init(Player player) {
		if (init) {
			return;
		}
		init = true;
		try {
			Object handle = NMS.getHandle(player);
			reciveItem = handle.getClass().getMethod("receive", getNMSClass("Entity"), int.class);
			getProfile = handle.getClass().getMethod("getProfile");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object createBlockAnimationPacket(int entityId, Location location, int stage) {
		try {
			Constructor<?> packetConstructor = getNMSClass("PacketPlayOutBlockBreakAnimation").getConstructor(int.class,
					getNMSClass("BlockPosition"), int.class);
			Constructor<?> blockPosConstructor = getNMSClass("BlockPosition").getConstructor(int.class, int.class,
					int.class);
			Object blockPos = blockPosConstructor.newInstance(location.getBlockX(), location.getBlockY(),
					location.getBlockZ());

			Object packet = packetConstructor.newInstance(entityId, blockPos, stage);
			return packet;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void sendPacket(Object packet, Location location, double radius) {
		double r = radius * radius;
		;
		try {
			location.getWorld().getPlayers().stream().filter(p -> location.distanceSquared(p.getLocation()) < r)
					.forEach(p -> sendPacket(p, packet));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = NMS.getHandle(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			Method sendPacket = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
			sendPacket.invoke(playerConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
