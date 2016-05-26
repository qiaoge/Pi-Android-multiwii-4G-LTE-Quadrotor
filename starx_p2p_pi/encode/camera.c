#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include "libv4lconvert.h"
#include "picture_t.h"
#include "log.h"
#include "simplerecorder.h"

#define NUM_BUF  3

static struct v4lconvert_data *lib;
static struct v4l2_requestbuffers reqbuf;
static struct v4l2_buffer buffers[NUM_BUF];
static int  *buf_pointer[NUM_BUF], YUV420_size;
static struct picture_t current_pic;
struct v4l2_format src_fmt, dst_fmt;

int fd_cam;
static int get_format()
{
	char pixel[5];
	static struct v4l2_format fmt = {0};

	struct v4l2_fmtdesc fmtdesc; 
	fmtdesc.index=0; 
	fmtdesc.type=V4L2_BUF_TYPE_VIDEO_CAPTURE; 
	printf("Support format:\n");

	while(ioctl(fd_cam, VIDIOC_ENUM_FMT, &fmtdesc) != -1)
	{
		printf("\t%d.%s\n",fmtdesc.index+1,fmtdesc.description);
		fmtdesc.index++;
	}
	
	fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	if(ioctl(fd_cam, VIDIOC_G_FMT, &fmt) < 0) {
		perror("VIDIOC_G_FMT");
		return 0;
	}

	*(int*)pixel = fmt.fmt.pix.pixelformat;
	pixel[4] = '\0';

	printf("input format: %s %dx%d %d\n", pixel, fmt.fmt.pix.width, fmt.fmt.pix.height, fmt.fmt.pix.sizeimage);

	current_pic.width = fmt.fmt.pix.width;
	current_pic.height = fmt.fmt.pix.height;
	current_pic.bitrate = fmt.fmt.pix.sizeimage;

	src_fmt = fmt;
	dst_fmt = fmt;
	dst_fmt.fmt.pix.pixelformat = V4L2_PIX_FMT_YUV420;
	return 1;
}
static int buf_alloc_mmap()
{
	int i;

	reqbuf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	reqbuf.memory = V4L2_MEMORY_MMAP;
	reqbuf.count = NUM_BUF;
	if(ioctl(fd_cam, VIDIOC_REQBUFS, &reqbuf) < 0) {
		perror("VIDIOC_REQBUFS");
		return 0;
	}
	printf("%d camera buffers\n", reqbuf.count);
	for(i=0; i<reqbuf.count; i++) {
		
		buffers[i].index = i;
		buffers[i].type = reqbuf.type;
		buffers[i].memory = V4L2_MEMORY_MMAP;
		if(ioctl(fd_cam, VIDIOC_QUERYBUF, &buffers[i]) < 0) {
			perror("VIDIOC_QUERYBUF");
			return 0;
		}
		if(MAP_FAILED == (buf_pointer[i] = mmap(0, buffers[i].length, PROT_READ|PROT_WRITE, MAP_SHARED, fd_cam, buffers[i].m.offset))) {
			perror("mmap camera buffer");
			return 0;
		}
		
		if(ioctl(fd_cam, VIDIOC_QBUF, &buffers[i]) < 0) {
			perror("VIDIOC_QBUF");
			return 0;
		}
	}
	return 1;
}
static void free_buf_mmap()
{
	int i;
	for(i=0; i<reqbuf.count; i++)
		munmap(buf_pointer[i], buffers[i].length);
}
static int set_para(int set_width,int set_height)
{
	int input = 0, width, height;
	static struct v4l2_format format = {0};

	ioctl (fd_cam, VIDIOC_S_INPUT, &input);

	format.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	if(ioctl(fd_cam, VIDIOC_G_FMT, &format) < 0) {
		perror("VIDIOC_G_FMT");
		return 0;
	}
	width = format.fmt.pix.width;
	height = format.fmt.pix.height;

	memset(&format, 0, sizeof(format));

	format.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

	format.fmt.pix.width       = width;
	format.fmt.pix.height      = height;
	if((set_width>0)&&(set_height>0)){
		format.fmt.pix.width       = set_width;
		format.fmt.pix.height      = set_height;
	}
	format.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV;//V4L2_PIX_FMT_UYVY;
	format.fmt.pix.field       = V4L2_FIELD_INTERLACED;
	ioctl (fd_cam, VIDIOC_S_FMT, &format);

	return 1;
}
static int set_parm(int fps){
	struct v4l2_streamparm sparm ={0};
	sparm.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	sparm.parm.capture.capturemode = V4L2_MODE_HIGHQUALITY;
	sparm.parm.capture.capability = V4L2_CAP_TIMEPERFRAME;
	sparm.parm.capture.timeperframe.denominator = fps;
	sparm.parm.capture.timeperframe.numerator =1;
	if(ioctl(fd_cam, VIDIOC_S_PARM, &sparm) < 0) {
		perror("VIDIOC_S_PARM");
		return 0;
	}
	return 1;
}
int camera_init(struct picture_t *out_info,char *cam_name,int fps)
{
	fd_cam = open(cam_name, O_RDWR);
	if(fd_cam < 0){
		perror("open camera ");
		return 0;
	}
	if(!set_para(out_info->width,out_info->height))
		goto label_close;

	//============================
	if(!set_parm(fps))	
		goto label_close;
	//===========================

	if(!get_format())
		goto label_close;

	lib = v4lconvert_create(fd_cam);
	if(!lib) {
		perror("v4lconvert_create");
		goto label_close;
	}

	if(!buf_alloc_mmap())
		goto label_free;
	YUV420_size = current_pic.width*current_pic.height*3/2;
	if(!(current_pic.buffer = malloc(YUV420_size))){
		perror("malloc");
		goto label_free;
	}
	*out_info = current_pic;
	return 1;

label_free:
	free_buf_mmap();
	v4lconvert_destroy(lib);
label_close:
	close(fd_cam);
	return 0;
}
int camera_on()
{
	if(ioctl(fd_cam, VIDIOC_STREAMON, &reqbuf.type) < 0) {
		perror("VIDIOC_STREAMON");
		return 0;
	}
	return 1;
}
int camera_get_frame(struct picture_t *pic)
{
	struct v4l2_buffer cam_buf = {0};
	cam_buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
	cam_buf.memory = V4L2_MEMORY_MMAP;
	if(ioctl(fd_cam, VIDIOC_DQBUF, &cam_buf) < 0) {
		perror("VIDIOC_DBUF");
		return 0;
	}
	//applog("buf_index=%02d, seq=%d, timestamp=%d%06d", 
	//	cam_buf.index, cam_buf.sequence, (int)cam_buf.timestamp.tv_sec, (int)cam_buf.timestamp.tv_usec);
	if(v4lconvert_convert(lib, &src_fmt, &dst_fmt, (void*)buf_pointer[cam_buf.index], 
		cam_buf.length, current_pic.buffer, YUV420_size) <= 0){
		perror("v4lconvert_convert");
		return 0;
	}
	current_pic.timestamp = cam_buf.timestamp;
	cam_buf.flags = cam_buf.reserved = 0;
	
	if(ioctl(fd_cam, VIDIOC_QBUF, &cam_buf) < 0) {
		perror("VIDIOC_QBUF");
		return 0;
	}
	*pic = current_pic;
	return 1;
}
int camera_off()
{
	if(ioctl(fd_cam, VIDIOC_STREAMOFF, &reqbuf.type) < 0) {
		perror("VIDIOC_STREAMOFF");
		return 0;
	}
	return 1;
}
void camera_close()
{
	free(current_pic.buffer);
	free_buf_mmap();
	v4lconvert_destroy(lib);
	close(fd_cam);
}
