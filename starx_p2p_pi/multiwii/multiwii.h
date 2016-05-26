static  int MSP_IDENT                =100;   //out message         multitype + multiwii version + protocol version + capability variable
static  int MSP_STATUS               =101;   //out message         cycletime & errors_count & sensor present & box activation & current setting number
static  int MSP_RAW_IMU              =102;   //out message         9 DOF
static  int MSP_SERVO                =103;   //out message         8 servos
static  int MSP_MOTOR                =104;   //out message         8 motors
static  int MSP_RC                   =105;   //out message         8 rc chan and more
static  int MSP_RAW_GPS              =106;   //out message         fix, numsat, lat, lon, alt, speed, ground course
static  int MSP_COMP_GPS             =107;   //out message         distance home, direction home
static  int MSP_ATTITUDE             =108;   //out message         2 angles 1 heading
static  int MSP_ALTITUDE             =109;   //out message         altitude, variometer
static  int MSP_ANALOG               =110;   //out message         vbat, powermetersum, rssi if available on RX
static  int MSP_RC_TUNING            =111;   //out message         rc rate, rc expo, rollpitch rate, yaw rate, dyn throttle PID
static  int MSP_PID                  =112;   //out message         P I D coeff (9 are used currently)
static  int MSP_BOX                  =113;   //out message         BOX setup (number is dependant of your setup)
static  int MSP_MISC                 =114;   //out message         powermeter trig
static  int MSP_MOTOR_PINS           =115;   //out message         which pins are in use for motors & servos, for GUI 
static  int MSP_BOXNAMES             =116;   //out message         the aux switch names
static  int MSP_PIDNAMES             =117;   //out message         the PID names
static  int MSP_WP                   =118;   //out message         get a WP, WP# is in the payload, returns (WP#, lat, lon, alt, flags) WP#0-home, WP#16-poshold
static  int MSP_BOXIDS               =119;   //out message         get the permanent IDs associated to BOXes
static  int MSP_SERVO_CONF           =120;   //out message         Servo settings=====

static  int MSP_NAV_STATUS           =121;   //out message         Returns navigation status
static  int MSP_NAV_CONFIG           =122;   //out message         Returns navigation parameters

static  int MSP_CELLS                =130;   //out message         FRSKY Battery Cell Voltages

static  int MSP_SET_RAW_RC           =200;   //in message          8 rc chan
static  int MSP_SET_RAW_GPS          =201;   //in message          fix, numsat, lat, lon, alt, speed
static  int MSP_SET_PID              =202;   //in message          P I D coeff (9 are used currently)
static  int MSP_SET_BOX              =203;   //in message          BOX setup (number is dependant of your setup)
static  int MSP_SET_RC_TUNING        =204;   //in message          rc rate, rc expo, rollpitch rate, yaw rate, dyn throttle PID
static  int MSP_ACC_CALIBRATION      =205;   //in message          no param
static  int MSP_MAG_CALIBRATION      =206;   //in message          no param
static  int MSP_SET_MISC             =207;  //in message          powermeter trig + 8 free for future use
static  int MSP_RESET_CONF           =208;   //in message          no param
static  int MSP_SET_WP               =209;   //in message          sets a given WP (WP#,lat, lon, alt, flags)
static  int MSP_SELECT_SETTING       =210;   //in message          Select Setting Number (0-2)
static  int MSP_SET_HEAD             =211;   //in message          define a new heading hold direction
static  int MSP_SET_SERVO_CONF       =212;   //in message          Servo settings
static  int MSP_SET_MOTOR            =214;   //in message          PropBalance function
static  int MSP_SET_NAV_CONFIG       =215;   //in message          Sets nav config parameters - write to the eeprom  
static  int MSP_SET_ACC_TRIM         =239;   //in message          set acc angle trim values
static  int MSP_ACC_TRIM             =240;   //out message         get acc angle trim values
static  int MSP_BIND                 =241;   //in message          no param

static  int MSP_EEPROM_WRITE         =250;   //in message          no param

static  int MSP_DEBUGMSG             =253;   //out message         debug string buffer
static  int MSP_DEBUG                =254;   //out message         debug1,debug2,debug3,debug4

struct gps_destination{
    char gpsMsg[40];
    struct gps_destination *next;
};
struct aircraft_mission_state{
    struct gps_destination *gd;
	int gps_num;
	char numSat;
	long lat;
	long lon;
	float head;
	unsigned int speed;
	unsigned int altitude;
	unsigned int ground_course;  //degree*10
};
void gps_mission_add(struct aircraft_mission_state *ams,char *m);
void gps_mission_remove(struct aircraft_mission_state *ams);
void read_multiwii(struct aircraft_mission_state *ams,int serial,char *m);
void evaluateCommand(struct aircraft_mission_state *ams,char *str);


