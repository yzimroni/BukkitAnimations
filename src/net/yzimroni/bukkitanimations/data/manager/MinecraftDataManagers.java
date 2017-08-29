package net.yzimroni.bukkitanimations.data.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.utils.Utils;

public class MinecraftDataManagers {

	private static final DataManager ENTITIES = new DataManager();
	private static final DataManager BLOCKS = new DataManager();

	static {
		createEntities();
		createBlocks();
	}

	private static void createEntities() {
		ENTITIES.setGlobalDataHandler(new DataHandler<Entity>() {

			@Override
			public void save(ActionData action, Entity object) {
				if (!action.has("entityId")) {
					action.data("entityId", object.getEntityId());
				}
			}

			@Override
			public void load(ActionData action, Entity object) {

			}

		});
		ENTITIES.register(Entity.class, new DataHandler<Entity>() {

			@Override
			public void save(ActionData action, Entity e) {
				action.data("location", e.getLocation()).data("entityId", e.getEntityId()).data("uuid", e.getUniqueId())
						.data("name", e.getName()).data("customName", e.getCustomName())
						.data("fireTicks", e.getFireTicks()).data("type", e.getType())
						.data("customNameVisble", e.isCustomNameVisible()).data("velocity", e.getVelocity());
			}

			@Override
			public void load(ActionData action, Entity e) {
				if (action.has("velocity")) {
					@SuppressWarnings("unchecked")
					Vector velocity = Vector.deserialize((Map<String, Object>) action.getData("velocity"));
					e.setVelocity(velocity);
				}
				if (action.has("customNameVisble")) {
					e.setCustomNameVisible((boolean) action.getData("customNameVisble"));
				}
				if (action.has("fireTicks")) {
					e.setFireTicks(((Number) action.getData("fireTicks")).intValue());
				}
			}
		});
		ENTITIES.register(LivingEntity.class, new DataHandler<LivingEntity>() {

			@Override
			public void save(ActionData action, LivingEntity l) {
				action.data("potions",
						l.getActivePotionEffects().stream().map(PotionEffect::serialize).collect(Collectors.toList()))
						.data("armor", l.getEquipment().getArmorContents())
						.data("itemInHand", l.getEquipment().getItemInHand());
			}

			@Override
			public void load(ActionData a, LivingEntity l) {
				if (a.has("armor")) {
					l.getEquipment().setArmorContents(a.getItemStackList("armor"));
				}
				if (a.has("itemInHand")) {
					l.getEquipment().setItemInHand(a.getItemStack("itemInHand"));
				}
				if (a.has("potions")) {
					@SuppressWarnings("unchecked")
					ArrayList<Map<String, Object>> potions = (ArrayList<Map<String, Object>>) a.getData("potions");
					potions.forEach(m -> {
						// Fixes Gson decode bug
						m.put("effect", ((Number) m.get("effect")).intValue());
						m.put("duration", ((Number) m.get("duration")).intValue());
						m.put("amplifier", ((Number) m.get("amplifier")).intValue());
					});
					potions.stream().map(PotionEffect::new).forEach(potion -> {
						potion.apply(l);
					});
				}
			}
		});
		ENTITIES.register(Player.class, new DataHandler<Player>() {

			@Override
			public void save(ActionData action, Player p) {
				action.data("flying", p.isFlying());
			}

			@Override
			public void load(ActionData a, Player p) {
				if (a.has("flying")) {
					boolean flying = (boolean) a.getData("flying");
					if (Utils.NPCREGISTRY.isNPC(p)) {
						Utils.NPCREGISTRY.getNPC(p).setFlyable(flying);
					}
					p.setAllowFlight(flying);
					p.setFlying(flying);
				}
			}
		});
		ENTITIES.register(Item.class, new DataHandler<Item>() {

			@Override
			public void save(ActionData action, Item i) {
				action.data("item", i.getItemStack());
			}

			@Override
			public void load(ActionData a, Item i) {
				i.setItemStack(a.getItemStack("item"));
				i.setPickupDelay(-1);
			}
		});
		ENTITIES.register(Colorable.class, new DataHandler<Colorable>() {

			@Override
			public void save(ActionData action, Colorable c) {
				action.data("color", c.getColor().name());
			}

			@Override
			public void load(ActionData action, Colorable c) {
				if (action.has("color")) {
					c.setColor(DyeColor.valueOf((String) action.getData("color")));
				}
			}
		});
		ENTITIES.register(Ageable.class, new DataHandler<Ageable>() {

			@Override
			public void save(ActionData action, Ageable a) {
				action.data("age", a.getAge()).data("ageLocked", a.getAgeLock());
			}

			@Override
			public void load(ActionData action, Ageable a) {
				if (action.has("age")) {
					a.setAge(action.getInt("age"));
				}
				if (action.has("ageLocked")) {
					a.setAgeLock((boolean) action.getData("ageLocked"));
				}
			}

		});
		ENTITIES.register(Creeper.class, new DataHandler<Creeper>() {

			@Override
			public void save(ActionData action, Creeper object) {
				action.data("powered", object.isPowered());
			}

			@Override
			public void load(ActionData action, Creeper object) {
				if (action.has("powered")) {
					object.setPowered((boolean) action.getData("powered"));
				}
			}
		});
		ENTITIES.register(Enderman.class, new DataHandler<Enderman>() {

			@Override
			public void save(ActionData action, Enderman object) {
				action.data("carriedMaterial", object.getCarriedMaterial());
			}

			@Override
			public void load(ActionData action, Enderman object) {
				if (action.has("carriedMaterial")) {
					object.setCarriedMaterial((MaterialData) action.getData("carriedMaterial"));
				}
			}

		});
		ENTITIES.register(FallingBlock.class, new DataHandler<FallingBlock>() {

			@SuppressWarnings("deprecation")
			@Override
			public void save(ActionData action, FallingBlock object) {
				action.data("type", object.getMaterial()).data("data", object.getBlockData());
			}

			@Override
			public void load(ActionData action, FallingBlock object) {

			}
		});
		ENTITIES.register(Fireball.class, new DataHandler<Fireball>() {

			@Override
			public void save(ActionData action, Fireball object) {
				action.data("direction", object.getDirection());
			}

			@SuppressWarnings("unchecked")
			@Override
			public void load(ActionData action, Fireball object) {
				if (action.has("direction")) {
					object.setDirection(Vector.deserialize((Map<String, Object>) action.getData("direction")));
				}
			}

		});
		ENTITIES.register(Projectile.class, new DataHandler<Projectile>() {

			@Override
			public void save(ActionData action, Projectile p) {
				int shooterId = -1;
				if (p.getShooter() instanceof Entity) {
					shooterId = ((Entity) p.getShooter()).getEntityId();
				}
				action.data("shooterId", shooterId);
			}

			@Override
			public void load(ActionData action, Projectile object) {

			}
		});

		/*
		 * TODO AreaEffectCloud ArmorStand
		 */
	}

	private static void createBlocks() {
		BLOCKS.setGlobalDataHandler(new DataHandler<Object>() {

			@Override
			public void save(ActionData action, Object object) {
				Location location = null;
				if (object instanceof Block) {
					location = ((Block) object).getLocation();
				} else if (object instanceof BlockState) {
					location = ((BlockState) object).getLocation();
				}
				if (!action.has("location")) {
					action.data("location", location);
				}
			}

			@Override
			public void load(ActionData action, Object object) {

			}
		});
		BLOCKS.register(Block.class, new DataHandler<Block>() {

			@SuppressWarnings("deprecation")
			@Override
			public void save(ActionData action, Block b) {
				action.data("location", b.getLocation()).data("type", b.getType()).data("data", b.getData());
			}

			@SuppressWarnings("deprecation")
			@Override
			public void load(ActionData a, Block b) {
				b.setType(Material.valueOf((String) a.getData("type")));
				b.setData(((Number) a.getData("data")).byteValue());
			}
		});
		BLOCKS.register(Sign.class, new DataHandler<Sign>() {

			@Override
			public void save(ActionData action, Sign object) {
				// TODO Auto-generated method stub

			}

			@Override
			public void load(ActionData action, Sign sign) {
				@SuppressWarnings("unchecked")
				String[] lines = (String[]) ((List<String>) action.getData("lines")).toArray(new String[0]);
				for (int i = 0; i < lines.length; i++) {
					sign.setLine(i, lines[i]);
				}
			}
		});
	}

	public static DataManager getEntities() {
		return ENTITIES;
	}

	public static DataManager getBlocks() {
		return BLOCKS;
	}

}
