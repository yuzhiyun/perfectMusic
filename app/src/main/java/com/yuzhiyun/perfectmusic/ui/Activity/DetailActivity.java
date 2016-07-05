package com.yuzhiyun.perfectmusic.ui.Activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.yuzhiyun.perfectmusic.Application.App;
import com.yuzhiyun.perfectmusic.R;
import com.yuzhiyun.perfectmusic.service.PlayService;
import com.yuzhiyun.perfectmusic.ui.customView.CDView;
import com.yuzhiyun.perfectmusic.ui.customView.LrcView;
import com.yuzhiyun.perfectmusic.util.Constant;
import com.yuzhiyun.perfectmusic.util.FormatHelper;
import com.yuzhiyun.perfectmusic.util.ImageTools;
import com.yuzhiyun.perfectmusic.util.MusicLoader;
import com.yuzhiyun.perfectmusic.service.PlayService.myBinder;

public class DetailActivity extends Activity implements OnClickListener {

    private static final String TAG = "com.example.natrue.DetailActivity";

    public static final String MUSIC_LENGTH = "com.example.nature.DetailActivity.MUSIC_LENGTH";
    public static final String CURRENT_POSITION = "com.example.nature.DetailActivity.CURRENT_POSITION";
    public static final String CURRENT_MUSIC = "com.example.nature.DetailActivity.CURRENT_MUSIC";
    private ViewPager mViewPager;
    //ViewPager内容
    private ArrayList<View> mViewPagerContent = new ArrayList<View>(2);

    private SeekBar pbDuration;
    private TextView tvTitle, tvTimeElapsed, tvDuration;
    private List<MusicLoader.MusicInfo> musicList;
    private int currentMusic = 0;

    private int currentPosition = 0;

    private ProgressReceiver progressReceiver;

    private myBinder natureBinder;
    //歌词view
    public TextView text;
    //圆形图片
    private CDView mCdView;
    public static LrcView lrcView;
    public static LrcView single_lrc_view;
    Bitmap bmp;
    public ImageButton imgBtnPlayPre;
    public ImageButton imgBtnPlayStart;
    public ImageButton imgBtnPlayNext;
    public ImageView imgBack;
//	private int[] btnResIds = new int[] {
//			R.id.btnMode,
//			R.id.btnPrevious, 
//			R.id.btnStartStop, 			
//			R.id.btnNext,
//			R.id.btnExit 
//	};

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            natureBinder = (myBinder) service;
            //初始化歌词
            natureBinder.BinderInitLrc();
            Log.i("", "这里是BinderInitLrc");
            /**
             * 由日志得知此处代码运行顺序在onResume之后
             * 那么初始化歌词natureBinder.BinderInitLrc();代码如果在获取控件之后立刻执行的话，就会出错，
             * 因为还没有成功连接上Service
             * */
            //显示歌手图片
            disPlay();


            /**值得考虑下为什么在这里换背景*/
            //设置imgBtnPlayStart背景图
            if (natureBinder.isPlaying()) {
                imgBtnPlayStart.setImageResource(R.drawable.player_btn_pause_normal);
            } else {
                imgBtnPlayStart.setImageResource(R.drawable.player_btn_play_normal);
            }

//			CustomAudioIcon btnMode = (CustomAudioIcon)findViewById(R.id.btnMode);
//			btnMode.setCurrentMode(natureBinder.getCurrentMode());
        }
    };

    private void connectToNatureService() {
        Intent intent = new Intent(DetailActivity.this, PlayService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
//		Log.v(TAG, "OnCreate");
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_right_in, R.anim.hold);
        MusicLoader musicLoader = new MusicLoader();
        musicList = musicLoader.getMusicList();
        setContentView(R.layout.detail_layout);

        connectToNatureService();
        //initLrc();
        initComponents();
    }

    private void initComponents() {
        //lrcView = (LrcView) findViewById(R.id.lrcShowView);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        mViewPager = (ViewPager) findViewById(R.id.vp_play_container);

        /**初始化ViewPager内容*/
        initViewPagerContent();
        //设置适配器
        mViewPager.setAdapter(mPagerAdapter);
        try {
            currentMusic = getIntent().getIntExtra(CURRENT_MUSIC, 0);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tvTitle.setText(musicList.get(currentMusic).getTitle());

        tvDuration = (TextView) findViewById(R.id.tvDuration);
        int max = getIntent().getIntExtra(MUSIC_LENGTH, 0);
        tvDuration.setText(FormatHelper.formatDuration(max));

        pbDuration = (SeekBar) findViewById(R.id.pbDuration);
        pbDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    natureBinder.changeProgress(progress);
                }
            }
        });
        pbDuration.setMax(max / 1000);
        //用户没有选择音乐的时候默认第一首歌，所以无法通过getIntent来获取currentPosition
        try {
            currentPosition = getIntent().getIntExtra(CURRENT_POSITION, 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        pbDuration.setProgress(currentPosition / 1000);

        tvTimeElapsed = (TextView) findViewById(R.id.tvTimeElapsed);
        tvTimeElapsed.setText(FormatHelper.formatDuration(currentPosition));
//				
//		for(int resId : btnResIds){
//			CustomAudioIcon icon = (CustomAudioIcon)findViewById(resId);
//			icon.setOnClickListener(this);
//		}		

        imgBtnPlayPre = (ImageButton) findViewById(R.id.play_pre);
        imgBtnPlayStart = (ImageButton) findViewById(R.id.play_StartStop);
        imgBtnPlayNext = (ImageButton) findViewById(R.id.play_next);
        imgBack = (ImageView) findViewById(R.id.iv_play_back);

        imgBtnPlayPre.setOnClickListener(this);
        imgBtnPlayStart.setOnClickListener(this);
        imgBtnPlayNext.setOnClickListener(this);
        imgBack.setOnClickListener(this);
    }

    @Override
    public void onResume() {
//		Log.e("detail", "onResume  initReceiver");
        super.onResume();
        initReceiver();
    }

    @Override
    public void onPause() {
//		Log.e(TAG, "onPause");
//		Log.v(TAG, "OnPause unregister progress receiver");
        super.onPause();
        unregisterReceiver(progressReceiver);
        overridePendingTransition(R.anim.hold, R.anim.push_right_out);
    }

    public void onDestroy() {

//		Log.e(TAG, "Destroy");
        super.onDestroy();
        if (natureBinder != null) {
            unbindService(serviceConnection);
        }

        bmp = null;
    }

    /**
     * 初始化viewpager的内容
     */
    private void initViewPagerContent() {
        View view1 = View.inflate(this, R.layout.detail_pager_item_1, null);
        lrcView = (LrcView) view1.findViewById(R.id.lrcShowView);
        Log.i("", "这里是lrcView的findViewById");
        View view2 = View.inflate(this, R.layout.detail_pager_item_2, null);
		mCdView = (CDView) view2.findViewById(R.id.play_cdview);
        single_lrc_view = (LrcView) view2.findViewById(R.id.single_lrc_view);
        //添加ViewPager内容
        mViewPagerContent.add(view2);
        mViewPagerContent.add(view1);
    }

    //显示歌手图片
    private void disPlay() {
        // TODO Auto-generated method stub


        Bitmap bmp = null;
        bmp = BitmapFactory.decodeResource(getResources(), Constant.array_artist[currentMusic]);
        //ImageTools.scaleBitmap把图片放大
        mCdView.setImage(ImageTools.scaleBitmap(bmp, (int) (App.sScreenWidth * 0.4)));
//        mCdView.setImage(bmp);


        if (natureBinder.isPlaying()) {
            mCdView.start();
//			imgBtnPlayStart.setImageResource(R.drawable.player_btn_pause_normal);
        } else {
            mCdView.pause();
//			imgBtnPlayStart.setImageResource(R.drawable.player_btn_play_normal);
        }
    }

    /**
     * viewPager适配器
     */
    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mViewPagerContent.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewPagerContent.get(position));
            return mViewPagerContent.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    };


    private void initReceiver() {
        progressReceiver = new ProgressReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayService.ACTION_UPDATE_PROGRESS);
        intentFilter.addAction(PlayService.ACTION_UPDATE_DURATION);
        intentFilter.addAction(PlayService.ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE);
        registerReceiver(progressReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.play_StartStop:
//			Toast.makeText(this, "btnStartStop", Toast.LENGTH_SHORT).show();
                if (natureBinder.isPlaying()) {
                    natureBinder.stopPlay();
//					mCdView.pause();
                    imgBtnPlayStart.setImageResource(R.drawable.player_btn_play_normal);
//				使用setBackgroundResource会出现图像重合问题，就像framlayout
                } else {
                    natureBinder.startPlay(currentMusic, currentPosition);
                    imgBtnPlayStart.setImageResource(R.drawable.player_btn_pause_normal);
//					mCdView.start();
                }
                break;
            case R.id.play_next:
                natureBinder.toNext();
                imgBtnPlayStart.setImageResource(R.drawable.player_btn_pause_normal);
                //初始化歌词
                natureBinder.BinderInitLrc();
                break;
            case R.id.play_pre:

                imgBtnPlayStart.setImageResource(R.drawable.player_btn_pause_normal);
                natureBinder.toPrevious();
                //初始化歌词
                natureBinder.BinderInitLrc();
                break;
            case R.id.iv_play_back:
                finish();
                break;
//		case R.id.btnMode:						
//			natureBinder.changeMode();
//			break;
            default:
                break;
        }
    }


    class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PlayService.ACTION_UPDATE_PROGRESS.equals(action)) {
                int progress = intent.getIntExtra(PlayService.ACTION_UPDATE_PROGRESS, currentPosition);
                if (progress > 0) {
                    currentPosition = progress; // Remember the current position
                    tvTimeElapsed.setText(FormatHelper.formatDuration(progress));
                    pbDuration.setProgress(progress / 1000);
                }
            } else if (PlayService.ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE.equals(action)) {
                //切换歌手图片
                disPlay();
                //Retrieve the current music and get the title to show on top of the screen.
                currentMusic = intent.getIntExtra(PlayService.ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE, 0);
                tvTitle.setText(musicList.get(currentMusic).getTitle());
            } else if (PlayService.ACTION_UPDATE_DURATION.equals(action)) {
                //Receive the duration and show under the progress bar
                //Why do this ? because from the ContentResolver, the duration is zero.
                int duration = intent.getIntExtra(PlayService.ACTION_UPDATE_DURATION, 0);
                tvDuration.setText(FormatHelper.formatDuration(duration));
                pbDuration.setMax(duration / 1000);
            }
        }
    }
}