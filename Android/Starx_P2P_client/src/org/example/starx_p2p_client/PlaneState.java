package org.example.starx_p2p_client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PlaneState extends Service implements Runnable{
	private static final String TAG = "PlaneStateService";
	private Thread mThread;
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return null;
	}
	
	public void onStart(Intent intent, int startId)  
    {  
		mThread = new Thread(this);  
        mThread.start();  
    }
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy() {

		Log.i(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void run() {
		while(true){
			
		}
	}
}
