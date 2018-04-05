package fileio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

public class DiskBasedFileManager implements IFileManager {

	private RandomAccessFile randomAccessFile;
	private int pieceSize;
	private int totalPieces;
	private int lastDataSize;
	private BitSet piecesAvailable;

	public DiskBasedFileManager(String fileName, int fileSize, int pieceSize,
			boolean hasFile) {
		try {
			randomAccessFile = new RandomAccessFile(fileName, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		totalPieces = (int) Math.ceil((double)fileSize/pieceSize);
		lastDataSize = fileSize % pieceSize;
		lastDataSize = (lastDataSize == 0)? pieceSize : lastDataSize;
		this.pieceSize = pieceSize;

		piecesAvailable = new BitSet(totalPieces + 1);
		piecesAvailable.set(0);

		if (hasFile) {
			piecesAvailable.set(1, totalPieces + 1);
		} else {
			try {
				randomAccessFile.setLength(fileSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	synchronized public byte[] getPiece(int pieceNumber) {

		byte[] data = null;
		if (pieceNumber == totalPieces) {
			data = new byte[lastDataSize];
		} else {
			data = new byte[pieceSize];
		}

		int nextReadPosition = (pieceNumber - 1) * pieceSize;
		try {
			randomAccessFile.seek(nextReadPosition);
			randomAccessFile.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	@Override
	synchronized public void setPiece(byte[] data, int pieceNumber) {

		if (piecesAvailable.get(pieceNumber)) {
			return;
		}

		piecesAvailable.set(pieceNumber);

		int nextWritePosition = (pieceNumber - 1) * pieceSize;
		try {
			randomAccessFile.seek(nextWritePosition);
			randomAccessFile.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void flush() {
		try {
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
