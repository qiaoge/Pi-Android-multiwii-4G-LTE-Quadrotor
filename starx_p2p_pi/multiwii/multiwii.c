#include <stdlib.h>
#include <string.h>
#include "multiwii.h"
/************************************** MultiWii Serial Protocol *******************************************************/
// Multiwii Serial Protocol 0 
#define MSP_VERSION              0

//to multiwii developpers/committers : do not add new MSP messages without a proper argumentation/agreement on the forum
//range id [50-99] won't be assigned and can therefore be used for any custom multiwii fork without further MSP id conflict

#define MSP_PRIVATE              1     //in+out message      to be used for a generic framework : MSP + function code (LIST/GET/SET) + data. no code yet

#define MSP_IDENT                100   //out message         multitype + multiwii version + protocol version + capability variable
#define MSP_STATUS               101   //out message         cycletime & errors_count & sensor present & box activation & current setting number
#define MSP_RAW_IMU              102   //out message         9 DOF
#define MSP_SERVO                103   //out message         8 servos
#define MSP_MOTOR                104   //out message         8 motors
#define MSP_RC                   105   //out message         8 rc chan and more
#define MSP_RAW_GPS              106   //out message         fix, numsat, lat, lon, alt, speed, ground course
#define MSP_COMP_GPS             107   //out message         distance home, direction home
#define MSP_ATTITUDE             108   //out message         2 angles 1 heading
#define MSP_ALTITUDE             109   //out message         altitude, variometer
#define MSP_ANALOG               110   //out message         vbat, powermetersum, rssi if available on RX
#define MSP_RC_TUNING            111   //out message         rc rate, rc expo, rollpitch rate, yaw rate, dyn throttle PID
#define MSP_PID                  112   //out message         P I D coeff (9 are used currently)
#define MSP_BOX                  113   //out message         BOX setup (number is dependant of your setup)
#define MSP_MISC                 114   //out message         powermeter trig
#define MSP_MOTOR_PINS           115   //out message         which pins are in use for motors & servos, for GUI 
#define MSP_BOXNAMES             116   //out message         the aux switch names
#define MSP_PIDNAMES             117   //out message         the PID names
#define MSP_WP                   118   //out message         get a WP, WP# is in the payload, returns (WP#, lat, lon, alt, flags) WP#0-home, WP#16-poshold
#define MSP_BOXIDS               119   //out message         get the permanent IDs associated to BOXes
#define MSP_SERVO_CONF           120   //out message         Servo settings

#define MSP_NAV_STATUS           121   //out message         Returns navigation status
#define MSP_NAV_CONFIG           122   //out message         Returns navigation parameters

#define MSP_CELLS                130   //out message         FRSKY Battery Cell Voltages

#define MSP_SET_RAW_RC           200   //in message          8 rc chan
#define MSP_SET_RAW_GPS          201   //in message          fix, numsat, lat, lon, alt, speed
#define MSP_SET_PID              202   //in message          P I D coeff (9 are used currently)
#define MSP_SET_BOX              203   //in message          BOX setup (number is dependant of your setup)
#define MSP_SET_RC_TUNING        204   //in message          rc rate, rc expo, rollpitch rate, yaw rate, dyn throttle PID
#define MSP_ACC_CALIBRATION      205   //in message          no param
#define MSP_MAG_CALIBRATION      206   //in message          no param
#define MSP_SET_MISC             207   //in message          powermeter trig + 8 free for future use
#define MSP_RESET_CONF           208   //in message          no param
#define MSP_SET_WP               209   //in message          sets a given WP (WP#,lat, lon, alt, flags)
#define MSP_SELECT_SETTING       210   //in message          Select Setting Number (0-2)
#define MSP_SET_HEAD             211   //in message          define a new heading hold direction
#define MSP_SET_SERVO_CONF       212   //in message          Servo settings
#define MSP_SET_MOTOR            214   //in message          PropBalance function
#define MSP_SET_NAV_CONFIG       215   //in message          Sets nav config parameters - write to the eeprom  

#define MSP_SET_ACC_TRIM         239   //in message          set acc angle trim values
#define MSP_ACC_TRIM             240   //out message         get acc angle trim values
#define MSP_BIND                 241   //in message          no param

#define MSP_EEPROM_WRITE         250   //in message          no param

#define MSP_DEBUGMSG             253   //out message         debug string buffer
#define MSP_DEBUG                254   //out message           debug1,debug2,debug3,debug4

typedef char uint8_t;
typedef unsigned int  uint16_t;


void gps_mission_add(struct aircraft_mission_state *ams,char *m){
    struct gps_destination *last;
    last=ams->gd;
    while(last!=NULL){
        last=last->next;
    }
    last=(struct gps_destination*)malloc(sizeof(struct gps_destination));
    strcpy(last->gpsMsg , m);
}
void gps_mission_remove(struct aircraft_mission_state *ams){
    struct gps_destination *head,*n;
    head=ams->gd;
    while(head!=NULL){
        n=head->next;
        free(head);
        head=n;
    }
}

void read_multiwii(struct aircraft_mission_state *ams,int serial,char *m){
    char msg[256]={0};
    int len;
    read_port(serial,msg,len=strlen(msg));
    if(len>0){
        if(msg[0]=='$' && msg[1]=='M' && msg[2]=='>'){
            evaluateCommand(ams,msg);
        }
        m=malloc(sizeof(msg));
        strcpy(m,msg);
    }
}    

void evaluateCommand(struct aircraft_mission_state *ams,char *str) {
    char checksum=0;
    int i;
    struct {
        uint8_t a,b;
        int32_t c,d;
        int16_t e;
        uint16_t f,g;
      } *set_raw_gps;
      
    for(i=0;i<=str[3];i++){
        checksum^=str[i+3];
    }
    if(str[4+str[3]+1]==checksum){
        switch((int)str[4]) {
            case MSP_RAW_GPS:
                 set_raw_gps=str;
                 ams->lat= set_raw_gps->c;
                 ams->lon= set_raw_gps->d;
                 ams->altitude=set_raw_gps->e;
                 ams->speed=set_raw_gps->f;
                 ams->ground_course=set_raw_gps->g;
            break;
            default :
            break;
        }
    }
}

