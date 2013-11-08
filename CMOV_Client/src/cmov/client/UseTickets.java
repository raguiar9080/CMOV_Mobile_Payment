package cmov.client;

import java.io.FileInputStream;
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
import common.Network;

public class UseTickets extends ListTickets {

	public static List<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.use_tickets, container, false);

		//Get UserdID
		SharedPreferences settings = this.getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
		if(settings.getString("T1", null)!=null && settings.getString("T2", null)!=null && settings.getString("T3", null)!=null &&settings.getString("TimeUpdated", null)!=null)
		{
			((TextView) view.findViewById(R.id.t1Number)).setText(settings.getString("T1", null));
			((TextView) view.findViewById(R.id.t2Number)).setText(settings.getString("T2", null));
			((TextView) view.findViewById(R.id.t3Number)).setText(settings.getString("T3", null));
			((TextView) view.findViewById(R.id.lastUpdated)).setText(settings.getString("TimeUpdated", null));
		}
		
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
				new AsyncUseTickets("T1").execute();
			}
		});
		final TextView t2Number = (TextView) view.findViewById(R.id.t2Number);
		t2Number.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncUseTickets("T2").execute();
			}
		});
		final TextView t3Number = (TextView) view.findViewById(R.id.t3Number);
		t3Number.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncUseTickets("T3").execute();
			}
		});
		PopulateBuses(view);
		
		return view;
	}

	private void PopulateBuses(View view)
	{
		//populate buses
		final RadioGroup group = (RadioGroup) view.findViewById(R.id.buses_list);
		group.removeAllViews();
		for(BluetoothDevice device : btDeviceList)
		{
			//TODO
			//if(device.getName() != null && device.getName().equals("Xperia neo"))
			//{
				RadioButton tmprb = new RadioButton(getActivity().getBaseContext());
				tmprb.setText(device.getName());
				tmprb.setId(group.getChildCount());
				group.addView(tmprb);
			//}
		}

	}

	private class AsyncListBuses extends AsyncTask<Void, Void, Void> {
		private static final int MAXIMUM_TIMEOUT_TRIES = 100;
		private static final int CONNECTION_CHECK_TIMEOUT = 350;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(Void... params) {
			try{getActivity().unregisterReceiver(ActionFoundReceiver);}catch (Exception e){}

			//Register the BroadcastReceiver
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			getActivity().registerReceiver(ActionFoundReceiver, filter); // Don't forget to unregister during onDestroy

			if (!BTFunctions.isBluetoothAvailable())
			{
				Toast.makeText(getActivity().getBaseContext(), "DEVICE DOES NOT SUPPORT BLUETOOTH. CANNOT CONTINUE.", Toast.LENGTH_LONG).show();
				return null;
			}
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
				Toast.makeText(getActivity().getBaseContext(), "TIMEOUT REACHED", Toast.LENGTH_LONG).show();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			getActivity().unregisterReceiver(ActionFoundReceiver);
			PopulateBuses(getView());
		}

		private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action))
				{
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					btDeviceList.add(device);
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
		private String type;
		
		@Override
		protected void onPreExecute() {
			SharedPreferences settings = getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
			

			cid = settings.getString("UserID", null);
			
			try {
				FileInputStream fis;
				fis = getActivity().openFileInput(Common.FILENAME);
				StringBuffer fileContent = new StringBuffer("");
				byte[] buffer = new byte[1024];
				while (fis.read(buffer) != -1) {
					fileContent.append(new String(buffer));
				}
				fis.close();
				String tickets[] = fileContent.toString().split("\\r?\\n");
				for (String ticket : tickets)
				{
					if(ticket.substring(0, 2).equals(type))
					{
						tid = ticket.substring(3);
						break;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			final RadioGroup group = (RadioGroup) getView().findViewById(R.id.buses_list);
			//because theyre ID's are sequential and from zero
			selected = group.getCheckedRadioButtonId();
			
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject tmpobject = new JSONObject();
			try {
				tmpobject.accumulate("cid", cid);
				tmpobject.accumulate("tid", tid);
			} catch (JSONException e){}
			
			BTFunctions.createsocket(btDeviceList.get(selected).getAddress());
			BTFunctions.write(tmpobject.toString());
			Object read = BTFunctions.read();
			BTFunctions.disconnect();
			Network connection = new Network(Common.SERVER_URL + "validate", "POST", null);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			validateTicketLocally(tid);
		}
		
		public AsyncUseTickets(String type)
		{
			this.type = type;
		}
		
		public void validateTicketLocally(String tid)
		{
			try {
				FileInputStream fis;
				fis = getActivity().openFileInput(Common.FILENAME);
				StringBuffer fileContent = new StringBuffer("");
				byte[] buffer = new byte[1024];
				while (fis.read(buffer) != -1) {
					fileContent.append(new String(buffer));
				}
				fis.close();
				String lines[] = fileContent.toString().split("\\r?\\n");
				for (int i = 0; i < lines.length; i++)
				{
					// NOTE that a line has Tx,id. id always start at same position
					if(lines[i].substring(3).equals(tid))
					{
						lines[i] = null;
						//TODO remove on preferences
						break;
					}
				}
				//if it gets here you don't have valid tickets but server said so :S

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

