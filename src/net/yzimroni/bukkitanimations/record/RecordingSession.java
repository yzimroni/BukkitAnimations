package net.yzimroni.bukkitanimations.record;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.yzimroni.bukkitanimations.BukkitAnimationsPlugin;
import net.yzimroni.bukkitanimations.data.Animation;
import net.yzimroni.bukkitanimations.utils.Utils;

public class RecordingSession {

	@SuppressWarnings("deprecation")
	private static final List<PacketType> SHOULD_HANDLE = Arrays.asList(Server.SPAWN_ENTITY,
			Server.SPAWN_ENTITY_EXPERIENCE_ORB, Server.SPAWN_ENTITY_WEATHER, Server.SPAWN_ENTITY_LIVING,
			Server.SPAWN_ENTITY_PAINTING, Server.NAMED_ENTITY_SPAWN, Server.ANIMATION, Server.BLOCK_BREAK_ANIMATION,
			Server.TILE_ENTITY_DATA, Server.BLOCK_ACTION, Server.BLOCK_CHANGE, Server.MULTI_BLOCK_CHANGE,
			Server.CUSTOM_SOUND_EFFECT, Server.ENTITY_STATUS, Server.EXPLOSION, Server.GAME_STATE_CHANGE,
			Server.WORLD_EVENT, Server.WORLD_PARTICLES, Server.MAP, Server.ENTITY, Server.REL_ENTITY_MOVE,
			Server.REL_ENTITY_MOVE_LOOK, Server.ENTITY_LOOK, Server.VEHICLE_MOVE, Server.COMBAT_EVENT,
			Server.PLAYER_INFO, Server.POSITION, Server.BED, Server.ENTITY_DESTROY, Server.REMOVE_ENTITY_EFFECT,
			Server.ENTITY_HEAD_ROTATION, Server.ENTITY_METADATA, Server.ATTACH_ENTITY, Server.ENTITY_VELOCITY,
			Server.ENTITY_EQUIPMENT, Server.MOUNT, Server.NAMED_SOUND_EFFECT, Server.COLLECT, Server.ENTITY_TELEPORT,
			Server.UPDATE_ATTRIBUTES, Server.ENTITY_EFFECT, Server.UPDATE_SIGN);

	private Animation animation;
	private boolean running;
	private File file;
	private DataOutputStream outputStream;
	private PacketListener packetListener;

	private int tick = 1;

	private Location minLocation;
	private Location maxLocation;

	public RecordingSession(String name, UUID uuid, Location min, Location max) {
		this.animation = new Animation(name, uuid);
		Preconditions.checkArgument(min.getWorld().equals(max.getWorld()), "World must be same");
		this.minLocation = new Location(min.getWorld(), Math.min(min.getBlockX(), max.getBlockX()),
				Math.min(min.getBlockY(), max.getBlockY()), Math.min(min.getBlockZ(), max.getBlockZ()));
		this.maxLocation = new Location(min.getWorld(), Math.max(min.getBlockX(), max.getBlockX()),
				Math.max(min.getBlockY(), max.getBlockY()), Math.max(min.getBlockZ(), max.getBlockZ()));
		this.file = new File(name + ".mcanimation");
		try {
			file.createNewFile();
			this.outputStream = new DataOutputStream(new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
		if (isRunning()) {
			return;
		}
		running = true;
		initPacketListener();
		RecordingManager.get().onStart(this);
	}

	protected void initPacketListener() {
		packetListener = new PacketAdapter(BukkitAnimationsPlugin.get(), ListenerPriority.LOWEST, SHOULD_HANDLE) {
			@Override
			public void onPacketSending(PacketEvent event) {
				handle(event.getPacket());
			}
		};
		ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
	}

	protected void tick() {
		if (!isRunning()) {
			return;
		}
		tick++;
	}

	public void handle(PacketContainer packet) {
		if (!isRunning()) {
			return;
		}
		if (shouldHandle(packet)) {
			try {
				outputStream.writeInt(1); // Action = packet
				outputStream.writeInt(tick);
				Utils.writeVarInt(outputStream, packet.getType().getCurrentId());
				byte[] data = getPacketData(packet);
				outputStream.writeInt(data.length);
				outputStream.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean shouldHandle(PacketContainer packet) {
		if (packet.getType() == Server.GAME_STATE_CHANGE) {
			byte reason = packet.getBytes().read(0);
			return reason == 6 || reason == 10;
		}
		return SHOULD_HANDLE.contains(packet.getType());
	}

	private byte[] getPacketData(PacketContainer packet) {
		WirePacket wire = WirePacket.fromPacket(packet);

		ByteBuf byteBuf = Unpooled.buffer();
		wire.writeBytes(byteBuf);
		byteBuf.readerIndex(0);
		byte[] array = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(array);

		byteBuf.release();

		return array;
	}

	public void stop() {
		if (!isRunning()) {
			return;
		}
		running = false;
		if (packetListener != null) {
			ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
		}
		RecordingManager.get().onStop(this);
		synchronized (outputStream) {
			try {
				outputStream.flush();
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public Animation getAnimation() {
		return animation;
	}

}
