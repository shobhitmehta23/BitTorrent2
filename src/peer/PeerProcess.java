package peer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Constants;

public class PeerProcess {
	int peerId;
	Map<Integer, PeerInfo> peerInfoMap;
	List<PeerConnectionManager> peerConnectionManagers = new ArrayList<>();

	public static void main(String[] args) {

		PeerProcess peerProcess = new PeerProcess();
		peerProcess.peerId = Integer.parseInt(args[0]);

		// load all peer info from peer config file.
		peerProcess.loadPeerInfoConfig();
		// set up connection with all other peers
		peerProcess.setUpConnectionWithOtherPeers();
	}

	public void setUpConnectionWithOtherPeers() {
		/*
		 * This method consist of two parts.
		 * part 1 - the peer will initiate a handshake connection with all the peers
		 * declared above the current peer in the peer config file.
		 * part 2 - the peer will wait for a handshake from all the peers declared
		 * below itself in the peer config file.
		 */
		PeerInfo currentPeerInfo = peerInfoMap.get(peerId);
		int currentPeerDeclaredOnLine = currentPeerInfo.getLineDeclared();
		for (int remotePeerId : peerInfoMap.keySet()) {
			PeerInfo remotePeerInfo = peerInfoMap.get(remotePeerId);
			// compare line numbers
			if (currentPeerDeclaredOnLine > remotePeerInfo.getLineDeclared()) {
				// if the remote peer was declared above, the peer would already have been initiated
				// and the current peer should start the handshake process
				try {
					Socket socket = new Socket(remotePeerInfo.getIp(), remotePeerInfo.getPortNo());
					remotePeerInfo.initializeSocket(socket);
					PeerConnectionManager peerConnectionManager = new PeerConnectionManager(currentPeerInfo, remotePeerInfo);
					peerConnectionManager.start();
					peerConnectionManagers.add(peerConnectionManager);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// this is now the second part for all the peers delcared below the current peer.
		// as those peer processes might not have started, this current peer will wait on a
		// new thread listening to connections.
		new Thread(new Runnable() {

			@Override
			public void run() {
				// we know that total connections for any peer should be (total_number_of_peers_in_system - 1)
				int totalConnectionsRequired = peerInfoMap.keySet().size() - 1;  // set also contains current peerInfo

				// but we have already made some connections in part 1
				int connectionCount = peerConnectionManagers.size();
				ServerSocket serverSocket = null;
				try {
					serverSocket = new ServerSocket(currentPeerInfo.getPortNo());

					// untill we get required number of connections listen
					while (connectionCount < totalConnectionsRequired) {
						Socket connectionSocket = serverSocket.accept();
						PeerInfo remotePeerInfo = new PeerInfo(); // we will initialize later
						remotePeerInfo.initializeSocket(connectionSocket);
						PeerConnectionManager peerConnectionManager= new PeerConnectionManager(currentPeerInfo, remotePeerInfo);
						peerConnectionManager.start();
						peerConnectionManagers.add(peerConnectionManager);
						connectionCount++;
					}

					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}


	public void loadPeerInfoConfig() {
		peerInfoMap = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(Constants.PEER_INFO_CONFIG_FILENAME));

			String line = null;
			int peerDeclaredOnLine = 0;  // line number in file
			while ((line = br.readLine()) != null) {
				// split on whitespace
				PeerInfo peerInfo = new PeerInfo(line.split("\\s+"), peerDeclaredOnLine++);
				peerInfoMap.put(peerInfo.getPeerId(), peerInfo);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
