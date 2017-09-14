package net.yzimroni.bukkitanimations.record;

import java.io.File;
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
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

import net.yzimroni.bukkitanimations.BukkitAnimationsPlugin;
import net.yzimroni.bukkitanimations.animation.AnimationData;
import net.yzimroni.bukkitanimations.animation.AnimationManager;
import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.data.action.ActionType;
import net.yzimroni.bukkitanimations.utils.Utils;

@SuppressWarnings("deprecation")
public class RecordingSession extends Recorder {

	private boolean running;

	private Location minLocation;
	private Location maxLocation;

	private EventRecorder eventRecorder;
	private PacketListener packetListener;

	private RecordingTracker tracker;

	public RecordingSession(String name, UUID uuid, Location min, Location max) {
		super(new AnimationData(name, uuid));
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
				Play.Server.WORLD_PARTICLES, Play.Server.ENTITY_METADATA, Play.Server.NAMED_SOUND_EFFECT) {
			@Override
			public void onPacketSending(PacketEvent e) {
				if (e.getPlayer().getUniqueId().equals(getAnimation().getPlayer())) {
					handlePacket(e.getPacket());
				}
			}
		};
		ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
	}

	private void onStart() {
		saveSchematic();
		minLocation.getWorld().getEntities().stream().filter(e -> isInside(e.getLocation())).forEach(e -> {
			ActionData action = new ActionData(
					e instanceof Projectile ? ActionType.SHOOT_PROJECTILE : ActionType.SPAWN_ENTITY).entityData(e);
			getTracker().addTrackedEntity(e);
			addAction(action);
		});
	}

	private void saveSchematic() {
		CuboidClipboard clipboard = new CuboidClipboard(BukkitUtil.toVector(getSize()),
				BukkitUtil.toVector(minLocation));
		clipboard.copy(new EditSession(new BukkitWorld(minLocation.getWorld()), Integer.MAX_VALUE));

		File tempFolder = new File(BukkitAnimationsPlugin.get().getDataFolder(), "temp");
		tempFolder.mkdirs();
		try {
			File schematicFile = File.createTempFile("animation", ".schematic", tempFolder);
			schematicFile.deleteOnExit();
			SchematicFormat.MCEDIT.save(clipboard, schematicFile);
			addExtraFile("schematics/animation.schematic", schematicFile);
			addAction(new ActionData(ActionType.LOAD_SCHEMATIC).data("location", minLocation).data("schematic",
					"animation"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isInside(Location location) {
		return Utils.isInside(location, minLocation, maxLocation);
	}

	public Vector getSize() {
		return maxLocation.clone().subtract(minLocation).toVector();
	}

	public Location getRelativeLocation(Location location) {
		return location.subtract(minLocation);
	}

	@Override
	public void addAction(ActionData action) {
		action.applyOffset(this);
		super.addAction(action);
	}

	@Override
	protected void tick() {
		if (!isRunning()) {
			return;
		}
		checkEntityMove();
		super.tick();
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
		} else if (p.getType() == Play.Server.NAMED_SOUND_EFFECT) {
			String sound = p.getStrings().read(0);
			Location location = new Location(minLocation.getWorld(), (double) p.getIntegers().read(0) / 8,
					(double) p.getIntegers().read(1) / 8, (double) p.getIntegers().read(2) / 8);
			float volume = p.getFloat().read(0);
			int pitch = p.getIntegers().read(3);
			addAction(new ActionData(ActionType.SOUND).data("location", location).data("sound", sound)
					.data("volume", volume).data("pitch", pitch));
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

		writeAnimation(AnimationManager.get().createAnimationFile(getAnimation().getName()));
	}

	public boolean isRunning() {
		return running;
	}

	public RecordingTracker getTracker() {
		return tracker;
	}

}
