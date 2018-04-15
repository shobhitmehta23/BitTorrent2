import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import messageformats.DataMessage;
import messageformats.HandshakeMessage;

public class Test {

	public static void main(String[] args) {
		Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
		System.out.println(path.getParent());
		File f = new File(path.getParent().toString() + "/" + "test_dir");
		f.mkdirs();
	}

}
