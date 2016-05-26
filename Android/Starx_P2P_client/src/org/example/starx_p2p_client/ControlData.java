package org.example.starx_p2p_client;

public class ControlData {
	public static short control_max_data=2000;
	public static short control_min_data=1000;
	public static short control_mid_data=1500;
	public static short control_rise_fall=970;  //fall<1500 rise>1500  THROTTLE
	public static short control_turn_left_right=1500; //left<1500 right>1500  YAW
	public static short control_forward_back=1500;   //back<1500 forward>1500  PITCH
	public static short control_move_left_right=1500; //left<1500 right>1500   ROLL
	public static short AUX1=970;
	public static short AUX2=1500;
	public static short AUX3=1500;
	public static short AUX4=1500;
	
	public static byte[] tobyte(){   //THROTTLE/ROLL/PITCH/YAW/AUX1/AUX2/AUX3/AUX4
		byte[] bytes=new byte[16];
		bytes[0] =(byte) (control_rise_fall&0xff-50);
		bytes[1] =(byte)((control_rise_fall&0xff00)>>8);
		bytes[2] =(byte) (control_move_left_right&0xff-27);
		bytes[3] =(byte)((control_move_left_right&0xff00)>>8);
		bytes[4] =(byte) (control_forward_back&0xff-27);
		bytes[5] =(byte)((control_forward_back&0xff00)>>8);
		bytes[6] =(byte) (control_turn_left_right&0xff-27);
		bytes[7] =(byte)((control_turn_left_right&0xff00)>>8);
		bytes[8] =(byte) (AUX1&0xff);
		bytes[9] =(byte)((AUX1&0xff00)>>8);
		bytes[10]=(byte) (AUX2&0xff);
		bytes[11]=(byte)((AUX2&0xff00)>>8);
		bytes[12]=(byte) (AUX3&0xff);
		bytes[13]=(byte)((AUX3&0xff00)>>8);
		bytes[14]=(byte) (AUX4&0xff);
		bytes[15]=(byte)((AUX4&0xff00)>>8);
		
		return bytes;
	}
}
