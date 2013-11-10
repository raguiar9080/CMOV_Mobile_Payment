package cmov.client;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import common.Common;
import common.Common.DateUtils;
import common.Network;

public class ListTickets extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.list_tickets, container, false);
		
		final Button refreshlisttickets = (Button) view.findViewById(R.id.refreshlist_tickets);
		refreshlisttickets.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncListTickets().execute();
			}
		});

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

	public class AsyncListTickets extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			SharedPreferences settings = getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
			elems.add(new BasicNameValuePair("cid",settings.getString("UserID", null)));
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			Network connection = new Network(Common.SERVER_URL + "listTickets", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			try {
				//fragment is not active on screen.
				if(getActivity() == null || getView() == null)
				{
					//we still want to save data even if not active
					if(result!=null && !result.has("error"))
					{
						save_data(result);
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
						ArrayList<String> values = save_data(result);
						((TextView) getView().findViewById(R.id.t1Number)).setText(values.get(0));
						((TextView) getView().findViewById(R.id.t2Number)).setText(values.get(1));
						((TextView) getView().findViewById(R.id.t3Number)).setText(values.get(2));
						((TextView) getView().findViewById(R.id.lastUpdated)).setText(values.get(3));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private ArrayList<String> save_data(JSONObject result) throws JSONException, IOException 
		{
			Integer t1=0,
					t2=0,
					t3=0;

			JSONArray tickets = result.getJSONArray("status");
			for (int i = 0; i < tickets.length(); i++)
			{
				if(((JSONObject)tickets.get(i)).get("type").equals("T1"))
					t1++;
				else if(((JSONObject)tickets.get(i)).get("type").equals("T2"))
					t2++;
				else 
					t3++;
			}
			
			String now = DateUtils.now();
			
			//Set UserID & Number of tickets for speed
			SharedPreferences settings = getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("T1", Integer.valueOf(t1).toString());
			editor.putString("T2", Integer.valueOf(t2).toString());
			editor.putString("T3", Integer.valueOf(t3).toString());
			editor.putString("TimeUpdated", now);
			// Commit the edits!
			editor.commit();




			//return to be presented on screen if active
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(t1.toString());
			tmp.add(t2.toString());
			tmp.add(t3.toString());
			tmp.add(now);
			return tmp;
		}
	}
}

