package net.yzimroni.bukkitanimations.record;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.gson.Gson;

import net.yzimroni.bukkitanimations.BukkitAnimationsPlugin;
import net.yzimroni.bukkitanimations.animation.AnimationData;
import net.yzimroni.bukkitanimations.animation.AnimationManager;
import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.data.action.ActionType;
import net.yzimroni.bukkitanimations.utils.Utils;

public class RecordingSession {

	private AnimationData animation;
	private boolean running;

	private int tick = 1;

	private Location minLocation;
	private Location maxLocation;

	private EventRecorder eventRecorder;

	private List<ActionData> actions = new ArrayList<ActionData>();
	private HashMap<Entity, Location> trackedEntities = new HashMap<Entity, Location>();

	public RecordingSession(String name, UUID uuid, Location min, Location max) {
		this.animation = new AnimationData(name, uuid);
		Preconditions.checkArgument(min.getWorld().equals(max.getWorld()), "World must be same");
		this.minLocation = new Location(min.getWorld(), Math.min(min.getBlockX(), max.getBlockX()),
				Math.min(min.getBlockY(), max.getBlockY()), Math.min(min.getBlockZ(), max.getBlockZ()));
		this.maxLocation = new Location(min.getWorld(), Math.max(min.getBlockX(), max.getBlockX()),
				Math.max(min.getBlockY(), max.getBlockY()), Math.max(min.getBlockZ(), max.getBlockZ()));

	}

	public void start() {
		if (isRunning()) {
			return;
		}
		running = true;
		eventRecorder = new EventRecorder(this);
		Bukkit.getPluginManager().registerEvents(eventRecorder, BukkitAnimationsPlugin.get());
		onStart();
		RecordingManager.get().onStart(this);
	}

	private void onStart() {
		minLocation.getWorld().getEntities().stream().filter(e -> isInside(e.getLocation())).forEach(e -> {
			ActionData action = new ActionData(ActionType.SPAWN_ENTITY).entityData(e);
			trackedEntities.put(e, e.getLocation());
			addAction(action);
		});
	}

	public boolean isInside(Location location) {
		return Utils.isInside(location, minLocation, maxLocation);
	}

	public void addAction(ActionData action) {
		if (action.getTick() == -1) {
			action.setTick(tick);
		}
		actions.add(action);
		System.out.println(action);
	}

	protected void tick() {
		if (!isRunning()) {
			return;
		}
		checkEntityMove();
		tick++;
	}

	private void checkEntityMove() {
		// TODO Check when entities walk into the recording area
		List<Entity> toRemove = new ArrayList<Entity>();
		trackedEntities.entrySet().stream().filter(e -> e.getKey().isValid())
				.filter(e -> e.getKey().getType() != EntityType.PLAYER).forEach(e -> {
					Location location = e.getKey().getLocation();
					// System.out.println(location.getYaw() + " " + location.getPitch());
					if (!location.equals(e.getValue())) {
						if (isInside(location)) {
							ActionData action = new ActionData(ActionType.ENTITY_MOVE)
									.data("entityId", e.getKey().getEntityId()).data("location", location);
							addAction(action);
							e.setValue(location);
						} else {
							ActionData action = new ActionData(ActionType.DESPAWN_ENTITY).data("entityId",
									e.getKey().getEntityId());
							toRemove.add(e.getKey());
							addAction(action);
						}
					}
				});
		toRemove.forEach(trackedEntities::remove);
	}

	public boolean isEntityTracked(Entity e) {
		return trackedEntities.containsKey(e);
	}

	public void addTrackedEntity(Entity e) {
		trackedEntities.put(e, e.getLocation());
	}

	public void removeTrackedEntity(Entity e) {
		trackedEntities.remove(e);
	}

	public void stop() {
		if (!isRunning()) {
			return;
		}
		if (eventRecorder != null) {
			HandlerList.unregisterAll(eventRecorder);
			eventRecorder = null;
		}
		running = false;
		RecordingManager.get().onStop(this);

		try {
			Files.write(new Gson().toJson(actions), AnimationManager.get().createAnimationFile(animation.getName()),
					Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isRunning() {
		return running;
	}

	public AnimationData getAnimation() {
		return animation;
	}

}
