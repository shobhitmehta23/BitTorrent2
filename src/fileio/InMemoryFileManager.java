package fileio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InMemoryFileManager implements IFileManager {

	private Map<Integer, byte[]> pieceToDataMap;
	private BitSet piecesAvailable;
	private int totalPieces;
	private int lastDataSize;
	private String fileName;
	private boolean hasFile;

	public InMemoryFileManager(String fileName, int fileSize, int pieceSize,
			boolean hasFile) {
		totalPieces = (int) Math.ceil((double)fileSize/pieceSize);
		lastDataSize = fileSize % pieceSize;
		lastDataSize = (lastDataSize == 0)? pieceSize : lastDataSize;
		this.fileName = fileName;
		this.hasFile = hasFile;

		piecesAvailable = new BitSet(totalPieces + 1); // to avoid 0 based indexing
		piecesAvailable.set(0); // it is a bit we are not using.

		if (hasFile) {
			piecesAvailable.set(1, totalPieces + 1);
			loadFile(fileName, totalPieces, pieceSize);
		} else {
			pieceToDataMap = new HashMap<>();
		}
	}

	@Override
	public byte[] getPiece(int pieceNumber) {
		return pieceToDataMap.get(pieceNumber);
	}

	@Override
	synchronized public boolean setPiece(byte[] data, int pieceNumber) {
		if (!pieceToDataMap.containsKey(pieceNumber)) {
			pieceToDataMap.put(pieceNumber, data);

			piecesAvailable.set(pieceNumber);
			return true;
		}
		return false;
	}

	@Override
	synchronized public void flush() {
		if (!hasFile) {
			constructFile();
			hasFile = true;
		}
	}

	private void constructFile() {
		BufferedOutputStream bufferedOutputStream = null;
		try {
			bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (int i = 1; i <= totalPieces; i++) {
			try {
				bufferedOutputStream.write(pieceToDataMap.get(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bufferedOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFile(String fileName, int numberOfPieces, int pieceSize) {
		BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new BufferedInputStream(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		pieceToDataMap = new HashMap<>();

		for (int i = 1; i <= numberOfPieces; i++) {
			byte data[] = null;
			if (i == numberOfPieces) {
				data = new byte[lastDataSize];
			} else {
				data = new byte[pieceSize];
			}

			try {
				bufferedInputStream.read(data);
			} catch (IOException e) {
				e.printStackTrace();
			}

			pieceToDataMap.put(i, data);
		}
	}

	@Override
	public int getRandomMissingPieceIndex(BitSet remotePeerBitSet) {
		BitSet temp = (BitSet)remotePeerBitSet.clone();
		temp.xor(piecesAvailable);
		temp.andNot(piecesAvailable);

		if (temp.isEmpty()) {
			return -1;
		}

		int randomIndex = new Random().nextInt(temp.length());
		return temp.nextSetBit(randomIndex);
	}

	@Override
	public boolean hasAllPieces() {
		return piecesAvailable.cardinality() == (1 + totalPieces);
	}

	@Override
	public boolean hasAllPieces(BitSet remotePeerBitSet) {
		return remotePeerBitSet.cardinality() == (1 + totalPieces);
	}

	@Override
	public BitSet getPieceSet() {
		return piecesAvailable;
	}
}