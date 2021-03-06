package com.CMOV.terminal;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import common.Common;
import common.Network;

public class MainActivity extends Activity {

	private final static int REQUEST_ENABLE_BT = 1;
	private boolean serverOn = false;
		
	//Id do autocarro. TODO automatico.
	private static int bid=1;
	
	private class AsyncValidateTickets extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		private JSONObject json = null;
		private BluetoothSocket socket=null;
		
		@Override
		protected void onPreExecute() {}
		
		public AsyncValidateTickets(JSONObject obj,BluetoothSocket s) {
			this.json = obj;
			this.socket=s;
		}
		
		@Override
		protected JSONObject doInBackground(Void... params) {
			try{
			elems.add(new BasicNameValuePair("bid",Integer.toString(bid)));
			elems.add(new BasicNameValuePair("cid",json.getString("cid")));
			elems.add(new BasicNameValuePair("tid",json.getString("tid")));
			}catch(Exception e){}
			
			Network connection = new Network(Common.SERVER_URL + "validate", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			try {
				Log.d("WRITE",result.toString());
				BTFunctions.write(socket,result.toString());
			} catch (Exception e) {}
		}
	}
	
	BroadcastReceiver broadcastReceiver=new BroadcastReceiver(){
		//tratar os pedidos recebidos
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			try {
				String adr = (String) arg1.getExtras().get("address");
				BluetoothSocket socket = BTServer.getSocket(adr);
				JSONObject req = new JSONObject();
				try {
					String received = (String) BTFunctions.read(socket);
					Log.d("UI",received);
					JSONObject j = new JSONObject(received);					
					req.put("bid",String.valueOf(bid));
					req.put("cid",j.getString("cid"));
					req.put("tid",j.getString("tid"));
					Log.d("UI","Chegou aqui");
					
				} catch(Exception e) {
					Log.e("OnReceive", "Erro a extrair dados do pedido", e);
				}
				
				Toast.makeText(getApplicationContext(), "Dados recebidos do utilizador: " + adr, Toast.LENGTH_SHORT).show();
				
				new AsyncValidateTickets(req,socket).execute();				
				
			} catch (Exception e) {}			
		}		
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{		
		//request code utilizado aquando da chamada da atividade
		if(requestCode == 1) 
		{
			if(resultCode == RESULT_CANCELED) 
			{
				Context appContext=getApplicationContext();
				Toast.makeText(appContext, "Erro a iniciar Bluetooth", Toast.LENGTH_SHORT).show();
			}
			else startServer();
		}				
	}
	
	View.OnClickListener btnListener = new View.OnClickListener() {		
		@Override
		public void onClick(View v) 
		{
			if(!serverOn)startServer();
			else close();
		}
	};
	
	public void enableWifi()
	{
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(wifi != null && !wifi.isWifiEnabled()) {
			wifi.setWifiEnabled(true);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		enableWifi();
		
		TextView text = (TextView) findViewById(R.id.numberText);
		text.setText(String.valueOf(bid));
		
		checkServiceState();
		
		findViewById(R.id.serviceBtn).setOnClickListener(btnListener);
	}
	
	@Override
	protected void onResume() {
		
		checkServiceState();
		super.onRestart();
	}
	
	@Override
    protected void onDestroy() 
	{
		close();
		super.onDestroy();
    }
	
	@Override
	protected void onPause() 
	{		
		close();
		super.onPause();
	}
	
	public void checkServiceState()
	{
		if(serverOn)
		{
			registerReceiver(broadcastReceiver, new IntentFilter(BTServer.ACTION_BLUETOOTH));
			Intent intent=new Intent(this,BTServer.class);
			startService(intent);			
			Toast.makeText(this, "Starting the service...", Toast.LENGTH_LONG).show();
		}
	}
	
	public void close()
	{
		if(serverOn) {
			unregisterReceiver(broadcastReceiver);
			BTServer.running.set(false);
			Intent intent=new Intent(this, BTServer.class);   	
			stopService(intent);	    	
	    	serverOn=false;
	    	Log.d("Main","Closing");
	    	Toast.makeText(this, "Shutting down the service...", Toast.LENGTH_LONG).show();
		}	
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean setupBt()
	{
		int res=BTFunctions.btEnable(this,1);
		if(res==-1)
		{
			Context context = getApplicationContext();
			CharSequence error = "Bluetooth not supported. Closing application...";
			int duration=Toast.LENGTH_LONG;			
			Toast.makeText(context, error, duration).show();
			return false;
		}
		else if (res==0)
		{
			Log.d("Main","Starting Server");
			startServer();
		}
		return true;
	}
	
	private void startServer()
	{
		if(!BTFunctions.isBluetoothAvailable()||!BTFunctions.isBluetoothDiscoverable()) 
		{
			BTFunctions.btEnable(this,REQUEST_ENABLE_BT);
		} 
		else 
		{
			try
			{
				Intent intent = new Intent(this, BTServer.class);
				startService(intent);
			}
			catch(Exception e)
			{
				Log.d("Start Server", "Error");
			}
			serverOn = true;
			registerReceiver(broadcastReceiver, new IntentFilter(BTServer.ACTION_BLUETOOTH));
		}
	}
	
	
}
