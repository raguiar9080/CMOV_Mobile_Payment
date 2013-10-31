package cmov.client;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import common.Common;
import common.Network;

public class LoginActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);		

		final Button createLoginButton = (Button) findViewById(R.id.createLoginUser);
		createLoginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AsyncLoginClient().execute();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class AsyncLoginClient extends AsyncTask<String, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("name",((EditText)findViewById(R.id.userName)).getText().toString()));
			elems.add(new BasicNameValuePair("pass",((EditText)findViewById(R.id.userPassword)).getText().toString()));		
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(String... params) {
			Network connection = new Network("http://192.168.1.2:81/client/login", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			try {
				//account doesnt exist, create it
				if (result == null)
					Toast.makeText(getBaseContext(), "Error Sending Data", Toast.LENGTH_LONG).show();
				else if (result.has("error"))
					new AsyncCreateClient().execute();
				else
				{
					//Set UserID
					SharedPreferences settings = getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("UserID", result.get("id").toString());
					// Commit the edits!
					editor.commit();
					finish();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}


	private class AsyncCreateClient extends AsyncTask<String, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			elems.add(new BasicNameValuePair("name",((EditText)findViewById(R.id.userName)).getText().toString()));		
			elems.add(new BasicNameValuePair("nib",((EditText)findViewById(R.id.userNib)).getText().toString()));		
			elems.add(new BasicNameValuePair("cardType",((EditText)findViewById(R.id.userCardType)).getText().toString()));		
			elems.add(new BasicNameValuePair("validity",((EditText)findViewById(R.id.userValidity)).getText().toString()));		
			elems.add(new BasicNameValuePair("pass",((EditText)findViewById(R.id.userPassword)).getText().toString()));				
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(String... params) {
			Network connection = new Network("http://192.168.1.2:81/client/create", "POST", elems);
			connection.run();
			return connection.getResultObject();
		}
		protected void onPostExecute(JSONObject result) {
			try {
				if (result == null)
					Toast.makeText(getBaseContext(), "Error Sending Data", Toast.LENGTH_LONG).show();
				else if (result.has("error"))
					Toast.makeText(getBaseContext(), result.get("error").toString(), Toast.LENGTH_LONG).show();
				else
				{

					//Set UserID
					SharedPreferences settings = getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("UserID", result.get("id").toString());
					// Commit the edits!
					editor.commit();
					finish();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}

}