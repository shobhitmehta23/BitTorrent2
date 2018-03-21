package messageformats;

import java.io.Serializable;
import java.util.Arrays;

import utils.CommonUtils;

public class HandshakeMessage implements Serializable {

	private  final static String HEADER_VALUE = "P2PFILESHARINGPROJ";
	private final static int NO_OF_RESERVED_BITS = 10;

	private final byte[] peerId;
	private final byte[] header;
	private final byte[] reservedBits;

	public HandshakeMessage(int peerId) {
		this.peerId = CommonUtils.intToByteArray(peerId);
		header = HEADER_VALUE.getBytes();
		reservedBits = new byte[NO_OF_RESERVED_BITS];
	}

	public int getPeerIdAsInt() {
		return CommonUtils.byteArrayToInteger(peerId);
	}

	public byte[] getPeerId() {
		return peerId;
	}

	public byte[] getHeader() {
		return header;
	}

	public byte[] getReservedBits() {
		return reservedBits;
	}

	@Override
	public String toString() {
		return "HandshakeMessage (peerid = " + CommonUtils.byteArrayToInteger(peerId) + ", "
				+ "reserved bits = " + CommonUtils.byteArrayToInteger(reservedBits) + ", header = "
				+ new String(header) + ")" + ", total_size = " +
				(peerId.length + header.length + reservedBits.length) * Byte.BYTES;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(header);
		result = prime * result + Arrays.hashCode(peerId);
		result = prime * result + Arrays.hashCode(reservedBits);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HandshakeMessage)) {
			return false;
		}
		HandshakeMessage other = (HandshakeMessage) obj;
		if (!Arrays.equals(header, other.header)) {
			return false;
		}
		if (!Arrays.equals(peerId, other.peerId)) {
			return false;
		}
		if (!Arrays.equals(reservedBits, other.reservedBits)) {
			return false;
		}
		return true;
	}
}
