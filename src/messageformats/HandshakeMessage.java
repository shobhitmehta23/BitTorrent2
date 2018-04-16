package messageformats;

import java.io.Serializable;
import java.util.Arrays;

import utils.CommonUtils;

/**
 * Implements the handshake message specified by the protocol
 *
 */
public class HandshakeMessage implements Serializable {

	private final static String HEADER_VALUE = "P2PFILESHARINGPROJ";
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
		return "HandshakeMessage (peerid = " + CommonUtils.byteArrayToInteger(peerId) + ", " + "reserved bits = "
				+ CommonUtils.byteArrayToInteger(reservedBits) + ", header = " + new String(header) + ")"
				+ ", total_size = " + (peerId.length + header.length + reservedBits.length) * Byte.BYTES;
	}
}
