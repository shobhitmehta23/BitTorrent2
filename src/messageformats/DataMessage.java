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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(messageLength);
		result = prime * result + messageType;
		result = prime * result + Arrays.hashCode(payload);
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
		if (!(obj instanceof DataMessage)) {
			return false;
		}
		DataMessage other = (DataMessage) obj;
		if (!Arrays.equals(messageLength, other.messageLength)) {
			return false;
		}
		if (messageType != other.messageType) {
			return false;
		}
		if (!Arrays.equals(payload, other.payload)) {
			return false;
		}
		return true;
	}
}
