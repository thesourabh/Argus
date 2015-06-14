package sour.project.be;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SocketService extends Service {

	ServerSocket server = null;
	Socket client = null;

	public SocketService() {

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		return null;
		// throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		initServerSocket();
	}

	private void initServerSocket() {
		try {
			server = new ServerSocket();
			server.setReuseAddress(true);
			server.bind(new InetSocketAddress(13580));
		} catch (IOException e) {
			Log.d(App.TAG, "service onCreate IOException");
			e.printStackTrace();
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Thread t = new Thread(new SocketThread());
		t.start();
		return 1;
	}

	private void notifyAccess(String rawDeviceString) {
		String[] deviceArr = rawDeviceString.split("@");
		String deviceName = deviceArr[0], deviceIp = deviceArr[1];
		String message = "Someeone just logged into the device " + deviceName
				+ " with address " + deviceIp + ".";
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent intent = new Intent(getApplicationContext(), IntruderActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, intent, 0);
		Notification n = new NotificationCompat.Builder(getApplicationContext())
				.setContentTitle("Daenerys: Access Attempt")
				.setContentText(message.toString())
				.setSmallIcon(R.drawable.ic_launcher)
				.setSound(
						RingtoneManager
								.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setContentIntent(pIntent).setAutoCancel(true)
				.setTicker("Access attempt on device: Daenerys").build();
		nm.notify(0, n);
	}

	private class SocketThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					if (server == null) {
						initServerSocket();
						Log.d(App.TAG,
								"initServerSocket called from thread instead of onCreate");
					}
					client = server.accept();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(client.getInputStream()));
					String line = br.readLine();
					FileOutputStream fos = openFileOutput("intruder.png",
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

					
					notifyAccess(line);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (client != null)
							client.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}

	@Override
	public void onDestroy() {
		try {
			if (server != null)
				server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}

}
