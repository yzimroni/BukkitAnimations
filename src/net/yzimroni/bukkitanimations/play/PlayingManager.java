package net.yzimroni.bukkitanimations.play;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;

import net.yzimroni.bukkitanimations.BukkitAnimationsPlugin;
import net.yzimroni.bukkitanimations.utils.Utils;

public class PlayingManager implements Listener {

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

	@EventHandler
	public void onCreatureSpawn(PlayerEggThrowEvent e) {
		if (Utils.NPCREGISTRY.isNPC(e.getPlayer())) {
			e.setHatching(false);
		}
	}

}
