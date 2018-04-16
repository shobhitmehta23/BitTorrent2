package peer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import fileio.IFileManager;
import messageformats.DataMessage;
import messageformats.HandshakeMessage;
import scheduledtasks.DetermineOptimisticallyUnchokedNeighbour;
import scheduledtasks.DeterminePreferredNeighbours;
import utils.CommonUtils;
import utils.Constants;

public class PeerConnectionManager extends Thread {
	private PeerInfo currentPeerInfo;
	private PeerInfo remotePeerInfo;
	private Logger logger;

	public PeerConnectionManager(PeerInfo currentPeerInfo, PeerInfo remotePeerInfo) {
		this.currentPeerInfo = currentPeerInfo;
		this.remotePeerInfo = remotePeerInfo;
		logger = PeerProcess.peerProcess.getLogger();
	}

	public void setRemotePeerInfo(PeerInfo remotePeerInfo) {
		this.remotePeerInfo = remotePeerInfo;
	}

	@Override
	public void run() {
		// if remotePeer is initialized, it means current peer should initiate handshake
		if (remotePeerInfo.isInitialized()) {
			sendHandshakeMessage();
			acceptHandshakeMessage();
		} else {
			acceptHandshakeMessage();
			logger.log(Level.ALL,
					CommonUtils.formatString(
							"peer # is connected from peer #",
							currentPeerInfo.getPeerId(),
							remotePeerInfo.getPeerId()));
			sendHandshakeMessage();
		}

		// send bit field message
		DataMessage bitFieldMessage = new DataMessage(
				DataMessage.MESSAGE_TYPE_BITFIELD,
				PeerProcess.peerProcess.getiFileManager().getPieceSet().toByteArray());
		bitFieldMessage.sendDataMessage(remotePeerInfo.getOut());

		receiveMessages();
	}

	public void acceptHandshakeMessage() {
		ObjectInputStream in = remotePeerInfo.getIn();
		try {
			HandshakeMessage handshakeMessage = (HandshakeMessage)in.readObject();
			PeerInfo remotePeerInfo = PeerProcess.peerProcess.getPeerInfoForPeerId(
					handshakeMessage.getPeerIdAsInt());
			remotePeerInfo.copyPeerInfoSocketConfigs(this.remotePeerInfo);
			this.remotePeerInfo = remotePeerInfo;
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			// this is the case when the node receives some other message
			// while it was expecting a handshake message
			// it will throw a ClassCastException while casting to
			// HandshakeMessage above. So lets try again.
			acceptHandshakeMessage();
		}
	}

	public void sendHandshakeMessage() {
		ObjectOutputStream out = remotePeerInfo.getOut();
		try {
			out.writeObject(new HandshakeMessage(currentPeerInfo.getPeerId()));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void receiveMessages() {

		IFileManager iFileManager = PeerProcess.peerProcess.getiFileManager();
		ObjectInputStream in = remotePeerInfo.getIn();
		ObjectOutputStream out = remotePeerInfo.getOut();
		DeterminePreferredNeighbours determinePreferredNeighbours =
				PeerProcess.peerProcess.getDeterminePreferredNeighbours();
		DetermineOptimisticallyUnchokedNeighbour determineOptimisticallyUnchokedNeighbour =
				PeerProcess.peerProcess.getDetermineOptimisticallyUnchokedNeighbour();
		PeerDownloadRate peerDownloadRate = new PeerDownloadRate();
		// while current peer does not have all pieces OR
		// the remote peer does not have all pieces.

		while ((!(iFileManager.hasAllPieces())) ||
				(!(iFileManager.hasAllPieces(remotePeerInfo.getPeerPieces())))) {

			try {

			DataMessage messageReceived = null;
			try {
				messageReceived = (DataMessage)in.readObject();
			} catch (OptionalDataException e) {
				if (e.eof) {
					System.out.println("EOF");
				} else {
					System.out.println("sss" + e.length);
				}
			} catch (ClassNotFoundException | IOException e) {
				logger.log(Level.ALL, "exception in " + remotePeerInfo.getPeerId());
				e.printStackTrace();
				continue;
			}

			switch(messageReceived.getMessageType()) {
			case DataMessage.MESSAGE_TYPE_BITFIELD:
				BitSet peerBitSet = BitSet.valueOf(messageReceived.getPayload());
				remotePeerInfo.setPeerPieces(peerBitSet);
				int index = iFileManager.getRandomMissingPieceIndex(peerBitSet);
				if (index == -1) {
					DataMessage notInterestedMessage = new DataMessage(DataMessage.MESSAGE_TYPE_NOT_INTERESTED, null);
					notInterestedMessage.sendDataMessage(out);
				} else {
					DataMessage interestedMessage = new DataMessage(DataMessage.MESSAGE_TYPE_INTERESTED, null);
					interestedMessage.sendDataMessage(out);
				}
				break;

			case DataMessage.MESSAGE_TYPE_HAVE:
				int pieceNumber = CommonUtils.byteArrayToInteger(messageReceived.getPayload());
				logger.log(Level.ALL,
						CommonUtils.formatString(
								"peer # received the 'have' message from peer # for piece #",
								currentPeerInfo.getPeerId(),
								remotePeerInfo.getPeerId(),
								pieceNumber));
				remotePeerInfo.markPeerPiece(pieceNumber);
				index = iFileManager.getRandomMissingPieceIndex(remotePeerInfo.getPeerPieces());
				if (index != -1) {
					DataMessage interestedMessage = new DataMessage(DataMessage.MESSAGE_TYPE_INTERESTED, null);
					interestedMessage.sendDataMessage(out);
				}
				break;

			case DataMessage.MESSAGE_TYPE_INTERESTED:
				logger.log(Level.ALL,
						CommonUtils.formatString(
								"peer # received the 'interested' message from peer #",
								currentPeerInfo.getPeerId(),
								remotePeerInfo.getPeerId()));
				// add to interested list
				determinePreferredNeighbours.addToInterested(remotePeerInfo.getPeerId());
				break;

			case DataMessage.MESSAGE_TYPE_NOT_INTERESTED:
				// remove from interested list
				determinePreferredNeighbours.removeFromInterested(remotePeerInfo.getPeerId());
				logger.log(Level.ALL,
						CommonUtils.formatString(
								"peer # received the 'not interested' message from peer #",
								currentPeerInfo.getPeerId(),
								remotePeerInfo.getPeerId()));
				break;

			case DataMessage.MESSAGE_TYPE_REQUEST:
				if (!(determinePreferredNeighbours.isPreferredNeighbour(remotePeerInfo.getPeerId()) ||
						determineOptimisticallyUnchokedNeighbour.isOptimisticallyUnchokedneighbour(remotePeerInfo.getPeerId()))) {
					break;
				}
				pieceNumber = CommonUtils.byteArrayToInteger(messageReceived.getPayload());
				byte[] pieceData = iFileManager.getPiece(pieceNumber);
				DataMessage pieceMessage = new DataMessage(
						DataMessage.MESSAGE_TYPE_PIECE, CommonUtils.mergeByteArrays(messageReceived.getPayload(),
								pieceData));
				pieceMessage.sendDataMessage(out);
				break;

			case DataMessage.MESSAGE_TYPE_PIECE:
				int numberOfPiecesBeforeThisPiece = iFileManager.getNumberOfPieces();
				peerDownloadRate.stopTimer();
				determinePreferredNeighbours.updateDownloadRate(peerDownloadRate);
				pieceNumber = messageReceived.getPieceNumberForPieceData(messageReceived.getPayload());
				pieceData = messageReceived.getPieceData(messageReceived.getPayload());

				boolean didISet = iFileManager.setPiece(pieceData, pieceNumber);

				/*if (!didISet) {
					break;
				}*/

				index = iFileManager.getRandomMissingPieceIndex(remotePeerInfo.getPeerPieces());
				if (index == -1) {
					DataMessage notInterestedMessage = new DataMessage(DataMessage.MESSAGE_TYPE_NOT_INTERESTED, null);
					notInterestedMessage.sendDataMessage(out);
				} else {
					DataMessage requestMessage = new DataMessage(
							DataMessage.MESSAGE_TYPE_REQUEST, CommonUtils.intToByteArray(index));
					requestMessage.sendDataMessage(out);
					peerDownloadRate.startTimer();
				}

				if (!didISet) {
					break;
				}

				logger.log(Level.ALL,
						CommonUtils.formatString(
								"peer # has downloaded the message # from peer #. "
								+ "The numer of pieces is now #",
								currentPeerInfo.getPeerId(),
								pieceNumber,
								remotePeerInfo.getPeerId(),
								(numberOfPiecesBeforeThisPiece + 1)));

				if ((numberOfPiecesBeforeThisPiece + 1) == iFileManager.getTotalPieces()) {
					logger.log(Level.ALL,
							CommonUtils.formatString(
									"peer # has downloaded the complete file",
									currentPeerInfo.getPeerId()));
				}

				PeerProcess.peerProcess.getPeerList().forEach(peerInfo->{
					DataMessage haveMessage = new DataMessage(
							DataMessage.MESSAGE_TYPE_HAVE, CommonUtils.intToByteArray(pieceNumber));
					ObjectOutputStream tempOut = peerInfo.getOut();
					if (tempOut != null) {
						haveMessage.sendDataMessage(peerInfo.getOut());
					}
				});
				break;

			case DataMessage.MESSAGE_TYPE_UNCHOKE:
				logger.log(Level.ALL,
						CommonUtils.formatString(
								"peer # is unchoked by peer #",
								currentPeerInfo.getPeerId(),
								remotePeerInfo.getPeerId()));
				index = iFileManager.getRandomMissingPieceIndex(remotePeerInfo.getPeerPieces());
				if (index == -1) {
					DataMessage notInterestedMessage = new DataMessage(DataMessage.MESSAGE_TYPE_NOT_INTERESTED, null);
					notInterestedMessage.sendDataMessage(out);
				} else {
					DataMessage requestMessage = new DataMessage(
							DataMessage.MESSAGE_TYPE_REQUEST, CommonUtils.intToByteArray(index));
					requestMessage.sendDataMessage(out);
					peerDownloadRate.startTimer();
				}

				break;

			case DataMessage.MESSAGE_TYPE_CHOKE:
				logger.log(Level.ALL,
						CommonUtils.formatString(
								"peer # is choked by peer #",
								currentPeerInfo.getPeerId(),
								remotePeerInfo.getPeerId()));
				peerDownloadRate.cancelTime();
				determinePreferredNeighbours.updateDownloadRate(peerDownloadRate);
				break;
			}

			} catch (Exception e) {
				PeerProcess.peerProcess.getDebugLogger().log(Level.ALL, CommonUtils.formatString(
						"Exception encoutered and handle in connection between peer # and peer #",
						currentPeerInfo.getPeerId(), remotePeerInfo.getPeerId()));
			}

		}

		try {
			iFileManager.flush();
			remotePeerInfo.bufferedShutdownSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}