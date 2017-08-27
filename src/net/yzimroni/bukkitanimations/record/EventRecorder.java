package net.yzimroni.bukkitanimations.record;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.data.action.ActionType;

public class EventRecorder implements Listener {

	private RecordingSession session;

	public EventRecorder(RecordingSession session) {
		super();
		this.session = session;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (session.isInside(e.getBlock().getLocation())) {
			session.addAction(new ActionData(ActionType.BLOCK_BREAK).data("location", e.getBlock().getLocation())
					.data("player", e.getPlayer().getEntityId()));
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockPlaceEvent e) {
		if (session.isInside(e.getBlock().getLocation())) {
			session.addAction(new ActionData(ActionType.BLOCK_PLACE).data("location", e.getBlock().getLocation())
					.data("player", e.getPlayer().getEntityId()).data("type", e.getBlock().getType())
					.data("data", e.getBlock().getData()));
		}
	}

}
