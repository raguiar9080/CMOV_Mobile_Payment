package common;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

public class BTFunctions {
	/**
	 * Constants.
	 */

	private static final UUID SERVICE_UUID = UUID.fromString("f74f7958-eae5-4202-bbfd-8700988f61f5");

	/**
	 * Instance variables.
	 */
	private static String terminalAddress = null;
	private static BluetoothSocket socket = null;
	public static AtomicBoolean isConnected = new AtomicBoolean(false);
	public static boolean isFinishedDiscovering = false;


	public static boolean isBluetoothAvailable() {
		BluetoothAdapter tempBtAdapter = BluetoothAdapter.getDefaultAdapter();
		return tempBtAdapter != null && tempBtAdapter.isEnabled();
	}

	public static int btEnable(Activity activity,int returnVal)
	{
		socket = null;
		terminalAddress = null;
		isConnected.set(false);

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			return -1;			
		}
		else
		{
			//Enabling discoverability
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			activity.startActivityForResult(discoverableIntent, returnVal);
			return 0;
		}		
	}

	public static void startdiscover()
	{
		//this means we start a new Discovery
		isFinishedDiscovering = false;
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.startDiscovery();
	}

	public static void stopdiscover()
	{
		isFinishedDiscovering = true;
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.cancelDiscovery();
	}

	public static void createsocket(String macAddr)
	{
		socket = null;
		isConnected = new AtomicBoolean(false);
		terminalAddress = macAddr;

		// Try to establish a connection.
		BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(terminalAddress);

		//if only this service discovery failed
		try {
			socket = device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID);
			//Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			//socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
			socket.connect();
			isConnected.set(true);
		}
		catch (IOException e)
		{
			Log.e("ERROR", e.toString());
			e.printStackTrace();
		}
		return;
	}

	public static boolean write(Object o)
	{
		try{
			ObjectOutputStream out=new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(o);
			out.flush();
			return true;
		}
		catch(Exception e)
		{
			Log.e("ERROR", e.toString());
			e.printStackTrace();
			return false;
		}		
	}

	public static void disconnect()
	{
		try {
			socket.getOutputStream().close();
			socket.getInputStream().close();
			socket.close();
		}
		catch (IOException e)
		{
			Log.e("ERROR", e.toString());
			e.printStackTrace();
		}
		finally
		{
			socket = null;
		}
	}
}
