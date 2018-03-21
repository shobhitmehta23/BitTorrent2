package peer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerInfo {
	private int peerId;
	private String ip;
	private int portNo;
	private boolean hasFileInitially;
	private int lineDeclared;
	private Socket socket; // socket connected to the peer
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean initialized;  // If waiting for other peer to connect, PeerInfo might not have been fully initialized

	public PeerInfo() {
	}

	/**
	 * Set up PeerInfo from provided params
	 * @param peerId Peer Id
	 * @param ip IP address of Peer
	 * @param portNo Port number at which the peer listens
	 * @param hasFileInitially status of whether the peer has the complete file at beginning
	 * @param line line at which the peer was declared in the config file
	 */
	public PeerInfo(int peerId, String ip, int portNo, boolean hasFileInitially, int line) {
		this.peerId = peerId;
		this.ip = ip;
		this.portNo = portNo;
		this.hasFileInitially = hasFileInitially;
		this.lineDeclared = line;
		initialized = true;
	}

	/**
	 * Set up instance from params provided as String array
	 * @param peerParams
	 * @param line
	 */
	public PeerInfo(String peerParams[], int line) {
		peerId = Integer.parseInt(peerParams[0]);
		ip = peerParams[1];
		portNo = Integer.parseInt(peerParams[2]);
		hasFileInitially = peerParams[3].equals("1");
		lineDeclared = line;
		initialized = true;
	}

	public void initializeSocket(Socket socket) {
		this.socket = socket;
		try {
			// do not exchange order of following two lines.
			// unless you want to see magic.
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getPeerId() {
		return peerId;
	}

	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPortNo() {
		return portNo;
	}

	public void setPortNo(int portNo) {
		this.portNo = portNo;
	}

	public boolean isHasFileInitially() {
		return hasFileInitially;
	}

	public void setHasFileInitially(boolean hasFileInitially) {
		this.hasFileInitially = hasFileInitially;
	}

	public int getLineDeclared() {
		return lineDeclared;
	}

	public void setLineDeclared(int lineDeclared) {
		this.lineDeclared = lineDeclared;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public void setInitialized() {
		initialized = true;
	}


	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hasFileInitially ? 1231 : 1237);
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + lineDeclared;
		result = prime * result + peerId;
		result = prime * result + portNo;
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
		if (!(obj instanceof PeerInfo)) {
			return false;
		}
		PeerInfo other = (PeerInfo) obj;
		if (hasFileInitially != other.hasFileInitially) {
			return false;
		}
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		} else if (!ip.equals(other.ip)) {
			return false;
		}
		if (lineDeclared != other.lineDeclared) {
			return false;
		}
		if (peerId != other.peerId) {
			return false;
		}
		if (portNo != other.portNo) {
			return false;
		}
		return true;
	}
}
