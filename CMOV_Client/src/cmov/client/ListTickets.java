package cmov.client;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import common.Common;
import common.Network;

public class ListTickets extends Activity {

	private String UserID;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_tickets);
		//Get UserdID
		SharedPreferences settings = getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
		UserID = settings.getString("UserID", null);
		new AsyncListTickets().execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class AsyncListTickets extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("cid",UserID));
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			Network connection = new Network("http://10.13.37.34:81/listTickets", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			Integer t1=0,
					t2=0,
					t3=0;
			try {
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
			} catch (JSONException e) {
				e.printStackTrace();
			}
			((TextView) findViewById(R.id.t1Number)).setText(new Integer(t1).toString());
			((TextView) findViewById(R.id.t2Number)).setText(new Integer(t2).toString());
			((TextView) findViewById(R.id.t3Number)).setText(new Integer(t3).toString());
		}
	}
}

