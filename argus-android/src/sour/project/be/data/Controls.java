package sour.project.be.data;

import android.graphics.drawable.Drawable;

public class Controls {
	public static final int PING = 0;
	public static final int SCREENSHOT = 1;

	public class Usb {
		public static final int ENABLE = 20, WRITE_PROTECT = 21, DISABLE = 22;
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

	public final String name;
	public final Drawable icon;
	public final Class<?> cls;

	public Controls(String _name, Drawable _icon, Class<?> _cls) {
		name = _name;
		icon = _icon;
		cls = _cls;
	}
}