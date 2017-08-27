package net.yzimroni.bukkitanimations.record;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;

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

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onStructureGrow(StructureGrowEvent e) {
		e.getBlocks().forEach(b -> {
			if (session.isInside(b.getLocation())) {
				session.addAction(new ActionData(ActionType.BLOCK_PLACE).data("location", b.getLocation())
						.data("type", b.getType()).data("data", b.getData().getData())); // TODO Use Multi Block Change
																							// insted of block place
																							// action for every block?
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent e) {
		if (session.isInside(e.getBlock().getLocation())) {
			session.addAction(new ActionData(ActionType.SIGN_UPDATE).data("location", e.getBlock().getLocation())
					.data("lines", e.getLines()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLightningStrike(LightningStrikeEvent e) {
		if (session.isInside(e.getLightning().getLocation())) {
			session.addAction(
					new ActionData(ActionType.LIGHTNING_STRIKE).data("location", e.getLightning().getLocation()));
		}
	}

}
