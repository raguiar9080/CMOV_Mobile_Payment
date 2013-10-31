package cmov.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import common.Common;

public class SplashScreen extends Activity {
	private String UserID;

	@Override
	protected void onResume() {
		//Get UserID if existent
		SharedPreferences settings = getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
		UserID = settings.getString("UserID", null);
		//need to create account
		if(UserID == null)
		{
			Intent intent = new Intent(getBaseContext(), LoginActivity.class);
			startActivity(intent);
		}
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		final Button listtickets = (Button) findViewById(R.id.listTickets);
		listtickets.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), ListTickets.class);
				startActivity(intent);
			}
		});

		final Button buytickets = (Button) findViewById(R.id.buyTickets);
		buytickets.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), BuyTickets.class);
				startActivity(intent);
			}
		});

		final Button usetickets = (Button) findViewById(R.id.useTicket);
		usetickets.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), UseTickets.class);
				startActivity(intent);
			}
		});

		final Button seelastticket = (Button) findViewById(R.id.lastValidatedTicket);
		seelastticket.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}