package scheduledtasks;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import fileio.IFileManager;
import messageformats.DataMessage;
import peer.PeerDownloadRate;
import peer.PeerInfo;
import peer.PeerProcess;
import peer.ProgramParams;
import utils.CommonUtils;

public class DeterminePreferredNeighbours implements Runnable {

	private static int SIZE_OF_THREAD_POOL = 5;
	private Set<Integer> interestedNeighbourSet;
	private Set<Integer> preferredNeighbourSet = new HashSet<>();
	private Map<Integer, Double> downloadRateMap;
	private int numberOfPreferredNeighbours;
	private ScheduledExecutorService scheduler;
	private IFileManager iFileManager;
	private Logger logger;

	public DeterminePreferredNeighbours() {
		initializeDownloadRates();
		interestedNeighbourSet = Collections.synchronizedSet(new HashSet<>());
		ProgramParams programParams = PeerProcess.peerProcess.getProgramParams();
		numberOfPreferredNeighbours = programParams.getNumberOfPreferredNeighbors();
		iFileManager = PeerProcess.peerProcess.getiFileManager();

		// schedule it
		scheduler = Executors.newScheduledThreadPool(SIZE_OF_THREAD_POOL);
		scheduler.scheduleAtFixedRate(this, programParams.getUnchokingInterval(), programParams.getUnchokingInterval(),
				TimeUnit.SECONDS);

		logger = PeerProcess.peerProcess.getLogger();
	}

	@Override
	synchronized public void run() {
		Set<Integer> newPreferredNeighbourSet = new HashSet<>();
		// if the current file has all pieces, select
		// preferred neighbours randomly
		if (iFileManager.hasAllPieces()) {
			List<Integer> interestedNeighbourList = new ArrayList<>();
			interestedNeighbourList.addAll(interestedNeighbourSet);
			Collections.shuffle(interestedNeighbourList);

			int numberOfPreferred = interestedNeighbourList.size() < numberOfPreferredNeighbours
					? interestedNeighbourList.size() : numberOfPreferredNeighbours;

			for (int i = 0; i < numberOfPreferred; i++) {
				newPreferredNeighbourSet.add(interestedNeighbourList.get(i));
			}

		} else {
			PriorityQueue<PeerDownloadRate> sortedQueue = new PriorityQueue<>();
			downloadRateMap.forEach((id, rate) -> {
				if (interestedNeighbourSet.contains(id)) {
					sortedQueue.add(new PeerDownloadRate(id, rate));
				}
			});

			for (int i = 0; i < numberOfPreferredNeighbours; i++) {
				PeerDownloadRate peer = sortedQueue.poll();
				if (peer == null) {
					break;
				}
				newPreferredNeighbourSet.add(peer.getPeerId());
			}
		}

		preferredNeighbourSet = newPreferredNeighbourSet;

		logger.log(Level.ALL, CommonUtils.formatString("peer # has the preferred neighbours #",
				PeerProcess.peerProcess.getPeerId(), preferredNeighbourSet));

		// send unchoke message
		for (int peer : preferredNeighbourSet) {
			ObjectOutputStream objectOutputStream = PeerProcess.peerProcess.getPeerInfoForPeerId(peer).getOut();
			new DataMessage(DataMessage.MESSAGE_TYPE_UNCHOKE, null).sendDataMessage(objectOutputStream);
		}
	}

	public void initializeDownloadRates() {
		downloadRateMap = new ConcurrentHashMap<>();

		PeerProcess.peerProcess.getPeerList().forEach(peer -> {
			downloadRateMap.put(peer.getPeerId(), 0.0);
		});
	}

	synchronized public Set<Integer> getNonPreferredButInterestedSet() {
		try {
			Set<Integer> nonPreferredButInterestedSet = new HashSet<>();
			nonPreferredButInterestedSet.addAll(interestedNeighbourSet);
			nonPreferredButInterestedSet.removeAll(preferredNeighbourSet);
			return nonPreferredButInterestedSet;
		} catch (Exception e) {
			System.out.println(e);
		}

		return null;
	}

	public void shutdown() {
		scheduler.shutdown();
	}

	public boolean isPreferredNeighbour(int peerId) {
		return preferredNeighbourSet.contains(peerId);
	}

	public void addToInterested(int peerId) {
		interestedNeighbourSet.add(peerId);
	}

	public void removeFromInterested(int peerId) {
		interestedNeighbourSet.remove(peerId);
	}

	public void updateDownloadRate(PeerDownloadRate peerDownloadRate) {
		downloadRateMap.put(peerDownloadRate.getPeerId(), peerDownloadRate.getDownloadRate());
	}
}
