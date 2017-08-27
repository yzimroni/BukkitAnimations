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

	private List<RecordingSession> recordings = new ArrayList<RecordingSession>();

	public RecordingManager() {
		instance = this;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitAnimationsPlugin.get(),
				() -> recordings.forEach(RecordingSession::tick), 1, 1);
	}

	public static RecordingManager get() {
		return instance;
	}

	public void disable() {
		new ArrayList<RecordingSession>(recordings).forEach(RecordingSession::stop);
	}

	protected void onStart(RecordingSession session) {
		recordings.add(session);
	}

	protected void onStop(RecordingSession session) {
		recordings.remove(session);
	}

	public List<RecordingSession> getSessionsByUUID(UUID uuid) {
		return recordings.stream().filter(s -> uuid.equals(s.getAnimation().getPlayer())).collect(Collectors.toList());
	}

}
