
 
void setup() {
   Serial.begin(115200);         //使用9600速率进行串口通讯
}
 String dd="";
 int data[6];
void loop() {
 
  short a = analogRead(A0);    
  short b = analogRead(A1);    
  short c = analogRead(A2);    
  short d = analogRead(A3);    
  short e = analogRead(A4);    
  short f = analogRead(A5);    
  
  a=map(a,100,1000,1000,2200);
  b=map(b,0,1000,1000,2000);
  c=map(c,0,1000,1000,2000);
  d=map(d,0,1000,1000,2000);
  e=map(e,0,1000,1000,2000);
  f=map(f,0,1000,1000,2000);
  
  Serial.print('S');
  Serial.print(a);
  Serial.print(' ');
  Serial.print(b);
  Serial.print(' ');
  Serial.print(c);
  Serial.print(' ');
  Serial.print(d);
  Serial.print(' ');
  Serial.print(e);
  Serial.print(' ');
  Serial.print(f);
  Serial.println('E');
  //Serial.println(dd);
  delay(150);                           //等待2秒，控制刷新速度
}
