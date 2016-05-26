package com.p2p.pppp_api;

import java.util.Arrays;

public class AVStreamIOHead {
	public static final int LEN_HEAD	=4;
	
	byte[] streamIOHead=new byte[LEN_HEAD];
	
	public AVStreamIOHead(){}
	public AVStreamIOHead(byte[] byts){ setData(byts); }
	
	public void setData(byte[] byts){
		if(byts==null || byts.length<LEN_HEAD) Arrays.fill(streamIOHead, (byte)0);
		else System.arraycopy(byts, 0, streamIOHead, 0, streamIOHead.length);
	}
	public int getStreamIOType() { return (int)(streamIOHead[3]&0xFF); }
	public int getDataSize()	 {
		int nSize=(streamIOHead[2]&0xFF)<<16 | (streamIOHead[1]&0xFF)<<8 | (streamIOHead[0]&0xFF); 
		return nSize;
	}
}
