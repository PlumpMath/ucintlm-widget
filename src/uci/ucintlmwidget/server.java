package uci.ucintlmwidget;

import java.io.IOException;


import android.os.AsyncTask;
import android.util.Log;

public class server extends AsyncTask {
	/*
	 * this starts the server as an AsyncTask
	 * no type arguments required
	 * */	
	private String user="",pass="",domain="uci.cu",server="10.0.0.1";
	private int inport = 8080,outport = 8080;
	private HttpForwarder p;
	
	
	
public server(String user, String pass, String domain, String server,
			int inport, int outport) {
		super();
		this.user = user;
		this.pass = pass;
		this.domain = domain;
		this.server = server;
		this.inport = inport;
		this.outport = outport;
	}

	
	public void stop(){
		try {
			p.terminate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected Object doInBackground(Object... arg0) {
		try {
			Log.w(getClass().getName(), "Server thread "+user+" "+domain+" "+server+" "+inport+" "+outport);
			p = new HttpForwarder("10.0.0.1", inport, domain,user, pass,outport, true);
			p.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
