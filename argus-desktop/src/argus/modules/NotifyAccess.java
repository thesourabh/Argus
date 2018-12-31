package argus.modules;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

public class NotifyAccess {

	public static void sendFile(OutputStream os, File file) {
		int byteCount = 2048;
		byte[] buffer = new byte[byteCount];
		BufferedOutputStream bos = new BufferedOutputStream(os, byteCount);
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			System.out.println(in.available());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = 0, total = 0;
		System.out.println("About to start sending file");
		try {
			while ((i = in.read(buffer, 0, byteCount)) != -1) {
				total += i;
				System.out.print(i + "  ");
				bos.write(buffer, 0, i);
				bos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\nTotal:" + total);
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void sendImage(OutputStream os, BufferedImage screen) {
		File file = new File("trans.png");
		try {
			ImageIO.write(screen, "png", file);
		} catch (IOException e) {
			System.out.println("Error in creating transfer file");
			e.printStackTrace();
		}
		sendFile(os, file);
	}

	public static void notifyAdmin(String serverIp) {
		Socket client = null;
		try {
			if (serverIp.equals("0.0.0.0"))
				serverIp = "192.168.43.1";
			System.out.println(serverIp);
			client = new Socket(serverIp, 13580);
			OutputStream os = client.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			String ip = " ", name = "", nameAtIp = " ";
			try {
				ip = Inet4Address.getLocalHost().getHostAddress();
				name = Inet4Address.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			nameAtIp = name + "@" + ip;
			pw.println(nameAtIp);
			pw.flush();
			/*
			File captureFile = new File("Capture.png");
			try {
				BufferedImage image = ImageIO.read(captureFile);
				sendImage(os, image);
				captureFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		notifyAdmin("0.0.0.0");
	}
}
