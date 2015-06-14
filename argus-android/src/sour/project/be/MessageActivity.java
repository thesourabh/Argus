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
import android.widget.EditText;
import android.widget.Toast;

public class MessageActivity extends AppCompatActivity {

	private EditText etMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		etMessage = (EditText) findViewById(R.id.etMessage);
		Button btnSendMessage = (Button) findViewById(R.id.btnSendMessage);
		btnSendMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String msg = etMessage.getText().toString();
				new MessageTask(Controls.MESSAGE).execute(msg);
			}
		});
	}

	private class MessageTask extends AsyncTask<String, Void, Void> {

		int errorCode = -1;
		int control;
		String msg;

		Socket client = null;

		public MessageTask(int _control) {
			control = _control;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (errorCode > 0) {
				Toast.makeText(getApplicationContext(), "Unable to connect",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Void doInBackground(String... params) {

			msg = params[0];
			errorCode = -1;

			try {
				client = new Socket(Server.IP, Server.PORT);
				PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				Log.d(App.TAG, "about to write control: " + control);
				pw.println(control);
				Log.d(App.TAG, "wrote control: " + control + "\nNow sending message: " + msg);
				pw.println(msg);
				Log.d(App.TAG, "wrote message: " + msg);
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
		getMenuInflater().inflate(R.menu.message, menu);
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
