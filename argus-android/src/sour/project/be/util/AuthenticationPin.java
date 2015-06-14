package sour.project.be.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AuthenticationPin {
	
	private static String failString = "????";

	public static String getStoredPin(Context context) {
		SharedPreferences settings = context.getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		String pin = settings.getString("pin", failString);
		return pin;
	}

	public static int validatePin(String enteredPinString) {
		if (enteredPinString.length() != 4)
			return -1;
		int enteredPinInt;
		try {
			enteredPinInt = Integer.parseInt(enteredPinString);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return -1;
		}
		if (enteredPinInt < 0)
			return -1;
		return enteredPinInt;

	}

	public static void createPin(String enteredPin, Context context) {
		Editor e = context.getSharedPreferences("settings",
				Context.MODE_PRIVATE).edit();
		e.putString("pin", enteredPin);
		e.commit();
	}

}
