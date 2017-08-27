package net.yzimroni.bukkitanimations;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import net.yzimroni.bukkitanimations.record.RecordingManager;

public class BukkitAnimationsPlugin extends JavaPlugin {

	private static BukkitAnimationsPlugin plugin;
	private RecordingManager recordingManager;

	@Override
	public void onEnable() {
		plugin = this;
		recordingManager = new RecordingManager();
		Bukkit.getPluginManager().registerEvents(recordingManager, this);
	}

	public static BukkitAnimationsPlugin get() {
		return plugin;
	}

	@Override
	public void onDisable() {
		recordingManager.disable();
		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
	}

}
