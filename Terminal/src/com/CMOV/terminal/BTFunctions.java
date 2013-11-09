package com.CMOV.terminal;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

public class BTFunctions {
	
	public static boolean isBluetoothAvailable() {
		BluetoothAdapter tempBtAdapter = BluetoothAdapter.getDefaultAdapter();
		return tempBtAdapter != null && tempBtAdapter.isEnabled();
	}
	
	public static boolean isBluetoothDiscoverable()
	{
		BluetoothAdapter tempBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if(tempBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
		{
		    return false;
		}
		return true;
	}
	
	public static int btEnable(Activity activity,int returnVal)
	{
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    return -1;	//O dispositivo não suporta bt		
		}
		else
		{
			//Tornar o dispositivo visível aos outros aparelhos
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			activity.startActivityForResult(discoverableIntent, returnVal);
			return 0;
		}		
	}
	
	public static Object read(BluetoothSocket socket)
	{
		Object o=null;
		try {					
			ObjectInputStream in=new ObjectInputStream(socket.getInputStream());
			o=in.readObject();			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		return o;
	}
	
	public static boolean write(BluetoothSocket socket,Object o)
	{		
		try{
			ObjectOutputStream out=new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(o);
			out.flush();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}		
	}
}
