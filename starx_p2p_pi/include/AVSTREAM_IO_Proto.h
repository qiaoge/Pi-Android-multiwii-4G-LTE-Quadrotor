#ifndef __INCLUDE_FILE_AV_STREAM_IO_PROTOCOL
#define __INCLUDE_FILE_AV_STREAM_IO_PROTOCOL

#include "STARX/STARX_Type.h"

typedef enum {
	SIO_TYPE_UNKN,
	SIO_TYPE_VIDEO,
	SIO_TYPE_AUDIO,
	SIO_TYPE_IOCTRL,

}ENUM_STREAM_IO_TYPE;

typedef enum {
	CODECID_UNKN,
	CODECID_V_MJPEG,
	CODECID_V_MPEG4,
	CODECID_V_H264,
	
	CODECID_A_PCM =0x4FF,
	CODECID_A_ADPCM,
	CODECID_A_SPEEX,	
	CODECID_A_AMR,
	CODECID_A_AAC,
}ENUM_CODECID;

typedef enum 
{
	VFRAME_FLAG_I	= 0x00,	// Video I Frame
	VFRAME_FLAG_P	= 0x01,	// Video P Frame
	VFRAME_FLAG_B	= 0x02,	// Video B Frame
}ENUM_VFRAME;

typedef enum
{
	ASAMPLE_RATE_8K	= 0x00,
	ASAMPLE_RATE_11K= 0x01,
	ASAMPLE_RATE_12K= 0x02,
	ASAMPLE_RATE_16K= 0x03,
	ASAMPLE_RATE_22K= 0x04,
	ASAMPLE_RATE_24K= 0x05,
	ASAMPLE_RATE_32K= 0x06,
	ASAMPLE_RATE_44K= 0x07,
	ASAMPLE_RATE_48K= 0x08,
}ENUM_AUDIO_SAMPLERATE;

typedef enum
{
	ADATABITS_8		= 0,
	ADATABITS_16	= 1,
}ENUM_AUDIO_DATABITS;

typedef enum
{
	ACHANNEL_MONO	= 0,
	ACHANNEL_STERO	= 1,
}ENUM_AUDIO_CHANNEL;

typedef enum {
	IOCTRL_TYPE_UNKN,

	IOCTRL_TYPE_VIDEO_START,
	IOCTRL_TYPE_VIDEO_STOP,
	IOCTRL_TYPE_AUDIO_START,
	IOCTRL_TYPE_AUDIO_STOP,

}ENUM_IOCTRL_TYPE;

//NOTE: struct below is all Little Endian
typedef struct{
	union{
		struct{			
			UCHAR  nDataSize[3];
			UCHAR  nStreamIOType; //refer to ENUM_STREAM_IO_TYPE
		}uionStreamIOHead;
		UINT32 nStreamIOHead;
	};
}st_AVStreamIOHead;

//for Video and Audio
typedef struct{
	UINT16 nCodecID;	//refer to ENUM_CODECID	
	UCHAR  nOnlineNum;	
	UCHAR  flag;		//Video:=ENUM_VFRAME; Audio:=(ENUM_AUDIO_SAMPLERATE << 2) | (ENUM_AUDIO_DATABITS << 1) | (ENUM_AUDIO_CHANNEL)
	UCHAR  reserve[4];

	UINT32 nDataSize;
	UINT32 nTimeStamp;	//system tick
}st_AVFrameHead;

//for IO Control
typedef struct{
	UINT16 nIOCtrlType;//refer to ENUM_IOCTRL_TYPE
	UINT16 nIOCtrlDataSize;
}st_AVIOCtrlHead;

#endif  //#ifndef __INCLUDE_FILE_AV_STREAM_IO_PROTOCOL
