package net.yzimroni.bukkitanimations.record;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent e) {
		boolean toInside = session.isInside(e.getTo());
		boolean fromInside = session.isInside(e.getFrom());
		if (toInside && !fromInside) {
			if (!session.isEntityTracked(e.getPlayer())) {
				ActionData action = new ActionData(ActionType.SPAWN_ENTITY).entityData(e.getPlayer());
				session.addTrackedEntity(e.getPlayer());
				session.addAction(action);
			}
		} else if (!toInside && fromInside) {
			if (session.isEntityTracked(e.getPlayer())) {
				ActionData action = new ActionData(ActionType.DESPAWN_ENTITY).data("entityId",
						e.getPlayer().getEntityId());
				session.removeTrackedEntity(e.getPlayer());
				session.addAction(action);
			}
		} else if (toInside && fromInside) {
			if (session.isEntityTracked(e.getPlayer())) {
				ActionData action = new ActionData(ActionType.ENTITY_MOVE).data("entityId", e.getPlayer().getEntityId())
						.data("location", e.getPlayer().getLocation());
				session.addAction(action);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (session.isInside(e.getLocation())) {
			ActionData action = new ActionData(ActionType.SPAWN_ENTITY).entityData(e.getEntity());
			session.addTrackedEntity(e.getEntity());
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e) {
		if (session.isEntityTracked(e.getEntity())) {
			ActionData action = new ActionData(ActionType.ENTITY_DAMAGE).data("entityId", e.getEntity().getEntityId());
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAnimation(PlayerAnimationEvent e) {
		if (session.isEntityTracked(e.getPlayer())) {
			ActionData action = new ActionData(ActionType.PLAYER_ANIMATION)
					.data("entityId", e.getPlayer().getEntityId()).data("type", e.getAnimationType());
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemDropped(PlayerDropItemEvent e) {
		if (session.isInside(e.getItemDrop().getLocation())) {
			ActionData action = new ActionData(ActionType.SPAWN_ENTITY).entityData(e.getItemDrop());
			session.addTrackedEntity(e.getItemDrop());
			session.addAction(action);
		}

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemMerge(ItemMergeEvent e) {
		if (session.isEntityTracked(e.getEntity())) {
			ActionData action = new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", e.getEntity().getEntityId());
			session.removeTrackedEntity(e.getEntity());
			session.addAction(action);
		}
		ItemStack clone = e.getTarget().getItemStack().clone();
		clone.setAmount(clone.getAmount() + e.getEntity().getItemStack().getAmount());
		ActionData action = new ActionData(ActionType.UPDATE_ENTITY_ITEM).data("entityId", e.getTarget().getEntityId())
				.data("item", clone);
		session.addAction(action);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemPickedUp(PlayerPickupItemEvent e) {
		if (session.isEntityTracked(e.getItem())) {
			ActionData action = new ActionData(ActionType.ENTITY_PICKUP).data("entityId", e.getItem().getEntityId())
					.data("playerId", e.getPlayer().getEntityId());
			session.removeTrackedEntity(e.getItem());
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent e) {
		if (session.isEntityTracked(e.getEntity())) {
			ActionData action = new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", e.getEntity().getEntityId());
			session.removeTrackedEntity(e.getEntity());
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemDespawn(ItemDespawnEvent e) {
		if (session.isEntityTracked(e.getEntity())) {
			ActionData action = new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", e.getEntity().getEntityId());
			session.removeTrackedEntity(e.getEntity());
			session.addAction(action);
		}
	}
}
