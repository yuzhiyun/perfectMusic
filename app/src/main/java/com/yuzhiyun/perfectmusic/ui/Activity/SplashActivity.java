package com.yuzhiyun.perfectmusic.ui.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import com.yuzhiyun.perfectmusic.R;
import com.yuzhiyun.perfectmusic.db.DBservice;
import com.yuzhiyun.perfectmusic.util.MusicLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class SplashActivity extends Activity {
	private List<MusicLoader.MusicInfo> list;

	private int[] array = {R.raw.a,R.raw.b};
	private int[] array_lrc = {R.raw.aa,R.raw.bb};
	private static final int TIME = 3000;
	private static final int GO_HOME = 1000;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what)
			{
				case GO_HOME:
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
					finish();
					break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//注意设置全屏和无标题要在setContentView之前，不然会报错
		setContentView(R.layout.activity_splash);
		initSQ();
		DBservice dBservice = new DBservice();
		list = dBservice.getList_data();
		initSONGS();
		handler.sendEmptyMessageDelayed(GO_HOME, TIME);
		// 2s跳转到主界面
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				startActivity(new Intent(SplashActivity.this,
//						MainActivity.class));
//				finish();
//			}
//		}, 3000);

	}
	/**歌曲和歌词复制到对应目录*/
	private void initSONGS() {
		String SONGS_PATH = "/data/data/com.yuzhiyun.perfectmusic/songs/";
//		for(int i = 0;i < list.size();i++){
		//由于歌曲歌词文件和数据库的不匹配，导致list.size()大于array.length，会出现indexOutofArray
		for(int i = 0;i < array.length;i++){
			String SONGS_NAME = list.get(i).getUrl();
			if ((new File(SONGS_PATH,SONGS_NAME).exists()) == false)
			{
				File dir = new File(SONGS_PATH);
				if (!dir.exists())
				{
					dir.mkdir();
				}
				/**歌曲复制到改目录*/
				try {

					InputStream is = this.getResources().openRawResource(array[i]);
					FileOutputStream  os = new FileOutputStream(SONGS_PATH + SONGS_NAME);
					byte[] buffer = new byte[2014];
					int length;

					while ((length = is.read(buffer)) > 0)
					{
						os.write(buffer,0,length);
					}
					os.flush();
					os.close();
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		for(int i = 0;i < array.length;i++){
			String SONGS_NAME = list.get(i).getLrc();
			if ((new File(SONGS_PATH,SONGS_NAME).exists()) == false)
			{
				File dir = new File(SONGS_PATH);
				if (!dir.exists())
				{
					dir.mkdir();
				}
				/**歌词也复制到对应目录*/
				try {
                    /*InputStream is = this.getResources().openRawResource(
                            );*/
					InputStream is = this.getResources().openRawResource(array_lrc[i]);
					FileOutputStream  os = new FileOutputStream(SONGS_PATH + SONGS_NAME);
					byte[] buffer = new byte[2014];
					int length;

					while ((length = is.read(buffer)) > 0)
					{
						os.write(buffer,0,length);
					}
					os.flush();
					os.close();
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	/**数据库复制到对应目录*/
	private void initSQ() {
		String DB_PATH = "/data/data/com.yuzhiyun.perfectmusic/databases/";
		String DB_NAME = "music.db";

		if ((new File(DB_PATH,DB_NAME).exists()) == false)
		{
			File dir = new File(DB_PATH);
			if (!dir.exists())
			{
				dir.mkdir();
			}
			/**数据库复制到该目录*/
			try {
				InputStream is = getBaseContext().getAssets().open(DB_NAME);
				OutputStream os = new FileOutputStream(DB_PATH + DB_NAME);
				byte[] buffer = new byte[2014];
				int length;

				while ((length = is.read(buffer)) > 0)
				{
					os.write(buffer,0,length);
				}
				os.flush();
				os.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
