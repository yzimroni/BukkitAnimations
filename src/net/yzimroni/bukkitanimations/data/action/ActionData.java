package net.yzimroni.bukkitanimations.data.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.yzimroni.bukkitanimations.data.manager.MinecraftDataManagers;
import net.yzimroni.bukkitanimations.play.ReplayingSession;
import net.yzimroni.bukkitanimations.record.RecordingSession;

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

	public Object get(String key) {
		return data.get(key);
	}

	public int getInt(String key) {
		return ((Number) get(key)).intValue();
	}

	public void set(String key, Object value) {
		if (value instanceof ItemStack) {
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
		set(key, value);
		return this;
	}

	public void applyOffset(RecordingSession session) {
		data.entrySet().stream().filter(e -> e.getValue() instanceof Location).forEach(e -> {
			Location l = session.getRelativeLocation(((Location) e.getValue()).clone());
			e.setValue(l.serialize());
		});
	}

	public Location getLocation(ReplayingSession session) {
		@SuppressWarnings("unchecked")
		Map<String, Object> loc = (Map<String, Object>) get("location");
		if (loc.containsKey("yaw") && loc.containsKey("pitch")) {
			loc.put("world", session.getBaseLocation().getWorld().getName());
			return session.getAbsoluteLocation(Location.deserialize(loc));
		}

		Location l = Vector.deserialize(loc).toLocation(session.getBaseLocation().getWorld());
		return session.getAbsoluteLocation(l);
	}

	public int getEntityId() {
		return getInt("entityId");
	}

	@SuppressWarnings("unchecked")
	public ItemStack getItemStack(String name) {
		Map<String, Object> m = (Map<String, Object>) get(name);
		if (m == null || m.isEmpty()) {
			return new ItemStack(Material.AIR);
		}
		return ItemStack.deserialize(m);
	}

	@SuppressWarnings("unchecked")
	public ItemStack[] getItemStackList(String name) {
		return ((ArrayList<Map<String, Object>>) get(name)).stream().map(ItemStack::deserialize)
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

	@SuppressWarnings("deprecation")
	public ActionData blockStateType(BlockState newState) {
		data("type", newState.getType()).data("data", newState.getData().getData());
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
