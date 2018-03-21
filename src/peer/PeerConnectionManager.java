package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import messageformats.HandshakeMessage;

public class PeerConnectionManager extends Thread {
	private PeerInfo currentPeerInfo;
	private PeerInfo remotePeerInfo;

	public PeerConnectionManager(PeerInfo currentPeerInfo, PeerInfo remotePeerInfo) {
		this.currentPeerInfo = currentPeerInfo;
		this.remotePeerInfo = remotePeerInfo;
	}

	public void setRemotePeerInfo(PeerInfo remotePeerInfo) {
		this.remotePeerInfo = remotePeerInfo;
	}

	@Override
	public void run() {
		// if remotePeer is initialized, it means current peer should initiate handshake
		if (remotePeerInfo.isInitialized()) {
			sendHandshakeMessage();
			acceptHandshakeMessage();
		} else {
			acceptHandshakeMessage();
			sendHandshakeMessage();
		}
		try {
			remotePeerInfo.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void acceptHandshakeMessage() {
		ObjectInputStream in = remotePeerInfo.getIn();
		try {
			HandshakeMessage handshakeMessage = (HandshakeMessage)in.readObject();
			remotePeerInfo.setPeerId(handshakeMessage.getPeerIdAsInt());
			System.out.println("messaged received by peer id " + currentPeerInfo.getPeerId());
			System.out.println(handshakeMessage);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void sendHandshakeMessage() {
		ObjectOutputStream out = remotePeerInfo.getOut();
		try {
			out.writeObject(new HandshakeMessage(currentPeerInfo.getPeerId()));
			out.flush();
			System.out.println("messaged sent by peer id " + currentPeerInfo.getPeerId());
			System.out.println(new HandshakeMessage(currentPeerInfo.getPeerId()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentPeerInfo == null) ? 0 : currentPeerInfo.hashCode());
		result = prime * result + ((remotePeerInfo == null) ? 0 : remotePeerInfo.hashCode());
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
		if (!(obj instanceof PeerConnectionManager)) {
			return false;
		}
		PeerConnectionManager other = (PeerConnectionManager) obj;
		if (currentPeerInfo == null) {
			if (other.currentPeerInfo != null) {
				return false;
			}
		} else if (!currentPeerInfo.equals(other.currentPeerInfo)) {
			return false;
		}
		if (remotePeerInfo == null) {
			if (other.remotePeerInfo != null) {
				return false;
			}
		} else if (!remotePeerInfo.equals(other.remotePeerInfo)) {
			return false;
		}
		return true;
	}
}
