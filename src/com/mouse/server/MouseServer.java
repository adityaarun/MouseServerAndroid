package com.mouse.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;


public class MouseServer extends Activity implements OnClickListener{
	public static final String TAG = "Connection";
	public static final int TIMEOUT = 30;
	public int CONNRES = 0;
	Intent i = null;
	TextView tv = null;
	private String connectionStatus = null;
	private String socketData = null;
	private Handler mHandler = null;
	ServerSocket server = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Set up click listeners for the buttons
		View connectButton = findViewById(R.id.connect_button);
		connectButton.setOnClickListener(this);
		
		final TextView textView = (TextView) findViewById(R.id.textView);

		//Set up touch listener for the textView
		
		final View touchView = findViewById(R.id.touchView);
		
		touchView.setOnTouchListener(new View.OnTouchListener() {
	        public boolean onTouch(View v, MotionEvent event) {
	            textView.setText("Touch coordinates : " +
	                String.valueOf(event.getX()) + " x " + String.valueOf(event.getY()) + "\nAction n Pressure " + String.valueOf(event.getAction())+","+String.valueOf(event.getPressure()));
	            String send = ((int)event.getX())+","+((int)event.getY())+","+event.getPressure()+","+event.getAction(); 
	            
	            Log.d(TAG, "disconnect" + Globals.socketOut);
				
	            if (Globals.socketOut != null) {
					Globals.socketOut.println(send);
					Globals.socketOut.flush();
				}
				return true;
	        }
	    });
		
		//touch listener ends
		mHandler = new Handler();
	}

	public void onClick(View v) {
			tv = (TextView) findViewById(R.id.textView);
			// initialize server socket in a new separate thread
			new Thread(initializeConnection).start();
			String msg = "Attempting to connect...";
			Toast.makeText(this, msg, msg.length()).show();
	}

	private Runnable initializeConnection = new Thread() {
		public void run() {

			Socket client = null;
			// initialize server socket
			try {
				server = new ServerSocket(10911);
				server.setSoTimeout(TIMEOUT * 1000);

				// attempt to accept a connection
				client = server.accept();
				Globals.socketIn = new Scanner(client.getInputStream());
				Globals.socketOut = new PrintWriter(client.getOutputStream(),
						true);
				CONNRES = 1;
				// Globals.socketIn.
			} catch (SocketTimeoutException e) {
				// print out TIMEOUT
				connectionStatus = "Connection has timed out! Please try again";
				mHandler.post(showConnectionStatus);
			} catch (IOException e) {
				Log.e(TAG, "" + e);
			} finally {
				// close the server socket
				try {
					if (server != null)
						server.close();
				} catch (IOException ec) {
					Log.e(TAG, "Cannot close server socket" + ec);
				}
			}

			if (client != null) {
				Globals.connected = true;
				// print out success
				connectionStatus = "Connection was succesful!";
				Log.d(TAG, "connected!");
				mHandler.post(showConnectionStatus);
				while (Globals.socketIn.hasNext()) {
					socketData = Globals.socketIn.next();
					mHandler.post(socketStatus);

				}
				// startActivity(i);
			}
		}
	};

	/**
	 * Pops up a "toast" to indicate the connection status
	 */
	private Runnable showConnectionStatus = new Runnable() {
		public void run() {
			Toast.makeText(getBaseContext(), connectionStatus,
					Toast.LENGTH_SHORT).show();
		}
	};

	private Runnable socketStatus = new Runnable() {

		public void run() {
			TextView tv = (TextView) findViewById(R.id.textView);
			tv.setText(socketData);
		}
	};

	public static class Globals {
		public static boolean connected;
		public static Scanner socketIn;
		public static PrintWriter socketOut;
	}
}
