package org.example.starx_p2p_client;

import android.util.Log;

import com.p2p.pppp_api.PPPP_APIs;

public class SendData implements Runnable{
	
	public static final int MSP_IDENT                =100;   //out message         multitype + multiwii version + protocol version + capability variable
	public static final int MSP_STATUS               =101;   //out message         cycletime & errors_count & sensor present & box activation & current setting number
	public static final int MSP_RAW_IMU              =102;   //out message         9 DOF
	public static final int MSP_SERVO                =103;   //out message         8 servos
	public static final int MSP_MOTOR                =104;   //out message         8 motors
	public static final int MSP_RC                   =105;   //out message         8 rc chan and more
	public static final int MSP_RAW_GPS              =106;   //out message         fix, numsat, lat, lon, alt, speed, ground course
	public static final int MSP_COMP_GPS             =107;   //out message         distance home, direction home
	public static final int MSP_ATTITUDE             =108;   //out message         2 angles 1 heading
	public static final int MSP_ALTITUDE             =109;   //out message         altitude, variometer
	public static final int MSP_ANALOG               =110;   //out message         vbat, powermetersum, rssi if available on RX
	public static final int MSP_RC_TUNING            =111;   //out message         rc rate, rc expo, rollpitch rate, yaw rate, dyn throttle PID
	public static final int MSP_PID                  =112;   //out message         P I D coeff (9 are used currently)
	public static final int MSP_BOX                  =113;   //out message         BOX setup (number is dependant of your setup)
	public static final int MSP_MISC                 =114;   //out message         powermeter trig
	public static final int MSP_MOTOR_PINS           =115;   //out message         which pins are in use for motors & servos, for GUI 
	public static final int MSP_BOXNAMES             =116;   //out message         the aux switch names
	public static final int MSP_PIDNAMES             =117;   //out message         the PID names
	public static final int MSP_WP                   =118;   //out message         get a WP, WP# is in the payload, returns (WP#, lat, lon, alt, flags) WP#0-home, WP#16-poshold
	public static final int MSP_BOXIDS               =119;   //out message         get the permanent IDs associated to BOXes
	public static final int MSP_SERVO_CONF           =120;   //out message         Servo settings=====

	public static final int MSP_NAV_STATUS           =121;   //out message         Returns navigation status
	public static final int MSP_NAV_CONFIG           =122;   //out message         Returns navigation parameters

	public static final int MSP_CELLS                =130;   //out message         FRSKY Battery Cell Voltages

	public static final int MSP_SET_RAW_RC           =200;   //in message          8 rc chan
	public static final int MSP_SET_RAW_GPS          =201;   //in message          fix, numsat, lat, lon, alt, speed
	public static final int MSP_SET_PID              =202;   //in message          P I D coeff (9 are used currently)
	public static final int MSP_SET_BOX              =203;   //in message          BOX setup (number is dependant of your setup)
	public static final int MSP_SET_RC_TUNING        =204;   //in message          rc rate, rc expo, rollpitch rate, yaw rate, dyn throttle PID
	public static final int MSP_ACC_CALIBRATION      =205;   //in message          no param
	public static final int MSP_MAG_CALIBRATION      =206;   //in message          no param
	public static final int MSP_SET_MISC             =207;  //in message          powermeter trig + 8 free for future use
	public static final int MSP_RESET_CONF           =208;   //in message          no param
	public static final int MSP_SET_WP               =209;   //in message          sets a given WP (WP#,lat, lon, alt, flags)
	public static final int MSP_SELECT_SETTING       =210;   //in message          Select Setting Number (0-2)
	public static final int MSP_SET_HEAD             =211;   //in message          define a new heading hold direction
	public static final int MSP_SET_SERVO_CONF       =212;   //in message          Servo settings
	public static final int MSP_SET_MOTOR            =214;   //in message          PropBalance function
	public static final int MSP_SET_NAV_CONFIG       =215;   //in message          Sets nav config parameters - write to the eeprom  
	public static final int MSP_SET_ACC_TRIM         =239;   //in message          set acc angle trim values
	public static final int MSP_ACC_TRIM             =240;   //out message         get acc angle trim values
	public static final int MSP_BIND                 =241;   //in message          no param

	public static final int MSP_EEPROM_WRITE         =250;   //in message          no param

	public static final int MSP_DEBUGMSG             =253;   //out message         debug string buffer
	public static final int MSP_DEBUG                =254;   //out message         debug1,debug2,debug3,debug4
	
	public static final byte go_arm[]={0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
									   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	public static final byte go_disarm[]={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		   							   0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	public static final byte motor[]={(byte)0xB0,0x04,(byte)0xE8,0x03,(byte)0xE8,0x03,(byte)0xE8,0x03,0,0,0,0,0,0,0,0};
	
	Thread thread;
	
	public void sendSerialDataFromP2P(byte deration,byte command,byte data[]) {
		byte checksum=0;
		byte[] all=new byte[5 + data.length+1]; //$M + '<'or'>' + datasize + command +data + crc
		all[0]='$';
		all[1]='M';
		all[2]=deration;
		all[3]=(byte)(data.length&0xff);
		checksum^=(all[3]&0xff);
		all[4]=command;
		checksum^=(all[4]&0xff);
		for(int i=0;i<data.length;i++){
			all[i+5]=data[i];
			checksum^=(all[i+5]&0xff);
		}
		all[all.length-1]=checksum;
		PPPP_APIs.PPPP_Write(StatePublic.connect,
						P2Pcontrol.CHANNEL_CONTROL, 
						all,
						all.length);
		all=null;
	}
	public void sendServoData(byte data[]){
		byte[] all=new byte[1+data.length];
		all[0]='S';
		for(int i=0;i<data.length;i++){
			all[i+1]=data[i];
		}
		PPPP_APIs.PPPP_Write(StatePublic.connect,
				P2Pcontrol.CHANNEL_SERVO, 
				all,
				all.length);
		//Log.d("data:", ControlData.control_turn_left_right+""+","+ControlData.control_rise_fall+"");
		all=null;
	}
	public SendData(){
		thread=new Thread(this);
		thread.start();
	}
	public void Esc(){
		thread.interrupt();
	}
	@Override
	public void run() {
		while (true) {			
				if(StatePublic.safeState==1){
					sendServoData(ControlData.tobyte());
					sendSerialDataFromP2P((byte)('<'&0xff), (byte)(MSP_SET_BOX & 0xff), go_arm);
					StatePublic.safeState--;
				}
				else if(StatePublic.safeState==-1){
					sendSerialDataFromP2P((byte)('<'&0xff), (byte)(MSP_SET_BOX & 0xff), go_disarm);
					StatePublic.safeState++;
				}
			if(StatePublic.manualcontrol){
				sendServoData(ControlData.tobyte());
			}
			/*
			if(StatePublic.okSend>0){
				StatePublic.okSend--;
				for(int i=0;i<StatePublic.ll.size();i--)
					sendSerialDataFromP2P((byte)('<'&0xff),(byte)(MSP_SET_RAW_GPS & 0xff), );
			}
			*/
			//sendSerialDataFromP2P((byte)('<'&0xff),(byte)(MSP_RAW_GPS & 0xff),null);
			try {
				if(StatePublic.manualcontrol) Thread.sleep(100);
				else Thread.sleep(1000);
			} catch (Exception e) {}	
		}
	}
}
