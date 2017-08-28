package net.yzimroni.bukkitanimations.play;

import java.util.HashMap;

import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;
import net.yzimroni.bukkitanimations.utils.Utils;

public class EntityTracker {

	private ReplayingSession session;
	private HashMap<Integer, Integer> oldToNewId = new HashMap<>();
	private HashMap<Integer, Entity> entities = new HashMap<>();
	private HashMap<Integer, NPC> npcs = new HashMap<>();

	public EntityTracker(ReplayingSession session) {
		super();
		this.session = session;
	}

	public void addOldToNewId(int old, int new_) {
		if (oldToNewId.containsKey(old)) {
			oldToNewId.remove(old);
		}
		oldToNewId.put(old, new_);
	}

	public void addNPC(NPC npc) {
		npcs.put(npc.getEntity().getEntityId(), npc);
	}

	public void addEntity(Entity entity) {
		entities.put(entity.getEntityId(), entity);
	}

	public Entity getEntityForOldId(int oldId) {
		int id = getNewId(oldId);
		if (npcs.containsKey(id)) {
			return npcs.get(id).getEntity();
		}
		if (entities.containsKey(id)) {
			return entities.get(id);
		}
		return null;
	}

	public int getNewId(int old) {
		return oldToNewId.get(old);
	}

	public NPC getNPC(int id) {
		return npcs.get(id);
	}

	public Entity getEntity(int id) {
		return entities.get(id);
	}

	public void removeNPC(int id) {
		npcs.remove(id);
	}

	public void removeEntity(int id) {
		entities.remove(id);
	}

	public void removeOldToNewId(int oldId) {
		oldToNewId.remove(oldId);
	}

	public void onEnd() {
		npcs.values().forEach(n -> {
			n.despawn();
			n.destroy();
			Utils.NPCREGISTRY.deregister(n);
		});
		npcs.clear();
		entities.values().forEach(Entity::remove);
		entities.clear();
	}

}
