#ifndef _STARX_API___INC_H_
#define _STARX_API___INC_H_

#ifdef WIN32DLL
#ifdef PPPP_API_EXPORTS
#define PPPP_API_API __declspec(dllexport)
#else
#define PPPP_API_API __declspec(dllimport)
#endif
#endif //// #ifdef WIN32DLL

#ifdef LINUX
#include <netinet/in.h>
#define PPPP_API_API 
#endif //// #ifdef LINUX
//===============================
#include <netinet/in.h>
#define PPPP_API_API 
//================================

#ifdef _ARC_COMPILER
#include "net_api.h"
#define PPPP_API_API 
#endif //// #ifdef _ARC_COMPILER

#include "STARX_Type.h"
#include "STARX_Error.h"

#ifdef __cplusplus
extern "C" {
#endif // __cplusplus


typedef struct{	
	CHAR bFlagInternet;		// Internet Reachable? 1: YES, 0: NO
	CHAR bFlagHostResolved;	// P2P Server IP resolved? 1: YES, 0: NO
	CHAR bFlagServerHello;	// P2P Server Hello? 1: YES, 0: NO
	CHAR NAT_Type;			// NAT type, 0: Unknow, 1: IP-Restricted Cone type,   2: Port-Restricted Cone type, 3: Symmetric 
	CHAR MyLanIP[16];		// My LAN IP. If (bFlagInternet==0) || (bFlagHostResolved==0) || (bFlagServerHello==0), MyLanIP will be "0.0.0.0"
	CHAR MyWanIP[16];		// My Wan IP. If (bFlagInternet==0) || (bFlagHostResolved==0) || (bFlagServerHello==0), MyWanIP will be "0.0.0.0"
} st_STARX_NetInfo;

typedef struct{	
	INT32  Skt;					// Sockfd
	struct sockaddr_in RemoteAddr;	// Remote IP:Port
	struct sockaddr_in MyLocalAddr;	// My Local IP:Port
	struct sockaddr_in MyWanAddr;	// My Wan IP:Port
	UINT32 ConnectTime;				// Connection build in ? Sec Before
	CHAR DID[24];					// Device ID
	CHAR bCorD;						// I am Client or Device, 0: Client, 1: Device
	CHAR bMode;						// Connection Mode: 0: P2P, 1:Relay Mode
	CHAR Reserved[2];				
} st_STARX_Session;

PPPP_API_API UINT32 STARX_GetAPIVersion(void);
PPPP_API_API INT32 STARX_QueryDID(const CHAR* DeviceName, CHAR* DID, INT32 DIDBufSize);
PPPP_API_API INT32 STARX_Initialize(CHAR *Parameter);
PPPP_API_API INT32 STARX_DeInitialize(void);
PPPP_API_API INT32 STARX_NetworkDetect(st_STARX_NetInfo *NetInfo, UINT16 UDP_Port);
PPPP_API_API INT32 STARX_NetworkDetectByServer(st_STARX_NetInfo *NetInfo, UINT16 UDP_Port, CHAR *ServerString);
PPPP_API_API INT32 STARX_Share_Bandwidth(CHAR bOnOff);
PPPP_API_API INT32 STARX_Listen(const CHAR *MyID, const UINT32 TimeOut_Sec, UINT16 UDP_Port, CHAR bEnableInternet, const CHAR* APILicense);
PPPP_API_API INT32 STARX_Listen_Break(void);
PPPP_API_API INT32 STARX_LoginStatus_Check(CHAR* bLoginStatus);
PPPP_API_API INT32 STARX_Connect(const CHAR *TargetID, CHAR bEnableLanSearch, UINT16 UDP_Port);
PPPP_API_API INT32 STARX_ConnectByServer(const CHAR *TargetID, CHAR bEnableLanSearch, UINT16 UDP_Port, CHAR *ServerString);
PPPP_API_API INT32 STARX_Connect_Break();
PPPP_API_API INT32 STARX_Check(INT32 SessionHandle, st_STARX_Session *SInfo);
PPPP_API_API INT32 STARX_Close(INT32 SessionHandle);
PPPP_API_API INT32 STARX_ForceClose(INT32 SessionHandle);
PPPP_API_API INT32 STARX_Write(INT32 SessionHandle, UCHAR Channel, CHAR *DataBuf, INT32 DataSizeToWrite);
PPPP_API_API INT32 STARX_Read(INT32 SessionHandle, UCHAR Channel, CHAR *DataBuf, INT32 *DataSize, UINT32 TimeOut_ms);
PPPP_API_API INT32 STARX_Check_Buffer(INT32 SessionHandle, UCHAR Channel, UINT32 *WriteSize, UINT32 *ReadSize);
#ifdef __cplusplus
}
#endif // __cplusplus
#endif ////#ifndef _STARX_API___INC_H_
