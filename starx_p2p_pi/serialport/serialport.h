
int open_serialport(char *dev_name);

int set_port(int fd,int baud_rate,int data_bits,char parity,int stop_bits);
void read_port(int fd,char *msg,int count);

void close_port(int fd);


