package testfiles;

import fileio.FileManagerFactory;
import fileio.IFileManager;

public class TestFileManager {

	//FileName /Users/shobhit/Documents/workspace/BitTorrent2/src/test.pdf
	//FileSize 9031660
	//PieceSize 2000000

	//FileName /Users/shobhit/Documents/workspace/BitTorrent2/src/testfiles/CNT5106C_0321.mp4
	//FileSize 1024805329
	//PieceSize 32000
	static String sourceFileName = "/Users/shobhit/Documents/workspace/BitTorrent2/src/testfiles/CNT5106C_0321.mp4";
	static int fileSize = 1024805329;
	static int pieceSize = 32000;
	static String destinationFileName = "/Users/shobhit/Documents/workspace/BitTorrent2/src/testfiles/test.mp4";
	static String destinationFileName2 = "/Users/shobhit/Documents/workspace/BitTorrent2/src/testfiles/test2.mp4";
	static String destinationFileName3 = "/Users/shobhit/Documents/workspace/BitTorrent2/src/testfiles/test3.mp4";

	public static void main(String[] args) {
		IFileManager iFileManagerSeed = FileManagerFactory.constructFileManager(sourceFileName,
				fileSize, pieceSize, true);

		IFileManager iFManagerPeer = FileManagerFactory.constructFileManager(destinationFileName,
				fileSize, pieceSize, false);

		IFileManager iFManagerPeer2 = FileManagerFactory.constructFileManager(destinationFileName2,
				fileSize, pieceSize, false);

		IFileManager iFManagerPeer3 = FileManagerFactory.constructFileManager(destinationFileName3,
				fileSize, pieceSize, false);

		int totalPieces = (int) Math.ceil((double)fileSize/pieceSize);

		for (int i = 1; i <= totalPieces; i++) {
			iFManagerPeer.setPiece(iFileManagerSeed.getPiece(i), i);
			iFManagerPeer2.setPiece(iFileManagerSeed.getPiece(i), i);
			iFManagerPeer3.setPiece(iFileManagerSeed.getPiece(i), i);
		}

		iFileManagerSeed.flush();
		iFManagerPeer.flush();
		iFManagerPeer2.flush();
		iFManagerPeer3.flush();
	}
}
