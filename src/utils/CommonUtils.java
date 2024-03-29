package utils;

import java.nio.ByteBuffer;

public class CommonUtils {

	/**
	 * Given an integer, returns a byte array representation of the integer.
	 * @param num the integer to be converted
	 * @return byte array representation of the integer
	 */
	public static byte[] intToByteArray(int num) {
		return ByteBuffer.allocate(Integer.BYTES).putInt(num).array();
	}

	/**
	 * Given a byte array representation of an integer, returns the integer
	 * contained in that byte array
	 * @param num byte array containing the integer
	 * @return the integer representation of the byte array
	 */
	public static int byteArrayToInteger(byte[] num) {
		return ByteBuffer.wrap(num).getInt();
	}

	public static byte[] mergeByteArrays(byte []arrayOne, byte []arrayTwo) {
		return ByteBuffer.allocate(arrayOne.length + arrayTwo.length)
				.put(arrayOne)
				.put(arrayTwo)
				.array();
	}

	public static String formatString(String str, Object...objects) {
		for (Object object : objects) {
			str = str.replaceFirst("#", object.toString());
		}

		return str;
	}
}
