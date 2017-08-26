package net.yzimroni.bukkitanimations.data;

import java.util.UUID;

public class Animation {

	private String name;
	private UUID player;

	public Animation(String name, UUID player) {
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
