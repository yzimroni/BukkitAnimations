package net.yzimroni.bukkitanimations.record;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Objects;

import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.data.action.ActionType;

public class EventRecorder implements Listener {

	private RecordingSession session;

	public EventRecorder(RecordingSession session) {
		super();
		this.session = session;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (session.isInside(e.getBlock().getLocation())) {
			session.addAction(new ActionData(ActionType.BLOCK_BREAK).data("location", e.getBlock().getLocation())
					.data("player", e.getPlayer().getEntityId()));
			if (e.getPlayer().getUniqueId().equals(session.getAnimation().getPlayer())) {
				session.addAction(new ActionData(ActionType.WORLD_EFFECT).data("effect", Effect.STEP_SOUND)
						.data("location", e.getBlock().getLocation()).data("data", e.getBlock().getType().getId())
						.data("disableRel", false));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent e) {
		if (session.isInside(e.getBlock().getLocation())) {
			session.addAction(new ActionData(ActionType.BLOCK_BREAK).data("location", e.getBlock().getLocation()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockPlaceEvent e) {
		if (session.isInside(e.getBlock().getLocation())) {
			session.addAction(new ActionData(ActionType.BLOCK_PLACE).blockData(e.getBlock()).data("player",
					e.getPlayer().getEntityId()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
		e.getReplacedBlockStates().forEach(b -> {
			if (!b.getLocation().equals(e.getBlockPlaced().getLocation())) {
				if (session.isInside(b.getLocation())) {
					session.addAction(new ActionData(ActionType.BLOCK_PLACE).blockData(b.getBlock()).blockStateType(b));
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onStructureGrow(StructureGrowEvent e) {
		e.getBlocks().forEach(b -> {
			if (session.isInside(b.getLocation())) {
				session.addAction(new ActionData(ActionType.BLOCK_PLACE).blockData(b.getBlock()).blockStateType(b));
				// TODO Use Multi Block Change insted of block place action for every block?
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent e) {
		if (session.isInside(e.getBlock().getLocation())) {
			session.addAction(new ActionData(ActionType.UPDATE_BLOCKSTATE).data("location", e.getBlock().getLocation())
					.data("lines", e.getLines()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
		Block target = e.getBlockClicked().getRelative(e.getBlockFace());
		if (session.isInside(target.getLocation())) {
			Material type = null;
			switch (e.getBucket()) {
				case WATER_BUCKET:
					type = Material.WATER;
					break;
				case LAVA_BUCKET:
					type = Material.LAVA;
					break;
				default:
					break;
			}
			if (type != null) {
				session.addAction(new ActionData(ActionType.BLOCK_PLACE).blockData(target).data("type", type));
				session.addAction(new ActionData(ActionType.UPDATE_ENTITY).data("entityId", e.getPlayer().getEntityId())
						.data("itemInHand", e.getItemStack()));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent e) {
		Block target = e.getBlockClicked().getRelative(e.getBlockFace());
		if (session.isInside(target.getLocation())) {
			session.addAction(new ActionData(ActionType.BLOCK_PLACE).blockData(target).data("type", Material.AIR));
			session.addAction(new ActionData(ActionType.UPDATE_ENTITY).data("entityId", e.getPlayer().getEntityId())
					.data("itemInHand", e.getItemStack()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent e) {
		if (session.isInside(e.getBlock().getLocation())) {
			System.out.println(e.getNewState());
			session.addAction(
					new ActionData(ActionType.BLOCK_PLACE).blockData(e.getBlock()).blockStateType(e.getNewState()));
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
		session.handleEntityMove(e.getPlayer(), e.getFrom(), e.getTo());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		session.handleEntityMove(e.getPlayer(), e.getFrom(), e.getTo());
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
	public void onHangingPlace(HangingPlaceEvent e) {
		if (session.isInside(e.getEntity().getLocation()) && session.isInside(e.getBlock().getLocation())) {
			ActionData action = new ActionData(ActionType.SPAWN_ENTITY).entityData(e.getEntity());
			session.addTrackedEntity(e.getEntity());
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSheepDyeWool(SheepDyeWoolEvent e) {
		if (session.isEntityTracked(e.getEntity())) {
			ActionData action = new ActionData(ActionType.UPDATE_ENTITY).data("entityId", e.getEntity().getEntityId())
					.data("color", e.getColor());
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
	public void onPlayerItemHeld(PlayerItemHeldEvent e) {
		if (session.isEntityTracked(e.getPlayer())) {
			ItemStack old = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
			ItemStack new_ = e.getPlayer().getInventory().getItem(e.getNewSlot());
			if (!Objects.equal(old, new_)) {
				if (new_ == null) {
					new_ = new ItemStack(Material.AIR);
				}
				ActionData action = new ActionData(ActionType.UPDATE_ENTITY)
						.data("entityId", e.getPlayer().getEntityId()).data("itemInHand", new_);
				session.addAction(action);
			}
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
	public void onItemSpawn(ItemSpawnEvent e) {
		if (session.isInside(e.getEntity().getLocation())) {
			ActionData action = new ActionData(ActionType.SPAWN_ENTITY).entityData(e.getEntity());
			session.addTrackedEntity(e.getEntity());
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if (session.isInside(e.getEntity().getLocation())) {
			ActionData action = new ActionData(ActionType.SHOOT_PROJECTILE).entityData(e.getEntity());
			session.addTrackedEntity(e.getEntity());
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
		if (session.isEntityTracked(e.getTarget())) {
			ItemStack clone = e.getTarget().getItemStack().clone();
			clone.setAmount(clone.getAmount() + e.getEntity().getItemStack().getAmount());
			ActionData action = new ActionData(ActionType.UPDATE_ENTITY).entityData(e.getTarget(), Item.class);
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent e) {
		if (session.isEntityTracked(e.getEntity())) {
			ActionData action = new ActionData(ActionType.ENTITY_DEATH).data("entityId", e.getEntity().getEntityId());
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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHangingBreak(HangingBreakEvent e) {
		if (session.isEntityTracked(e.getEntity())) {
			ActionData action = new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", e.getEntity().getEntityId());
			session.removeTrackedEntity(e.getEntity());
			session.addAction(action);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerToggleFlying(PlayerToggleFlightEvent e) {
		if (session.isEntityTracked(e.getPlayer())) {
			session.addAction(new ActionData(ActionType.UPDATE_ENTITY).data("entityId", e.getPlayer().getEntityId())
					.data("flying", e.isFlying()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerToggleSprinting(PlayerToggleSprintEvent e) {
		if (session.isEntityTracked(e.getPlayer())) {
			session.addAction(new ActionData(ActionType.UPDATE_ENTITY).data("entityId", e.getPlayer().getEntityId())
					.data("sprinting", e.isSprinting()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerToggleFlying(PlayerToggleSneakEvent e) {
		if (session.isEntityTracked(e.getPlayer())) {
			session.addAction(new ActionData(ActionType.UPDATE_ENTITY).data("entityId", e.getPlayer().getEntityId())
					.data("sneaking", e.isSneaking()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (session.isInside(e.getPlayer().getLocation())) {
			session.addAction(new ActionData(ActionType.SPAWN_ENTITY).entityData(e.getPlayer()));
			session.addTrackedEntity(e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (session.isEntityTracked(e.getPlayer())) {
			session.addAction(new ActionData(ActionType.DESPAWN_ENTITY).data("entityId", e.getPlayer().getEntityId()));
			session.removeTrackedEntity(e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerShearEntity(PlayerShearEntityEvent e) {
		if (session.isEntityTracked(e.getEntity())) {
			session.addAction(new ActionData(ActionType.UPDATE_ENTITY).data("entityId", e.getEntity().getEntityId())
					.data("sheared", true));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (session.isEntityTracked(e.getRightClicked())) {
			String equipmentSlotName = null;
			switch (e.getSlot()) {
				case HAND:
					equipmentSlotName = "itemInHand";
					break;
				case HEAD:
					equipmentSlotName = "helmet";
					break;
				case CHEST:
					equipmentSlotName = "chestplate";
					break;
				case LEGS:
					equipmentSlotName = "leggings";
					break;
				case FEET:
					equipmentSlotName = "boots";
					break;
				default:
					throw new IllegalArgumentException("Unknown EquipmentSlot: " + e.getSlot());
			}
			session.addAction(new ActionData(ActionType.UPDATE_ENTITY)
					.data("entityId", e.getRightClicked().getEntityId()).data(equipmentSlotName, e.getPlayerItem()));
			if (session.isEntityTracked(e.getPlayer())) {
				session.addAction(new ActionData(ActionType.UPDATE_ENTITY).data("entityId", e.getPlayer().getEntityId())
						.data("itemInHand", e.getArmorStandItem()));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent e) {
		recordExplosion(e.getBlock().getLocation(), e.blockList());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
		recordExplosion(e.getLocation(), e.blockList());
	}

	private void recordExplosion(Location location, List<Block> blocks) {
		// TODO relative location
		session.addAction(new ActionData(ActionType.EXPLOSION).data("location", location).data("blocks",
				blocks.stream().map(Block::getLocation).map(Location::toVector).collect(Collectors.toList())));
	}

}
