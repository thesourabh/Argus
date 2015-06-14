package argus.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Registry {
	private static void add(String key) throws IOException,
			InterruptedException {
		key = "reg add " + key;
		String k[] = key.split(" ");
		ProcessBuilder pb = new ProcessBuilder(k);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		p.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		System.out.println(br.readLine());
	}

	public static String addKey(String key, String value, String type,
			String data) throws IOException {
		String keyString;
		keyString = key + " /v " + value + " /t " + type + " /d " + data
				+ " /f";
		try {
			add(keyString);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyString;
	}

	public static void main(String[] args) throws IOException {
		// System.out.println(addKey("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\StorageDevicePolicies",
		// "WriteProtect", "REG_DWORD", "1"));
	}

}
