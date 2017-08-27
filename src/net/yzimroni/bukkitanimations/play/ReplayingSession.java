package net.yzimroni.bukkitanimations.play;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.yzimroni.bukkitanimations.data.Animation;
import net.yzimroni.bukkitanimations.data.action.ActionData;

public class ReplayingSession {

	private Animation animation;
	private boolean loop;

	private boolean running;
	private int tick = 1;
	private EntityTracker entityTracker = new EntityTracker(this);

	private List<ActionData> actions = new ArrayList<ActionData>();
	int index = 0;

	public ReplayingSession(Animation animation) {
		this.animation = animation;
		load();
	}

	public void load() {
		try {
			actions = new Gson().fromJson(new FileReader(animation.getName() + ".mcanimation"),
					new TypeToken<List<ActionData>>() {
					}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		}

		tick++;
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

}
