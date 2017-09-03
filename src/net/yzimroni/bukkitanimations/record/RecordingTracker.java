package net.yzimroni.bukkitanimations.record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class RecordingTracker {

	private RecordingSession session;
	private HashMap<Entity, Location> trackedEntities = new HashMap<Entity, Location>();
	private List<Integer> entitiesUseItems = new ArrayList<Integer>();

	public RecordingTracker(RecordingSession session) {
		super();
		this.session = session;
	}

	public boolean isEntityTracked(Entity e) {
		return trackedEntities.containsKey(e);
	}

	public Entity getTrackedEntityById(int id) {
		return trackedEntities.keySet().stream().filter(e -> e.getEntityId() == id).findAny().orElse(null);
	}

	public void addTrackedEntity(Entity e) {
		trackedEntities.put(e, e.getLocation());
	}

	public void removeTrackedEntity(Entity e) {
		trackedEntities.remove(e);
		removeEntityUseItem(e.getEntityId());
	}

	public boolean isEntityUsingItem(int id) {
		return entitiesUseItems.contains(id);
	}

	public void addEntityUseItem(int id) {
		entitiesUseItems.add(id);
	}

	public void removeEntityUseItem(int id) {
		entitiesUseItems.remove(new Integer(id));
	}

	public HashMap<Entity, Location> getTrackedEntities() {
		return trackedEntities;
	}

	public void setTrackedEntities(HashMap<Entity, Location> trackedEntities) {
		this.trackedEntities = trackedEntities;
	}

}
