package scheduledtasks;

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

import fileio.IFileManager;
import peer.PeerDownloadRate;
import peer.PeerProcess;
import peer.ProgramParams;

public class DeterminePreferredNeighbours implements Runnable {

	private static int SIZE_OF_THREAD_POOL = 5;
	private Set<Integer> interestedNeighbourSet;
	private Set<Integer> preferredNeighbourSet = new HashSet<>();
	private Map<Integer, Double> downloadRateMap;
	private int numberOfPreferredNeighbours;
	private ScheduledExecutorService scheduler;
	private IFileManager iFileManager;

	public DeterminePreferredNeighbours() {
		initializeDownloadRates();
		interestedNeighbourSet = Collections.synchronizedSet(new HashSet<>());
		ProgramParams programParams = PeerProcess.peerProcess.getProgramParams();
		numberOfPreferredNeighbours = programParams.getNumberOfPreferredNeighbors();
		iFileManager = PeerProcess.peerProcess.getiFileManager();

		// schedule it
		scheduler = Executors.newScheduledThreadPool(SIZE_OF_THREAD_POOL);
		scheduler.scheduleAtFixedRate(
				this, programParams.getUnchokingInterval(),
				programParams.getUnchokingInterval(), TimeUnit.SECONDS);
	}

	@Override
	synchronized public void run() {

		// if the current file has all pieces, select
		// preferred neighbours randomly
		if (iFileManager.hasAllPieces()) {
			List<Integer> interestedNeighbourList = new ArrayList<>();
			interestedNeighbourList.addAll(interestedNeighbourSet);
			Collections.shuffle(interestedNeighbourList);

			int numberOfPreferred = interestedNeighbourList.size() < numberOfPreferredNeighbours?
					interestedNeighbourList.size() : numberOfPreferredNeighbours;

			Set<Integer> newPreferredNeighbourSet = new HashSet<>();
			for (int i = 0; i < numberOfPreferred; i++) {
				newPreferredNeighbourSet.add(interestedNeighbourList.get(i));
			}
			preferredNeighbourSet = newPreferredNeighbourSet;
			return;
		}

		PriorityQueue<PeerDownloadRate> sortedQueue = new PriorityQueue<>();
		downloadRateMap.forEach((id, rate)-> {
			if (interestedNeighbourSet.contains(id)) {
				sortedQueue.add(new PeerDownloadRate(id, rate));
			}
		});

		Set<Integer> newPreferredNeighbourSet = new HashSet<>();
		for (int i = 0; i < numberOfPreferredNeighbours; i++) {
			PeerDownloadRate peer = sortedQueue.poll();
			if (peer == null) {
				break;
			}
			newPreferredNeighbourSet.add(peer.getPeerId());
		}

		preferredNeighbourSet = newPreferredNeighbourSet;

		// TODO
		// send unchoke message
	}

	public void initializeDownloadRates() {
		downloadRateMap = new ConcurrentHashMap<>();

		PeerProcess.peerProcess.getPeerList().forEach(peer->{
			downloadRateMap.put(peer.getPeerId(), 0.0);
		});
	}

	synchronized public Set<Integer> getNonPreferredButInterestedSet() {
		Set<Integer> nonPreferredButInterestedSet = new HashSet<>();
		nonPreferredButInterestedSet.addAll(interestedNeighbourSet);
		nonPreferredButInterestedSet.removeAll(preferredNeighbourSet);
		return nonPreferredButInterestedSet;
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

	public void updateDownloadRate(int peerId, double downloadRate) {
		downloadRateMap.put(peerId, downloadRate);
	}
}
