package net.yzimroni.bukkitanimations.play;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.api.npc.NPC;
import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.data.action.ActionType;
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

		register(ActionType.SPAWN_PLAYER, (s, a) -> {
			Location location = a.getLocation(s);
			String name = (String) a.getData("name");
			int entityId = ((Number) a.getData("entityId")).intValue();
			NPC npc = Utils.NPCREGISTRY.createNPC(EntityType.PLAYER, name);
			npc.spawn(location);
			Entity e = npc.getEntity();

			e.setFireTicks(((Number) a.getData("fireTicks")).intValue());
			if (e instanceof LivingEntity) {
				LivingEntity l = (LivingEntity) e;
				// TODO potion effects and armor
			}
			s.getEntityTracker().addOldToNewId(entityId, e.getEntityId());
			s.getEntityTracker().addNPC(npc);
		});

		register(ActionType.ENTITY_MOVE, (s, a) -> {
			int entityId = ((Number) a.getData("entityId")).intValue();
			Location location = a.getLocation(s);
			s.getEntityTracker().getEntityForOldId(entityId).teleport(location);
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
