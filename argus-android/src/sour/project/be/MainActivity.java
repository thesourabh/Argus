package sour.project.be;

import sour.project.be.util.AuthenticationPin;


import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private String pin;
	private String pinPositiveMessage;

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow()
					.addFlags(
							WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().setStatusBarColor(
					getResources().getColor(R.color.black));
		}
		setContentView(R.layout.activity_main);
		// final String channel = "demo";
		// final Pubnub pubnub = new Pubnub("demo", "demo", false);
		
		startService(new Intent(this, SocketService.class));

		TextView terms = (TextView) findViewById(R.id.tvTerms);
		terms.setText(Html
				.fromHtml("By using this application, you agree to our <font color=\"#FFCDD2\">Terms of Service</font> and <font color=\"#FFCDD2\">Privacy Policy</font>."));
		Button btnLogIn = (Button) findViewById(R.id.btnLogIn);
		btnLogIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createDialog();
			}
		});
		// Button btnSignUp = (Button) findViewById(R.id.btnSignUp);
		// btnSignUp.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// /*
		// * pubnub.publish(channel, "Testing from Android lalala", new
		// * Callback() {
		// *
		// * @Override public void errorCallback(String channel,
		// * PubnubError error) { super.errorCallback(channel, error);
		// * Log.d("sour.project.be", channel + ": " + error.toString());
		// * }
		// *
		// * @Override public void successCallback(String channel, Object
		// * message) { super.successCallback(channel, message);
		// * Log.d("sour.project.be", channel + ": " +
		// * message.toString()); }
		// *
		// * });
		// */
		//
		// NotificationManager nm = (NotificationManager)
		// getSystemService(NOTIFICATION_SERVICE);
		// Intent intent = new Intent(MainActivity.this,
		// ComputersActivity.class);
		// PendingIntent pIntent = PendingIntent.getActivity(
		// getApplicationContext(), 0, intent, 0);
		// Notification n = new NotificationCompat.Builder(
		// getApplicationContext())
		// .setContentTitle("Daenerys: Access Attempt")
		// .setContentText(
		// "Someone just tried to access the computer Daenerys.")
		// .setSmallIcon(R.drawable.ic_launcher)
		// .setContentIntent(pIntent).setAutoCancel(true)
		// .setTicker("Access attempt on Computer: Daenerys")
		// .build();
		// nm.notify(0, n);
		//
		// }
		// });

		// Intent serviceIntent = new Intent(this, PubnubService.class);
		// startService(serviceIntent);
		// Log.i("HelloWorldActivity", "PubNub Activity Started!");
	}

	private void createDialog() {

		LayoutInflater inflater = getLayoutInflater();

		final View authenticationView = inflater.inflate(
				R.layout.authentication_pin, null);
		AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
		adBuilder.setView(authenticationView).setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText etPin = (EditText) authenticationView
								.findViewById(R.id.etPin);
						etPin.setText("");
						dialog.dismiss();
					}
				});
		pin = AuthenticationPin.getStoredPin(getApplicationContext());
		Log.d(App.TAG, "Pin: " + pin);
		pinPositiveMessage = (pin.equals("????")) ? "Create Pin" : "Log In";
		adBuilder.setPositiveButton(pinPositiveMessage,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText etPin = (EditText) authenticationView
								.findViewById(R.id.etPin);
						String enteredPinString = etPin.getText().toString();
						etPin.setText("");
						int valid = AuthenticationPin
								.validatePin(enteredPinString);
						if (valid == -1) {
							Toast.makeText(getApplicationContext(),
									"Invalid Pin", Toast.LENGTH_SHORT)
									.show();
							dialog.dismiss();
							return;
						}
						if (pin.equals("????")) {
							AuthenticationPin.createPin(enteredPinString,
									getApplicationContext());
							pin = AuthenticationPin
									.getStoredPin(getApplicationContext());
						} else {
							if (!pin.equals(enteredPinString)) {
								dialog.dismiss();
								return;
							}
						}
						dialog.dismiss();
						Intent intent = new Intent(MainActivity.this,
								ComputersActivity.class);
						startActivity(intent);

					}
				});
		AlertDialog dialog = adBuilder.create();
		dialog.show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
