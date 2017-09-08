package net.yzimroni.bukkitanimations.play;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import net.yzimroni.bukkitanimations.animation.Animation;
import net.yzimroni.bukkitanimations.data.action.ActionData;

public class ReplayingSession {

	private Animation animation;
	private boolean loop;
	private Location baseLocation;

	private boolean running;
	private int tick = 1;
	private EntityTracker entityTracker = new EntityTracker(this);

	private List<ActionData> actions = new ArrayList<ActionData>();
	int index = 0;

	public ReplayingSession(Animation animation, Location baseLocation) {
		this.animation = animation;
		this.actions = animation.getActions();
		this.baseLocation = baseLocation;
	}

	public void start() {
		if (isRunning()) {
			return;
		}
		running = true;
		PlayingManager.get().onStart(this);
	}

	public void tick() {
		if (!isRunning()) {
			return;
		}
		playTick();
	}

	private void playTick() {
		for (; index < actions.size(); index++) {
			ActionData action = actions.get(index);
			if (action.getTick() > tick) {
				break;
			}
			try {
				ActionHandler.handle(this, action);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (actions.size() == index) {
			stop();
			return;
		}

		tick++;
	}

	public Location getAbsoluteLocation(Location relative) {
		return relative.add(baseLocation);
	}

	public Location getAbsoluteLocation(Vector relative) {
		return baseLocation.clone().add(relative);
	}

	public void stop() {
		if (!isRunning()) {
			return;
		}
		running = false;
		entityTracker.onEnd();
		PlayingManager.get().onStop(this);
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public EntityTracker getEntityTracker() {
		return entityTracker;
	}

	public Location getBaseLocation() {
		return baseLocation;
	}

	public void setBaseLocation(Location baseLocation) {
		this.baseLocation = baseLocation;
	}

}
