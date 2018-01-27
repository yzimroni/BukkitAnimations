package net.yzimroni.bukkitanimations.play;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

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
		// Using a new list to prevent ConcurrentModificationException
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

	public boolean isAnimationEntity(Entity e) {
		return e.hasMetadata("animationEntity");
	}

	@EventHandler
	public void onCreatureSpawn(PlayerEggThrowEvent e) {
		if (Utils.NPCREGISTRY.isNPC(e.getPlayer())) {
			e.setHatching(false);
		}
	}

	@EventHandler
	public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent e) {
		if (isAnimationEntity(e.getEntity())) {
			e.setCancelled(true);
			e.setTarget(null);
		}
	}

	@EventHandler
	public void onHangingBreak(HangingBreakEvent e) {
		if (isAnimationEntity(e.getEntity())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (isAnimationEntity(e.getRightClicked())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
		if (isAnimationEntity(e.getEntity())) {
			if (!(isAnimationEntity(e.getDamager()) || Utils.NPCREGISTRY.isNPC(e.getDamager()))) {
				e.setCancelled(true);
			}
		}
	}

}
