import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.xml.sax.ext.LexicalHandler;

import logging.PeerLogger;
import messageformats.DataMessage;
import messageformats.HandshakeMessage;

public class Test {

	public static void main(String[] args) {

		test("aca", "ac");

		/*Logger logger = new PeerLogger(101).getLogger();
		logger.log(Level.ALL, "ewfrf");
		logger.log(Level.ALL, "this is info 2");*/

		/*Logger LOGGER = Logger.getLogger(Test.class.getName());
		LOGGER.setLevel(Level.ALL);
		FileHandler fileHandler = null;
		try {
			fileHandler = new FileHandler("logtest.txt");
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fileHandler.setFormatter(new Formatter() {

			@Override
			public String format(LogRecord record) {
				return record.getMessage();
			}

			@Override
			public String getHead(Handler h) {
				return "test";
			}
		});

		LOGGER.addHandler(fileHandler);
		LOGGER.log(Level.ALL, "this is a test");*/
	}

	public static void test(Object... objects) {
		System.out.println(objects.length);
	}

}
