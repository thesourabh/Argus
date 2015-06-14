package argus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandLine {

	public static String exec(String command) {
		String[] args = command.split(" ");
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String line = "", wholeOutput = "";
		try {
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				wholeOutput += line + "\n";
			}
		} catch (IOException ioe) {

		}
		return wholeOutput;

	}

}
