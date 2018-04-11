package peer;

public class PeerDownloadRate implements Comparable<PeerDownloadRate> {

	private int peerId;
	private double downloadRate;

	public PeerDownloadRate(int peerId, double downloadRate) {
		this.peerId = peerId;
		this.downloadRate = downloadRate;
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
