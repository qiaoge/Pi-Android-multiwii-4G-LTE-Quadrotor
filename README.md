# Pi-Android-multiwii-4G-LTE-Quadrotor
Android as controller, Pi as FPV sender and receiver through 4G LTE stick. Finally I use multiwii as coprocessor. 
Mark: 20160525 ~ 20160527 update 

Hardware:
 1. Pi 2 b+ 
 2. Pi camera
 3. Huawei E3372s-210 USB 4G LTE stick
 4. Multiwii SE V2.6 or 2.5
 5. Android phone
 6. Two unicom SIM car for E3372s-210 and phone
 7. Quadrotor and so on 
 
Set 4G stick on after boot
 sudo apt-get install usb_modeswitch
then:
 nano /etc/udev/rules.d/40-modemswitch.rules
add:
 ACTION=="add", SUBSYSTEM=="usb", ATTRS{idVendor}=="12d1", ATTRS{idProduct}=="14fe", RUN+="/usr/sbin/usb_modeswitch -v 12d1 -p 1506 -M '55534243123456780000000000000011062000000100000000000000000000'"
and then:
 ctrl+X save file and exit the file and reboot, then 4G network done. Don't forget 4G stick....

Here I don't want to introduce how to set Pi camera. Google please.

Set software on pi
 CP file starx_p2p_pi to " ~/ " 
 cd starx_p2p_pi
 nano config
 
 
