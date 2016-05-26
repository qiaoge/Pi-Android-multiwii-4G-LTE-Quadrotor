package org.example.starx_p2p_client;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.example.draw.DrawRocker2view;
import org.example.draw.DrawRockerView;

import com.p2p.pppp_api.PPPP_APIs;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.view.KeyEvent;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

public class VideoActivity extends Activity implements SurfaceHolder.Callback{
	P2Pcontrol p2Pcontrol;
	//P2Pcontrol_audio p2Pcontrol_audio;
	Thread thread=null;
	Thread thread2=null;
	Surface surface;
	DrawRockerView drawRockerView;
	DrawRocker2view drawRocker2View;
	
	CheckBox checkBox;
	Button ButtonJoystick;
	
	RadioGroup resolutionGroup;
	RadioButton radioButton0;
	RadioButton radioButton1;
	RadioButton radioButton2;
	RadioGroup fpsGroup;
	RadioButton fpsButton0;
	RadioButton fpsButton1;
	RadioButton fpsButton2;
	SeekBar brightnessBar;
	
	BluetoothAdapter adapter ;
	BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
	private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
	private InputStream is;
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
	boolean bRun = true;
	boolean bThread = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);    
		setContentView(R.layout.p2pvideo);
        SurfaceView sv =(SurfaceView)findViewById(R.id.surfaceView1);
        sv.getHolder().addCallback(this);
        
		p2Pcontrol=new P2Pcontrol(null);
		//p2Pcontrol_audio=new P2Pcontrol_audio(P2Pcontrol.m_handleSession);
		drawRockerView=(DrawRockerView)findViewById(R.id.rockview);
		drawRocker2View=(DrawRocker2view)findViewById(R.id.rockview2);	
        checkBox=(CheckBox)findViewById(R.id.manualCheckBox);
        
		checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/*
				if(StatePublic.manualcontrol==true){
					StatePublic.safeState=-1;
					StatePublic.manualcontrol=false;
				}
				else{
					StatePublic.safeState=1; 
					StatePublic.manualcontrol=true;
				}
				*/
				StatePublic.manualcontrol=checkBox.isChecked();
				if(StatePublic.manualcontrol==true)
					StatePublic.safeState=1; 
				else 
					StatePublic.safeState=-1; 
			}
		});
		
		ButtonJoystick=(Button)findViewById(R.id.ButtonJoystick);
		ButtonJoystick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(_socket==null){
		    		Intent serverIntent = new Intent(VideoActivity.this, DeviceListActivity.class); //跳转程序设置
		    		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
				}else{
					try {
						is.close();
						_socket.close();    	    	
					} catch (IOException e) {
						e.printStackTrace();
					}
					_socket = null;
					ButtonJoystick.setText("RC:OFF");
					drawRockerView.start();
					drawRocker2View.start();
				}	
			}
		});
		
		/*
		resolutionGroup=(RadioGroup)findViewById(R.id.radioGroup1);
		radioButton0=(RadioButton)findViewById(R.id.radio0);
		radioButton1=(RadioButton)findViewById(R.id.radio1);
		radioButton2=(RadioButton)findViewById(R.id.radio2);
		radioButton0.setText(StatePublic.resolution[0][0]+""+"x"+StatePublic.resolution[0][1]+"");
		radioButton1.setText(StatePublic.resolution[1][0]+""+"x"+StatePublic.resolution[1][1]+"");
		radioButton2.setText(StatePublic.resolution[2][0]+""+"x"+StatePublic.resolution[2][1]+"");
		
		fpsGroup=(RadioGroup)findViewById(R.id.radioGroup2);
		fpsButton0=(RadioButton)findViewById(R.id.fpsradio0);
		fpsButton1=(RadioButton)findViewById(R.id.fpsradio1);
		fpsButton2=(RadioButton)findViewById(R.id.fpsradio2);
		fpsButton0.setText(StatePublic.fps[0]+""+"fps");
		fpsButton1.setText(StatePublic.fps[1]+""+"fps");
		fpsButton2.setText(StatePublic.fps[2]+""+"fps");
		
		brightnessBar=(SeekBar)findViewById(R.id.brightnessBar);
		*/
        
		/*
		resolutionGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==radioButton0.getId()){
					StatePublic.resolution_c=0;
				}else if(checkedId==radioButton1.getId()){
					StatePublic.resolution_c=1;
				}else if(checkedId==radioButton2.getId()){
					StatePublic.resolution_c=2;
				}
				senddata();
			}
		});
		fpsGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==fpsButton0.getId()){
					StatePublic.fps_c=0;
				}else if(checkedId==fpsButton1.getId()){
					StatePublic.fps_c=1;
				}else if(checkedId==fpsButton2.getId()){
					StatePublic.fps_c=2;
				}
				senddata();
			}
		});
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				StatePublic.brightness=brightnessBar.getProgress();
				senddata();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		*/
		
	}
	 @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	switch(requestCode){
	    	case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
	    		// 响应返回结果
	            if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
	                // MAC地址，由DeviceListActivity设置返回
	                String address = data.getExtras()
	                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	                // 得到蓝牙设备句柄      
	                _device = _bluetooth.getRemoteDevice(address);
	 
	                // 用服务号得到socket
	                try{
	                	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
	                }catch(IOException e){
	                	Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
	                }
	                //连接socket
	                try{
	                	_socket.connect();
	                	Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
	                	ButtonJoystick.setText("RC:ON");
	                }catch(IOException e){
	                	try{
	                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
	                		_socket.close();
	                		_socket = null;
	                	}catch(IOException ee){
	                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
	                	}
	                	
	                	return;
	                }
	                StatePublic.joystick=true;
	                drawRockerView.stop();
	                drawRocker2View.stop();
	                
	                //打开接收线程
	                try{
	            		is = _socket.getInputStream();   //得到蓝牙数据输入流
	            		}catch(IOException e){
	            			Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
	            			return;
	            		}
	            		if(bThread==false){
	            			ReadThread.start();
	            			bThread=true;
	            		}else{
	            			bRun = true;
	            		}		
	            }
	    		break;
	    	default:break;
	    	}
	    } 
	 private String smsg = "";    //显示用数据缓存
	 Thread ReadThread=new Thread(){
	    	String ss="";
	    	public void run(){
	    		int num = 0;
	    		byte[] buffer = new byte[1024];
	    		byte[] buffer_new = new byte[1024];
	    		int i = 0;
	    		int n = 0;
	    		bRun = true;
	    		//接收线程
	    		while(true){
	    			try{
	    				while(is.available()==0){
	    					while(bRun == false){}
	    				}
	    				while(true){
	    					num = is.read(buffer);         //读入数据
	    					n=0;
	    					String s0 = new String(buffer,0,num);
	    					for(i=0;i<num;i++){
	    						if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
	    							buffer_new[n] = 0x0a;
	    							i++;
	    						}else{
	    							buffer_new[n] = buffer[i];
	    						}
	    						n++;
	    					}
	    					String s = new String(buffer_new,0,n);
	    					smsg+=s;   //写入接收缓存
	    					if(is.available()==0)break;  //短时间没有数据才跳出进行显示
	    				}
	    				//发送显示消息，进行显示刷新	
	    				if(smsg.length()>0)
	    					if(smsg.indexOf("S")!=-1&&smsg.indexOf("E")!=-1){
	    						//Log.d("dd",smsg+" "+smsg.indexOf("E")+"");
	    						String[] aa=smsg.split(" ");
	    						//Log.d("dd",aa[5].replaceAll("\\D", ""));
	    						if(aa[0]!="")
	    							ControlData.control_rise_fall=Short.parseShort(aa[0].replaceAll("\\D", ""));
	    						if(aa[1]!="")
		    						ControlData.control_turn_left_right=Short.parseShort(aa[1].replaceAll("\\D", ""));
	    						if(aa[2]!="")
		    						ControlData.control_forward_back=Short.parseShort(aa[2].replaceAll("\\D", ""));
	    						if(aa[3]!="")
		    						ControlData.control_move_left_right=Short.parseShort(aa[3].replaceAll("\\D", ""));
	    						if(aa[4]!="")
		    						ControlData.AUX1=Short.parseShort(aa[4].replaceAll("\\D", ""));
	    						if(aa[5]!="")
		    						ControlData.AUX2=Short.parseShort(aa[5].replaceAll("\\D", ""));
	    						
	    						smsg="";
	    					}
	    					
	    				
	    				
	    				
	    				
	    	    		}catch(IOException e){
	    	    		}
	    			
	    		}
	    	}
	    };
	@Override
	public void onPause(){
		StatePublic.manualcontrol=false;
		checkBox.setChecked(false);
		p2Pcontrol.stop();
		super.onPause();
	}
	@Override
	public void onResume(){

		super.onResume();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) {
        	p2Pcontrol.stop();
        	//p2Pcontrol_audio.stop();
        	thread.interrupt();
        	//thread2.interrupt();
        	VideoActivity.this.finish();
         }
        
         return true;
     }
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		surface=arg0.getSurface();
	
		if(thread==null){
			thread=new Thread(p2Pcontrol);
			p2Pcontrol.start(surface);
			thread.start();
		}
		/*
		if(thread2==null){
			thread2=new Thread(p2Pcontrol_audio);
			p2Pcontrol_audio.start();
			thread2.start();
		}
		*/
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}
	//=======================================
		
}
