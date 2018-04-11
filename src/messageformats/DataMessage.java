package messageformats;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
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
	public final static int BYTES_FOR_MESSAGE_LENGTH = 4;


	private final byte[] messageLength;
	private final byte messageType;
	private final byte[] payload;

	public DataMessage(byte messageType, byte[] payload) {

		if (payload == null) {
			messageLength = CommonUtils.intToByteArray(1);
		} else {
			messageLength = CommonUtils.intToByteArray(payload.length + 1);
		}
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

	public void sendDataMessage(ObjectOutputStream objectOutputStream) {
		try {
			objectOutputStream.writeObject(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getPieceNumberForPieceData(byte[] data) {
		if (messageType != DataMessage.MESSAGE_TYPE_PIECE) {
			throw new RuntimeException("getPieceNumberForPieceData called for non-piece message");
		}

		return ByteBuffer.wrap(data).getInt();
	}

	public byte[] getPieceData(byte []data) {
		if (messageType != DataMessage.MESSAGE_TYPE_PIECE) {
			throw new RuntimeException("getPieceNumberForPieceData called for non-piece message");
		}

		return Arrays.copyOfRange(data, Integer.BYTES, data.length);
	}
}
