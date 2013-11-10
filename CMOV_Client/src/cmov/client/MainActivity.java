package cmov.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import common.Common;

public class MainActivity extends FragmentActivity {
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
		setContentView(R.layout.main_activity);

		/** Getting a reference to the ViewPager defined the layout file */
		ViewPager pager = (ViewPager) findViewById(R.id.ViewPager);

		/** Getting fragment manager */
		FragmentManager fm = getSupportFragmentManager();

		/** Instantiating FragmentPagerAdapter */
		MyFragmentPagerAdapter pagerAdapter = new MyFragmentPagerAdapter(fm);

		/** Setting the pagerAdapter to the pager object */
		pager.setAdapter(pagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter{

		final int PAGE_COUNT = 4;

		/** Constructor of the class */
		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/** This method will be invoked when a page is requested to create */
		@Override
		public Fragment getItem(int arg0) {
			switch (arg0) {

			case 0:
				return new ListTickets();
			case 1:
				return new BuyTickets();
			case 2:
				return new UseTickets();
			default:
				return new ShowLastTicket();
			}

		}

		/** Returns the number of pages */
		@Override
		public int getCount() {
			return PAGE_COUNT;
		}
	}
}