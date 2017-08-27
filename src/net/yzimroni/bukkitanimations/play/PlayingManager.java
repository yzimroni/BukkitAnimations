package net.yzimroni.bukkitanimations.play;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import net.yzimroni.bukkitanimations.BukkitAnimationsPlugin;

public class PlayingManager {

	private static PlayingManager instance;

	private List<ReplayingSession> replaying = new ArrayList<ReplayingSession>();

	public PlayingManager() {
		instance = this;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitAnimationsPlugin.get(),
				() -> replaying.forEach(ReplayingSession::tick), 1, 1);
	}

	public static PlayingManager get() {
		return instance;
	}

	public void disable() {
		new ArrayList<ReplayingSession>(replaying).forEach(ReplayingSession::stop);
	}

	protected void onStart(ReplayingSession session) {
		replaying.add(session);
	}

	protected void onStop(ReplayingSession session) {
		synchronized (session) {
			// replaying.remove(session); //TODO
		}
	}

}
