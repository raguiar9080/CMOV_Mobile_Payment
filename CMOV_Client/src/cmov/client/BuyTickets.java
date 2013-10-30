package cmov.client;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;

import common.Common;
import common.Network;

public class BuyTickets extends Activity {

	private String UserID;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buy_tickets);
		//Get UserdID
		SharedPreferences settings = getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
		UserID = settings.getString("UserID", null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		final Button buytickets = (Button) findViewById(R.id.buybtn);
		buytickets.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncBuyTickets().execute();
			}
		});
		
		final SeekBar t1tickets = (SeekBar) findViewById(R.id.seekBarT1);
		t1tickets.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				final TextView label = (TextView) findViewById(R.id.t1label);
				label.setText((new Integer(arg1)).toString());
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		final SeekBar t2tickets = (SeekBar) findViewById(R.id.seekBarT2);
		t2tickets.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				final TextView label = (TextView) findViewById(R.id.t2label);
				label.setText((new Integer(arg1)).toString());
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		final SeekBar t3tickets = (SeekBar) findViewById(R.id.seekBarT3);
		t3tickets.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				final TextView label = (TextView) findViewById(R.id.t3label);
				label.setText((new Integer(arg1)).toString());
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		return true;
	}

	private class AsyncBuyTickets extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("cid",UserID));
			elems.add(new BasicNameValuePair("t1",(String) ((TextView) findViewById(R.id.t1label)).getText()));
			elems.add(new BasicNameValuePair("t2",(String) ((TextView) findViewById(R.id.t2label)).getText()));
			elems.add(new BasicNameValuePair("t3",(String) ((TextView) findViewById(R.id.t3label)).getText()));
			
			
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			Network connection = new Network("http://10.13.37.34:81/buyTickets", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			Toast.makeText(getApplicationContext(),result.toString() , Toast.LENGTH_LONG).show();
		}
	}
}

