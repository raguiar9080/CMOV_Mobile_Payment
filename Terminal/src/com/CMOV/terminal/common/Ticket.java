package com.CMOV.terminal.common;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Ticket implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id = -1;
	private int userId = -1;
	private String busMacAddress = null;
	private Date validatedAt = null;
	private int validityTime = -1;
	
	public Ticket() {
	}
	
	public Ticket(JSONObject json) {
		try {
			this.id = json.getInt("id");
			this.userId = json.getInt("user_id");
			this.busMacAddress = json.getString("bus_mac_address");
			this.validityTime = json.getInt("validity_time");
			this.validatedAt = parseDate(json);
		} catch (Exception e ) {
			Log.d("Ticket","Failed to parse ticket.");
		}
	}
	
	private Date parseDate(JSONObject json) throws ParseException, JSONException {
		String target = json.getString("validated_at").replace('T', ' ');
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.ENGLISH);
	    Date result =  df.parse(target);
	    return result;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getBusMacAddress() {
		return busMacAddress;
	}

	public void setBusMacAddress(String busMacAddress) {
		this.busMacAddress = busMacAddress;
	}

	public Date getValidatedAt() {
		return validatedAt;
	}

	public void setValidatedAt(Date validatedAt) {
		this.validatedAt = validatedAt;
	}

	public int getValidityTime() {
		return validityTime;
	}

	public void setValidityTime(int validityTime) {
		this.validityTime = validityTime;
	}
}
