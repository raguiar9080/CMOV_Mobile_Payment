package common;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
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
	private static final String TAG = "BluetoothConnectionAsyncTask";
	private static final int CONNECTION_CHECK_TIMEOUT = 10000;
	private static final int CONNECTION_CHECK_TRIES = 20;
	private static final int CONNECTION_TRIES = 15;

	/**
	 * Instance variables.
	 */
	private static String terminalAddress = null;
	//private static Activity callingActivity = null;
	private static BluetoothSocket socket = null;
	private static AtomicBoolean isConnected = null;


	public static boolean isBluetoothAvailable() {
		BluetoothAdapter tempBtAdapter = BluetoothAdapter.getDefaultAdapter();
		return tempBtAdapter != null && tempBtAdapter.isEnabled();
	}

	public static int btEnable(Activity activity,int returnVal)
	{
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
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.startDiscovery();
	}

	public static void stopdiscover()
	{
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.cancelDiscovery();
	}

	public static void createsocket(String macAddr)
	{
		socket = null;
		isConnected = new AtomicBoolean(false);
		terminalAddress = macAddr;
		// callingActivity = (Activity) activity;

		// Try to establish a connection.
		int tries = 0;
		while(tries < CONNECTION_TRIES && !isConnected.get()) {

			// Should immediately stop when the socket cannot 
			// be created.
			try {
				Log.e(TAG, "Starting all conections");
				BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(terminalAddress);

				//if only this service discovery failed
				//socket = device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID);
				
				Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
				socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
				Log.d("ZeeTest", "++++ Connecting");
				socket.connect();
		        Log.d("ZeeTest", "++++ Connected");
		        socket.getInputStream();
		        
		        
				if(socket != null) {
					startConnection();
				}
				++tries;
			} catch (Exception e) {
				Log.e(TAG, "Problem getting bluetooth device.", e);
				return;
			}
		}
		System.out.print(isConnected.get());
		return;
	}

	/**
	 * Launches a new thread to execute the blocking call
	 * to connect(), while keeping a timeout counter to
	 * prevent hangs.
	 */
	private static void startConnection() {

		// Try to connect with the remote device. Essential to
		// implement a timeout system, connect() hangs frequently;
		// new AsyncTask is needed for this purpose.
		isConnected.set(false);
		Thread connectThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Log.d(TAG, "Trying to connect to socket.");
					socket.connect();
					isConnected.set(true);
					Log.e(TAG, "SUCESS ON CONNECTION");
				} catch (Exception e) {
					Log.e(TAG, "Connection failed.", e);
				}
			}
		});
		Log.d(TAG, "Starting async connection try.");
		connectThread.start();

		// Wait for some time while the connection is not established. 
		// If too much times passes, the connection attempt if forcefully shut down.
		// Hangs at most CONNECTION_CHECK_TIMEOUT * CONNECTION_CHECK_TRIES.
		int retries = 0;
		while(retries < CONNECTION_CHECK_TRIES && !isConnected.get() && connectThread.isAlive()) {
			retries++;
			try {
				Log.d(TAG, "Connection try timeout.");
				Thread.sleep(CONNECTION_CHECK_TIMEOUT);
			} catch (InterruptedException e) {}
		}

		// Stops the connection attempt if it is hanging.
		if(!isConnected.get()) {
			Log.e(TAG, "Connection try timed out, cancelling task.");
			connectThread.interrupt();
			try {
				socket.getOutputStream().close();
				socket.getInputStream().close();
				socket.close();
			} catch(Exception e) {
			} finally {
				socket = null;
			}
		} else {
			Log.d(TAG, "Connection established.");
		}
	}

	public static Object read()
	{
		Object o=null;
		try {					
			ObjectInputStream in=new ObjectInputStream(socket.getInputStream());
			o=in.readObject();			
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return o;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
	}
}
