package uci.ucintlmwidget;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ProxyService extends Service {
	/*
	 * This is the service that starts the server thread.
	 * It remains in the notification area.
	 * */
	private String user = "";
	private String pass = "";
	private String domain = "";
	private String server = "";
	private int inputport = 8080;
	private int outputport = 8080;
	private server s;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		s.stop();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		user = intent.getStringExtra("user");
		pass = intent.getStringExtra("pass");
		domain = intent.getStringExtra("domain");
		server = intent.getStringExtra("server");
		inputport = Integer.valueOf(intent.getStringExtra("inputport"));
		outputport = Integer.valueOf(intent.getStringExtra("outputport"));
		Log.w(getClass().getName(), "Starting for user " + user+"@"+domain);
		s = new server(user, pass, domain, server, inputport, outputport);
		s.execute();
		notifyit();

		return START_NOT_STICKY;
	}

	public void notifyit() {
		Notification note = new Notification(R.drawable.ic_ntlm, Antlm.notif1,
				System.currentTimeMillis());
		Intent i = new Intent(this, Antlm.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		note.setLatestEventInfo(this, "UCIntlm", Antlm.notif2 + ": " + user, pi);
		note.flags |= Notification.FLAG_NO_CLEAR;

		startForeground(1337, note);
	}

}
