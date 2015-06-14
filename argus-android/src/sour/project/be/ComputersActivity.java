package sour.project.be;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import sour.project.be.adapters.ComputersAdapter;
import sour.project.be.data.Computer;
import sour.project.be.data.Controls;
import sour.project.be.data.Server;
import sour.project.be.util.AuthenticationPin;
import sour.project.be.util.SwipeableRecyclerViewTouchListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ComputersActivity extends AppCompatActivity {

	List<Computer> computers;
	RecyclerView rvComputers;
	ComputersAdapter adapter;
	SwipeRefreshLayout srlComputers;
	FloatingActionButton fabAdd;
	Toolbar toolbar;
	public static final int QR_REQUEST_CODE = 1;
	private String filename = "computers_list";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_computers);

		toolbar = (Toolbar) findViewById(R.id.toolbarComputers);
		setSupportActionBar(toolbar);
		getSupportActionBar().getHeight();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		toolbar.getChildCount();
		// toolbar.removeAllViews();

		fabAdd = (FloatingActionButton) findViewById(R.id.fabAddComputer);
		fabAdd.setTag(true);
		fabAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if ((boolean) v.getTag()) {
					LayoutInflater.from(getApplicationContext()).inflate(
							R.layout.toolbar_computers, toolbar);
					Button btnQR = (Button) findViewById(R.id.btnQRCode);
					btnQR.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getApplicationContext(),
									QRReaderActivity.class);
							startActivityForResult(intent, QR_REQUEST_CODE);
						}
					});
				} else {
					EditText etCompName = (EditText) findViewById(R.id.etComputerName);
					String compName = etCompName.getText().toString();
					EditText etCompIp = (EditText) findViewById(R.id.etComputerIp);
					String compIp = etCompIp.getText().toString();
					if (compName.isEmpty() || compIp.isEmpty())
						return;
					Log.d(App.TAG, compName + compIp);
					toolbar.removeViewAt(3);
					computers.add(0, new Computer(compName, compIp));
					adapter.notifyItemInserted(0);
					new PingTask(0).execute(Controls.PING);
					saveDevicesList();
				}
				v.setTag(!(boolean) v.getTag());

			}
		});

		srlComputers = (SwipeRefreshLayout) findViewById(R.id.srlComputers);
		srlComputers.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				onResume();
			}
		});

		readDevicesList();

		rvComputers = (RecyclerView) findViewById(R.id.rvComputers);
		rvComputers.setHasFixedSize(true);
		LinearLayoutManager llm = new LinearLayoutManager(this);

		llm.setOrientation(LinearLayoutManager.VERTICAL);
		rvComputers.setLayoutManager(llm);
		adapter = new ComputersAdapter(this, computers, rvComputers);
		rvComputers.setAdapter(adapter);

		SwipeableRecyclerViewTouchListener swipeTouchListener = new SwipeableRecyclerViewTouchListener(
				rvComputers,
				new SwipeableRecyclerViewTouchListener.SwipeListener() {
					@Override
					public boolean canSwipe(int position) {
						return true;
					}

					@Override
					public void onDismissedBySwipeLeft(
							RecyclerView recyclerView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							removeDevice(position);
						}
						adapter.notifyDataSetChanged();
					}

					@Override
					public void onDismissedBySwipeRight(
							RecyclerView recyclerView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							removeDevice(position);
						}
						adapter.notifyDataSetChanged();
					}
				});

		rvComputers.addOnItemTouchListener(swipeTouchListener);

	}

	private void removeDevice(int position) {
		computers.remove(position);
		adapter.notifyItemRemoved(position);
		saveDevicesList();
	}

	@Override
	public void onBackPressed() {
		boolean toolbarStatus = (boolean) fabAdd.getTag();
		Log.d(App.TAG, (toolbarStatus ? "CLOSED" + toolbarStatus : "OPEN"
				+ toolbarStatus));
		if (!toolbarStatus) {
			toolbar.removeViewAt(3);
			fabAdd.setTag(!toolbarStatus);
		} else {
			super.onBackPressed();
		}
	}

	private List<Computer> unflattenDevicesList(String flatDevices) {

		computers = new ArrayList<Computer>();
		String[] devices = flatDevices.split("\n");
		Log.d(App.TAG, "DEVLENGTH: " + devices.length);
		Log.d(App.TAG, "DEV: " + devices[0]);
		Log.d(App.TAG, "DEVDEVLENGTH: " + devices[0].length());
		for (int i = 0; i < devices.length; i++) {
			if (devices[i].length() == 0)
				continue;
			String[] device = devices[i].split("%`%");
			computers.add(new Computer(device[0], device[1]));
		}
		return computers;
	}

	private void readDevicesList() {
		String devices = "";
		try {
			FileInputStream fis = openFileInput(filename);
			int c;
			while ((c = fis.read()) != -1) {
				devices += Character.toString((char) c);
			}
		} catch (FileNotFoundException e) {
			Log.d(App.TAG, "Read: FileNotFound");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(App.TAG, "Read: IOExce");
			e.printStackTrace();
		}
		unflattenDevicesList(devices);
	}

	private String flattenDevicesList() {
		String devices = "";
		String deviceName, deviceIp;
		for (int i = 0; i < computers.size(); i++) {
			deviceName = computers.get(i).getName();
			deviceIp = computers.get(i).getIpAddress();
			devices += deviceName + Computer.SEPARATOR + deviceIp + "\n";
		}
		return devices;
	}

	private void saveDevicesList() {
		String data = flattenDevicesList();
		try {
			FileOutputStream fos = openFileOutput(filename,
					Context.MODE_PRIVATE);
			fos.write(data.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(App.TAG, "Write: FileNotFound");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(App.TAG, "Write: IOExce");
			e.printStackTrace();
		}

	}

	@Override
	protected void onResume() {
		// DELETE THE IF RETURN PART BEFORE PUBLISHING FOR GOD'S SAKE

		for (int i = 0; i < computers.size(); i++) {
			Log.d(App.TAG, "Launching PingTask for computer #" + i);
			new PingTask(i).execute(Controls.PING);
		}
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == QR_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String ip = data.getStringExtra("nameAtIp");
				try {
					String host[] = ip.split("@");
					Log.d(App.TAG, "Received host details: " + ip);
					EditText etCompName = (EditText) findViewById(R.id.etComputerName);
					etCompName.setText(host[0]);
					EditText etCompIp = (EditText) findViewById(R.id.etComputerIp);
					etCompIp.setText(host[1]);
				} catch (ArrayIndexOutOfBoundsException e) {
					Toast.makeText(this, "Incorrect QR Code",
							Toast.LENGTH_SHORT).show();
				}
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.computers, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.change_pin) {
			changePin();
			return true;
		}
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void changePin() {
		String pinPositiveMessage = "Set Pin";
		final View authenticationView = getLayoutInflater().inflate(
				R.layout.authentication_pin, null);

		AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
		adBuilder
				.setView(authenticationView)
				.setPositiveButton(pinPositiveMessage,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								EditText etPin = (EditText) authenticationView
										.findViewById(R.id.etPin);
								String enteredPinString = etPin.getText()
										.toString();
								etPin.setText("");
								int valid = AuthenticationPin
										.validatePin(enteredPinString);
								if (valid == -1) {
									Toast.makeText(getApplicationContext(),
											"Invalid Pin", Toast.LENGTH_SHORT)
											.show();
									return;
								}
								AuthenticationPin.createPin(enteredPinString,
										getApplicationContext());
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								EditText etPin = (EditText) authenticationView
										.findViewById(R.id.etPin);
								etPin.setText("");
								dialog.dismiss();
							}
						});
		AlertDialog dialog = adBuilder.create();
		dialog.show();
	}

	public class PingTask extends AsyncTask<Integer, Void, Boolean> {

		int errorCode = -1;
		int control;
		boolean open;

		int position;
		String ip;

		public PingTask(int position) {
			this.position = position;
			ip = computers.get(position).getIpAddress();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			boolean isChanged = computers.get(position).isOnline() != result;
			if (isChanged) {
				computers.get(position).setOnline(result);
				adapter.notifyItemChanged(position);
			}
			if (srlComputers.isRefreshing())
				srlComputers.setRefreshing(false);
		}

		@Override
		protected Boolean doInBackground(Integer... params) {

			control = params[0];
			errorCode = -1;
			int online = 0;
			Boolean result = false;

			Socket client = null;

			try {
				SocketAddress sockaddr = new InetSocketAddress(ip, Server.PORT);
				client = new Socket();
				int timeout = 2000;
				Log.d(App.TAG, "attempting ip: " + ip);
				try {
					client.connect(sockaddr, timeout);
				} catch (Exception e) {
					return result;
				}
				Log.d(App.TAG, ip + " is reachable");
				Log.d(App.TAG, "done connecting ip: " + ip);
				PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				pw.println(control);
				WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
				wm.getConnectionInfo().getIpAddress();
				String ip = Formatter.formatIpAddress(wm.getConnectionInfo()
						.getIpAddress());
				Log.d(App.TAG, "Server IP: " + ip);
				pw.println(ip);
				online = Integer.parseInt(br.readLine());
				Log.d(App.TAG, "control: " + control + "\nonline?" + online);
				result = (online == control);
				Log.d(App.TAG, "result: " + result);
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
			Log.d(App.TAG, "result" + result);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
}
