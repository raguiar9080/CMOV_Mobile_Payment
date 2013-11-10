package com.CMOV.terminal;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

public class BTServer extends IntentService{

	private static final String name = "BTServer";
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothServerSocket mmServerSocket = null;

	public static final String NAME="BTServer";
	public static final UUID MY_UUID=UUID.fromString("f74f7958-eae5-4202-bbfd-8700988f61f5");
	public static AtomicBoolean running=new AtomicBoolean(false);
	public static final String ACTION_BLUETOOTH = "org.CMOV.terminal.BTServer";

	private static HashMap<String,BluetoothSocket> sockets=new HashMap<String,BluetoothSocket>();

	public BTServer() {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if(running.get()) return;
		else running.set(true);

		while (running.get()==true)
		{
			try {
				if (BTFunctions.isBluetoothAvailable())
				{
					BTServer.resetSockets();				
					mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();			
					mmServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);

					while(running.get()==true)
					{
						try {
							BluetoothSocket client = mmServerSocket.accept(15000);

							if (client != null) 
							{
								BTServer.addSocket(client);
								Intent notify = new Intent(ACTION_BLUETOOTH);
								notify.putExtra("address", client.getRemoteDevice().getAddress());
								Log.d("BTServer","Client Connected");
								sendBroadcast(notify);
							}
						} catch (IOException e) {						
							Log.d("BTServer","There was an error accepting the client request or no one tried to connect");
						}
					}
				}
				else
				{
					mBluetoothAdapter = null;
					mmServerSocket = null;					
					Thread.sleep(5000);	
				}
				
			} 
			catch (Exception e) 
			{
				try 
				{
					mBluetoothAdapter = null;
					mmServerSocket = null;					
					Thread.sleep(5000);					
				}
				catch (InterruptedException e1) {}
			}
		}
	}

	private static void resetSockets() {
		synchronized (BTServer.sockets) {
			for (BluetoothSocket socket : BTServer.sockets.values()) {
				try {
					socket.getOutputStream().close();
					socket.getInputStream().close();
					socket.close();
				} catch (IOException e) {}
			}
			BTServer.sockets.clear();
		}
	}

	public void onDestroy() {
		running.set(false);
		BTServer.resetSockets();
		super.onDestroy();
	}

	public static void addSocket(BluetoothSocket socket)
	{
		synchronized (BTServer.sockets) {
			//procura a existência de uma ligação existente com o mesmo cliente
			BluetoothSocket old = BTServer.sockets.get(socket.getRemoteDevice().getAddress());
			if(old!= null) {
				try {
					old.getOutputStream().close();
					old.getInputStream().close();
					old.close();
				} catch (IOException e) {}
			}
			sockets.put(socket.getRemoteDevice().getAddress(), socket);
		}		
	}

	public static BluetoothSocket getSocket(String address) {
		BluetoothSocket socket = null;
		synchronized (BTServer.sockets) {
			socket = BTServer.sockets.remove(address);
		}
		return socket;
	}
}
