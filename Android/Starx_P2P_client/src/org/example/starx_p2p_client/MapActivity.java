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
	Button go;                      //���Ϳ���ָ���
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
        mapView.onCreate(savedInstanceState);// ����Ҫд
        
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
        mUiSettings.setZoomControlsEnabled(true); //�Ƿ���ʾ���Ű�ť
        mUiSettings.setCompassEnabled(true); //�Ƿ���ʾָ����
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
	        			destinationButton.setText("ȡ������");
	        		}
	        		else{
	        			setDestination=false;
	        			ClickMapSetDestination.destinationflag=false;
	        			ClickMapSetDestination.delAllDestination();
						destinationButton.setText("�躽��");
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
					destinationButton.setText("�躽��");				
				}
				voyageSetTest();
			}
		});
        satelliteBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				satellite_map=!satellite_map;
				if(satellite_map)
					aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// ���ǵ�ͼģʽ
				else
					aMap.setMapType(AMap.MAP_TYPE_NORMAL);// ʸ����ͼģʽ
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
    		destinationButton.setText("ȡ������");
    		setDestination=true;
    	}
    	voyageSetTest();
    }
    protected void voyageSetTest(){
    	int i=ClickMapSetDestination.voyage();
    	String string;
    	if(i<1000)
    		voyageText.setText(i+""+"��");
    	else{
    		string=i/1000+"";
    		if(i%1000!=0) string+="."+i%1000+""+"����";
    		voyageText.setText(i/1000+""+"."+i%1000+""+"����");
    	}
    }
    
    private void setUpMap() {
    	aMap.setLocationSource(this);// ���ö�λ����
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// ����Ĭ�϶�λ��ť�Ƿ���ʾ
        aMap.setMyLocationEnabled(true);// ����Ϊtrue��ʾ��ʾ��λ�㲢�ɴ�����λ��false��ʾ���ض�λ�㲢���ɴ�����λ��Ĭ����false
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
                mListener.onLocationChanged(amapLocation);// ��ʾϵͳС����
            }
        }
    }

	@Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);
            //�˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
            //ע�����ú��ʵĶ�λʱ��ļ���������ں���ʱ�����removeUpdates()������ȡ����λ����
            //�ڶ�λ�������ں��ʵ��������ڵ���destroy()����     
            //����������ʱ��Ϊ-1����λֻ��һ��
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
	@Override //�˵�
    public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "����STARX_DID");
        return true;
    }
	/*
	@Override    
	public boolean onOptionsItemSelected(MenuItem item) {
		 switch (item.getItemId()) {
		 case Menu.FIRST + 1:
			 final EditText pwdEditText=new EditText(this);
		 		pwdEditText.setText(pwd);
			 new AlertDialog.Builder(this).setTitle("����������").setIcon(
				     android.R.drawable.ic_dialog_info).setView(
				    		 pwdEditText).setPositiveButton("ȷ��",new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,int which) {
									pwd=pwdEditText.getText().toString();					
								}	    			 
				    		 })
				     .setNegativeButton("ȡ��", null).show(); 
			 break;
		 
		 default:
			 break;
		 }
		 return false;
	 } 
	 */
}	
