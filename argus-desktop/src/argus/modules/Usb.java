package argus.modules;
import java.io.IOException;

import argus.util.Registry;

public class Usb {
	public static void enable() {
		try {
			Registry.addKey(
					"HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\USBSTOR",
					"Start", "REG_DWORD", "3");
			Registry.addKey(
					"HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\StorageDevicePolicies",
					"WriteProtect", "REG_DWORD", "0");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void disable() {
		try {
			Registry.addKey(
					"HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\USBSTOR",
					"Start", "REG_DWORD", "4");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeProtect() {
		try {
			enable();
			Registry.addKey(
					"HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\StorageDevicePolicies",
					"WriteProtect", "REG_DWORD", "1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
