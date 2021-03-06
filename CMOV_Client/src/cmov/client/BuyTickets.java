package cmov.client;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import common.Common;
import common.Network;

public class BuyTickets extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.buy_tickets, container, false);

		final Button preparebuytickets = (Button) view.findViewById(R.id.preparebuybtn);
		preparebuytickets.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncPrepareBuyTickets().execute();
			}
		});

		final Button buytickets = (Button) view.findViewById(R.id.buybtn);
		buytickets.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncBuyTickets().execute();
			}
		});

		final SeekBar t1tickets = (SeekBar) view.findViewById(R.id.seekBarT1);
		t1tickets.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				final TextView label = (TextView) view.findViewById(R.id.t1label);
				label.setText((Integer.valueOf(arg1)).toString());
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		final SeekBar t2tickets = (SeekBar) view.findViewById(R.id.seekBarT2);
		t2tickets.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				final TextView label = (TextView) view.findViewById(R.id.t2label);
				label.setText((Integer.valueOf(arg1)).toString());
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		final SeekBar t3tickets = (SeekBar) view.findViewById(R.id.seekBarT3);
		t3tickets.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				final TextView label = (TextView) view.findViewById(R.id.t3label);
				label.setText((Integer.valueOf(arg1)).toString());
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		return view;
	}

	private class AsyncPrepareBuyTickets extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("t1",(String) ((TextView) getView().findViewById(R.id.t1label)).getText()));
			elems.add(new BasicNameValuePair("t2",(String) ((TextView) getView().findViewById(R.id.t2label)).getText()));
			elems.add(new BasicNameValuePair("t3",(String) ((TextView) getView().findViewById(R.id.t3label)).getText()));

			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			Network connection = new Network(Common.SERVER_URL + "prepareBuyTickets", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			//fragment is not active on screen.
			if(getActivity() == null || getView() == null)
				return;
			try {
				if (result == null)
					Toast.makeText(getActivity().getBaseContext(), "Error Sending Data", Toast.LENGTH_LONG).show();
				else if (result.has("error"))
					Toast.makeText(getActivity().getBaseContext(), result.get("error").toString(), Toast.LENGTH_LONG).show();	
				else
				{
					((TextView) getView().findViewById(R.id.pricelabel)).setText(result.getJSONObject("info").getString("price") + "�");
					String tmp ="T1:" + result.getJSONObject("info").getString("t1") + " T2:" + result.getJSONObject("info").getString("t2") + " T3:" + result.getJSONObject("info").getString("t3");
					((TextView) getView().findViewById(R.id.tickets)).setText(tmp);
				}
				//Toast.makeText(getActivity().getApplicationContext(),result.toString() , Toast.LENGTH_LONG).show();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private class AsyncBuyTickets extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			SharedPreferences settings = getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);

			elems.add(new BasicNameValuePair("cid",settings.getString("UserID", null)));
			elems.add(new BasicNameValuePair("t1",(String) ((TextView) getView().findViewById(R.id.t1label)).getText()));
			elems.add(new BasicNameValuePair("t2",(String) ((TextView) getView().findViewById(R.id.t2label)).getText()));
			elems.add(new BasicNameValuePair("t3",(String) ((TextView) getView().findViewById(R.id.t3label)).getText()));

			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			Network connection = new Network(Common.SERVER_URL + "buyTickets", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			//fragment is not active on screen.
			if(getActivity() == null || getView() == null)
				return;
			try {
				if (result == null)
					Toast.makeText(getActivity().getBaseContext(), "Error Sending Data", Toast.LENGTH_LONG).show();
				else if (result.has("error"))
					Toast.makeText(getActivity().getBaseContext(), result.get("error").toString(), Toast.LENGTH_LONG).show();	
				else
				{
					((TextView) getView().findViewById(R.id.pricelabel)).setText(result.getJSONObject("info").getString("price") + "�");
					String tmp ="T1:" + result.getJSONObject("info").getString("t1") + " T2:" + result.getJSONObject("info").getString("t2") + " T3:" + result.getJSONObject("info").getString("t3");
					((TextView) getView().findViewById(R.id.tickets)).setText(tmp);
					SharedPreferences settings = getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();

					editor.putString("T1", ((Integer) (Integer.valueOf(settings.getString("T1", null)) + Integer.valueOf(elems.get(1).getValue()))).toString());
					editor.putString("T2", ((Integer) (Integer.valueOf(settings.getString("T2", null)) + Integer.valueOf(elems.get(2).getValue()))).toString());
					editor.putString("T3", ((Integer) (Integer.valueOf(settings.getString("T3", null)) + Integer.valueOf(elems.get(3).getValue()))).toString());
					editor.putString("TimeUpdated", "LOCAL");
					editor.commit();
					
					Toast.makeText(getActivity().getApplicationContext(), "Tickets Successfully Bought" , Toast.LENGTH_LONG).show();
				}
				//Toast.makeText(getActivity().getApplicationContext(),result.toString() , Toast.LENGTH_LONG).show();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}

