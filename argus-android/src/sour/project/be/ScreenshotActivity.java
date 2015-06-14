package sour.project.be;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import sour.project.be.data.Controls;
import sour.project.be.data.Server;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ScreenshotActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screenshot);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Button btnTakeScreenshot = (Button) findViewById(R.id.btnTakeScreenshot);
		btnTakeScreenshot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ScreenshotTask().execute();
			}
		});
	}

	public class ScreenshotTask extends AsyncTask<Void, Void, Bitmap> {

		int errorCode = -1;

		@Override
		protected void onPostExecute(Bitmap result) {
			if (errorCode > 0) {
				Toast.makeText(getApplicationContext(), "Unable to connect",
						Toast.LENGTH_SHORT).show();
			}
			ImageView ivScreen = (ImageView) findViewById(R.id.imageView1);
			ivScreen.setImageBitmap(result);
			super.onPostExecute(result);
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			errorCode = -1;
			Socket client = null;
			Bitmap screen = null;

			try {
				Log.d(App.TAG, "Creating socket");
				client = new Socket(Server.IP, Server.PORT);
				Log.d(App.TAG, "Created socket");
				PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				Log.d(App.TAG, "Created PrintWriter with OutputStream");
				pw.println(Controls.SCREENSHOT);
				Log.d(App.TAG, "Sent Screenshot Command");
				Log.d(App.TAG, "Closed PrintWriter");
				FileOutputStream fos = openFileOutput("CurrentScreen.png",
						Context.MODE_PRIVATE);
				Log.d(App.TAG, "Created FileOutputStream");
				int byteCount = 2048;
				byte[] buffer = new byte[byteCount];
				Log.d(App.TAG, "Created byte buffer");
				InputStream is = client.getInputStream();
				Log.d(App.TAG, "Created inputstream");
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
				screen = BitmapFactory.decodeFile(path);

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
