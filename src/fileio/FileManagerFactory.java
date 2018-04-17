package fileio;

import java.util.logging.Level;

import peer.PeerProcess;

public class FileManagerFactory {
	private final static long BYTES_IN_GB = 1024 * 1024 * 1024;

	public static IFileManager constructFileManager(String fileName, int fileSize, int pieceSize, boolean hasFile) {
		if (fileSize < (BYTES_IN_GB) / 2) {
			PeerProcess.peerProcess.getDebugLogger().log(Level.ALL, "using in memory file handler");
			return new InMemoryFileManager(fileName, fileSize, pieceSize, hasFile);
		} else {
			PeerProcess.peerProcess.getDebugLogger().log(Level.ALL, "using disk handler");
			return new DiskBasedFileManager(fileName, fileSize, pieceSize, hasFile);
		}
	}
}
