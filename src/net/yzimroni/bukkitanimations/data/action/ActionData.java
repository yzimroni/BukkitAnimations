package net.yzimroni.bukkitanimations.data.action;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

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

	public Object getData(String key) {
		return data.get(key);
	}

	public void setData(String key, Object value) {
		if (value instanceof Location) {
			Location l = ((Location) value);
			if (l.getPitch() != 0 || l.getYaw() != 0) {
				value = l.serialize();
			} else {
				value = ((Location) value).toVector();
			}
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

	public ActionData spawnEntity(Entity e) {
		data("location", e.getLocation()).data("entityId", e.getEntityId()).data("uuid", e.getUniqueId())
				.data("name", e.getName()).data("customName", e.getCustomName()).data("fireTicks", e.getFireTicks())
				.data("type", e.getType());
		if (e instanceof LivingEntity) {
			LivingEntity l = (LivingEntity) e;
			// data("potions", l.getActivePotionEffects());
			// data("armor", l.getEquipment().getArmorContents());

		}
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
