package net.yzimroni.bukkitanimations.data.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.yzimroni.bukkitanimations.data.manager.MinecraftDataManagers;
import net.yzimroni.bukkitanimations.play.ReplayingSession;

public class ActionData {

	private ActionType type;
	private int tick;
	private HashMap<String, Object> data = new HashMap<String, Object>();

	public ActionData(ActionType type, int tick) {
		super();
		this.type = type;
		this.tick = tick;
	}

	public ActionData(ActionType type) {
		this(type, -1);
	}

	protected ActionData() {
		// Gson
	}

	public boolean has(String key) {
		return data.containsKey(key);
	}

	public Object getData(String key) {
		return data.get(key);
	}

	public int getInt(String key) {
		return ((Number) getData(key)).intValue();
	}

	public void setData(String key, Object value) {
		if (value instanceof Location) {
			Location l = ((Location) value);
			if (l.getPitch() != 0 || l.getYaw() != 0) {
				value = l.serialize();
			} else {
				value = ((Location) value).toVector();
			}
		} else if (value instanceof ItemStack) {
			value = ((ItemStack) value).serialize();
		} else if (value instanceof ItemStack[]) {
			ItemStack[] list = (ItemStack[]) value;
			Object[] serialized = new Object[list.length];
			for (int i = 0; i < list.length; i++) {
				ItemStack item = list[i];
				if (item == null) {
					serialized[i] = null;
				} else {
					serialized[i] = item.serialize();
				}
			}
			value = serialized;
		}
		data.put(key, value);
	}

	public ActionData data(String key, Object value) {
		setData(key, value);
		return this;
	}

	public Location getLocation(ReplayingSession session) {
		// TODO relative location
		@SuppressWarnings("unchecked")
		Map<String, Object> loc = (Map<String, Object>) getData("location");
		if (loc.containsKey("yaw") && loc.containsKey("pitch")) {
			loc.put("world", Bukkit.getWorlds().get(0).getName());
			return Location.deserialize(loc);
		}

		return Vector.deserialize(loc).toLocation(Bukkit.getWorlds().get(0));
	}

	public int getEntityId() {
		return ((Number) getData("entityId")).intValue();
	}

	@SuppressWarnings("unchecked")
	public ItemStack getItemStack(String name) {
		Map<String, Object> m = (Map<String, Object>) getData(name);
		if (m == null || m.isEmpty()) {
			return new ItemStack(Material.AIR);
		}
		return ItemStack.deserialize(m);
	}

	@SuppressWarnings("unchecked")
	public ItemStack[] getItemStackList(String name) {
		return ((ArrayList<Map<String, Object>>) getData(name)).stream().map(ItemStack::deserialize)
				.toArray(ItemStack[]::new);
	}

	public ActionData entityData(Entity e, Class<?>... classes) {
		MinecraftDataManagers.getEntities().save(this, e, classes);
		return this;
	}

	public ActionData entityData(Entity e) {
		MinecraftDataManagers.getEntities().save(this, e);
		return this;
	}

	public ActionData blockData(Block b) {
		MinecraftDataManagers.getBlocks().save(this, b);
		MinecraftDataManagers.getBlocks().save(this, b.getState());
		return this;
	}

	public ActionData blockData(Block b, Class<?>... classes) {
		MinecraftDataManagers.getBlocks().save(this, b, classes);
		return this;
	}

	public ActionData blockData(BlockState b) {
		MinecraftDataManagers.getBlocks().save(this, b);
		return this;
	}

	public ActionData blockData(BlockState b, Class<?>... classes) {
		MinecraftDataManagers.getBlocks().save(this, b, classes);
		return this;
	}

	public ActionType getType() {
		return type;
	}

	public void setType(ActionType type) {
		this.type = type;
	}

	public int getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public HashMap<String, Object> getData() {
		return data;
	}

	public void setData(HashMap<String, Object> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ActionData [type=" + type + ", tick=" + tick + ", data=" + data + "]";
	}

}
