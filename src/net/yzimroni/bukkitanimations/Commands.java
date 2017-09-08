package net.yzimroni.bukkitanimations;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.yzimroni.bukkitanimations.animation.Animation;
import net.yzimroni.bukkitanimations.animation.AnimationData;
import net.yzimroni.bukkitanimations.animation.AnimationManager;
import net.yzimroni.bukkitanimations.play.ReplayingSession;
import net.yzimroni.bukkitanimations.record.RecordingManager;
import net.yzimroni.bukkitanimations.record.RecordingSession;

public class Commands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.isOp()) {
			return false;
		}
		if (args.length == 0) {
			sender.sendMessage("/" + label + " list");
			sender.sendMessage("/" + label + " record <name>");
			sender.sendMessage("/" + label + " stop");
			sender.sendMessage("/" + label + " play <name>");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list")) {
				List<AnimationData> animations = AnimationManager.get().getAnimationsData();
				if (animations.isEmpty()) {
					sender.sendMessage("There are no animations");
				} else {
					sender.sendMessage("There are " + animations.size() + " animations:");
					animations.forEach(a -> {
						sender.sendMessage(a.getName());
					});
				}
			}
		}
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("end")) {
					RecordingManager.get().getSessionsByUUID(p.getUniqueId()).forEach(s -> {
						s.stop();
						System.out.println("Stopped recording " + s.getAnimation().getName());
						p.sendMessage("Stopped recording " + s.getAnimation().getName());
					});
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("record")) {
					String name = args[1];
					if (AnimationManager.get().hasAnimation(name)) {
						p.sendMessage("Amination already exist!");
						return false;
					}
					RecordingSession session = new RecordingSession(name, p.getUniqueId(),
							new Location(p.getWorld(), 151, 65, 208), new Location(p.getWorld(), 175, 82, 236));
					session.start();
					System.out.println("Start recording " + name);
					p.sendMessage("Start recording " + name);
				} else if (args[0].equalsIgnoreCase("play")) {
					String name = args[1];
					if (!AnimationManager.get().hasAnimation(name)) {
						p.sendMessage("Amination not found!");
						return false;
					}
					Animation animation = AnimationManager.get().getAnimation(name);
					p.sendMessage("Playing " + animation.getData().getName());
					System.out.println("Playing " + animation.getData().getName());
					ReplayingSession play = new ReplayingSession(animation, new Location(p.getWorld(), 113, 93, 208));
					play.start();
				}
			}
		}
		return true;
	}

}
