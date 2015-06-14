package sour.project.be;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import sour.project.be.data.Controls;
import sour.project.be.data.Server;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WebcamActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webcam);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		final Button btnTakePicture = (Button) findViewById(R.id.btnTakePicture);
		final ToggleButton tbCamera = (ToggleButton) findViewById(R.id.tbCamera);
		// new WebcamTask().execute(Controls.Camera.CHECK_CAMERA);
		tbCamera.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					tbCamera.setText("Initializing...");
					new WebcamTask().execute(Controls.Camera.OPEN_CAMERA);
				} else {
					new WebcamTask().execute(Controls.Camera.CLOSE_CAMERA);
				}

			}
		});
		btnTakePicture.setEnabled(false);
		btnTakePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new WebcamTask().execute(Controls.Camera.TAKE_PICTURE);
			}
		});
	}

	@Override
	protected void onResume() {
		Log.d(App.TAG, "in onResume");
		new WebcamTask().execute(Controls.Camera.CHECK_CAMERA);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		Log.d(App.TAG, "in onDestroy");
		new WebcamTask().execute(Controls.Camera.CLOSE_CAMERA);
		super.onDestroy();
	}

	public class WebcamTask extends AsyncTask<Integer, Void, Bitmap> {

		int errorCode = -1;
		int control;
		boolean open;

		Socket client = null;
		InputStream is = null;

		@Override
		protected void onPostExecute(Bitmap result) {
			if (errorCode == Controls.Camera.ERROR_CAMERA) {
				Toast.makeText(
						getApplicationContext(),
						"Camera error. It might be under use by another application.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (errorCode > 0) {
				Toast.makeText(getApplicationContext(), "Unable to connect",
						Toast.LENGTH_SHORT).show();
			}
			if (result == null) {
				Button btnTakePicture = (Button) findViewById(R.id.btnTakePicture);
				ToggleButton tbCamera = (ToggleButton) findViewById(R.id.tbCamera);
				switch (control) {
				case Controls.Camera.OPEN_CAMERA:
					tbCamera.setText("Camera On");
					btnTakePicture.setEnabled(true);
					break;
				case Controls.Camera.CLOSE_CAMERA:
					btnTakePicture.setEnabled(false);
					break;
				case Controls.Camera.CHECK_CAMERA:
					btnTakePicture.setEnabled(open);
					if (open)
						tbCamera.setText("Camera On");
					else
						tbCamera.setText("Camera Off");
					break;
				}
				return;
			}
			ImageView ivScreen = (ImageView) findViewById(R.id.imageView1);
			ivScreen.setImageBitmap(result);
			super.onPostExecute(result);
		}

		@Override
		protected Bitmap doInBackground(Integer... params) {

			control = params[0];
			errorCode = -1;
			Bitmap screen = null;

			try {
				client = new Socket(Server.IP, Server.PORT);
				PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				is = client.getInputStream();
				pw.println(control);
				switch (control) {
				case Controls.Camera.OPEN_CAMERA:
					int code = openCamera();
					if (code == Controls.Camera.ERROR_CAMERA)
						errorCode = code;
					break;
				case Controls.Camera.CLOSE_CAMERA:
					break;
				case Controls.Camera.TAKE_PICTURE:
					screen = takePicture();
					break;
				case Controls.Camera.CHECK_CAMERA:
					checkCamera();
					break;
				}
				pw.close();

				return screen;
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

		private void checkCamera() throws IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String recv = br.readLine();
			Log.d(App.TAG, "Is camera on? Server says " + recv);
			open = recv.equalsIgnoreCase("yes");
		}

		private int openCamera() throws IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String recv = br.readLine();
			int nullCount = 0;
			do {
				if (recv == null) {
					nullCount++;
					Log.d(App.TAG, "null #" + nullCount);
					if (nullCount > 10) {
						recv = "" + Controls.Camera.ERROR_CAMERA;
						break;
					}
				} else
					Log.d(App.TAG, recv);
			} while (recv == null);
			return Integer.parseInt(recv);

		}

		private Bitmap takePicture() throws IOException {
			FileOutputStream fos = openFileOutput("CurrentScreen.png",
					Context.MODE_PRIVATE);
			int byteCount = 2048;
			byte[] buffer = new byte[byteCount];
			BufferedInputStream bis = new BufferedInputStream(is, byteCount);
			int i = 0, fileSize = 0;
			Log.d(App.TAG, "About to start receiving.");
			while ((i = bis.read(buffer, 0, byteCount)) != -1) {
				fileSize += i;
				fos.write(buffer, 0, i);
				fos.flush();
			}
			Log.d(App.TAG, "File Length: " + fileSize);
			String path = getFileStreamPath("CurrentScreen.png")
					.getAbsolutePath();
			return BitmapFactory.decodeFile(path);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.screenshot, menu);
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
