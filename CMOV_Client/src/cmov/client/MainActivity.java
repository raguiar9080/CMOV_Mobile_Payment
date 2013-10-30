package cmov.client;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import common.Network;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//new CreateClient().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private class CreateClient extends AsyncTask<String, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("name","Ruben"));
			elems.add(new BasicNameValuePair("pass","passwordencrypted"));		
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(String... params) {
			Network connection = new Network("http://10.13.37.34:81/client/create", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
	     protected void onPostExecute(JSONObject result) {
	         Toast.makeText(getApplicationContext(),result.toString() , Toast.LENGTH_LONG).show();
	     }
	 }
	
	private class LoginClient extends AsyncTask<String, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("name","Ruben"));		
			elems.add(new BasicNameValuePair("nib","nib123"));		
			elems.add(new BasicNameValuePair("cardType","cardVisa"));		
			elems.add(new BasicNameValuePair("validity","Valid2013"));		
			elems.add(new BasicNameValuePair("pass","password"));				
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(String... params) {
			Network connection = new Network("http://10.13.37.34:81/client/login", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
	     protected void onPostExecute(JSONObject result) {
	         Toast.makeText(getApplicationContext(),result.toString() , Toast.LENGTH_LONG).show();
	     }
	 }

}
