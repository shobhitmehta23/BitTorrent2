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

		peerProcess.loadPeerInfoConfig();
		peerProcess.setUpConnectionWithOtherPeers();
	}

	public void setUpConnectionWithOtherPeers() {
		PeerInfo currentPeerInfo = peerInfoMap.get(peerId);
		int currentPeerDeclaredOnLine = currentPeerInfo.getLineDeclared();
		for (int remotePeerId : peerInfoMap.keySet()) {
			PeerInfo remotePeerInfo = peerInfoMap.get(remotePeerId);
			if (currentPeerDeclaredOnLine > remotePeerInfo.getLineDeclared()) {
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

		new Thread(new Runnable() {

			@Override
			public void run() {
				int totalConnectionsRequired = peerInfoMap.keySet().size() - 1;  // set also contains current peerInfo

				int connectionCount = peerConnectionManagers.size();
				ServerSocket serverSocket = null;
				try {
					serverSocket = new ServerSocket(currentPeerInfo.getPortNo());

					while (connectionCount < totalConnectionsRequired) {
						Socket connectionSocket = serverSocket.accept();
						PeerInfo remotePeerInfo = new PeerInfo();
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
			int peerDeclaredOnLine = 0;
			while ((line = br.readLine()) != null) {
				PeerInfo peerInfo = new PeerInfo(line.split("\\s+"), peerDeclaredOnLine++);
				peerInfoMap.put(peerInfo.getPeerId(), peerInfo);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
