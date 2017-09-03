package net.yzimroni.bukkitanimations.record;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
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
	private PacketListener packetListener;

	private RecordingTracker tracker;

	private List<ActionData> actions = new ArrayList<ActionData>();

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
		this.tracker = new RecordingTracker(this);
		eventRecorder = new EventRecorder(this);
		Bukkit.getPluginManager().registerEvents(eventRecorder, BukkitAnimationsPlugin.get());
		initPacketListener();
		onStart();
		RecordingManager.get().onStart(this);
	}

	protected void initPacketListener() {
		packetListener = new PacketAdapter(BukkitAnimationsPlugin.get(), ListenerPriority.LOWEST,
				Play.Server.WORLD_EVENT, Play.Server.BLOCK_BREAK_ANIMATION, Play.Server.COLLECT,
				Play.Server.WORLD_PARTICLES, Play.Server.ENTITY_METADATA) {
			@Override
			public void onPacketSending(PacketEvent e) {
				if (e.getPlayer().getUniqueId().equals(animation.getPlayer())) {
					handlePacket(e.getPacket());
				}
			}
		};
		ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
	}

	private void onStart() {
		minLocation.getWorld().getEntities().stream().filter(e -> isInside(e.getLocation())).forEach(e -> {
			ActionData action = new ActionData(
					e instanceof Projectile ? ActionType.SHOOT_PROJECTILE : ActionType.SPAWN_ENTITY).entityData(e);
			getTracker().addTrackedEntity(e);
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
		List<Entity> toRemove = new ArrayList<Entity>();
		getTracker().getTrackedEntities().entrySet().stream().filter(e -> e.getKey().isValid())
				.filter(e -> e.getKey().getType() != EntityType.PLAYER).forEach(e -> {
					Location location = e.getKey().getLocation();
					// System.out.println(location.getYaw() + " " + location.getPitch());
					// handleEntityMove(e.getKey(), e.getValue(), e.getKey().getLocation());
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
		toRemove.forEach(getTracker().getTrackedEntities()::remove);

		minLocation.getWorld().getEntities().stream().filter(e -> isInside(e.getLocation()))
				.filter(e -> !getTracker().isEntityTracked(e)).forEach(e -> {
					ActionData action = new ActionData(ActionType.SPAWN_ENTITY).entityData(e);
					getTracker().addTrackedEntity(e);
					addAction(action);
				});
	}

	public void handleEntityMove(Entity e, Location from, Location to) {
		boolean toInside = isInside(to);
		boolean fromInside = isInside(from);
		if (toInside && !fromInside) {
			if (!getTracker().isEntityTracked(e)) {
				ActionData action = new ActionData(ActionType.SPAWN_ENTITY).entityData(e);
				getTracker().addTrackedEntity(e);
				addAction(action);
			}
		} else if (!toInside && fromInside) {
			if (getTracker().isEntityTracked(e)) {
				ActionData action = new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", e.getEntityId());
				getTracker().removeTrackedEntity(e);
				addAction(action);
			}
		} else if (toInside && fromInside) {
			if (getTracker().isEntityTracked(e)) {
				ActionData action = new ActionData(ActionType.ENTITY_MOVE).data("entityId", e.getEntityId())
						.data("location", to);
				addAction(action);
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void handlePacket(PacketContainer p) {
		if (p.getType() == Play.Server.WORLD_EVENT) {
			int effectId = p.getIntegers().read(0);
			Effect effect = Effect.getById(effectId);
			if (effect != null) {
				Location location = p.getBlockPositionModifier().read(0).toLocation(minLocation.getWorld());
				int data = p.getIntegers().read(1);
				boolean disableRelVolume = p.getBooleans().read(0);
				ActionData action = new ActionData(ActionType.WORLD_EFFECT).data("effect", effect)
						.data("location", location).data("data", data).data("disableRel", disableRelVolume);
				addAction(action);
			}
		} else if (p.getType() == Play.Server.BLOCK_BREAK_ANIMATION) {
			int entityId = p.getIntegers().read(0);
			Location location = p.getBlockPositionModifier().read(0).toLocation(minLocation.getWorld());
			int stage = p.getIntegers().read(1);
			addAction(new ActionData(ActionType.BLOCK_BREAK_ANIMATION).data("entityId", entityId)
					.data("location", location).data("stage", stage));
		} else if (p.getType() == Play.Server.COLLECT) {
			int entityId = p.getIntegers().read(0);
			Entity entity = getTracker().getTrackedEntityById(entityId);
			if (entity != null && getTracker().isEntityTracked(entity)) {
				int playerId = p.getIntegers().read(1);
				Entity player = getTracker().getTrackedEntityById(playerId);
				if (player != null) {
					addAction(new ActionData(ActionType.ENTITY_PICKUP).data("entityId", entityId).data("playerId",
							playerId));
				} else {
					addAction(new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", entity));
				}
				getTracker().removeTrackedEntity(entity);
			}
		} else if (p.getType() == Play.Server.WORLD_PARTICLES) {
			int id = p.getParticles().read(0).getId();
			boolean longDis = p.getBooleans().read(0);
			Location location = new Location(minLocation.getWorld(), p.getFloat().read(0), p.getFloat().read(1),
					p.getFloat().read(2));
			Vector offset = new Vector(p.getFloat().read(3), p.getFloat().read(4), p.getFloat().read(5));
			float data = p.getFloat().read(6);
			int count = p.getIntegers().read(0);
			int[] dataArray = p.getIntegerArrays().read(0);
			addAction(new ActionData(ActionType.PARTICLE).data("particleId", id).data("longDis", longDis)
					.data("location", location).data("offset", offset).data("data", data).data("count", count)
					.data("dataArray", dataArray));
		} else if (p.getType() == Play.Server.ENTITY_METADATA) {
			int entityId = p.getIntegers().read(0);
			Entity entity = getTracker().getTrackedEntityById(entityId);
			if (entity != null && getTracker().isEntityTracked(entity)) {
				List<WrappedWatchableObject> metadata = p.getWatchableCollectionModifier().read(0);
				for (WrappedWatchableObject w : metadata) {
					if (w.getIndex() == 0) {
						byte value = (byte) w.getValue();
						boolean itemUse = (value & 1 << 4) != 0;
						if (itemUse != getTracker().isEntityUsingItem(entityId)) {
							if (itemUse) {
								getTracker().addEntityUseItem(entityId);
							} else {
								getTracker().removeEntityUseItem(entityId);
							}
							addAction(new ActionData(ActionType.ENTITY_ITEM_USE).data("entityId", entityId)
									.data("useItem", itemUse));
						}
						break;
					}
				}
			}
		}

	}

	public void stop() {
		if (!isRunning()) {
			return;
		}
		if (packetListener != null) {
			ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
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

	public RecordingTracker getTracker() {
		return tracker;
	}

}
