package cmov.client;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.jwetherell.quick_response_code.data.Contents;
import com.jwetherell.quick_response_code.qrcode.QRCodeEncoder;
import common.Common;

public class ShowLastTicket extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.show_last_ticket, container, false);

		final Button refreshlastticket = (Button) view.findViewById(R.id.refreshlastticket);
		refreshlastticket.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Show();
			}
		});
		//onResume();
		return view;
	}

	public void Show() {
		SharedPreferences settings = this.getActivity().getSharedPreferences(Common.PREFS_NAME, Context.MODE_PRIVATE);
		if(settings.getString("LastTicket", null)!=null)
		{
			JSONObject tmp = new JSONObject();
			try {
				tmp.accumulate("tid", settings.getString("LastTicket", null));
				tmp.accumulate("cid", settings.getString("UserID", null));


				final ImageView qrcodeimg = (ImageView) getView().findViewById(R.id.qrcode);
				//Encode with a QR Code image
				QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(tmp.toString(), 
						null, 
						Contents.Type.TEXT,  
						BarcodeFormat.QR_CODE.toString(), 
						qrcodeimg.getWidth());
				Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
				qrcodeimg.setImageBitmap(bitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onResume();
	}
}

