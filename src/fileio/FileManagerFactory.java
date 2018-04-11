package fileio;

public class FileManagerFactory {
	private final static long BYTES_IN_GB = 1024 * 1024 * 1024;

	public static IFileManager constructFileManager(String fileName, int fileSize, int pieceSize,
			boolean hasFile) {
		if (fileSize < (BYTES_IN_GB)/2) {
			return new InMemoryFileManager(fileName, fileSize,
					pieceSize, hasFile);
		} else {
			return new DiskBasedFileManager(fileName, fileSize,
					pieceSize, hasFile);
		}
	}
}
