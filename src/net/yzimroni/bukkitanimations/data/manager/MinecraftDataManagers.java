package net.yzimroni.bukkitanimations.data.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.material.Attachable;
import org.bukkit.material.Colorable;
import org.bukkit.material.FlowerPot;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Gravity;
import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.utils.NMSUtils;
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
					Vector velocity = Vector.deserialize((Map<String, Object>) action.get("velocity"));
					e.setVelocity(velocity);
				}
				if (action.has("customNameVisble")) {
					e.setCustomNameVisible((boolean) action.get("customNameVisble"));
				}
				if (action.has("fireTicks")) {
					e.setFireTicks(action.getInt("fireTicks"));
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
				// Armor can be changed using an array of all the armor items, or by specific
				// slot (used in armor stand manipulation)
				if (a.has("armor")) {
					l.getEquipment().setArmorContents(a.getItemStackList("armor"));
				}
				if (a.has("helmet")) {
					l.getEquipment().setHelmet(a.getItemStack("helmet"));
				}
				if (a.has("chestplate")) {
					l.getEquipment().setChestplate(a.getItemStack("chestplate"));
				}
				if (a.has("leggings")) {
					l.getEquipment().setLeggings(a.getItemStack("leggings"));
				}
				if (a.has("boots")) {
					l.getEquipment().setBoots(a.getItemStack("boots"));
				}

				if (a.has("itemInHand")) {
					l.getEquipment().setItemInHand(a.getItemStack("itemInHand"));
				}
				if (a.has("potions")) {
					@SuppressWarnings("unchecked")
					ArrayList<Map<String, Object>> potions = (ArrayList<Map<String, Object>>) a.get("potions");
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
				action.data("flying", p.isFlying()).data("sneaking", p.isSneaking()).data("sprinting", p.isSprinting());

				GameProfile profile = NMSUtils.getGameProfile(p);
				Property textures = profile.getProperties().get("textures").stream().findAny().orElse(null);
				if (textures != null) {
					action.data("textures", textures);
				}
			}

			@Override
			public void load(ActionData a, Player p) {
				if (a.has("flying")) {
					boolean flying = (boolean) a.get("flying");
					if (flying != p.isFlying()) {
						if (Utils.NPCREGISTRY.isNPC(p)) {
							NPC npc = Utils.NPCREGISTRY.getNPC(p);
							npc.setFlyable(flying);
							npc.getTrait(Gravity.class).gravitate(flying); // Should be true for no-gravity
						}
						p.setAllowFlight(flying);
						p.setFlying(flying);
					}
				}
				if (a.has("sneaking")) {
					p.setSneaking((boolean) a.get("sneaking"));
				}
				if (a.has("sprinting")) {
					p.setSprinting((boolean) a.get("sprinting"));
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
				if (a.has("item")) {
					i.setItemStack(a.getItemStack("item"));
					i.setPickupDelay(-1);
				}
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
					c.setColor(DyeColor.valueOf((String) action.get("color")));
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
					a.setAgeLock((boolean) action.get("ageLocked"));
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
					object.setPowered((boolean) action.get("powered"));
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
					object.setCarriedMaterial((MaterialData) action.get("carriedMaterial"));
				}
			}

		});
		ENTITIES.register(FallingBlock.class, new DataHandler<FallingBlock>() {

			@SuppressWarnings("deprecation")
			@Override
			public void save(ActionData action, FallingBlock object) {
				action.data("blockType", object.getMaterial()).data("data", object.getBlockData());
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
					object.setDirection(Vector.deserialize((Map<String, Object>) action.get("direction")));
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

		ENTITIES.register(Attachable.class, new DataHandler<Attachable>() {

			@Override
			public void save(ActionData action, Attachable object) {
				action.data("attachedFace", object.getAttachedFace());
			}

			@Override
			public void load(ActionData action, Attachable object) {
				if (action.has("attachedFace")) {
					object.setFacingDirection(BlockFace.valueOf((String) action.get("attachedFace")).getOppositeFace());
				}
			}
		});

		ENTITIES.register(ItemFrame.class, new DataHandler<ItemFrame>() {

			@Override
			public void save(ActionData action, ItemFrame frame) {
				action.data("item", frame.getItem()).data("rotation", frame.getRotation());
			}

			@Override
			public void load(ActionData action, ItemFrame frame) {
				if (action.has("item")) {
					frame.setItem(action.getItemStack("item"));
				}
				if (action.has("rotation")) {
					frame.setRotation(Rotation.valueOf((String) action.get("rotation")));
				}
			}
		});

		ENTITIES.register(Painting.class, new DataHandler<Painting>() {

			@Override
			public void save(ActionData action, Painting object) {
				action.data("art", object.getArt());
			}

			@Override
			public void load(ActionData action, Painting object) {
				if (action.has("art")) {
					object.setArt(Art.valueOf((String) action.get("art")), true);
				}
			}
		});

		ENTITIES.register(ArmorStand.class, new DataHandler<ArmorStand>() {

			@Override
			public void save(ActionData action, ArmorStand stand) {
				action.data("headPose", stand.getHeadPose()).data("bodyPose", stand.getBodyPose())
						.data("leftArmPose", stand.getLeftArmPose()).data("rightArmPose", stand.getRightArmPose())
						.data("leftLegPose", stand.getLeftLegPose()).data("rightLeftPose", stand.getRightLegPose());

				action.data("basePlate", stand.hasBasePlate()).data("visible", stand.isVisible())
						.data("arms", stand.hasArms()).data("small", stand.isSmall()).data("marker", stand.isMarker());

				// Item in hand and armor are saved in the LivingEntity data handler
			}

			@Override
			public void load(ActionData action, ArmorStand stand) {
				if (action.has("headPose")) {
					stand.setHeadPose(getEulerAngle(action, "headPose"));
					stand.setBodyPose(getEulerAngle(action, "bodyPose"));
					stand.setLeftArmPose(getEulerAngle(action, "leftArmPose"));
					stand.setRightArmPose(getEulerAngle(action, "rightArmPose"));
					stand.setLeftLegPose(getEulerAngle(action, "leftLegPose"));
					stand.setRightLegPose(getEulerAngle(action, "rightLeftPose"));

					stand.setBasePlate((boolean) action.get("basePlate"));
					stand.setVisible((boolean) action.get("visible"));
					stand.setArms((boolean) action.get("arms"));
					stand.setSmall((boolean) action.get("small"));
					stand.setMarker((boolean) action.get("marker"));
				}
			}

			private EulerAngle getEulerAngle(ActionData action, String name) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) action.get(name);
				return new EulerAngle((double) map.get("x"), (double) map.get("y"), (double) map.get("z"));
			}
		});

		ENTITIES.register(Horse.class, new DataHandler<Horse>() {

			@Override
			public void save(ActionData action, Horse object) {
				action.data("color", object.getColor()).data("style", object.getStyle());
			}

			@Override
			public void load(ActionData action, Horse object) {
				if (action.has("color")) {
					object.setColor(Color.valueOf((String) action.get("color")));
				}
				if (action.has("style")) {
					object.setStyle(Style.valueOf((String) action.get("style")));
				}
			}
		});

		ENTITIES.register(Minecart.class, new DataHandler<Minecart>() {

			@Override
			public void save(ActionData action, Minecart object) {
				action.data("displayBlock", object.getDisplayBlock().toItemStack()).data("displayBlockOffset",
						object.getDisplayBlockOffset());
			}

			@Override
			public void load(ActionData action, Minecart object) {
				object.setDisplayBlock(action.getItemStack("displayBlock").getData());
				object.setDisplayBlockOffset(action.getInt("displayBlockOffset"));
			}
		});

		ENTITIES.register(Ocelot.class, new DataHandler<Ocelot>() {

			@Override
			public void save(ActionData action, Ocelot object) {
				action.data("catType", object.getCatType());
			}

			@Override
			public void load(ActionData action, Ocelot object) {
				if (action.has("catType")) {
					object.setCatType(Type.valueOf((String) action.get("catType")));
				}
			}
		});

		ENTITIES.register(Pig.class, new DataHandler<Pig>() {

			@Override
			public void save(ActionData action, Pig object) {
				action.data("saddle", object.hasSaddle());
			}

			@Override
			public void load(ActionData action, Pig object) {
				if (action.has("saddle")) {
					object.setSaddle((boolean) action.get("saddle"));
				}
			}
		});

		ENTITIES.register(Rabbit.class, new DataHandler<Rabbit>() {

			@Override
			public void save(ActionData action, Rabbit object) {
				action.data("rabbitType", object.getRabbitType());
			}

			@Override
			public void load(ActionData action, Rabbit object) {
				if (action.has("rabbitType")) {
					object.setRabbitType(Rabbit.Type.valueOf((String) action.get("rabbitType")));
				}
			}
		});

		ENTITIES.register(Sheep.class, new DataHandler<Sheep>() {

			@Override
			public void save(ActionData action, Sheep object) {
				action.data("sheared", object.isSheared());
			}

			@Override
			public void load(ActionData action, Sheep object) {
				if (action.has("sheared")) {
					object.setSheared((boolean) action.get("sheared"));
				}
			}
		});

		ENTITIES.register(Slime.class, new DataHandler<Slime>() {

			@Override
			public void save(ActionData action, Slime object) {
				action.data("size", object.getSize());
			}

			@Override
			public void load(ActionData action, Slime object) {
				if (action.has("size")) {
					object.setSize(action.getInt("size"));
				}
			}
		});

		ENTITIES.register(Tameable.class, new DataHandler<Tameable>() {

			@Override
			public void save(ActionData action, Tameable object) {
				action.data("tamed", object.isTamed());
			}

			@Override
			public void load(ActionData action, Tameable object) {
				if (action.has("tamed")) {
					object.setTamed((boolean) action.get("tamed"));
				}
			}
		});

		ENTITIES.register(Villager.class, new DataHandler<Villager>() {

			@Override
			public void save(ActionData action, Villager object) {
				action.data("profession", object.getProfession());
			}

			@Override
			public void load(ActionData action, Villager object) {
				if (action.has("profession")) {
					object.setProfession(Profession.valueOf((String) action.get("profession")));
				}
			}
		});

		ENTITIES.register(Zombie.class, new DataHandler<Zombie>() {

			@Override
			public void save(ActionData action, Zombie object) {
				action.data("baby", object.isBaby());
			}

			@Override
			public void load(ActionData action, Zombie object) {
				if (action.has("baby")) {
					object.setBaby((boolean) action.get("baby"));
				}
			}
		});

		ENTITIES.register(Wolf.class, new DataHandler<Wolf>() {

			@Override
			public void save(ActionData action, Wolf object) {
				action.data("collarColor", object.getCollarColor());
			}

			@Override
			public void load(ActionData action, Wolf object) {
				if (action.has("collarColor")) {
					object.setCollarColor(DyeColor.valueOf((String) action.get("collarColor")));
				}
			}
		});
		/*
		 * TODO AreaEffectCloud Firework
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
				b.setType(Material.valueOf((String) a.get("type")));
				b.setData(((Number) a.get("data")).byteValue());
			}
		});
		BLOCKS.register(Sign.class, new DataHandler<Sign>() {

			@Override
			public void save(ActionData action, Sign object) {
				action.data("lines", object.getLines());
			}

			@Override
			public void load(ActionData action, Sign sign) {
				if (action.has("lines")) {
					@SuppressWarnings("unchecked")
					String[] lines = (String[]) ((List<String>) action.get("lines")).toArray(new String[0]);
					for (int i = 0; i < lines.length; i++) {
						sign.setLine(i, lines[i]);
					}
				}
			}
		});
		BLOCKS.register(FlowerPot.class, new DataHandler<FlowerPot>() {

			@Override
			public void save(ActionData action, FlowerPot object) {
				action.data("contents", object.getContents());
			}

			@Override
			public void load(ActionData action, FlowerPot object) {
				if (action.has("contents")) {
					object.setContents((MaterialData) action.get("contents"));
				}
			}
		});

		BLOCKS.register(Banner.class, new DataHandler<Banner>() {
			// TODO patterns

			@Override
			public void save(ActionData action, Banner object) {
				action.data("baseColor", object.getBaseColor());
			}

			@Override
			public void load(ActionData action, Banner object) {
				if (action.has("baseColor")) {
					object.setBaseColor(DyeColor.valueOf((String) action.get("baseColor")));
				}
			}
		});

		BLOCKS.register(CreatureSpawner.class, new DataHandler<CreatureSpawner>() {

			@Override
			public void save(ActionData action, CreatureSpawner object) {
				action.data("spawnedType", object.getSpawnedType());
			}

			@Override
			public void load(ActionData action, CreatureSpawner object) {
				if (action.has("spawnedType")) {
					object.setSpawnedType(EntityType.valueOf((String) action.get("spawnedType")));
				}
			}
		});

		/*
		 * TODO skull
		 */
	}

	public static DataManager getEntities() {
		return ENTITIES;
	}

	public static DataManager getBlocks() {
		return BLOCKS;
	}

}
