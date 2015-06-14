package argus.util;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import argus.modules.QRCode;

public class Tray {

	public static void addToTray() {
		TrayIcon trayIcon;
		if (SystemTray.isSupported()) {
			// get the SystemTray instance
			SystemTray tray = SystemTray.getSystemTray();
			// load an image
			Image image = Toolkit.getDefaultToolkit().getImage("ic_qrcode.png");
			// create a action listener to listen for default action executed on
			// the tray icon
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					QRCode.createIpAddressQR();
				}
			};
			// create a popup menu
			PopupMenu popup = new PopupMenu();
			// create menu item for the default action
			MenuItem defaultItem = new MenuItem("QR");
			defaultItem.addActionListener(listener);
			popup.add(defaultItem);
			// / ... add other items
			// construct a TrayIcon
			trayIcon = new TrayIcon(image, "Argus", popup);
			// set the TrayIcon properties
			// trayIcon.addActionListener(listener);
			// ...
			// add the tray image
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println(e);
			}
			// ...
		}
	}

	public static void main(String[] args) {
		addToTray();
	}
}
