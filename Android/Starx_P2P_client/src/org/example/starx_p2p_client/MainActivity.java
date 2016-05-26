package org.example.starx_p2p_client;


import com.p2p.pppp_api.PPPP_APIs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final int MSG_LOG = 0;

	public static final String TAG = "P2PClient";

	private TextView stateText;
	private EditText didText;
	private Button connectButton;
	private Button disconnectButton;
	private Button videoButton;
	private Button manualButton;
	public P2Pcontrol p2Pcontrol;

	private Handler handler;
	//==============test===================
	private boolean test=true;
	
	Intent intent;

//	public PassP2pcontrol passP2pcontrol;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		intent=new Intent();

		int n = PPPP_APIs.ms_verAPI;
		initHandler();

		p2Pcontrol=new P2Pcontrol(handler);
		
//		passP2pcontrol=new PassP2pcontrol();
//		passP2pcontrol.p2Pcontrol=this.p2Pcontrol; //
		
		stateText=(TextView) findViewById(R.id.textView1);
		stateText.setText("");
		didText=(EditText) findViewById(R.id.editText1);
		didText.setText("STAR-000054-UZCSB");
		connectButton=(Button) findViewById(R.id.button1);
		
		disconnectButton=(Button) findViewById(R.id.button4);
		
		videoButton=(Button) findViewById(R.id.button2);
		manualButton=(Button) findViewById(R.id.button3);
		
		connectButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				if(StatePublic.connect<0){
					stateText.setText(" ");
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							StatePublic.connect=p2Pcontrol.connectDev(didText.getText().toString());
							if(StatePublic.connect>0) StatePublic.sendData=new SendData();
						}
					});
					t.start();
				}
			}
		});
		disconnectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(StatePublic.connect>0){
					StatePublic.connect=p2Pcontrol.disconnectDev();
				}
			}
		});
		videoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(StatePublic.connect>0||test){
					intent.setClass(MainActivity.this, VideoActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
				}
			}
		});
		manualButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(StatePublic.connect>0||test){
					intent.setClass(MainActivity.this, MapActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
				}	
			}
		});
	}
	
	private void log(String msg) { 
		Log.i(MainActivity.TAG, msg);
		stateText.setText(msg);
	}
	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_LOG:
					String text = (String) msg.obj;
					log(text);
					break;
				default:
					;
				}
			}
		};
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
