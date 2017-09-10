package net.yzimroni.bukkitanimations.record;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import net.yzimroni.bukkitanimations.BukkitAnimationsPlugin;

public class RecordingManager implements Listener {

	private static RecordingManager instance;

	private List<Recorder> recordings = new ArrayList<Recorder>();

	public RecordingManager() {
		instance = this;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitAnimationsPlugin.get(),
				() -> recordings.forEach(Recorder::tick), 1, 1);
	}

	public static RecordingManager get() {
		return instance;
	}

	public void disable() {
		new ArrayList<Recorder>(recordings).stream().filter(RecordingSession.class::isInstance)
				.map(RecordingSession.class::cast).forEach(RecordingSession::stop);
	}

	protected void onStart(Recorder session) {
		recordings.add(session);
	}

	protected void onStop(Recorder session) {
		recordings.remove(session);
	}

	public List<Recorder> getSessionsByUUID(UUID uuid) {
		return recordings.stream().filter(s -> uuid.equals(s.getAnimation().getPlayer())).collect(Collectors.toList());
	}

}
