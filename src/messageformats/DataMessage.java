package messageformats;

import java.io.Serializable;
import java.util.Arrays;

import utils.CommonUtils;

public class DataMessage implements Serializable {

	public final static byte MESSAGE_TYPE_CHOKE = 0;
	public final static byte MESSAGE_TYPE_UNCHOKE = 1;
	public final static byte MESSAGE_TYPE_INTERESTED = 2;
	public final static byte MESSAGE_TYPE_NOT_INTERESTED = 3;
	public final static byte MESSAGE_TYPE_HAVE = 4;
	public final static byte MESSAGE_TYPE_BITFIELD = 5;
	public final static byte MESSAGE_TYPE_REQUEST = 6;
	public final static byte MESSAGE_TYPE_PIECE = 7;


	private final byte[] messageLength;
	private final byte messageType;
	private final byte[] payload;

	public DataMessage(byte messageType, byte[] payload) {
		messageLength = CommonUtils.intToByteArray(payload.length + 1);
		this.messageType = messageType;
		this.payload = payload;
	}

	public byte[] getMessageLength() {
		return messageLength;
	}

	public byte getMessageType() {
		return messageType;
	}

	public byte[] getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		return "DataMessage [messageLength=" + CommonUtils.byteArrayToInteger(messageLength) + ", messageType=" + messageType
				+ ", payload=" + Arrays.toString(payload) + "]";
	}
}
