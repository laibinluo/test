package com.fy.ballacce;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;

public class BallAcceActivity extends Activity {
	/** Called when the activity is first created. */

	MyView mAnimView = null;
	private Socket socket = null;
	private static final String IP = "192.168.52.205";
	private static final int port = 6666;
	private static byte[] bBuffer = new byte[20];
	private InetSocketAddress addr = new InetSocketAddress(IP, port); //����socket
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ȫ����ʾ����,��Щ����������setContentView()֮ǰ��ʾ
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// ǿ�ƺ���
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// ��ʾ�Զ����View
		mAnimView = new MyView(this);
		setContentView(mAnimView);
	}

	class MyView extends SurfaceView implements Callback, Runnable,
			SensorEventListener {

		/** ÿ50֡ˢ��һ����Ļ **/
		public static final int TIME_IN_FRAME = 50;
		/** ��Ϸ���� **/
		Paint mPaint = null;
		Paint mTextPaint = null;
		SurfaceHolder mSurfaceHolder = null;
		
		/** ������Ϸ����ѭ�� **/
		boolean mRunning = false;
		/** ��Ϸ���� **/
		Canvas mCanvas = null;
		/** ������Ϸѭ�� **/
		boolean mIsRunning = false;
		/** SensorManager������ **/
		private SensorManager mSensorMgr = null;
		Sensor mSensor = null;
		
		/** �ֻ���Ļ���� **/
		int mScreenWidth = 0;
		int mScreenHeight = 0;
		
		/** С����Դ�ļ�Խ������ **/
		private int mScreenBallWidth = 0;
		private int mScreenBallHeight = 0;
		
		/** ��Ϸ�����ļ� **/
		private Bitmap mbitmapBg;
		
		/** С����Դ�ļ� **/
		private Bitmap mbitmapBall;
		
		/** С�������λ�� **/
		private float mPosX = 200;
		private float mPosY = 0;
		
		/** ������ӦX�� Y�� Z�������ֵ **/
		private float mGX = 0;
		private float mGY = 0;
		private float mGZ = 0;

		public MyView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			/** ���õ�ǰViewӵ�п��ƽ��� **/
			this.setFocusable(true);
			/** ���õ�ǰViewӵ�д����¼� **/
			this.setFocusableInTouchMode(true);
			/** �õ�SurfaceHolder���� **/
			mSurfaceHolder = this.getHolder();
			/** ��mSurfaceHolder���ӵ�Callback�ص������� **/
			mSurfaceHolder.addCallback(this);
			/** �������� **/
			mCanvas = new Canvas();
			/** �������߻��� **/
			mPaint = new Paint();
			mPaint.setColor(Color.WHITE);
			/** ����С����Դ **/
			mbitmapBall = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.ball);
			/** ������Ϸ���� **/
			mbitmapBg = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.bg);
			/** �õ�SensorManager���� **/
			mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
			mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			// ע��listener�������������Ǽ��ľ�ȷ��
			// SENSOR_DELAY_FASTEST ������ ��Ϊ̫����û��Ҫʹ��
			// SENSOR_DELAY_GAME ��Ϸ������ʹ��
			// SENSOR_DELAY_NORMAL �����ٶ�
			// SENSOR_DELAY_UI �������ٶ�
			
			
			/*new Thread(new Runnable() {
				@Override
				public void run() {
					try {
				    	//socket = new Socket(IP, port);
						socket.connect(addr, 5000);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.v("BallAcce", e.getMessage());
					} 
					// TODO Auto-generated method stub
					
				}
			}).start();*/
			mSensorMgr.registerListener(this, mSensor,
					SensorManager.SENSOR_DELAY_GAME);
		}

		public void Draw() {
			/** ������Ϸ���� **/
			mCanvas.drawBitmap(mbitmapBg, 0, 0, mPaint);
			/** ����С�� **/
			mCanvas.drawBitmap(mbitmapBall, mPosX, mPosY, mPaint);
			/** X�� Y�� Z�������ֵ **/
			mCanvas.drawText("X������ֵ ��" + mGX, 0, 20, mPaint);
			mCanvas.drawText("Y������ֵ ��" + mGY, 0, 40, mPaint);
			mCanvas.drawText("Z������ֵ ��" + mGZ, 0, 60, mPaint);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

		public byte[] floatToByte(float x, float y, float z){
			byte [] b = new byte[20];
			int n = Float.floatToIntBits(x);
			int i = 0;
			for(i = 0; i < 4; i++){
				b[i] = (byte)(n >> (24 - i*8));
			}
			n = Float.floatToIntBits(y);
			for( ; i < 8; i++){
				b[i] = (byte)(n >> (24 - i*8));
			}
			n = Float.floatToIntBits(z);
			for( ; i < 12; i++){
				b[i] = (byte)(n >> (24 - i*8));
			}
			
			return b;
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			mGX = event.values[0];
			mGY = event.values[1];
			mGZ = event.values[2];
			
			/*try {	
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				byte [] byData = floatToByte(mGX, mGY, mGZ);
				dos.write(byData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			// �������2��Ϊ����С���ƶ��ĸ���
			mPosX -= mGX * 2;
			mPosY += mGY * 2;
			// ���С���Ƿ񳬳��߽�
			if (mPosX < 0) {
				mPosX = 0;
			} else if (mPosX > mScreenBallWidth) {
				mPosX = mScreenBallWidth;
			}
			if (mPosY < 0) {
				mPosY = 0;
			} else if (mPosY > mScreenBallHeight) {
				mPosY = mScreenBallHeight;
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (mIsRunning) {
				/** ȡ�ø�����Ϸ֮ǰ��ʱ�� **/
				long startTime = System.currentTimeMillis();
				/** ����������̰߳�ȫ�� **/
				synchronized (mSurfaceHolder) {
					/** �õ���ǰ���� Ȼ������ **/
					mCanvas = mSurfaceHolder.lockCanvas();
					Draw();
					/** ���ƽ����������ʾ����Ļ�� **/
					mSurfaceHolder.unlockCanvasAndPost(mCanvas);
				}
				/** ȡ�ø�����Ϸ������ʱ�� **/
				long endTime = System.currentTimeMillis();
				/** �������Ϸһ�θ��µĺ����� **/
				int diffTime = (int) (endTime - startTime);
				/** ȷ��ÿ�θ���ʱ��Ϊ50֡ **/
				while (diffTime <= TIME_IN_FRAME) {
					diffTime = (int) (System.currentTimeMillis() - startTime);
					/** �̵߳ȴ� **/
					Thread.yield();
				}
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			/** ��ʼ��Ϸ��ѭ���߳� **/
			mIsRunning = true;
			new Thread(this).start();
			/** �õ���ǰ��Ļ���� **/
			mScreenWidth = this.getWidth();
			mScreenHeight = this.getHeight();
			/** �õ�С��Խ������ **/
			mScreenBallWidth = mScreenWidth - mbitmapBall.getWidth();
			mScreenBallHeight = mScreenHeight - mbitmapBall.getHeight();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			mIsRunning = false;
		}

	}
}