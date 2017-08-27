package net.yzimroni.bukkitanimations.data.action;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
			value = ((Location) value).toVector();
		}
		data.put(key, value);
	}

	public ActionData data(String key, Object value) {
		setData(key, value);
		return this;
	}
	
	public Location getLocation(ReplayingSession session) {
		//TODO relative location
		return Vector.deserialize((Map<String, Object>) getData("location")).toLocation(Bukkit.getWorlds().get(0));
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
