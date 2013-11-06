package cmov.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import common.Common;
import common.Network;

public class UseTickets extends ListTickets {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

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
		return view;
	}

	private class AsyncUseTickets extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		public AsyncUseTickets(String type)
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
				String tickets[] = fileContent.toString().split("\\r?\\n");
				for (String ticket : tickets)
				{
					if(ticket.substring(0, 2).equals(type))
					{
						elems.add(new BasicNameValuePair("tid",ticket.substring(4)));
						return;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		protected void onPreExecute() {
			SharedPreferences settings = getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);

			elems.add(new BasicNameValuePair("cid",settings.getString("UserID", null)));

			elems.add(new BasicNameValuePair("bid","1"));

			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			Network connection = new Network(Common.SERVER_URL + "validate", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			validateTicket(elems.get(0).getValue());
			/*//fragment is not active on screen.
			if(getActivity() == null || getView() == null)
				return;
			try {
				if (result == null)
					Toast.makeText(getActivity().getBaseContext(), "Error Fetching Data", Toast.LENGTH_LONG).show();
				else if (result.has("error"))
					Toast.makeText(getActivity().getBaseContext(), result.get("error").toString(), Toast.LENGTH_LONG).show();
				else
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
					((TextView) getView().findViewById(R.id.t1Number)).setText(Integer.valueOf(t1).toString());
					((TextView) getView().findViewById(R.id.t2Number)).setText(Integer.valueOf(t2).toString());
					((TextView) getView().findViewById(R.id.t3Number)).setText(Integer.valueOf(t3).toString());

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}*/
		}
		public void validateTicket(String tid)
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
				//if it gets here you don't have valid tickets
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

