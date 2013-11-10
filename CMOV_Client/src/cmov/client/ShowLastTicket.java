package cmov.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import common.Common;

public class ShowLastTicket extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.show_last_ticket, container, false);
		
		final Button refreshlastticket = (Button) view.findViewById(R.id.refreshlastticket);
		refreshlastticket.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onResume();
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		SharedPreferences settings = this.getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
		if(settings.getString("LastTicket", null)!=null)
		{
			//TODO show qrcode
		}
		super.onResume();
	}
}

