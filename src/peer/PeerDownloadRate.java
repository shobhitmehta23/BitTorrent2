package peer;

public class PeerDownloadRate implements Comparable<PeerDownloadRate> {

	private int peerId;
	private double downloadRate;
	private long requestTime;
	private boolean isRequestTimeInitialized;

	public PeerDownloadRate() {};

	public PeerDownloadRate(int peerId, double downloadRate) {
		this.peerId = peerId;
		this.downloadRate = downloadRate;
	}

	synchronized public void startTimer() {
		requestTime = System.currentTimeMillis();
		isRequestTimeInitialized = true;
	}

	synchronized public void stopTimer() {

		if (!isRequestTimeInitialized) {
			return;
		}

		long receivedTime = System.currentTimeMillis();
		long totalTime = receivedTime - requestTime;
		downloadRate = 1.0/((double)totalTime);
		isRequestTimeInitialized = false;
	}

	synchronized public void cancelTime() {
		isRequestTimeInitialized = false;
		downloadRate = 0.0;
	}

	@Override
	public int compareTo(PeerDownloadRate o) {
		return ((Double)this.getDownloadRate()).compareTo(o.getDownloadRate());
	}

	public int getPeerId() {
		return peerId;
	}

	public double getDownloadRate() {
		return downloadRate;
	}
}
