package scheduledtasks;

import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import messageformats.DataMessage;
import peer.PeerProcess;
import peer.ProgramParams;
import utils.CommonUtils;

public class DetermineOptimisticallyUnchokedNeighbour implements Runnable {

	private static int SIZE_OF_THREAD_POOL = 5;
	private Integer optimisticallyUnchokedNeighbour = 0;
	private DeterminePreferredNeighbours determinePreferredNeighbours;
	private ScheduledExecutorService scheduler;
	private Logger logger;

	public DetermineOptimisticallyUnchokedNeighbour() {
		determinePreferredNeighbours = PeerProcess.peerProcess.getDeterminePreferredNeighbours();
		ProgramParams programParams = PeerProcess.peerProcess.getProgramParams();

		scheduler = Executors.newScheduledThreadPool(SIZE_OF_THREAD_POOL);
		scheduler.scheduleAtFixedRate(this, programParams.getOptimisticUnchokingInterval(),
				programParams.getOptimisticUnchokingInterval(), TimeUnit.SECONDS);
		logger = PeerProcess.peerProcess.getLogger();
	}

	@Override
	public void run() {
		Set<Integer> candidateSet = determinePreferredNeighbours.getNonPreferredButInterestedSet();

		if (candidateSet.size() == 0) {
			return;
		}

		int randomInt = new Random().nextInt(candidateSet.size());

		int i = 0;
		for (Integer peerId : candidateSet) {
			if (i++ == randomInt) {
				optimisticallyUnchokedNeighbour = peerId;
			}
		}

		logger.log(Level.ALL,
				CommonUtils.formatString(
						"peer # has the optimistically unchoked neighbour #",
						PeerProcess.peerProcess.getPeerId(),
						optimisticallyUnchokedNeighbour));

		ObjectOutputStream objectOutputStream =
				PeerProcess.peerProcess.getPeerInfoForPeerId(optimisticallyUnchokedNeighbour).getOut();
		new DataMessage(DataMessage.MESSAGE_TYPE_UNCHOKE, null).sendDataMessage(objectOutputStream);
	}


	public void shutdown() {
		Logger debugLogger = PeerProcess.peerProcess.getDebugLogger();
		debugLogger.log(Level.ALL, CommonUtils.formatString(
				"shutdown attempted on scheduler for peer #",
				PeerProcess.peerProcess.getPeerId()));
		scheduler.shutdownNow();
	}

	public boolean isOptimisticallyUnchokedneighbour(int peerId) {
		return peerId == (int) optimisticallyUnchokedNeighbour;
	}
}
