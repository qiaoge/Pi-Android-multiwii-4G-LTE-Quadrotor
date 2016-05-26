package org.example.starx_p2p_client;

import java.util.ArrayList;
import java.util.List;

import com.amap.api.maps.model.LatLng;

import android.R.integer;
import android.app.Application;

public class StatePublic extends Application{
	public static String DID="STAR-000054-UZCSB";
	//public static String strPara="EFGFFBBOKAIEGEJKEIHCFPEGGOIHHHIADPBBFEHLBANJKKLMHBBHGCKHDLODNDLPFMNMLC";
	public static String strPara="EFGBFFBJKFJOGCJNFHHCFHEMGENHHBMHHLFGBKDFAMJLLDKHDHACDEPBGCLAIALDADMPKDDIODMEBOCNJLNDJJ";
	//public static String strPara="ECGBFFBJKAIEGHJAEBHLFGEMHLNBHCNIGEFCBNCIBIJALMLFCFAPCHODHOLCJNKIBIMCLDCNOBMOAKDMJGNMIJBJML";
	public static List<LatLng> ll = new ArrayList<LatLng>();
	public static LatLng planeGPS=null; 
	public static int connect=-1;
	public static boolean manualcontrol=false;
	public static boolean autocontrol=false;
	public static boolean joystick=false;
	public static int safeState = 0;
	public static int okSend=0;
	public static SendData sendData;
	
	public static int[][] resolution={{320,240},{800,600},{1920,600}};
	public static int resolution_c = 1;
	public static int brightness=50;
	public static int[] fps={20,40,60};
	public static int fps_c=0;
}
