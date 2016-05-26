#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <errno.h>
#include <ctype.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <math.h>
#include <semaphore.h>
#include <pthread.h>
#include <termios.h>
#include <signal.h>
#include <linux/kd.h>
#include <linux/fb.h>
#include <sys/time.h>
#include <sys/ipc.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/un.h>
#include <sys/socket.h>
#include <sys/un.h>
#include "serialport.h"

static int IsReceve = 0;
static unsigned char msg[200];
static int fd =0;


int open_serialport(char *dev_name){
    long  vdisable;
	fd = open( dev_name , O_RDWR);
    if (fd<0){
        perror("Can't Open Serial Port");
        return(-1);
    }
    else{
        //printf("open %s.....\n",*dev_name);
    }
    if(fcntl(fd, F_SETFL, 0)<0){
        printf("fcntl failed!\n");
    }
    else{
        printf("fcntl=%d\n",fcntl(fd, F_SETFL,0));
    }
    if(isatty(STDIN_FILENO)==0){
        printf("standard input is not a terminal device\n");
    }
    else{
        printf("isatty success!\n");
    }
    printf("fd-open=%d\n",fd);
    return fd;
}

int set_port(int fd,int baud_rate,int data_bits,char parity,int stop_bits){
        struct termios newtio,oldtio; //      
        //fprintf(stdout,"The Fucntion Set_Port() Begin!\n");      
        if( tcgetattr(fd,&oldtio) !=0 ){
                perror("Setup Serial:");
                return -1;
        }       
        bzero(&newtio,sizeof(newtio));        
        newtio.c_cflag |= CLOCAL | CREAD;
        newtio.c_cflag &= ~CSIZE;
        //Set BAUDRATE    
        switch(baud_rate)
        {
                case 2400:
                        cfsetispeed(&newtio,B2400);
                        cfsetospeed(&newtio,B2400);
                        break;
                case 4800:
                        cfsetispeed(&newtio,B4800);
                        cfsetospeed(&newtio,B4800);
                        break;
                case 9600:
                        cfsetispeed(&newtio,B9600);
                        cfsetospeed(&newtio,B9600);
                        break;
                case 19200:
                        cfsetispeed(&newtio,B19200);
                        cfsetospeed(&newtio,B19200);
                        break;        
                case 38400:
                        cfsetispeed(&newtio,B38400);
                        cfsetospeed(&newtio,B38400);
                        break;
                case 115200:
                        cfsetispeed(&newtio,B115200);
                        cfsetospeed(&newtio,B115200);
break;                                                                                                                                
                default:
                        cfsetispeed(&newtio,B9600);
                        cfsetospeed(&newtio,B9600);
                        break;         
        }
        //Set databits upon 7 or 8
        switch(data_bits){
                case 7:
                      newtio.c_cflag |= CS7;
                      break;
                case 8:
                default:
                        newtio.c_cflag |= CS8;
        }  
        switch(parity)
        {
                default:
                case 'N':
                case 'n':
                {
                        newtio.c_cflag &= ~PARENB;
                        newtio.c_iflag &= ~INPCK;
                }
                break;
                
                case 'o':
                case 'O':
                {
                        newtio.c_cflag |= (PARODD|PARENB);
                        newtio.c_iflag |= INPCK;
                }
                break;
                
                case 'e':
                case 'E':
                {
                        newtio.c_cflag |= PARENB;
                        newtio.c_cflag &= ~PARODD;
                        newtio.c_iflag |= INPCK;
                }
                break;
                
                
                case 's':
                case 'S':
                {
                        newtio.c_cflag &= ~PARENB;
                        newtio.c_cflag &= ~CSTOPB;
                        
                }
                break;
        }
        //Set STOPBITS 1 or 2
        switch(stop_bits)
        {
           default:
           case 1:{
                        newtio.c_cflag &= ~CSTOPB;
           }
           		break;
           case 2:
                {
                        newtio.c_cflag |= CSTOPB;
                }
                break;    
        }
        newtio.c_cc[VTIME]  = 1;
        newtio.c_cc[VMIN]  = 255;  //Read Comport Buffer when the bytes in Buffer is more than VMIN bytes!
        tcflush(fd,TCIFLUSH);
        if(( tcsetattr(fd,TCSANOW,&newtio))!=0 ){
                perror("Com set error");
                return -1;
        }
        //fprintf(stdout,"The Fucntion Set_Port() End!\n");
        return 0;
}
void read_port(int fd,char *msg,int count)
{
        fd_set rd;
        int nread,retval;
 
        struct timeval timeout;
        FD_ZERO(&rd);
        FD_SET(fd,&rd);
        timeout.tv_sec = 1;
        timeout.tv_usec = 0;
        while(IsReceve == 1);
        retval = select(fd+1,&rd,NULL,NULL,&timeout);
        switch(retval)
        {
        case 0:
                //printf("No data input within 1 seconds.\n");
                break;
        case -1:
                perror("select:");
                break;
        default:
                if( (nread = read(fd,msg,count))>0 )
                        IsReceve =1;
                break;                
        }//end of switch

}
void close_port(int fd){
	close(fd);
}



