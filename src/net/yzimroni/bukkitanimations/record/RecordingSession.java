package net.yzimroni.bukkitanimations.record;

import java.util.UUID;

import org.bukkit.Location;

import com.google.common.base.Preconditions;

import net.yzimroni.bukkitanimations.data.Animation;

public class RecordingSession {

	private Animation animation;
	private boolean running;

	private int tick = 1;

	private Location minLocation;
	private Location maxLocation;

	public RecordingSession(String name, UUID uuid, Location min, Location max) {
		this.animation = new Animation(name, uuid);
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
		RecordingManager.get().onStart(this);
	}

	protected void tick() {
		if (!isRunning()) {
			return;
		}
		tick++;
	}

	public void stop() {
		if (!isRunning()) {
			return;
		}
		running = false;
		RecordingManager.get().onStop(this);
	}

	public boolean isRunning() {
		return running;
	}

	public Animation getAnimation() {
		return animation;
	}

}
