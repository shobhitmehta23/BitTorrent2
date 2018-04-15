package logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeerLogger {
	private Logger logger;

	public PeerLogger(int peerId) {
		logger = Logger.getLogger(PeerLogger.class.getName());
		logger.setLevel(Level.ALL);

		FileHandler fileHandler = null;
		try {
			fileHandler = new FileHandler(getLogFileName(peerId));
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}

		fileHandler.setFormatter(new LogFormatter());
		logger.addHandler(fileHandler);
	}

	public Logger getLogger() {
		return logger;
	}

	private String getLogFileName(int peerId) {
		return "log_peer_" + peerId + ".log";
	}
}
