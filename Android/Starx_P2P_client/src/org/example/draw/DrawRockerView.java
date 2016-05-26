package org.example.draw;

import org.example.starx_p2p_client.ControlData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawRockerView extends SurfaceView implements SurfaceHolder.Callback,Runnable{
	private Thread th;
	private SurfaceHolder sfh;
	private Canvas canvas;
	private Paint paint;
	private boolean flag;
	public boolean stop=false;
	private int RockerCenterX;
	private int RockerCenterY;
	private int RockerCenterWide;
	private float SmallRockerCircleX;
	private float SmallRockerCircleY;
	private float SmallRockerCircleR;
	
	public DrawRockerView(Context context, AttributeSet attrs) {
		
		super(context,attrs);
		this.setKeepScreenOn(true);
		sfh = this.getHolder();
		sfh.addCallback(this);
		sfh.setFormat(PixelFormat.TRANSPARENT);
		setZOrderOnTop(true);
		
		paint = new Paint();
		paint.setAntiAlias(true);
		setFocusable(true);
		setFocusableInTouchMode(true);	
		canvas=sfh.lockCanvas();
	}
	public void stop(){
		stop=true;
		
	}
	public void start(){
		stop=false;
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//draw();
		initdraw();
		th = new Thread(this);
		flag = true;
		th.start();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, 
			int format, int width, int height) {
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		th.interrupt();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!stop){
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
				if (Math.abs(RockerCenterX - event.getX())>RockerCenterWide) {
					if(RockerCenterX < event.getX()) 
						SmallRockerCircleX=RockerCenterX+RockerCenterWide;
					else 
						SmallRockerCircleX=RockerCenterX-RockerCenterWide;
				} else {
					SmallRockerCircleX = (int) event.getX();
				}
				if (Math.abs(RockerCenterY - event.getY())>RockerCenterWide) {
					if(RockerCenterY < event.getY()) 
						SmallRockerCircleY=RockerCenterY+RockerCenterWide;
					else 
						SmallRockerCircleY=RockerCenterY-RockerCenterWide;
				} else {
					SmallRockerCircleY = (int) event.getY();
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				SmallRockerCircleX = RockerCenterX;
				SmallRockerCircleY = RockerCenterY;
			}
			ControlData.control_forward_back=
	
					(short)(ControlData.control_mid_data-(SmallRockerCircleY-RockerCenterY)/RockerCenterWide
							*(ControlData.control_max_data-ControlData.control_mid_data)
							);
							
			ControlData.control_move_left_right=
					(short)((SmallRockerCircleX-RockerCenterX)/RockerCenterWide
							*(ControlData.control_max_data-ControlData.control_mid_data)
							+ControlData.control_mid_data);
		}
		return true;
	}
	
	public void initdraw() {
		canvas = sfh.lockCanvas();
		RockerCenterX=canvas.getWidth()/2;
		RockerCenterY=canvas.getHeight()/2;
		RockerCenterWide=RockerCenterX*2/3;
		SmallRockerCircleX=RockerCenterX;
		SmallRockerCircleY=RockerCenterY;
		SmallRockerCircleR=RockerCenterWide/2;
		sfh.unlockCanvasAndPost(canvas);
	}
	
	public void draw() {
		try {
				canvas = sfh.lockCanvas();
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				paint.setColor(Color.GRAY);
				paint.setAlpha(50);
				//canvas.drawCircle(RockerCircleX, RockerCircleY, RockerCircleR, paint);
				canvas.drawRect(RockerCenterX-RockerCenterWide,RockerCenterX-RockerCenterWide,RockerCenterX+RockerCenterWide,RockerCenterX+RockerCenterWide, paint);
				paint.setColor(Color.RED);
				paint.setAlpha(180);
				canvas.drawCircle(SmallRockerCircleX, SmallRockerCircleY, SmallRockerCircleR, paint);
				if (canvas != null) sfh.unlockCanvasAndPost(canvas);
		} catch (Exception e) {
		}
	}
	@Override
	public void run() {
		while (flag) {
			if(!stop){
			draw();
				try {
					Thread.sleep(50);
				} catch (Exception ex) {
				}
			}else{
				try {
					Thread.sleep(1000);
				} catch (Exception ex) {
				}
			}
		}
	}
}
