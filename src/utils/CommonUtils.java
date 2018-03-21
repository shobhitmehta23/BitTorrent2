package utils;

import java.nio.ByteBuffer;

public class CommonUtils {
	public static byte[] intToByteArray(int num) {
		return ByteBuffer.allocate(Integer.BYTES).putInt(num).array();
	}

	public static int byteArrayToInteger(byte[] num) {
		return ByteBuffer.wrap(num).getInt();
	}
}
