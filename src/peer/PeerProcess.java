package peer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import fileio.IFileManager;
import scheduledtasks.DetermineOptimisticallyUnchokedNeighbour;
import scheduledtasks.DeterminePreferredNeighbours;
import utils.Constants;

public class PeerProcess {

	public static PeerProcess peerProcess;
	private int peerId;
	private Map<Integer, PeerInfo> peerInfoMap;
	private List<PeerInfo> peerList = new ArrayList<>(); // will not include the current peer
	private List<PeerConnectionManager> peerConnectionManagers = new ArrayList<>();
	private ProgramParams programParams = new ProgramParams();
	private IFileManager iFileManager;
	private DetermineOptimisticallyUnchokedNeighbour determineOptimisticallyUnchokedNeighbour;
	private DeterminePreferredNeighbours determinePreferredNeighbours;
	private List<Socket> socketsToBeClosedRequestsPending = new ArrayList<>();

	public static void main(String[] args) {

		peerProcess = new PeerProcess();
		peerProcess.peerId = Integer.parseInt(args[0]);

		// load all peer info from peer config file.
		peerProcess.loadPeerInfoConfig();
		peerProcess.loadFileManager();

		peerProcess.determinePreferredNeighbours =
				new DeterminePreferredNeighbours();

		peerProcess.determineOptimisticallyUnchokedNeighbour =
				new DetermineOptimisticallyUnchokedNeighbour();

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
				if (peerInfo.getPeerId() != peerId) {
					peerList.add(peerInfo);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized public void closeRemotePeerInfoSocket(int peerId) {
		socketsToBeClosedRequestsPending.add(peerInfoMap.get(peerId).getSocket());

		// we will close all sockets after we get requests from all peers to
		// shut down socket. Just to avoid some peer writing to a closed socket
		// exception.
		if (socketsToBeClosedRequestsPending.size() == peerList.size()) {
			socketsToBeClosedRequestsPending.forEach(socket->{
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			// handle program shut down
			shutDownSchedulers();
		}
	}

	private void shutDownSchedulers() {
		determineOptimisticallyUnchokedNeighbour.shutdown();
		determinePreferredNeighbours.shutdown();
		System.out.println("peer id " + peerId + " terminating");
	}

	public void loadFileManager() {
		iFileManager = programParams.constructFileManager(
				peerInfoMap.get(peerId).isHasFileInitially());
	}

	public PeerInfo getPeerInfoForPeerId(int peerId) {
		return peerInfoMap.get(peerId);
	}


	public ProgramParams getProgramParams() {
		return programParams;
	}

	public IFileManager getiFileManager() {
		return iFileManager;
	}

	public List<PeerInfo> getPeerList() {
		return peerList;
	}

	public int getPeerId() {
		return peerId;
	}

	public DetermineOptimisticallyUnchokedNeighbour getDetermineOptimisticallyUnchokedNeighbour() {
		return determineOptimisticallyUnchokedNeighbour;
	}

	public DeterminePreferredNeighbours getDeterminePreferredNeighbours() {
		return determinePreferredNeighbours;
	}
}
