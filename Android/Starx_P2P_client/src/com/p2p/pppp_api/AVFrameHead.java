package com.p2p.pppp_api;

public class AVFrameHead {
	public static final int LEN_HEAD	=16;
	
	int  nCodecID=0;
	byte nOnlineNum=0;
	byte flag=0;
	int  nDataSize=0;
	long nTimeStamp=0L;
	
	
	public AVFrameHead() {}
	public void setData(byte[] byts)
	{
		if(byts==null || byts.length<LEN_HEAD) reset();
		else {
			nCodecID=(byts[1]&0xFF)<<8 | byts[0]&0xFF;
			nOnlineNum=byts[2];
			flag=byts[3];
			//reserve[4];			
			nDataSize =(0xff & byts[8]) | (0xff & byts[9])<<8 | (0xff & byts[10])<<16 | (0xff & byts[11])<<24;
			nTimeStamp=(0xff & byts[12]) |(0xff & byts[13])<<8 |(0xff & byts[14])<<16 | (0xff & byts[15])<<24;
		}
	}
	
	private void reset(){
		nCodecID=0;
		nOnlineNum=0;
		flag=0;
		nDataSize=0;
		nTimeStamp=0L;
	}
	
	public int  getCodecID()	{ return nCodecID; 					}
	public int  getOnlineNum()	{ return (int)(nOnlineNum &0xFF); 	}
	public int  getFlag()		{ return (int)(flag&0xFF);			}
	public int  getDataSize()	{ return nDataSize;					}
	public long getTimeStamp()	{ return nTimeStamp;				}
	
}
