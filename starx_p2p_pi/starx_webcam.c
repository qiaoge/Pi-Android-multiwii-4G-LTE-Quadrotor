#include <limits.h>
#include <sys/stat.h>
#include <sys/select.h>
#include <time.h>
#include <sys/ioctl.h>
#include <sys/file.h>
#include <stdlib.h>
#include <unistd.h> 
#include <fcntl.h>
#include <pthread.h>
#include <stdio.h> 
#include <sys/types.h>
#include <sys/time.h> 
#include <signal.h> 
#include <netinet/in.h>
#include <netdb.h> 
#include <net/if.h>
#include <string.h>
#include <sched.h>
#include <stdarg.h>
#include <dirent.h>
#include <arpa/inet.h>  // inet_ntoa
#include <semaphore.h>  
#include <wiringPi.h>
#include <softPwm.h>
//=================================
#include "STARX/STARX_API.h"
#include "AVSTREAM_IO_Proto.h"
#include "readconfig.h"
//=================================
#include <errno.h>
#include "picture_t.h"
#include "simplerecorder.h"
#include "log.h"
#include "ringfifo.h"
#include "serialport.h"
#include "multiwii.h"
//=================================


#define MAX_SIZE 1024*1024*200   //

char MyDID[20];
char StrPara[100];
char APILicense[10];
unsigned int TimeOut_Sec=60;
char bEnableInternet=1;
int VideoChannel=7;
int ControlChannel=5;
int ServoChannel=4;
int yuv_type=1;
int fps=20;
int brightness=60;
int bitrate=10000000;
int networkport=0;
int UDP_Port=0;
int timestoInit=1;

unsigned int fallsafe_ms=700;

int serialspeed=19200;

char serialPort[100] ="/dev/ttyS1";
char camera_name[100]="/dev/video0";
char replayFileName[100];

struct picture_t pic;
struct encoded_pic_t encoded_pic,header_pic;
struct encoded_pic_t sendcoded;
struct encoded_pic_t copycoded;

pthread_t thread[3];
pthread_mutex_t mut; 

//===============add==================

static char mkv_filename[100];
static char dirname[20];
static char tempname[20];
//====================================
INT32 SessionHandle = 0;

sem_t sem1, sem2;
//===============serial==============
int fd_serial =0;
struct aircraft_mission_state ams;
/*
//===============i2c==============
char i2c_name[100]="/dev/i2c-1";
int fd_i2c=0;
char i2c_address=0X04;
*/
struct timeval timestart,timeend;


static void get_filename(char replayFileName[])
{
    DIR *dir=NULL;
    dir = opendir(replayFileName);
    if(NULL == dir){
        strcpy(mkv_filename,".");
        printf("Use default directory.\n");
    }
    else
        strcpy(mkv_filename,replayFileName);
	time_t t = time(0);
	
	strcat(mkv_filename,"/Record/");
	if(NULL==opendir(mkv_filename))
		mkdir(mkv_filename,0775);
	strftime(dirname, 20, "%Y%m%d",localtime(&t));
	strftime(tempname, 20, "%H%M%S",localtime(&t));
	strcat(mkv_filename,dirname);
	if(NULL==opendir(mkv_filename))
		mkdir(mkv_filename,0775);
	strcat(mkv_filename,"/CAM_");
	strcat(mkv_filename,tempname);
	strcat(mkv_filename,".mkv");
	printf("/033[40;31mFile:%s/033[0m\n",mkv_filename); 
}

int g_s32Quit = 0;//退出全局变量
int camera_Quit = 0;
int encode_Quit =0;
int schedule_do_Quit = 0;
int control_thread_go=1;
int g_s32DoPlay=0;    //开始播放
int headflag=0;      //传输H264开头nal
int hold_on=0;
int hold_on_ready=0;

void *schedule_do(void *arg){
    int i=0;
	//unsigned int ringbuffer;
	//int buflen=0,ringbuflen=0,ringbuftype;
	//struct ringbuf ringinfo;
	int WriteSize=0;
	int flag=0;
	CHAR *cBuf = (CHAR *) malloc(10);
	int nRet;
	int beginFrame=1;//0;
	
	sendcoded.buffer=malloc(1024*100);
	sendcoded.length=0;
	copycoded.buffer=malloc(1024*100);
	copycoded.length=0;
	
	
	while(!schedule_do_Quit){
		if(hold_on==0){
			hold_on_ready=0;
			memset(sendcoded.buffer,0,sendcoded.length);
			sendcoded.length=0;
			encoder_encode_frame(&sendcoded);
		
			if(g_s32DoPlay>0)
			{
				if(headflag>0)
				{
					headflag--;
					STARX_Write(SessionHandle,VideoChannel,header_pic.buffer,header_pic.length);
					printf("write head\r\n");
				}
				if(sendcoded.length>0)
				STARX_Write(SessionHandle,VideoChannel,sendcoded.buffer,sendcoded.length);
			}
			memcpy(copycoded.buffer,sendcoded.buffer,sendcoded.length);
			copycoded.length=sendcoded.length;
		}else{
			hold_on_ready=1;
			headflag=1;
		}
	}
	encoder_close();
	//free(sendcoded.buffer);
	printf("schedule_do exit");
	pthread_exit(0);	
}

void networkCheck(){
	int times=0;
	INT32 ret;
		
	st_STARX_NetInfo NetInfo;
	ret = STARX_Initialize(StrPara);
	ret = STARX_NetworkDetect(&NetInfo, networkport);
	while((times<timestoInit) && NetInfo.bFlagInternet != 1){
		STARX_DeInitialize();
		ret = STARX_Initialize(StrPara);
		ret = STARX_NetworkDetect(&NetInfo, networkport+times*7);
		times++;
		printf("%d ",times);
	}
	printf("\n");
	printf("-------------- NetInfo: -------------------\n");
	printf("Internet Reachable     : %s\n",
			(NetInfo.bFlagInternet == 1) ? "YES" : "NO");
	printf("P2P Server IP resolved : %s\n",
			(NetInfo.bFlagHostResolved == 1) ? "YES" : "NO");
	printf("P2P Server Hello Ack   : %s\n",
			(NetInfo.bFlagServerHello == 1) ? "YES" : "NO");
	printf("Local NAT Type         :");
	STARX_Share_Bandwidth(1);
	switch (NetInfo.NAT_Type) {
	case 0:
		printf(" Unknow\n");
		break;
	case 1:
		printf(" IP-Restricted Cone\n");
		break;
	case 2:
		printf(" Port-Restricted Cone\n");
		break;
	case 3:
		printf(" Symmetric\n");
		break;
	}
	printf("My Wan IP : %s\n", NetInfo.MyWanIP);
	printf("My Lan IP : %s\n", NetInfo.MyLanIP);
}

void *client_thread()  
{  	
	int nRet;
	CHAR *cBuf = (CHAR *) malloc(1);
	CHAR *cBuf2 = (CHAR *) malloc(40);
	CHAR *cBuf3 = (CHAR *) malloc(17);
	int rsize;
	int Rsize=1;
	int rsize2;
	int Rsize2=1;
	int rsize3;
	int Rsize3=1;
	int i;
	unsigned int servodata[8]={0};
	networkCheck();
	st_STARX_Session Sinfo;
	//=============================
	struct timeval starttime,endtime;
	gettimeofday(&starttime,0);
	// 登陆到P2P Server，一直阻塞到有客户端连进来，或者TimeOut到期
	while(!g_s32Quit) {
		SessionHandle = STARX_Listen(MyDID, TimeOut_Sec, UDP_Port, 1,APILicense);
		if (SessionHandle >= 0) { //有合法客户端连入，取得合法会话句柄
			printf("Connected\n");
			control_thread_go=0; //go
			while(!control_thread_go){
				usleep(1000);
				nRet =STARX_Check_Buffer(SessionHandle, VideoChannel,
								                NULL,
								                (UINT32 *) &rsize);
	                	
				STARX_Check_Buffer(SessionHandle, ControlChannel,
								                NULL,
								                (UINT32 *) &rsize2);
				STARX_Check_Buffer(SessionHandle, ServoChannel,
								                NULL,
								                (UINT32 *) &rsize3);
				if (nRet == ERROR_STARX_SESSION_CLOSED_TIMEOUT) {
					printf("control_thread, Session TimeOUT!!\n");
					g_s32DoPlay=0;
					control_thread_go=1;
				
				} else if (nRet == ERROR_STARX_SESSION_CLOSED_REMOTE) {
					printf("control_thread, Session Remote Close!!\n");
					g_s32DoPlay=0;
					control_thread_go=1;
				} else if (nRet == ERROR_STARX_INVALID_SESSION_HANDLE) {
					printf("control_thread, invalid session handle!!\n");
					g_s32DoPlay=0;
					control_thread_go=1;
				}
				if (rsize > 0) {
					Rsize =rsize;
					STARX_Read(SessionHandle, VideoChannel, cBuf, &Rsize,100);		
					if(cBuf[0]==1){			
						cBuf[0]=0;
						g_s32DoPlay=1;
						headflag=1;
						printf("call 264stream\r\n");
					}else if(cBuf[0]=2){
						pic.width=(cBuf[1]|cBuf[2]<<8);
						pic.height=(cBuf[3]|cBuf[4]<<8);
						fps=(int)cBuf[5];
						brightness=(int)cBuf[6];
						printf("%d %d %d %d\n",pic.width,pic.height,fps,brightness);
						reinit();
					}
				}
				if (rsize2 > 0) {
					Rsize2 =rsize2;
					STARX_Read(SessionHandle, ControlChannel, cBuf2, &Rsize2,100);
					switch(cBuf2[4]){	
						case 201:    //MSP_SET_RAW_GPS 			
							gps_mission_add( &ams,cBuf2);
							 memset(cBuf2, 0, sizeof(cBuf2));
							  break;
						default:
							flock(fd_serial,LOCK_EX);
							if(!write(fd_serial,cBuf2,Rsize2))
								printf("serial can't write\n");
							flock(fd_serial, LOCK_UN);
							memset(cBuf2, 0, sizeof(cBuf2));
							break;
					}
				}
				if(rsize3 > 0){
					Rsize3=rsize3;
					STARX_Read(SessionHandle, ServoChannel, cBuf3, &Rsize3,100);
				    if(Rsize3>0){
				    	if(cBuf3[0]==0x53){
				    		for(i=0;i<8;i++){
				    			servodata[i]=(cBuf3[i*2+1]|cBuf3[i*2+2]<<8);
				    		}
				    		softPwmWrite(0,servodata[0]);
				    		softPwmWrite(1,servodata[1]);
				    		softPwmWrite(2,servodata[2]);
				    		softPwmWrite(3,servodata[3]);
				    		softPwmWrite(4,servodata[4]);
				    		softPwmWrite(5,servodata[5]);
				    	}
				    	gettimeofday(&starttime,0);
				    }
				    gettimeofday(&endtime,0);
				    double timeuse=1000000*(endtime.tv_sec-starttime.tv_sec)+endtime.tv_usec-starttime.tv_usec;
				    if((timeuse/=1000)>fallsafe_ms){
				    		softPwmWrite(0,0);
				    		softPwmWrite(1,0);
				    		softPwmWrite(2,0);
				    		softPwmWrite(3,0);
				    		softPwmWrite(4,0);
				    		softPwmWrite(5,0);
				    }
				    memset(cBuf3, 0, sizeof(cBuf3));
				}		
			}
			STARX_Close(SessionHandle);
			printf("client exit\r\n");
		}
	}
	if(thread[1])
		pthread_join(thread[1],NULL);
	if(thread[2])
		pthread_join(thread[2],NULL);
	STARX_DeInitialize();
	printf("client_thread exit");
    pthread_exit(0);  
}
void reinit(){
	int i=0;
	hold_on=1;
	while(hold_on_ready==0&&i<1000){i++;}
	if(hold_on_ready==1&&hold_on==1){
		encoder_close();
		encoder_init(&pic,fps,brightness,bitrate);
		encoder_encode_headers(&header_pic);
		printf("reinit\n");
		hold_on=0;
	}
}
void *aircraft_thread(){
    char gps_state[]={'$','M','<',0,MSP_RAW_GPS,MSP_RAW_GPS};
    char *mwc_msg;
    while(!control_thread_go){
		usleep(1000);
		if(flock(fd_serial,LOCK_EX | LOCK_NB)==0){
        	write(fd_serial,gps_state,strlen(gps_state));
        	read_multiwii(&ams,fd_serial,mwc_msg);
        	if(flock(fd_serial,LOCK_UN)<0)
			printf("Unlock serial in aircraft_thread!\n");
        }
        
        if(strlen(mwc_msg)>0){
            STARX_Write(SessionHandle,ControlChannel,mwc_msg,strlen(mwc_msg));
            free(mwc_msg);
        }
        if(((ams.lat-(unsigned long)(ams.gd->gpsMsg+2))<1000)&& 
             ((ams.lon-(unsigned long)(ams.gd->gpsMsg+6))<1000)) 
             gps_mission_remove(&ams);
    }
    pthread_exit(0);
}
void stop_running(int param){
	camera_Quit = 1;	
}
void value_readfile_init(){ //config配置文件读取
	char value[20]="";
	FILE *fp;
	if(NULL==opendir("./")) printf("opendir err\r\n");
	if((fp=fopen("./config","r"))==NULL){
		printf("can not read config\r\n"); exit (0);
	}
	else{
			GetProfileString(fp,"MyDID","MyDID",MyDID);
			GetProfileString(fp,"StrPara","StrPara",StrPara);
			GetProfileString(fp,"APILicense","APILicense",APILicense);
			if(GetProfileString(fp,"timestoInit","timestoInit",value)){
				sscanf(value,"%u",&timestoInit);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"networkport","networkport",value)){
				sscanf(value,"%u",&networkport);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"UDP_Port","UDP_Port",value)){
				sscanf(value,"%u",&UDP_Port);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"TimeOut_Sec","TimeOut_Sec",value)){
				sscanf(value,"%u",&TimeOut_Sec);
				memset(value, 0, sizeof(value));
			}	
			if(GetProfileString(fp,"bEnableInternet","bEnableInternet",value)){
				bEnableInternet=value[0]; 
				memset(value, 0, sizeof(value));
			}
		
			if(GetProfileString(fp,"VideoChannel","VideoChannel",value)){
				sscanf(value,"%d",&VideoChannel);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"ControlChannel","ControlChannel",value)){
				sscanf(value,"%d",&ControlChannel);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"ServoChannel","ServoChannel",value)){
				sscanf(value,"%d",&ServoChannel);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"width","width",value)){
				sscanf(value,"%d",&(pic.width));
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"height","height",value)){
				sscanf(value,"%d",&(pic.height));
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"fps","fps",value)){
				sscanf(value,"%d",&fps);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"brightness","brightness",value)){
				sscanf(value,"%d",&brightness);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"bitrate","bitrate",value)){
				sscanf(value,"%d",&bitrate);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"serialspeed","serialspeed",value)){
				sscanf(value,"%d",&serialspeed);
				memset(value, 0, sizeof(value));
			}
			if(GetProfileString(fp,"fallsafe_ms","fallsafe_ms",value)){
				sscanf(value,"%d",&fallsafe_ms);
				memset(value, 0, sizeof(value));
			}
			GetProfileString(fp,"serialPort","serialPort",serialPort);
			printf("%s\r\n",serialPort);
			
			GetProfileString(fp,"cameraName","cameraName",camera_name);
			printf("%s\r\n",camera_name);

			GetProfileString(fp,"replayFileName","replayFileName",replayFileName);
			printf("%s\r\n",replayFileName);
			closefile(fp);
		}
}
void serial_init(){	
	if((fd_serial = open_serialport(serialPort)) == -1){
		perror("Open port");
		return -1;
	}
	if( set_port(fd_serial,serialspeed,8,'N',1) == -1){
		perror("Set_Port");
		return -1;
	}
}
void serial_close(){
	close_port(fd_serial);
}

int main(){
	int FileSize;
	FileSize =0;
	int WriteSize;
	long tstmp;
	value_readfile_init();
	serial_init();
	wiringPiSetup();
	softPwmCreate(0,0,20000);
	softPwmCreate(1,0,20000);
	softPwmCreate(2,0,20000);
	softPwmCreate(3,0,20000);
	softPwmCreate(4,0,20000);
	softPwmCreate(5,0,20000);
	//i2c_init();
	gettimeofday(&timestart,NULL);
	get_filename(replayFileName);

	if(!encoder_init(&pic,fps,brightness,bitrate))
		goto error_encoder;
	if(!output_init(&pic,mkv_filename))
		goto error_output;

	encoded_pic.buffer=malloc(1024*100);
	encoded_pic.length=0;
	if(!encoder_encode_headers(&encoded_pic))
		goto error_output;
		
	memcpy(&header_pic,&encoded_pic,sizeof(encoded_pic));
	header_pic.buffer=malloc(1024*100);//(encoded_pic.length);
	printf("header_pic len:%d\n",encoded_pic.length);
	memcpy(header_pic.buffer,encoded_pic.buffer,encoded_pic.length);
	if(!output_write_headers(&encoded_pic))
		goto error_output;

	if(signal(SIGINT, stop_running) == SIG_ERR){
		printf("signal() failed\n");
		goto error_signal;
	}

	if(pthread_create(&thread[0], NULL, client_thread, NULL) != 0)         
               printf("client_thread error!\n");  
        else  
                printf("client_thread ok\n"); 
	if(pthread_create(&thread[1],NULL,schedule_do,NULL) != 0)
				printf("schedule_do error!\n");  
        else  
               printf("schedule_do ok\n");
	
    if(pthread_create(&thread[2],NULL,aircraft_thread,NULL) != 0)
				printf("aircraft_thread error!\n");  
        else  
               printf("aircraft_thread ok\n");
 	
	while (!camera_Quit) 
	{	
		gettimeofday(&timeend,NULL);
		tstmp=(timeend.tv_sec-timestart.tv_sec)*1000000ll+timeend.tv_usec-timestart.tv_usec;
		if(tstmp<(1000000/fps)){
			usleep(tstmp);
		}else{
			timestart.tv_sec=timeend.tv_sec;
			timestart.tv_usec=timeend.tv_usec;
			/*
			if(copycoded.length>0){
				
			if ((FileSize>MAX_SIZE) && (sendcoded.frame_type ==FRAME_TYPE_I)) {
				output_close();
				printf("file full\n");	
				get_filename(replayFileName);
				printf("file:%s\n",mkv_filename);
				
				if(!output_init(&pic,mkv_filename)){
					printf("output_init error\r\n");
					break;
				}
				if(!output_write_headers(&header_pic)){
					printf("output_write_headers error\r\n");
					break;
				}
			
				FileSize=0;
				//ResetTime(&pic,&encoded_pic);
				
				if(!output_write_frame(&copycoded)){
					printf("output_write_frame1 error\r\n");
					break;
				}
				//encoder_release(&encoded_pic);
			} else {
			
				if(!output_write_frame(&copycoded)){
					printf("output_write_frame2 error\r\n");
					break;
				}
				//encoder_release(&encoded_pic);
			}
			copycoded.length=0;
			}
			*/
		}
	}	
	error_signal:
	
	printf("exit the cam_thread\n");
	encode_Quit =1;
	//=========add==========
	free(sendcoded.buffer);
	//=========add==========
	schedule_do_Quit = 1;
	g_s32Quit = 1;	
	control_thread_go=1;
	printf("The device quit!\n");

	pthread_cancel(thread[0]); 
	pthread_cancel(thread[1]); 
	pthread_cancel(thread[2]); 
	
	if(thread[0])
		pthread_join(thread[0],NULL);
	if(thread[1])
		pthread_join(thread[1],NULL);
	if(thread[2])
		pthread_join(thread[2],NULL);
	
	error_output:

	error_cam_on:
		output_close();

	error_encoder:

	error_cam:
		serial_close();
		//i2c_close();
		return 0;
}




	
		







