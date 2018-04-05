package testfiles;

import fileio.DiskBasedFileManager;
import fileio.IFileManager;
import fileio.InMemoryFileManager;

public class TestInMemoryFileManager {

	static String sourceFileName = "/Users/shobhit/Desktop/sem3/DBI/table_data/tpch-dbgen/lineitem.tbl";
	static int fileSize = 759863287;
	static int pieceSize = 25000;
	static String destinationFileName = "/Users/shobhit/Documents/workspace/BitTorrent2/src/testfiles/lineitem.tbl";

	public static void main(String[] args) {
		IFileManager iFileManagerSeed = new InMemoryFileManager(sourceFileName,
				fileSize, pieceSize, true);

		IFileManager iFManagerPeer = new InMemoryFileManager(destinationFileName,
				fileSize, pieceSize, false);

		/*IFileManager iFileManagerSeed = new DiskBasedFileManager(sourceFileName,
				fileSize, pieceSize, true);

		IFileManager iFManagerPeer = new DiskBasedFileManager(destinationFileName,
				fileSize, pieceSize, false);*/

		int totalPieces = (int) Math.ceil((double)fileSize/pieceSize);

		for (int i = 1; i <= totalPieces; i++) {
			iFManagerPeer.setPiece(iFileManagerSeed.getPiece(i), i);
		}

		iFileManagerSeed.flush();
		iFManagerPeer.flush();
	}

}
