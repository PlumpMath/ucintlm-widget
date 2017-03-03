package uci.ucintlmwidget;

import cu.uci.wsuci.classes.SoapObjectResultsAbstraction;
import cu.uci.wsuci.classes.SoapService;
import cu.uci.wsuci.service.Akademos;
import cu.uci.wsuci.service.Autenticacion;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Antlm extends Activity {

	Button startButton;
	Button stopButton;
	EditText user;
	EditText pass;
	EditText domain;
	EditText server;
	EditText inputport;
	EditText outputport;
	public static String notif1, notif2;
	public String shprefreg = "MSG_switch_status";
	public static ProgressDialog pDialog;

	public static SharedPreferences prefs;
	public static SharedPreferences.Editor editor;
	public static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_antlm);
		startButton = (Button) findViewById(R.id.button1);
		stopButton = (Button) findViewById(R.id.button2);
		user = (EditText) findViewById(R.id.euser);
		pass = (EditText) findViewById(R.id.epass);
		domain = (EditText) findViewById(R.id.edomain);
		server = (EditText) findViewById(R.id.eserver);
		inputport = (EditText) findViewById(R.id.einputport);
		outputport = (EditText) findViewById(R.id.eoutputport);
		notif1 = getString(R.string.notif1);
		notif2 = getString(R.string.notif2);
		context = this.getApplicationContext();

		prefs = getSharedPreferences(shprefreg, Context.MODE_PRIVATE);
		editor = prefs.edit();

		loadConf();

		if (isMyServiceRunning()) {
			disbleAll();
		} else {
			enableAll();
		}

	}

	public void loadConf() {
		SharedPreferences settings = getSharedPreferences("UCIntlm.conf", 0);

		user.setText(settings.getString("user", ""));
		domain.setText(settings.getString("domain", "uci.cu"));
		server.setText(settings.getString("server", "10.0.0.1"));
		inputport.setText(settings.getString("input_port", "8080"));
		outputport.setText(settings.getString("output_port", "8080"));
		if (user.getText().toString().equals("")) {
			user.requestFocus();
		} else {
			pass.requestFocus();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.antlm, menu);
		return true;
	}

	private void disbleAll() {
		user.setEnabled(false);
		pass.setEnabled(false);
		domain.setEnabled(false);
		server.setEnabled(false);
		inputport.setEnabled(false);
		outputport.setEnabled(false);
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
	}

	private void enableAll() {
		user.setEnabled(true);
		pass.setEnabled(true);
		domain.setEnabled(true);
		server.setEnabled(true);
		inputport.setEnabled(true);
		outputport.setEnabled(true);
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (ProxyService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void clickRun(View arg0) throws Exception {
		login(user.getText().toString(), pass.getText().toString());
	}

	private void login(String user, String pass) {
		pDialog = new ProgressDialog(Antlm.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage("Comprobando...");
		pDialog.setCancelable(false);
		pDialog.setMax(100);

		AutenticationAsyncTask task = new AutenticationAsyncTask();
		task.execute(new String[] { user, pass });
	}

	public void clickStop(View arg0) {
		stopService(new Intent(this, ProxyService.class));
		enableAll();

		editor.putString(shprefreg, "off"); // Estado apagado
		editor.commit();
		UCIntlmWidget.actualizarWidget(context,
				AppWidgetManager.getInstance(context), "off");
	}

	public void saveConf() {
		SharedPreferences settings = getSharedPreferences("UCIntlm.conf", 0);
		SharedPreferences.Editor settingsEditor = settings.edit();
		settingsEditor.putString("user", user.getText().toString());
		settingsEditor.putString("pass",
				Encripter.encrypt(pass.getText().toString()));
		settingsEditor.putString("domain", domain.getText().toString());
		settingsEditor.putString("server", server.getText().toString());
		settingsEditor.putString("inputport", inputport.getText().toString());
		settingsEditor.putString("outputport", outputport.getText().toString());
		settingsEditor.commit();
	}

	private class AutenticationAsyncTask extends
			AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				return validAccount(params[0], params[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				pDialog.dismiss();
				Toast.makeText(Antlm.this, "Login correcto!",
						Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(context, ProxyService.class);

				intent.putExtra("user", user.getText().toString());
				intent.putExtra("pass", pass.getText().toString());
				intent.putExtra("domain", domain.getText().toString());
				intent.putExtra("server", server.getText().toString());
				intent.putExtra("inputport", inputport.getText().toString());
				intent.putExtra("outputport", outputport.getText().toString());
				startService(intent);

				disbleAll();
				saveConf();

				editor.putString(shprefreg, "on"); // Actualizando el widget
				editor.commit();
				UCIntlmWidget.actualizarWidget(context,
						AppWidgetManager.getInstance(context), "on");
			} else {
				SharedPreferences settings = getSharedPreferences(
						"UCIntlm.conf", 0);
				SharedPreferences.Editor settingsEditor = settings.edit();
				settingsEditor.putString("pass", "nan");
				settingsEditor.commit();

				pDialog.dismiss();
				Toast.makeText(Antlm.this, "Usuario o pass inválido!!",
						Toast.LENGTH_SHORT).show();
			}
		}

		private boolean validAccount(String user, String pass) throws Exception {
			SoapService miServicio = new SoapService(
					new Autenticacion().autenticarUsuario(new Object[] { user,
							pass, "uci.cu" }));
			SoapObjectResultsAbstraction results = new SoapObjectResultsAbstraction(
					miServicio.Consultar());
			return results.getElementValueByIndex(8).equals("true");
		}
	}

}
