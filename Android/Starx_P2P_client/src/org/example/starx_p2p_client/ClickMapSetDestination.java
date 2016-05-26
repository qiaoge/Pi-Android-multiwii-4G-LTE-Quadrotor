package org.example.starx_p2p_client;

import java.util.ArrayList;
import java.util.List;
import org.example.starx_p2p_client.R;
import android.graphics.Color;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

public abstract class ClickMapSetDestination{
	
	static boolean destinationflag=false;

	static List<Marker> circles = new ArrayList<Marker>();
	static LatLng startpoint;
	static AMap aMap;
	static private Polyline polyline;
	static MarkerOptions markerOptions = new MarkerOptions();
	
	public static void onMapClick(LatLng arg0) {
		if(destinationflag){ 
			StatePublic.ll.add(arg0);
			if(StatePublic.ll.size()==1){
				addmarker(R.drawable.green,StatePublic.ll.get(0));
			}
			else if(StatePublic.ll.size()==2){
				drawline();
				addmarker(R.drawable.red,StatePublic.ll.get(1));
			}
			else if(StatePublic.ll.size()>2){
				polyline.setPoints(StatePublic.ll);
				deletemarker();
				addmarker(R.drawable.blue,StatePublic.ll.get(StatePublic.ll.size()-2));
				addmarker(R.drawable.red,StatePublic.ll.get(StatePublic.ll.size()-1));
			}
		}
	}
	protected static void drawline(){
		polyline = aMap.addPolyline((new PolylineOptions())
				.addAll(StatePublic.ll).width(8)
				.setDottedLine(true).geodesic(true)
				.color(Color.BLACK));
	}
	public static void addmarker(int color,LatLng p){
		circles.add(aMap.addMarker(markerOptions.anchor(0.5f, 0.5f)
			    .position(p).icon(BitmapDescriptorFactory.fromResource(color))
			    .draggable(false)));	
	}
	protected static void deletemarker(){
		try {
			circles.get(circles.size()-1).destroy();
			circles.remove(circles.size()-1);
		} catch (Exception e) {
		}
	}
	protected static void deleteallmarker(){
		for(int i=circles.size();i>0;i--){
			deletemarker();
		}
	}
	public static int voyage(){   //计算航程多少米
		int distance = 0;
		for(int count=StatePublic.ll.size();count>=2;count--)
		distance+=AMapUtils.calculateLineDistance(StatePublic.ll.get(count-1), StatePublic.ll.get(count-2));
		return distance;
	}
		
	public static int delDestination(){
		int count=StatePublic.ll.size();
		if(count>2){
			StatePublic.ll.remove(count-1);
			polyline.setPoints(StatePublic.ll);
			deletemarker();
			deletemarker();
			addmarker(R.drawable.red,StatePublic.ll.get(StatePublic.ll.size()-1));
		}else if(count==2){
			StatePublic.ll.remove(count-1);
			try {
				polyline.remove();
			} catch (Exception e) {
			}	
			deletemarker();
		}else if(count==1){
			StatePublic.ll.remove(count-1);
			deletemarker();
		}
		return count-1;
	}
	public static void delAllDestination(){
		for(int count=StatePublic.ll.size();count>=1;count--)
			StatePublic.ll.remove(count-1);
		try {
			polyline.remove(); 
		} catch (Exception e) {
		}
		deleteallmarker();
	}
	public static void alldraw() {
		int j=0;
		circles.removeAll(circles);
		polyline=null;
		if(StatePublic.ll.size()>0){
			drawline();
			for(int i=0;i<StatePublic.ll.size();i++){
				j=R.drawable.blue;
				if(i==0) j=R.drawable.green;
				if(i==StatePublic.ll.size()-1) j=R.drawable.red;
				try {
					addmarker(j,StatePublic.ll.get(i));
				} catch (Exception e) {
				}
			}
		}
	}

}
