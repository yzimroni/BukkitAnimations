package net.yzimroni.bukkitanimations;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import net.yzimroni.bukkitanimations.animation.AnimationManager;
import net.yzimroni.bukkitanimations.play.PlayingManager;
import net.yzimroni.bukkitanimations.record.RecordingManager;
import net.yzimroni.bukkitanimations.utils.Utils;

public class BukkitAnimationsPlugin extends JavaPlugin {

	private static BukkitAnimationsPlugin plugin;
	private RecordingManager recordingManager;
	private PlayingManager playingManager;

	@Override
	public void onEnable() {
		plugin = this;
		AnimationManager.get().setAnimationsFolder(new File(getDataFolder(), "animations"));
		recordingManager = new RecordingManager();
		playingManager = new PlayingManager();
		Bukkit.getPluginManager().registerEvents(recordingManager, this);
		Bukkit.getPluginManager().registerEvents(playingManager, this);
		getCommand("bukkitanimations").setExecutor(new Commands());
	}

	public static BukkitAnimationsPlugin get() {
		return plugin;
	}

	@Override
	public void onDisable() {
		recordingManager.disable();
		playingManager.disable();
		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
		Utils.NPCREGISTRY.deregisterAll();
	}

}
