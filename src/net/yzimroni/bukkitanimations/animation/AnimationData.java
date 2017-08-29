package net.yzimroni.bukkitanimations.animation;

import java.util.UUID;

public class AnimationData {

	private String name;
	private UUID player;

	public AnimationData(String name, UUID player) {
		super();
		this.name = name;
		this.player = player;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getPlayer() {
		return player;
	}

	public void setPlayer(UUID player) {
		this.player = player;
	}

}
