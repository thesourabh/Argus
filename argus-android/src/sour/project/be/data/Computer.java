package sour.project.be.data;

public class Computer {

	public static final String SEPARATOR = "%`%";
	private String name, address;
	private boolean online;

	public Computer() {
	}

	public Computer(String name) {
		setName(name);
	}

	public Computer(String name, String ip) {
		setName(name);
		setIpAddress(ip);
		setOnline(false);
	}

	public Computer(String name, boolean online) {
		setName(name);
		setOnline(online);
	}

	public void setName(String _name) {
		name = _name;
	}

	public void setIpAddress(String ip) {
		address = ip;
	}

	public void setOnline(boolean _online) {
		online = _online;
	}

	public String getName() {
		return name;
	}

	public String getIpAddress() {
		return address;
	}

	public boolean isOnline() {
		return online;
	}

}
