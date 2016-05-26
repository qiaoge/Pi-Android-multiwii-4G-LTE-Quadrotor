#CC = arm-none-linux-gnueabi-gcc
#CPP = arm-none-linux-gnueabi-g++
#STRIP = arm-none-linux-gnueabi-strip
#AR = arm-none-linux-gnueabi-ar
CC = arm-bcm2708hardfp-linux-gnueabi-gcc
CPP = arm-bcm2708hardfp-linux-gnueabi-g++
STRIP = arm-bcm2708hardfp-linux-gnueabi-strip
AR = arm-bcm2708hardfp-linux-gnueabi-ar


SRCDIRS     =./ \
		./include \
		./include/STARX \
		./serialport \
		./multiwii \
		./wiringPi \
		./encode \
		./encode/pi2GPUlib \
		./encode/pi2GPUlib/interface/vcos \
		./encode/pi2GPUlib/interface/vcos/generic \
		./encode/pi2GPUlib/interface/mmal \
		./encode/pi2GPUlib/interface/mmal/core \
		./encode/pi2GPUlib/interface/mmal/util \
		./encode/pi2GPUlib/interface/vcos/pthreads \
		./encode/pi2GPUlib/interface/vmcs_host \
		./encode/pi2GPUlib/interface/vmcs_host/linux \
		./encode/pi2GPUlib/interface/vchiq_arm 

INCLUDES = $(foreach dir,$(SRCDIRS),-I$(dir))
SRCCS    = $(foreach dir,$(SRCDIRS),$(wildcard $(dir)/*.c))
OBJ  	 = $(SRCCS:%.c=%.o)

			  
#CFLAGS = -Wall -O0 -g -static
CFLAGS =  -static
CFLAGS+=-DSTANDALONE -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS -DTARGET_POSIX -D_LINUX -fPIC -DPIC -D_REENTRANT -D_LARGEFILE64_SOURCE -D_FILE_OFFSET_BITS=64 -U_FORTIFY_SOURCE -Wall -g -DHAVE_LIBOPENMAX=2 -DOMX -DOMX_SKIP64BIT -ftree-vectorize -pipe -DUSE_EXTERNAL_OMX -DHAVE_LIBBCM_HOST -DUSE_EXTERNAL_LIBBCM_HOST -DUSE_VCHIQ_ARM -Wno-psabi

CFLAGS := $(CFLAGS) $(INCLUDES)

CFLAGS += -D__OS_LINUX

LIBS += -pthread -ldl -L. ./encode/libv4lconvert.a -lm -lrt ./lib/BCM2835/libPPPP_API.a 

TARGET = p2pWebcam

all: $(TARGET)

$(TARGET): $(OBJ)
	$(CPP) $(CFLAGS) -o $@ $^ $(LIBS) 

#	mv enc_dec_test test
clean:
	@rm -f $(TARGET)
	@rm -f $(OBJ)
