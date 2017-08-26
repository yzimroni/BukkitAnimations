package net.yzimroni.bukkitanimations.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Utils {

	private Utils() {

	}

	// Source: http://wiki.vg/Protocol#VarInt_and_VarLong
	public static int readVarInt(DataInput input) throws IOException {
		int numRead = 0;
		int result = 0;
		byte read;
		do {
			read = input.readByte();
			int value = (read & 0b01111111);
			result |= (value << (7 * numRead));

			numRead++;
			if (numRead > 5) {
				throw new RuntimeException("VarInt is too big");
			}
		} while ((read & 0b10000000) != 0);

		return result;
	}

	public static void writeVarInt(DataOutput output, int value) throws IOException {
		do {
			byte temp = (byte) (value & 0b01111111);
			// Note: >>> means that the sign bit is shifted with the rest of the number
			// rather than being left alone
			value >>>= 7;
			if (value != 0) {
				temp |= 0b10000000;
			}
			output.writeByte(temp);
		} while (value != 0);
	}

}
