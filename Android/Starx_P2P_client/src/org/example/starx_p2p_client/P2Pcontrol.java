package org.example.starx_p2p_client;

import java.nio.ByteBuffer;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import com.p2p.pppp_api.PPPP_APIs;
import com.p2p.pppp_api.st_PPPP_Session;

public class P2Pcontrol implements Runnable{

	public static final byte CHANNEL_VIDEO=7;
	public static final byte CHANNEL_CONTROL=5;
	public static final byte CHANNEL_SERVO=3;
	protected static final int CUSTOM_SOCKET_LENGTH=51200;//5120;
	
	volatile boolean running=false;
	
	static int m_handleSession=-1;	
	private Handler uiHandler;

	byte[] pAVData1=new byte[CUSTOM_SOCKET_LENGTH];
	byte[] pAVData2=new byte[CUSTOM_SOCKET_LENGTH];
	byte[] writeData=new byte[1];
	
	int[] nRecvSize=new int[1]; 
	int[] nRecvSize1=new int[1]; 
	
	boolean ok1=false;
    boolean ok2=false;
    int run12=1;
    int flag=0;
    Runnable runnable;
	//================================================================
	MediaCodec mediaCodec;
	ByteBuffer[] inputBuffers;
	long mCount=0;
    //=====================================================================
	
	public P2Pcontrol(Handler handler){
		uiHandler=handler;
	}
	public static int initAll(){
		PPPP_APIs.PPPP_DeInitialize();
		int nRet=PPPP_APIs.PPPP_Initialize(StatePublic.strPara.getBytes());	
		return nRet;
	}
	private void updateStatus(String string) {
		Message msg = new Message();
		msg.what = MainActivity.MSG_LOG;
		msg.obj = string;
		uiHandler.sendMessage(msg);
	}
	
	public int connectDev(String mDID) {
		byte bEnableLanSearch = 1; // 启用局域网搜索
		int UDP_Port = 0; // 随机端口
		updateStatus("正在连接设备...");
		initAll();
		if(m_handleSession<0){		
			m_handleSession = PPPP_APIs.PPPP_Connect(mDID, bEnableLanSearch,UDP_Port);
			//m_handleSession = PPPP_APIs.PPPP_ConnectByServer(mDID, bEnableLanSearch,UDP_Port,StatePublic.strPara);
			if (m_handleSession < 0) {
				updateStatus("设备连接出错");
				return m_handleSession;
			} else {
				updateStatus("设备连接成功");
			}
		}
		
		
		st_PPPP_Session SInfo = new st_PPPP_Session();
        if (PPPP_APIs.PPPP_Check(m_handleSession, SInfo) == PPPP_APIs.ERROR_PPPP_SUCCESSFUL) {
            String str;
            str = String.format("  ----Session Ready: -%s----",
                    (SInfo.getMode() == 0) ? "P2P" : "RLY");
            System.out.println(str);
            str = String.format("  Socket: %d", SInfo.getSkt());
            System.out.println(str);
            str = String.format("  Remote Addr: %s:%d", SInfo.getRemoteIP(),
                    SInfo.getRemotePort());
            System.out.println(str);
            str = String.format("  My Lan Addr: %s:%d", SInfo.getMyLocalIP(),
                    SInfo.getMyLocalPort());
            System.out.println(str);
            str = String.format("  My Wan Addr: %s:%d", SInfo.getMyWanIP(),
                    SInfo.getMyWanPort());
            System.out.println(str);
            str = String
                    .format("  Connection time: %d", SInfo.getConnectTime());
            System.out.println(str);
            str = String.format("  DID: %s", SInfo.getDID());
            System.out.println(str);
            str = String.format("  I am : %s",
                    (SInfo.getCorD() == 0) ? "Client" : "Device");
            System.out.println(str);
        }
        return m_handleSession;
	}
	public int disconnectDev(){
		int nRet=PPPP_APIs.ER_ANDROID_NULL;
		updateStatus("正在断连设备...");
		if(m_handleSession>0){
			nRet=PPPP_APIs.PPPP_Close(m_handleSession);
			m_handleSession=-1;
		}
		return m_handleSession;
	}
	
	public void start(Surface surface){
//		H264Decode.InitDecoder(640,480); //mark
		mediaCodec = MediaCodec.createDecoderByType("video/avc");  
		MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 640, 480);
		mediaCodec.configure(mediaFormat, surface, null, 0);  
		mediaCodec.start(); 

		running=true;
		
		runnable=new Runnable() {	
			@Override
			public void run() {
				while(running){
					if(ok1){
						onFrame(pAVData1, 0, nRecvSize[0], 1);
						ok1=false;
					}
					if(ok2){
						onFrame(pAVData2, 0, nRecvSize1[0], 1);
						ok2=false;
					}
				}
			}
		};
		new Thread(runnable).start();
		writeData[0]=1;
		PPPP_APIs.PPPP_Write(m_handleSession, CHANNEL_VIDEO, writeData, 1);
		Log.d(MainActivity.TAG,"send 1");
	}
	
	public void stop(){
		running=false;
		try {
			mediaCodec.stop();  
			mediaCodec.release();
		} catch (Exception e) {
		}
		ok1=false;
	    ok2=false;
	    run12=1;
	    flag=0;
	}
	@Override
	public void run() {
		int nRet=0;
	    inputBuffers = mediaCodec.getInputBuffers();
		while(running){		
			if(flag==0&&ok1==false){
				nRecvSize[0]=CUSTOM_SOCKET_LENGTH;
				nRet=PPPP_APIs.PPPP_Read(m_handleSession, 
						CHANNEL_VIDEO, pAVData1, nRecvSize, 30);
				if(nRecvSize[0]>0&&running){
					flag=1;
					ok1=true;
				} 
			}else if(flag==1&&ok2==false){
				nRecvSize1[0]=CUSTOM_SOCKET_LENGTH;
				nRet=PPPP_APIs.PPPP_Read(m_handleSession, 
						CHANNEL_VIDEO, pAVData2, nRecvSize1, 30); 		
				if(nRecvSize1[0]>0&&running){
					flag=0;
					ok2=true;
				} 
			}
		}
	}
	public synchronized void onFrame(byte[] buf, int offset, int length, int flag) {  
		//long curtime = SystemClock.elapsedRealtime();
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);//dequeueInputBuffer(-1);  
        if (inputBufferIndex >= 0) {  
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];  
            inputBuffer.clear();  
            inputBuffer.put(buf, offset, length);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * 1000000 /30 , 0);
            mCount++;
        }  
       MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();  
       int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);  
       while (outputBufferIndex >= 0) {  
           mediaCodec.releaseOutputBuffer(outputBufferIndex, true);  
           outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);  
       }  
       //while(SystemClock.elapsedRealtime()-curtime<33){}
	}
	
}
