package sour.project.be;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import sour.project.be.data.Controls;
import sour.project.be.data.Server;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class UsbActivity extends AppCompatActivity {
	private int usbAction = Controls.Usb.ENABLE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usb);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		RadioGroup radio = (RadioGroup) findViewById(R.id.rgUsbOptions);
		Button btnApplyUsb = (Button) findViewById(R.id.btnApplyUsb);
		radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.d(App.TAG, "Checked ID: " + checkedId);
				switch (checkedId) {
				case R.id.rbUsb1:
					usbAction = Controls.Usb.ENABLE;
					break;
				case R.id.rbUsb2:
					usbAction = Controls.Usb.WRITE_PROTECT;
					break;
				case R.id.rbUsb3:
					usbAction = Controls.Usb.DISABLE;
					break;
				}
				Log.d(App.TAG, "Action ID: " + usbAction);
			}
		});
		btnApplyUsb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new UsbTask().execute(usbAction);
			}
		});
	}

	public class UsbTask extends AsyncTask<Integer, Void, Void> {

		int errorCode = -1;
		int control;
		boolean open;

		Socket client = null;

		@Override
		protected void onPostExecute(Void result) {
			if (errorCode > 0) {
				Toast.makeText(getApplicationContext(), "Unable to connect",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Void doInBackground(Integer... params) {

			control = params[0];
			errorCode = -1;

			try {
				client = new Socket(Server.IP, Server.PORT);
				PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				pw.println(control);
				pw.close();
			} catch (UnknownHostException e) {
				Log.d(App.TAG, "unknown host");
				e.printStackTrace();
				errorCode = 0;
			} catch (IOException e) {
				Log.d(App.TAG, "io #1");
				e.printStackTrace();
				errorCode = 1;
			} finally {
				try {
					if (client != null)
						client.close();
				} catch (IOException e) {
					Log.d(App.TAG, "io #2");
					e.printStackTrace();
					errorCode = 2;
				}
			}
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.usb, menu);
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
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}