import messageformats.DataMessage;
import messageformats.HandshakeMessage;

public class Test {

	public static void main(String[] args) {
		byte[] a = {1,2,3,4};
		/*System.out.println(Integer.BYTES);
		HandshakeMessage h = new HandshakeMessage(1000);
		System.out.println(h);*/

		System.out.println(new DataMessage(DataMessage.MESSAGE_TYPE_PIECE, a));
	}

}
