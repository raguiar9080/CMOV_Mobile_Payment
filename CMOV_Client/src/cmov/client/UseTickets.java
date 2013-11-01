package cmov.client;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import common.Common;
import common.Network;

public class UseTickets extends ListTickets {

	private String UserID;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		//Override to Buy When Selected
		final TextView t1Number = (TextView) view.findViewById(R.id.t1Number);
		t1Number.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncUseTickets().execute();
			}
		});
		final TextView t2Number = (TextView) view.findViewById(R.id.t2Number);
		t2Number.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncUseTickets().execute();
			}
		});
		final TextView t3Number = (TextView) view.findViewById(R.id.t3Number);
		t3Number.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncUseTickets().execute();
			}
		});
		return view;
	}

	private class AsyncUseTickets extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("cid",UserID));
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			Network connection = new Network(Common.SERVER_URL + "listTickets", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			//fragment is not active on screen.
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
			}
		}
	}
}

