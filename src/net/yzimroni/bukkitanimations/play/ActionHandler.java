package net.yzimroni.bukkitanimations.play;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.yzimroni.bukkitanimations.BukkitAnimationsPlugin;
import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.data.action.ActionType;
import net.yzimroni.bukkitanimations.data.manager.MinecraftDataManagers;
import net.yzimroni.bukkitanimations.utils.NMSUtils;
import net.yzimroni.bukkitanimations.utils.Utils;

public class ActionHandler {

	private static final HashMap<ActionType, BiConsumer<ReplayingSession, ActionData>> HANDLERS = new HashMap<>();

	static {
		registerDefaultHandlers();
	}

	@SuppressWarnings("unchecked")
	private static void registerDefaultHandlers() {
		register(ActionType.BLOCK_PLACE, (s, a) -> {
			Block b = a.getLocation(s).getBlock();
			MinecraftDataManagers.getBlocks().load(a, b);
			MinecraftDataManagers.getBlocks().load(a, b.getState());
		});
		register(ActionType.BLOCK_BREAK, (s, a) -> {
			a.getLocation(s).getBlock().setType(Material.AIR);
		});

		register(ActionType.BLOCK_BREAK_ANIMATION, (s, a) -> {
			int entityId = a.getEntityId();
			Location location = a.getLocation(s);
			int stage = a.getInt("stage");
			NMSUtils.sendPacket(NMSUtils.createBlockAnimationPacket(entityId, location, stage), location, 64);
		});
		register(ActionType.UPDATE_BLOCKSTATE, (s, a) -> {
			Block b = a.getLocation(s).getBlock();
			MinecraftDataManagers.getBlocks().load(a, b.getState());
			b.getState().update(true);
		});
		register(ActionType.LIGHTNING_STRIKE, (s, a) -> {
			Location l = a.getLocation(s);
			l.getWorld().strikeLightningEffect(l);
		});

		register(ActionType.SPAWN_ENTITY, (s, a) -> {
			EntityType type = EntityType.valueOf((String) a.get("type"));
			int entityId = a.getEntityId();
			Location location = a.getLocation(s);

			Entity entity = null;
			boolean delayedEntityAdd = false;
			if (Utils.isSpecialEntity(type)) {
				entity = location.getWorld().spawnEntity(location, type);
				if (type == EntityType.PAINTING || type == EntityType.ITEM_FRAME) {
					delayedEntityAdd = true;
				} else {
					s.getEntityTracker().addEntity(entity);
				}
			} else {
				String name = (String) a.get("name");
				NPC npc = Utils.NPCREGISTRY.createNPC(type, name);

				if (type == EntityType.PLAYER) {
					Map<String, Object> textures = (Map<String, Object>) a.get("textures");
					if (textures != null && !textures.isEmpty()) {
						npc.data().set(Skin.CACHED_SKIN_UUID_NAME_METADATA, npc.getName());
						npc.data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, textures.get("value"));
						npc.data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, textures.get("signature"));
						npc.data().set(NPC.PLAYER_SKIN_USE_LATEST, false);
					}
				}
				npc.spawn(location);
				s.getEntityTracker().addNPC(npc);
				entity = npc.getEntity();
			}
			MinecraftDataManagers.getEntities().load(a, entity);
			if (delayedEntityAdd) {
				s.getEntityTracker().addEntity(entity);
			}

			s.getEntityTracker().addOldToNewId(entityId, entity.getEntityId());
		});

		register(ActionType.SHOOT_PROJECTILE, (s, a) -> {
			int entityId = a.getEntityId();
			int shooterId = a.getInt("shooterId");
			Entity shooter = s.getEntityTracker().getEntityForOldId(shooterId);
			EntityType type = EntityType.valueOf((String) a.get("type"));
			Vector velocity = Vector.deserialize((Map<String, Object>) a.get("velocity"));

			Projectile projectile = null;
			if (shooter != null && shooter instanceof ProjectileSource) {
				projectile = ((ProjectileSource) shooter)
						.launchProjectile((Class<? extends Projectile>) type.getEntityClass(), velocity);
			} else {
				Location location = a.getLocation(s);
				if (type == EntityType.ARROW) {
					projectile = location.getWorld().spawnArrow(location, velocity, (float) 0.6, 12);
				} else {
					projectile = (Projectile) location.getWorld().spawnEntity(location, type);
				}
			}
			MinecraftDataManagers.getEntities().load(a, projectile);
			s.getEntityTracker().addOldToNewId(entityId, projectile.getEntityId());
			s.getEntityTracker().addEntity(projectile);
		});

		register(ActionType.ENTITY_MOVE, (s, a) -> {
			int entityId = a.getEntityId();
			Location location = a.getLocation(s);
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			e.teleport(location);
			if (e.hasMetadata("NPC")) {
				NMS.setHeadYaw(NMS.getHandle(e), location.getYaw());
			}
		});
		register(ActionType.ENTITY_DAMAGE, (s, a) -> {
			int entityId = a.getEntityId();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			e.playEffect(EntityEffect.HURT);
		});

		register(ActionType.PLAYER_ANIMATION, (s, a) -> {
			int entityId = a.getEntityId();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			PlayerAnimationType type = PlayerAnimationType.valueOf((String) a.get("type"));
			if (type == PlayerAnimationType.ARM_SWING) {
				PlayerAnimation.ARM_SWING.play((Player) e);
			}
		});
		register(ActionType.ENTITY_PICKUP, (s, a) -> {
			int entityId = a.getEntityId();
			Entity pickedUp = s.getEntityTracker().getEntityForOldId(entityId);
			int playerId = a.getInt("playerId");
			Entity player = s.getEntityTracker().getEntityForOldId(playerId);
			if (player != null) {
				NMSUtils.pickUp(pickedUp, (Player) player);
			}
			handle(s, new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", entityId));
		});
		register(ActionType.UPDATE_ENTITY, (s, a) -> {
			int entityId = a.getEntityId();
			Entity update = s.getEntityTracker().getEntityForOldId(entityId);
			MinecraftDataManagers.getEntities().load(a, update);
		});

		register(ActionType.ENTITY_DEATH, (s, a) -> {
			int entityId = a.getEntityId();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			e.playEffect(EntityEffect.DEATH);
			Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitAnimationsPlugin.get(),
					() -> handle(s, new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", entityId)), 20);
		});
		register(ActionType.DESPAWN_ENTITY, (s, a) -> {
			int entityId = ((Number) a.get("entityId")).intValue();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			if (e == null) {
				return;
			}
			NPC npc = s.getEntityTracker().getNPC(e.getEntityId());
			e.remove();
			if (npc != null) {
				s.getEntityTracker().removeNPC(e.getEntityId());
				npc.despawn();
			}
			s.getEntityTracker().removeEntity(e.getEntityId());
			s.getEntityTracker().removeOldToNewId(entityId);
		});

		register(ActionType.WORLD_EFFECT, (s, a) -> {
			Effect effect = Effect.valueOf((String) a.get("effect"));
			Location location = a.getLocation(s);
			int data = a.getInt("data");
			location.getWorld().playEffect(location, effect, data);
		});

		register(ActionType.PARTICLE, (s, a) -> {
			int id = a.getInt("particleId");
			boolean longDis = (boolean) a.get("longDis");
			Location location = a.getLocation(s);
			Vector offset = Vector.deserialize((Map<String, Object>) a.get("offset"));
			float data = ((Double) a.get("data")).floatValue();
			int count = a.getInt("count");
			int[] dataArray = ((ArrayList<Double>) a.get("dataArray")).stream().mapToInt(Double::intValue).toArray();

			PacketContainer particle = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
			particle.getParticles().write(0, Particle.getById(id));
			particle.getBooleans().write(0, longDis);
			particle.getFloat().write(0, (float) location.getX()).write(1, (float) location.getY())
					.write(2, (float) location.getZ()).write(3, (float) offset.getX()).write(4, (float) offset.getY())
					.write(5, (float) offset.getZ()).write(6, data);
			particle.getIntegers().write(0, count);
			particle.getIntegerArrays().write(0, dataArray);

			try {
				ProtocolLibrary.getProtocolManager().broadcastServerPacket(particle, location, 64);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		register(ActionType.EXPLOSION, (s, a) -> {
			Location location = a.getLocation(s);
			List<Map<String, Object>> blockList = (List<Map<String, Object>>) a.get("blocks");
			List<Block> blocks = blockList.stream().map(m -> {
				// TODO relative location
				Vector v = Vector.deserialize(m);
				return v.toLocation(Bukkit.getWorlds().get(0)).getBlock();
			}).collect(Collectors.toList());

			// World#createExplosion allows to modify affected blocks only via events and it
			// drops items from blocks, so i think its better to send the explosion packet
			// directly

			PacketContainer explosion = new PacketContainer(PacketType.Play.Server.EXPLOSION);
			explosion.getDoubles().write(0, location.getX()).write(1, location.getY()).write(2, location.getZ());
			explosion.getFloat().write(0, 4F); // Radius, unused by clients
			explosion.getBlockPositionCollectionModifier().write(0, Collections.emptyList()); // Block removal is
																								// handled below
			try {
				ProtocolLibrary.getProtocolManager().broadcastServerPacket(explosion, location, 64);
			} catch (Exception e) {
				e.printStackTrace();
			}

			blocks.forEach(b -> {
				b.setType(Material.AIR);
			});
		});
	}

	public static void register(ActionType type, BiConsumer<ReplayingSession, ActionData> handler) {
		if (HANDLERS.containsKey(type)) {
			HANDLERS.remove(type);
		}
		HANDLERS.put(type, handler);
	}

	public static void handle(ReplayingSession session, ActionData action) {
		Optional.ofNullable(HANDLERS.get(action.getType()))
				.orElseThrow(() -> new IllegalStateException("Handler not found for ActionType " + action.getType()))
				.accept(session, action);
	}

}
