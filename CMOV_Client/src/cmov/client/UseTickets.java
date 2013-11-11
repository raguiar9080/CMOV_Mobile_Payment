package cmov.client;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import common.BTFunctions;
import common.Common;

public class UseTickets extends ListTickets {

	public static List<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.use_tickets, container, false);

		final Button refreshlisttickets = (Button) view.findViewById(R.id.refreshlist_tickets);
		refreshlisttickets.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncListTickets().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		final Button refreshbuses = (Button) view.findViewById(R.id.refreshbuses);
		refreshbuses.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncListBuses().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		//Override to Buy When Selected
		final TextView t1Number = (TextView) view.findViewById(R.id.t1Number);
		t1Number.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncUseTickets("1").execute();
			}
		});
		final TextView t2Number = (TextView) view.findViewById(R.id.t2Number);
		t2Number.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncUseTickets("2").execute();
			}
		});
		final TextView t3Number = (TextView) view.findViewById(R.id.t3Number);
		t3Number.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncUseTickets("3").execute();
			}
		});
		PopulateBuses(view);

		return view;
	}

	@Override
	public void onResume() {
		SharedPreferences settings = this.getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
		if(settings.getString("T1", null)!=null && settings.getString("T2", null)!=null && settings.getString("T3", null)!=null &&settings.getString("TimeUpdated", null)!=null)
		{
			((TextView) getView().findViewById(R.id.t1Number)).setText(settings.getString("T1", null));
			((TextView) getView().findViewById(R.id.t2Number)).setText(settings.getString("T2", null));
			((TextView) getView().findViewById(R.id.t3Number)).setText(settings.getString("T3", null));
			((TextView) getView().findViewById(R.id.lastUpdated)).setText(settings.getString("TimeUpdated", null));
		}
		super.onResume();
	}

	private void PopulateBuses(View view)
	{
		final RadioGroup group = (RadioGroup) view.findViewById(R.id.buses_list);
		group.removeAllViews();
		for(BluetoothDevice device : btDeviceList)
		{
			RadioButton tmprb = new RadioButton(getActivity().getBaseContext());
			tmprb.setText(device.getName());
			tmprb.setId(group.getChildCount());
			group.addView(tmprb);
		}
	}

	private void AddBus(BluetoothDevice device)
	{
		final RadioGroup group = (RadioGroup) getView().findViewById(R.id.buses_list);

		RadioButton tmprb = new RadioButton(getActivity().getBaseContext());
		tmprb.setText(device.getName());
		tmprb.setId(group.getChildCount());
		group.addView(tmprb);
	}

	private class AsyncListBuses extends AsyncTask<Void, Void, Void> {
		private static final int MAXIMUM_TIMEOUT_TRIES = 100;
		private static final int CONNECTION_CHECK_TIMEOUT = 350;

		@Override
		protected void onPreExecute() {
			//clean 
			BTFunctions.stopdiscover();
			try{getActivity().unregisterReceiver(ActionFoundReceiver);}catch (Exception e){}

			btDeviceList.clear();
			PopulateBuses(getView());
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(Void... params) {
			
			if (!BTFunctions.isBluetoothAvailable())
			{
				Log.d("ERROR", "DEVICE DOES NOT SUPPORT BLUETOOTH. CANNOT CONTINUE.");
				return null;
			}
			
			//Register the BroadcastReceiver
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			getActivity().registerReceiver(ActionFoundReceiver, filter); // Don't forget to unregister during onDestroy


			BTFunctions.btEnable(getActivity(), 0);
			BTFunctions.startdiscover();
			int retries = 0;
			while(!BTFunctions.isFinishedDiscovering && retries < MAXIMUM_TIMEOUT_TRIES)
			{
				try {
					Thread.sleep(CONNECTION_CHECK_TIMEOUT);
				} catch (InterruptedException e) {}
				retries++;
			}

			if(!(retries < MAXIMUM_TIMEOUT_TRIES))
				Log.d("ERROR", "TIMEOUT REACHED");
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try{getActivity().unregisterReceiver(ActionFoundReceiver);} catch(Exception e){e.printStackTrace();}
		}

		private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action))
				{
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					btDeviceList.add(device);
					AddBus(device);
				}
				else
				{
					if(!BluetoothDevice.ACTION_UUID.equals(action))
					{
						if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
						{
							btDeviceList.clear();
						}
						else
						{
							if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
							{
								BTFunctions.stopdiscover();
							}
						}
					}
				}
			}
		};
	}


	private class AsyncUseTickets extends AsyncTask<Void, Void,  JSONObject> {
		private String cid;
		private String tid;
		private int selected = -1;

		public AsyncUseTickets(String type)
		{
			this.tid = type;
			SharedPreferences settings = getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
			this.cid = settings.getString("UserID", null);
		}
		
		@Override
		protected void onPreExecute() {
			final RadioGroup group = (RadioGroup) getView().findViewById(R.id.buses_list);
			//because theyre ID's are sequential and from zero
			selected = group.getCheckedRadioButtonId();

			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			
			//TESTING PURPOSES
			/*ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
			elems.add(new BasicNameValuePair("cid",cid));
			elems.add(new BasicNameValuePair("tid",tid));
			elems.add(new BasicNameValuePair("bid","1"));
			Network connection = new Network(Common.SERVER_URL + "validate", "POST", elems);
			connection.run();
			connection.getResultObject();*/
			
			if(selected == -1)
				return null;
			
			JSONObject tmpobject = new JSONObject();
			try {
				tmpobject.accumulate("cid", cid);
				tmpobject.accumulate("tid", tid);
			} catch (JSONException e){}

			BTFunctions.createsocket(btDeviceList.get(selected).getAddress());
			BTFunctions.write(tmpobject.toString());
			Object read = BTFunctions.read();
			BTFunctions.disconnect();
			JSONObject readjson = null;
			try {
				readjson = new JSONObject(read.toString());
			} catch (JSONException e) {e.printStackTrace();}
			return readjson;
		}

		protected void onPostExecute(JSONObject result) {
			try {
				//fragment is not active on screen.
				if(getActivity() == null || getView() == null)
				{
					//we still want to save data even if not active
					if(result!=null && !result.has("error"))
					{
						validateTicketLocally(result.get("id").toString());
						Toast.makeText(getActivity().getBaseContext(), "SUCESS\nKey:" + result.get("key").toString(), Toast.LENGTH_LONG).show();
					}
					return;
				}
				else
				{
					if (result == null)
						Toast.makeText(getActivity().getBaseContext(), "Error Fetching Data", Toast.LENGTH_LONG).show();
					else if (result.has("error"))
						Toast.makeText(getActivity().getBaseContext(), result.get("error").toString(), Toast.LENGTH_LONG).show();
					else
					{
						validateTicketLocally(result.get("id").toString());
						Toast.makeText(getActivity().getBaseContext(), "SUCESS\nKey:" + result.get("key").toString(), Toast.LENGTH_LONG).show();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void validateTicketLocally(String key)
		{
			SharedPreferences settings = getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			
			if (tid.equals("1"))
				editor.putString("T1", ((Integer) (Integer.valueOf(settings.getString("T1", null)) - 1)).toString());
			else if (tid.equals("2"))
				editor.putString("T2", ((Integer) (Integer.valueOf(settings.getString("T2", null)) - 1)).toString());
			else
				editor.putString("T3", ((Integer) (Integer.valueOf(settings.getString("T3", null)) - 1)).toString());
			
			editor.putString("LastTicket", key);
			editor.putString("TimeUpdated", "LOCAL");
			editor.commit();

			if(settings.getString("T1", null)!=null && settings.getString("T2", null)!=null && settings.getString("T3", null)!=null &&settings.getString("TimeUpdated", null)!=null)
			{
				((TextView) getView().findViewById(R.id.t1Number)).setText(settings.getString("T1", null));
				((TextView) getView().findViewById(R.id.t2Number)).setText(settings.getString("T2", null));
				((TextView) getView().findViewById(R.id.t3Number)).setText(settings.getString("T3", null));
				((TextView) getView().findViewById(R.id.lastUpdated)).setText(settings.getString("TimeUpdated", null));
			}
		}
	}
}