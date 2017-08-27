package net.yzimroni.bukkitanimations.utils;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;

public class EmptyNPCDataStore implements NPCDataStore {

	private int id = 0;

	@Override
	public void clearData(NPC arg0) {

	}

	@Override
	public int createUniqueNPCId(NPCRegistry arg0) {
		return ++id;
	}

	@Override
	public void loadInto(NPCRegistry arg0) {

	}

	@Override
	public void saveToDisk() {

	}

	@Override
	public void saveToDiskImmediate() {

	}

	@Override
	public void store(NPC arg0) {

	}

	@Override
	public void storeAll(NPCRegistry arg0) {

	}

}
