package net.yzimroni.bukkitanimations.play;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.yzimroni.bukkitanimations.data.Animation;

public class ReplayingSession {

	private Animation animation;
	private ByteBuf packetStream;
	private boolean loop;

	private boolean running;
	private int tick = 1;

	public ReplayingSession(Animation animation) {
		this.animation = animation;
		File file = new File(animation.getName() + ".mcanimation");
		try {
			FileInputStream stream = new FileInputStream(file);
			packetStream = Unpooled.buffer();
			packetStream.writeBytes(stream, stream.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		if (isRunning()) {
			return;
		}
		running = true;
		PlayingManager.get().onStart(this);
	}

	public void tick() {
		if (!isRunning()) {
			return;
		}
		playTick();
	}

	private void playTick() {
		int currentTick = -1;
		while (true) {
			try {
				if (!packetStream.isReadable()) {
					stop();
					break;
				}
				packetStream.markReaderIndex();
				int action = packetStream.readInt();
				int tick = packetStream.readInt();
				if (currentTick == -1) {
					currentTick = tick;
				} else if (currentTick != tick) {
					packetStream.resetReaderIndex();
					break;
				}
				if (action == 1) {
					PacketContainer packet = createPacket();
					ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet);
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

	}

	private PacketContainer createPacket() throws Exception {
		int id = WirePacket.readVarInt(packetStream);
		int length = packetStream.readInt();
		byte[] data = new byte[length];
		packetStream.readBytes(data);
		PacketType packetType = PacketType.findCurrent(Protocol.PLAY, Sender.SERVER, id);

		Object handle = StructureCache.newPacket(packetType);
		ByteBuf buffer = PacketContainer.createPacketBuffer();
		buffer.writeBytes(data);

		MinecraftMethods.getPacketReadByteBufMethod().invoke(handle, buffer);

		PacketContainer packetContainer = new PacketContainer(packetType, handle);

		// Walkaround for a minecraft bug
		if (packetContainer.getType() == Play.Server.SPAWN_ENTITY_LIVING) {
			WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
			for (WrappedWatchableObject w : packetContainer.getWatchableCollectionModifier().read(0)) {
				dataWatcher.setObject(w.getIndex(), w.getRawValue());
			}
			packetContainer.getDataWatcherModifier().write(0, dataWatcher);
		}

		return packetContainer;
	}

	public void stop() {
		if (!isRunning()) {
			return;
		}
		running = false;
		PlayingManager.get().onStop(this);
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
