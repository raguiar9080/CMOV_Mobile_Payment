package com.CMOV.inspector;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import common.Common;
import common.Network;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ArrayList<Ticket> bilhetes = new ArrayList<Ticket>();
	public final static int CAPTURE_QR = 1;
	public final static String CONTENT_QR = "QRResult";

	View.OnClickListener downloadListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int busID;
			//Mostrar uma dialog a pedir o ID do autocarro
			final EditText input = new EditText(MainActivity.this);

			new AlertDialog.Builder(MainActivity.this)
			.setTitle("Fetch Tickets")
			.setMessage("Insert the bus ID")
			.setView(input)
			.setPositiveButton("Send", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{ 
					String text=input.getText().toString();
					AsyncFetchTickets fetcher = new AsyncFetchTickets(text);
					fetcher.execute();	
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) {}
			}).show();

			//Fazer o pedido ao servidor com o id do autocarro
			//Se tudo correr bem, toast			
		}

	};

	View.OnClickListener validateListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
			startActivityForResult(intent, CAPTURE_QR);

		}

	};

	View.OnClickListener retrievedListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//Arrancar uma atividade para mostrar uma lista
		}

	};

	public void processQRData(Intent data)
	{
		try {
			JSONObject json = new JSONObject(data.getExtras().getCharSequence(CONTENT_QR).toString());
			String tid=json.getString("tid");
			String cid=json.getString("cid");
			if (bilhetes.size()==0)
			{
				Toast.makeText(this, "There are no validated tickets for this bus!", Toast.LENGTH_LONG).show();
				return;
			}
			else
			{
				for(int i=0;i<bilhetes.size();i++)
				{
					if(tid.equals(Integer.toString(bilhetes.get(i).getTicketID()))&&cid.equals(Integer.toString(bilhetes.get(i).getClientID())))
					{
						if(checkTime(bilhetes.get(i).getValidTime()))
						showResult(true);
						break;
					}
				}
				showResult(false);
			}
		} catch (Exception e) {}		
	}
	
	public void showResult(boolean result)
	{
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case CAPTURE_QR:
				processQRData(data);
				break;
			default:
				break;
			}
		} else 
		{
			Log.d("QR ERROR", "There was an error capturing data from the QR");
		}
		
	}

	public void enableWifi()
	{
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(wifi != null && !wifi.isWifiEnabled()) {
			wifi.setWifiEnabled(true);
		}
	}
	
	public boolean checkTime(String data)
	{
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)	
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Definir os listeners dos botões
		Button downBtn = (Button) findViewById(R.id.downloadBtn);
		downBtn.setOnClickListener(downloadListener);

		Button valBtn = (Button) findViewById(R.id.validateBtn);
		valBtn.setOnClickListener(validateListener);

		Button retrievedBtn = (Button) findViewById(R.id.retrievedBtn);
		retrievedBtn.setOnClickListener(retrievedListener);	

		enableWifi();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	/*AsyncTasks*/	
	
	public class AsyncFetchTickets extends AsyncTask<Void, Void,  JSONObject> 
	{
		private String bid=null;
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();

		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("bid",bid));
			super.onPreExecute();
		}

		AsyncFetchTickets(String value)
		{
			bid=value;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			Network connection = new Network(Common.SERVER_URL + "getValidatedTickets", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) 
		{
			try
			{
				if (result == null)
					Toast.makeText(MainActivity.this, "Error Fetching Data", Toast.LENGTH_LONG).show();
				else if (result.has("error"))
					Toast.makeText(MainActivity.this, result.get("error").toString(), Toast.LENGTH_LONG).show();
				else
				{
					JSONArray tickets = result.getJSONArray("status");
					for (int i = 0; i < tickets.length(); i++)
					{
						JSONObject obj=(JSONObject) tickets.get(i);
						Ticket temp=new Ticket(obj.getInt("tid"),obj.getInt("cid"),obj.get("dateValidated").toString());
						bilhetes.add(temp);
					}
					Toast.makeText(MainActivity.this, "Tickets fetched", Toast.LENGTH_LONG).show();
				}
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

	}
}
