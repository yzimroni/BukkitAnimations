package net.yzimroni.bukkitanimations;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.yzimroni.bukkitanimations.data.Animation;
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
			sender.sendMessage("/" + label + " record <name>");
			sender.sendMessage("/" + label + " stop");
			sender.sendMessage("/" + label + " play <name>");
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
					RecordingSession session = new RecordingSession(name, p.getUniqueId(),
							new Location(p.getWorld(), 108, 67, 95), new Location(p.getWorld(), 86, 80, 108));
					session.start();
					System.out.println("Start recording " + name);
					p.sendMessage("Start recording " + name);
				} else if (args[0].equalsIgnoreCase("play")) {
					String name = args[1];
					p.sendMessage("Playing " + name);
					System.out.println("Playing " + name);
					ReplayingSession play = new ReplayingSession(new Animation(name, p.getUniqueId()));
					play.start();
				}
			}
		}
		return true;
	}

}
