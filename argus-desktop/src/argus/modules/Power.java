package argus.modules;
import java.io.File;
import java.io.IOException;

import argus.util.CommandLine;

public class Power {
	public static void lock() {
		CommandLine.exec("rundll32.exe user32.dll, LockWorkStation");
	}

	public static void shutdown() {
		CommandLine.exec("shutdown /s /t 10");
	}

	public static void restart() {
		CommandLine.exec("shutdown /r /t 10");
	}

	public static void hibernate() {
		CommandLine.exec("shutdown /h");
	}

	public static void main(String args[]) throws IOException {
		File[] files = File.listRoots();
		for (File file : files) {
			if (file.exists())
				System.out.println(file.getPath() + " Free: "
						+ file.getFreeSpace() / 1024.0 / 1024.0 / 1024.0);
		}
	}

}
