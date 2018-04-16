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

	private byte[] messageLength;
	private byte messageType;
	private byte[] payload;

	public DataMessage() {
		messageLength = new byte[4];
	}

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
		return "DataMessage [messageLength=" + CommonUtils.byteArrayToInteger(messageLength) + ", messageType="
				+ messageType + ", payload=" + Arrays.toString(payload) + "]";
	}

	public void sendDataMessage(ObjectOutputStream objectOutputStream) {
		synchronized (objectOutputStream) {
			try {
				// objectOutputStream.writeObject(serializeAsByte());

				objectOutputStream.writeObject(this);
				objectOutputStream.flush();
			} catch (IOException e) {
			}
		}
	}

	public int getPieceNumberForPieceData(byte[] data) {
		if (messageType != DataMessage.MESSAGE_TYPE_PIECE) {
			throw new RuntimeException("getPieceNumberForPieceData called for non-piece message");
		}

		return ByteBuffer.wrap(data).getInt();
	}

	public byte[] getPieceData(byte[] data) {
		if (messageType != DataMessage.MESSAGE_TYPE_PIECE) {
			throw new RuntimeException("getPieceNumberForPieceData called for non-piece message");
		}

		return Arrays.copyOfRange(data, Integer.BYTES, data.length);
	}

	@Deprecated // the class now implements serializable
	public byte[] serializeAsByte() {
		byte data[] = new byte[4 + CommonUtils.byteArrayToInteger(messageLength)];

		System.arraycopy(messageLength, 0, data, 0, 4);
		data[4] = messageType;

		if (payload != null) {
			System.arraycopy(payload, 0, data, 5, payload.length);
		}

		return data;
	}

	@Deprecated // the class now implements serializable
	public void constructDataMessageFromByteArray(int msglen, byte[] data) {
		byte[] temp = CommonUtils.intToByteArray(msglen);
		System.arraycopy(temp, 0, messageLength, 0, BYTES_FOR_MESSAGE_LENGTH);
		messageType = data[0];

		if (msglen > 1) {
			payload = new byte[msglen - 1];
			System.arraycopy(data, 1, payload, 0, (msglen - 1));
		} else {
			payload = null;
		}
	}

	@Deprecated // the class now implements serializable
	public void constructDataMessageFromByteArray(byte[] data) {
		System.arraycopy(data, 0, messageLength, 0, BYTES_FOR_MESSAGE_LENGTH);
		messageType = data[BYTES_FOR_MESSAGE_LENGTH];

		int messageLengthAsInt = CommonUtils.byteArrayToInteger(messageLength);

		if (messageLengthAsInt > 1) {
			payload = new byte[messageLengthAsInt - 1];
			System.arraycopy(data, BYTES_FOR_MESSAGE_LENGTH + 1, payload, 0, (messageLengthAsInt - 1));
		} else {
			payload = null;
		}
	}
}
