package net.yzimroni.bukkitanimations.play;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.data.action.ActionType;

public class ActionHandler {

	private static final HashMap<ActionType, BiConsumer<ReplayingSession, ActionData>> HANDLERS = new HashMap<>();

	static {
		registerDefaultHandlers();
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private static void registerDefaultHandlers() {
		register(ActionType.BLOCK_BREAK, (s, a) -> {
			a.getLocation(s).getBlock().setType(Material.AIR);
		});
		register(ActionType.BLOCK_PLACE, (s, a) -> {
			Block b = a.getLocation(s).getBlock();
			b.setType(Material.valueOf((String) a.getData("type")));
			b.setData(((Number) a.getData("data")).byteValue());
		});
		register(ActionType.SIGN_UPDATE, (s, a) -> {
			Block b = a.getLocation(s).getBlock();
			if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
				Sign sign = (Sign) b.getState();
				String[] lines = (String[]) ((List<String>) a.getData("lines")).toArray(new String[0]);
				for (int i = 0; i < lines.length; i++) {
					sign.setLine(i, lines[i]);
				}
				sign.update(true);
			}
		});
		register(ActionType.LIGHTNING_STRIKE, (s, a) -> {
			Location l = a.getLocation(s);
			l.getWorld().strikeLightningEffect(l);
		});
	}
	
	public static void register(ActionType type, BiConsumer<ReplayingSession, ActionData> handler) {
		if (HANDLERS.containsKey(type)) {
			HANDLERS.remove(type);
		}
		HANDLERS.put(type, handler);
	}

	public static void handle(ReplayingSession session, ActionData action) {
		Optional.ofNullable(HANDLERS.get(action.getType()))
				.orElseThrow(() -> new IllegalStateException("Handler not found for ActionType " + action.getType()))
				.accept(session, action);
	}

}
