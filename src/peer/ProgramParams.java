package peer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import fileio.FileManagerFactory;
import fileio.IFileManager;
import utils.Constants;

public class ProgramParams {

	private int numberOfPreferredNeighbors;
	private int unchokingInterval;
	private int optimisticUnchokingInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;
	private int totalPieces;

	public ProgramParams() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(Constants.COMMON_CONFIG_FILENAME));
			numberOfPreferredNeighbors = Integer.parseInt(br.readLine().split("\\s+")[1]);
			unchokingInterval = Integer.parseInt(br.readLine().split("\\s+")[1]);
			optimisticUnchokingInterval = Integer.parseInt(br.readLine().split("\\s+")[1]);
			fileName = br.readLine().split("\\s+")[1];
			fileSize = Integer.parseInt(br.readLine().split("\\s+")[1]);
			pieceSize = Integer.parseInt(br.readLine().split("\\s+")[1]);
			totalPieces = (int) Math.ceil((double)fileSize/pieceSize);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public IFileManager constructFileManager(boolean hasFile) {
		if (!hasFile) {
			String []temp = fileName.split("\\.");
			fileName = temp[0] + "_" + PeerProcess.peerProcess.getPeerId() + "." + temp[1];
		}
		return FileManagerFactory.constructFileManager(fileName, fileSize, pieceSize, hasFile);
	}

	public int getNumberOfPreferredNeighbors() {
		return numberOfPreferredNeighbors;
	}

	public int getUnchokingInterval() {
		return unchokingInterval;
	}

	public int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}

	public String getFileName() {
		return fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getPieceSize() {
		return pieceSize;
	}

	public int getTotalPieces() {
		return totalPieces;
	}
}
