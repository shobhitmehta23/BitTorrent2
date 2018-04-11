package testfiles;

import fileio.FileManagerFactory;
import fileio.IFileManager;

public class TestFileManager {

	static String sourceFileName = "/Users/shobhit/Documents/workspace/BitTorrent2/src/peer/PeerInfo.java";
	static int fileSize = 2785;
	static int pieceSize = 19;
	static String destinationFileName = "/Users/shobhit/Documents/workspace/BitTorrent2/src/testfiles/PeerInfo.java";

	public static void main(String[] args) {
		IFileManager iFileManagerSeed = FileManagerFactory.constructFileManager(sourceFileName,
				fileSize, pieceSize, true);

		IFileManager iFManagerPeer = FileManagerFactory.constructFileManager(destinationFileName,
				fileSize, pieceSize, false);

		int totalPieces = (int) Math.ceil((double)fileSize/pieceSize);

		for (int i = 1; i <= totalPieces; i++) {
			iFManagerPeer.setPiece(iFileManagerSeed.getPiece(i), i);
		}

		iFileManagerSeed.flush();
		iFManagerPeer.flush();
	}
}
