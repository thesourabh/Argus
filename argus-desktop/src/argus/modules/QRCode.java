package argus.modules;

import java.awt.image.BufferedImage;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import argus.util.QRGen;

public class QRCode {
	public static void createIpAddressQR() {
		String ip = " ", name = "", nameAtIp = " ";
		try {
			ip = Inet4Address.getLocalHost().getHostAddress();
			name = Inet4Address.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		nameAtIp = name + "@" + ip;
		System.out.println(nameAtIp);
		BufferedImage image = QRGen.create(nameAtIp);
		JFrame frame = new JFrame();
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);

	}

	public static void main(String[] args) {
		createIpAddressQR();
	}
}
