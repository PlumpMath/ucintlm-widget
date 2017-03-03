package uci.ucintlmwidget;

import org.apache.commons.logging.Log;

public class UCIntlmWidget extends AppWidgetProvider {
	private static final String ACTION_cambiarlayout = "a_cambiarlayout";
	public String shprefreg = "MSG_switch_status", user, pass;

	private static boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (ProxyService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onEnabled(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(shprefreg,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		if (isMyServiceRunning(context)) {// Actualizar el widget en el momento
											// de instanciarlo
			editor.putString(shprefreg, "on");
			editor.commit();
			actualizarWidget(context, AppWidgetManager.getInstance(context),
					"on");
		} else {
			editor.putString(shprefreg, "off");
			editor.commit();
			actualizarWidget(context, AppWidgetManager.getInstance(context),
					"off");
		}
		super.onEnabled(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		/*
		 * Accedemos al shared preference shprefreg y tratamos de leer, si hay
		 * algun error mensaje tomara el valor "error" lo que indicara que no
		 * existe dentro de los shared preference 'MSG_switch_status' con lo
		 * cual lo creamos y añadimos como valor off
		 */

		SharedPreferences prefs = context.getSharedPreferences(shprefreg,
				Context.MODE_PRIVATE);
		String mensaje = prefs.getString(shprefreg, "error");

		if (mensaje == "error") {

			// si cuando intenta leer es error añade un registro.
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(shprefreg, "off"); // Estado apagado
			editor.commit();

			Log.e(" SharedPreferences error read", "" + mensaje);
			Log.e(" SharedPreferences W -&gt;", "off");
			mensaje = "off";

		} else {
			// No hubo error, ya existia .
			Log.e(" SharedPreferences read ok", "" + mensaje);

		}
		// Actualizamos el widget con el estado leido previamente
		actualizarWidget(context, appWidgetManager, mensaje);

	}

	public static void actualizarWidget(Context context,
			AppWidgetManager appWidgetManager, String value) {

		RemoteViews remoteViews;

		ComponentName thisWidget;

		int lay_id = 0;

		// Asignamos el layout a la variable lay_id segun el parametro recibido
		// por value
		if (value.equals("on")) {
			// ON
			lay_id = R.layout.main_on;
		}

		if (value.equals("off")) {
			// off
			lay_id = R.layout.main_of;

		}
		// Vamos a acceder a la vista y cambiar el layout segun lay_id
		remoteViews = new RemoteViews(context.getPackageName(), lay_id);
		// identifica a nuestro widget
		thisWidget = new ComponentName(context, UCIntlmWidget.class);

		// Creamos un intent a nuestra propia clase
		Intent intent = new Intent(context, UCIntlmWidget.class);
		// seleccionamos la accion ACTION_cambiarlayout
		intent.setAction(ACTION_cambiarlayout);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, 0);

		/*
		 * Equivalente a setOnClickListener de un boton comun lo asocio con el
		 * layout1 ya que al tocar este se ejecutara la accion y con
		 * pendingIntent
		 */

		remoteViews.setOnClickPendingIntent(R.id.layout1, pendingIntent);

		// actualizamos el widget
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Controlamos que la accion recibida sea la nuestra
		if (intent.getAction().equals(ACTION_cambiarlayout)) {
			// Leemos nuevamente SharedPreferences
			SharedPreferences prefs = context.getSharedPreferences(shprefreg,
					Context.MODE_PRIVATE);
			String mensaje = prefs.getString(shprefreg, "error");
			SharedPreferences.Editor editor = prefs.edit();

			String new_status = "";

			Log.e("! :)  status onReceive! ", mensaje);

			/*
			 * Si el estado que leimos es on definimos que el nuevo sea off y lo
			 * grabamos en SharedPreferences realizamos lo mismo con off pero
			 * usando on El valor grabado lo utilizaremos para determinar el
			 * layout a cargar
			 */

			if (loggedOnce(context)) {
				Intent serviceIntent = new Intent(context, ProxyService.class);// Proxy
				serviceIntent.putExtra("user", user);
				serviceIntent.putExtra("pass", pass);
				serviceIntent.putExtra("domain", "uci.cu");
				serviceIntent.putExtra("server", "10.0.0.1");
				serviceIntent.putExtra("inputport", "8080");
				serviceIntent.putExtra("outputport", "8080");

				if (mensaje.equals("on")) {

					editor.putString(shprefreg, "off");
					editor.commit();
					new_status = "off";

					context.stopService(serviceIntent);// Deteniendo el servicio
														// del proxy
					Toast.makeText(context, "UCIntlm se detuvo.",
							Toast.LENGTH_SHORT).show();

				} else if (mensaje.equals("off")) {
					context.startService(serviceIntent);// Se inicia el proxy

					editor.putString(shprefreg, "on");
					editor.commit();
					new_status = "on";

					Toast.makeText(context, "UCIntlm se inició.",
							Toast.LENGTH_SHORT).show();
					Toast.makeText(context, "UCIntlm conectado como: " + user,
							Toast.LENGTH_LONG).show();

				}
				// Actualizamos el estado del widget.
				AppWidgetManager widgetManager = AppWidgetManager
						.getInstance(context);
				actualizarWidget(context, widgetManager, new_status);
			} else
				Toast.makeText(context, "No existen datos para autenticarse.",
						Toast.LENGTH_SHORT).show();

		}

		super.onReceive(context, intent);
	}

	public boolean loggedOnce(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("UCIntlm.conf",
				Context.MODE_PRIVATE);
		user = prefs.getString("user", "nan");
		pass = prefs.getString("pass", "nan");
		// Toast.makeText(context, "user " + user + " pass:" + pass,
		// Toast.LENGTH_SHORT).show();
		if (!pass.equals("nan"))
			pass = Encripter.decrypt(pass);
		return !pass.equals("nan");
	}

}