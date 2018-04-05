package fileio;

public interface IFileManager {
	public byte[] getPiece(int pieceNumber);
	public void setPiece(byte[] data, int pieceNumber);
	public void flush();
}
