package net.yzimroni.bukkitanimations.play;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.data.action.ActionType;
import net.yzimroni.bukkitanimations.utils.NMSUtils;
import net.yzimroni.bukkitanimations.utils.Utils;

public class ActionHandler {

	private static final HashMap<ActionType, BiConsumer<ReplayingSession, ActionData>> HANDLERS = new HashMap<>();

	static {
		registerDefaultHandlers();
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private static void registerDefaultHandlers() {
		register(ActionType.BLOCK_BREAK, (s, a) -> {
			a.getLocation(s).getBlock().setType(Material.AIR);
		});
		register(ActionType.BLOCK_PLACE, (s, a) -> {
			Block b = a.getLocation(s).getBlock();
			b.setType(Material.valueOf((String) a.getData("type")));
			b.setData(((Number) a.getData("data")).byteValue());
		});
		register(ActionType.SIGN_UPDATE, (s, a) -> {
			Block b = a.getLocation(s).getBlock();
			if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
				Sign sign = (Sign) b.getState();
				String[] lines = (String[]) ((List<String>) a.getData("lines")).toArray(new String[0]);
				for (int i = 0; i < lines.length; i++) {
					sign.setLine(i, lines[i]);
				}
				sign.update(true);
			}
		});
		register(ActionType.LIGHTNING_STRIKE, (s, a) -> {
			Location l = a.getLocation(s);
			l.getWorld().strikeLightningEffect(l);
		});

		register(ActionType.SPAWN_ENTITY, (s, a) -> {
			Location location = a.getLocation(s);
			String name = (String) a.getData("name");
			int entityId = ((Number) a.getData("entityId")).intValue();
			Vector velocity = Vector.deserialize((Map<String, Object>) a.getData("velocity"));
			EntityType type = EntityType.valueOf((String) a.getData("type"));
			NPC npc = Utils.NPCREGISTRY.createNPC(type, name);
			npc.spawn(location);
			Entity e = npc.getEntity();
			e.setVelocity(velocity);

			e.setCustomNameVisible((boolean) a.getData("customNameVisble"));
			e.setFireTicks(((Number) a.getData("fireTicks")).intValue());
			if (e instanceof LivingEntity) {
				LivingEntity l = (LivingEntity) e;
				l.getEquipment().setArmorContents(a.getItemStackList("armor"));
				l.getEquipment().setItemInHand(a.getItemStack("itemInHand"));
				ArrayList<Map<String, Object>> potions = (ArrayList<Map<String, Object>>) a.getData("potions");
				potions.forEach(m -> {
					// Fixes Gson decode bug
					m.put("effect", ((Number) m.get("effect")).intValue());
					m.put("duration", ((Number) m.get("duration")).intValue());
					m.put("amplifier", ((Number) m.get("amplifier")).intValue());
				});
				potions.stream().map(PotionEffect::new).forEach(potion -> {
					potion.apply(l);
				});
				// TODO potion effects
				if (e instanceof Player) {
					Player p = (Player) e;
					boolean flying = (boolean) a.getData("flying");
					npc.setFlyable(flying);
					p.setAllowFlight(flying);
					p.setFlying(flying);
				}
			}
			if (e instanceof Item) {
				((Item) e).setItemStack(a.getItemStack("item"));
				((Item) e).setPickupDelay(-1);
			}
			if (e instanceof Projectile) {
				Entity shooter = s.getEntityTracker().getEntityForOldId(((Number) a.getData("shooterId")).intValue());
				if (shooter != null && shooter instanceof ProjectileSource) {
					((Projectile) e).setShooter((ProjectileSource) shooter);
				}
			}
			s.getEntityTracker().addOldToNewId(entityId, e.getEntityId());
			s.getEntityTracker().addNPC(npc);
		});

		register(ActionType.ENTITY_MOVE, (s, a) -> {
			int entityId = ((Number) a.getData("entityId")).intValue();
			Location location = a.getLocation(s);
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			e.teleport(location);
			if (e.hasMetadata("NPC")) {
				NMS.setHeadYaw(NMS.getHandle(e), location.getYaw());
			}
		});
		register(ActionType.ENTITY_DAMAGE, (s, a) -> {
			int entityId = ((Number) a.getData("entityId")).intValue();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			if (e instanceof Damageable) {
				((Damageable) e).damage(0);
			} else {
				throw new IllegalStateException("Cannot damage entity " + entityId + ", entity not damageable");
			}
		});

		register(ActionType.PLAYER_ANIMATION, (s, a) -> {
			int entityId = ((Number) a.getData("entityId")).intValue();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			PlayerAnimationType type = PlayerAnimationType.valueOf((String) a.getData("type"));
			if (type == PlayerAnimationType.ARM_SWING) {
				PlayerAnimation.ARM_SWING.play((Player) e);
			}
		});
		register(ActionType.ENTITY_PICKUP, (s, a) -> {
			int entityId = ((Number) a.getData("entityId")).intValue();
			Entity pickedUp = s.getEntityTracker().getEntityForOldId(entityId);
			int playerId = ((Number) a.getData("playerId")).intValue();
			Entity player = s.getEntityTracker().getEntityForOldId(playerId);
			if (player != null) {
				NMSUtils.pickUp(pickedUp, (Player) player);
			}
			handle(s, new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", entityId));
		});
		register(ActionType.UPDATE_ENTITY_ITEM, (s, a) -> {
			int entityId = a.getEntityId();
			Entity update = s.getEntityTracker().getEntityForOldId(entityId);
			ItemStack item = ItemStack.deserialize((Map<String, Object>) a.getData("item"));
			if (update instanceof Item) {
				((Item) update).setItemStack(item);
			}
		});
		register(ActionType.UPDATE_EQUIPMENT, (s, a) -> {
			int entityId = a.getEntityId();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			if (e instanceof LivingEntity) {
				if (a.hasData("itemInHand")) {
					((LivingEntity) e).getEquipment().setItemInHand(a.getItemStack("itemInHand"));
				}
				if (a.hasData("armor")) {
					((LivingEntity) e).getEquipment().setArmorContents(a.getItemStackList("armor"));
				}
			}
		});

		register(ActionType.ENTITY_DEATH, (s, a) -> {
			//TODO Play the full death animation
			int entityId = a.getEntityId();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			((Damageable) e).damage(Integer.MAX_VALUE);
		});
		register(ActionType.DESPAWN_ENTITY, (s, a) -> {
			int entityId = ((Number) a.getData("entityId")).intValue();
			Entity e = s.getEntityTracker().getEntityForOldId(entityId);
			NPC npc = s.getEntityTracker().getNPC(e.getEntityId());
			e.remove();
			if (npc != null) {
				npc.despawn();
			}
			s.getEntityTracker().removeEntity(e.getEntityId());
			s.getEntityTracker().removeOldToNewId(entityId);
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
