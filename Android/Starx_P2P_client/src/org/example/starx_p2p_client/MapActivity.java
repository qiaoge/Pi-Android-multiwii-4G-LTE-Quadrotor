package org.example.starx_p2p_client;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.MapView;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class MapActivity extends Activity implements LocationSource,AMapLocationListener{
	
	public static final int MSG_LOG = 0;
	public static final String TAG = "AutoF_gaode";
	
	private MapView mapView;
    private AMap aMap;
    UiSettings mUiSettings;
    
    boolean enableSetDestination=false;
    boolean setDestination=false;
	Button planelocate_button;
	Button searchButton;
	Button destinationButton;
	Button destinationOK;
	Button destinationDelete; 
	Button go;                      //发送控制指令按键
	boolean satellite_map=false;
	CheckBox satelliteBox;
	TextView voyageText;

	//ClickMapSetDestination ClickMapSetDestination;
	OnMapClickListener onMapClickListener;
	
	private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
    	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 必须要写
        
        destinationButton=(Button)findViewById(R.id.button_set);
        destinationDelete=(Button)findViewById(R.id.button_del);
        satelliteBox=(CheckBox)findViewById(R.id.satellite_box);
        planelocate_button=(Button)findViewById(R.id.button_airplane);
        voyageText=(TextView)findViewById(R.id.text_voyage);
        go=(Button)findViewById(R.id.buttonOK);
        init();
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            ClickMapSetDestination.aMap=aMap;
            initdraw();
        }  
        UiSettings mUiSettings = aMap.getUiSettings();
        //aMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(23.01707391,113.06369969)));
        mUiSettings.setZoomControlsEnabled(true); //是否显示缩放按钮
        mUiSettings.setCompassEnabled(true); //是否显示指南针
        setUpMap();
        
        enableSetDestination=true;
        //ClickMapSetDestination=new ClickMapSetDestination(new LatLng(43.828, 87.621),aMap);
		aMap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng arg0){
				ClickMapSetDestination.onMapClick(arg0);
				voyageSetTest();
			}
		});
		planelocate_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
			}
		});
        destinationButton.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if(enableSetDestination){
	        		if(setDestination==false){     
	        			setDestination=true;
	        			ClickMapSetDestination.destinationflag=true;
	        			destinationButton.setText("取消航线");
	        		}
	        		else{
	        			setDestination=false;
	        			ClickMapSetDestination.destinationflag=false;
	        			ClickMapSetDestination.delAllDestination();
						destinationButton.setText("设航线");
						voyageSetTest();
					}
        		}
			}	
		});
        destinationDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(ClickMapSetDestination.delDestination()==0){
					ClickMapSetDestination.destinationflag=false;
					setDestination=false;
					destinationButton.setText("设航线");				
				}
				voyageSetTest();
			}
		});
        satelliteBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				satellite_map=!satellite_map;
				if(satellite_map)
					aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
				else
					aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
			}
		});
        go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				StatePublic.okSend++;
			}
		});
    }
    private void initdraw(){
    	ClickMapSetDestination.alldraw();
    	if(ClickMapSetDestination.destinationflag==true){
    		destinationButton.setText("取消航线");
    		setDestination=true;
    	}
    	voyageSetTest();
    }
    protected void voyageSetTest(){
    	int i=ClickMapSetDestination.voyage();
    	String string;
    	if(i<1000)
    		voyageText.setText(i+""+"米");
    	else{
    		string=i/1000+"";
    		if(i%1000!=0) string+="."+i%1000+""+"公里";
    		voyageText.setText(i/1000+""+"."+i%1000+""+"公里");
    	}
    }
    
    private void setUpMap() {
    	aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }
   
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
	@Override
	public void onLocationChanged(Location arg0) {}
	@Override
	public void onProviderDisabled(String arg0) {}
	@Override
	public void onProviderEnabled(String arg0) {}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
	@Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getAMapException().getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            }
        }
    }

	@Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);
            //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
            //在定位结束后，在合适的生命周期调用destroy()方法     
            //其中如果间隔时间为-1，则定位只定一次
            mAMapLocationManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 6000, 10, this);
        }
    }

	@Override
	public void deactivate() {	
		mListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
	}
	@Override //菜单
    public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "输入STARX_DID");
        return true;
    }
	/*
	@Override    
	public boolean onOptionsItemSelected(MenuItem item) {
		 switch (item.getItemId()) {
		 case Menu.FIRST + 1:
			 final EditText pwdEditText=new EditText(this);
		 		pwdEditText.setText(pwd);
			 new AlertDialog.Builder(this).setTitle("请输入密码").setIcon(
				     android.R.drawable.ic_dialog_info).setView(
				    		 pwdEditText).setPositiveButton("确定",new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,int which) {
									pwd=pwdEditText.getText().toString();					
								}	    			 
				    		 })
				     .setNegativeButton("取消", null).show(); 
			 break;
		 
		 default:
			 break;
		 }
		 return false;
	 } 
	 */
}	
