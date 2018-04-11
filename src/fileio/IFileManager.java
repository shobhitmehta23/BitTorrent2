package fileio;

import java.util.BitSet;

public interface IFileManager {
	public byte[] getPiece(int pieceNumber);
	public boolean setPiece(byte[] data, int pieceNumber);
	public void flush();
	public int getRandomMissingPieceIndex(BitSet remotePeerBitSet);
	public boolean hasAllPieces();
	public boolean hasAllPieces(BitSet remotePeerBitSet);
	public BitSet getPieceSet();
}
