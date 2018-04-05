package fileio;

public class FileManager implements IFileManager {

	private final static long BYTES_IN_GB = 1024 * 1024 * 1024;

	IFileManager iFileManager;

	public FileManager(String fileName, int fileSize, int pieceSize,
			boolean hasFile) {
		if (fileSize < (BYTES_IN_GB)/2) {
			iFileManager = new InMemoryFileManager(fileName, fileSize,
					pieceSize, hasFile);
		} else {
			iFileManager = new DiskBasedFileManager(fileName, fileSize,
					pieceSize, hasFile);
		}
	}

	@Override
	public byte[] getPiece(int pieceNumber) {
		return iFileManager.getPiece(pieceNumber);
	}

	@Override
	public void setPiece(byte[] data, int pieceNumber) {
		iFileManager.setPiece(data, pieceNumber);
	}

	@Override
	public void flush() {
		iFileManager.flush();
	}

}
