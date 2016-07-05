package com.yuzhiyun.perfectmusic.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.yuzhiyun.perfectmusic.ui.Activity.DetailActivity;
import com.yuzhiyun.perfectmusic.ui.customView.LrcView;
import com.yuzhiyun.perfectmusic.util.LrcContent;
import com.yuzhiyun.perfectmusic.util.LrcProcess;
import com.yuzhiyun.perfectmusic.util.MusicLoader;

import java.util.ArrayList;
import java.util.List;


public class PlayService extends Service {

    private static final String TAG = "NATURE_SERVICE";

    public static final String MUSICS = "com.example.nature.MUSIC_LIST";

    public static final String NATURE_SERVICE = "com.example.nature.NatureService";

    private MediaPlayer mediaPlayer;

    private boolean isPlaying = false;

    private List<MusicLoader.MusicInfo> musicList;

    private Binder natureBinder = new myBinder();

    private int currentMusic;
    private int currentPosition;

    //多行歌词view
    LrcView  lrcView;
    //一行当前歌词
    LrcView single_lrc_view;
    //歌词处理
    private LrcProcess mLrcProcess;
    //存放歌词列表对象
    private List<LrcContent> lrcList = new ArrayList<LrcContent>();
    //歌词检索值
    private int index = 0;


    private static final int updateProgress = 1;
    private static final int updateCurrentMusic = 2;
    private static final int updateDuration = 3;
    private static final int updateImage = 4;

    public static final String ACTION_UPDATE_PROGRESS = "com.example.nature.UPDATE_PROGRESS";
    public static final String ACTION_UPDATE_DURATION = "com.example.nature.UPDATE_DURATION";
    public static final String ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE = "com.example.nature.UPDATE_CURRENT_MUSIC";

    private int currentMode = 3; //default sequence playing

    public static final String[] MODE_DESC = {"Single Loop", "List Loop", "Random", "Sequence"};

    public static final int MODE_ONE_LOOP = 0;
    public static final int MODE_ALL_LOOP = 1;
    public static final int MODE_RANDOM = 2;
    public static final int MODE_SEQUENCE = 3;

    private Notification notification;
    //���ʹ��
    private Handler LrcHandler	;
    //���¸�����ʱ��������
    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            switch(msg.what){
                case updateProgress:
                    toUpdateProgress();
                    break;
                case updateDuration:
                    toUpdateDuration();
                    break;
                case updateCurrentMusic:
                    toUpdateCurrentMusic();
                    break;
            }
        }
    };

    private void toUpdateProgress(){
        if(mediaPlayer != null && isPlaying){
            int progress = mediaPlayer.getCurrentPosition();
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_PROGRESS);
            intent.putExtra(ACTION_UPDATE_PROGRESS,progress);
            sendBroadcast(intent);
            //����ᵼ��һ��ѭ����ʹ��ÿ1��ͻ���½�����
            handler.sendEmptyMessageDelayed(updateProgress, 1000);

        }
    }
    /**
     * ��ʼ���������
     */
//    public void initLrc(){
//    	//���и��
//    	lrcView=DetailActivity.lrcView;
//    	//һ�е�ǰ���
//    	single_lrc_view=DetailActivity.single_lrc_view;
//
//        mLrcProcess = new LrcProcess();
//        //��ȡ����ļ�
//        mLrcProcess.readLRC(musicList.get(currentMusic).getUrl());
//        //���ش����ĸ���ļ�
//        lrcList = mLrcProcess.getLrcList();
//
//        lrcView.setmLrcList(lrcList);
//        single_lrc_view.setmLrcList(lrcList);
//        //�л���������ʾ���
////        lrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this,R.anim.alpha_z));
//        LrcHandler =new Handler();
//        LrcHandler.post(mRunnable);
////    }
//    Runnable mRunnable = new Runnable() {
//
//        @Override
//        public void run() {
//        	//���� һ��booleanֵ���ж��Ƿ����
//            lrcView.setIndex(lrcIndex(lrcList ),true);
//            single_lrc_view.setIndex(lrcIndex(lrcList ),false);
//            //�ػ�
//            lrcView.invalidate();
//            single_lrc_view.invalidate();
//            LrcHandler.postDelayed(mRunnable, 100);
//        }
//    };
//    /**
//     * ����ʱ���ȡ�����ʾ������ֵ
//     * @return
//     */
////    public  int lrcIndex(List<LrcContent> lrcList ) {
////    	int duration = 0 ;
////    	//�Ҳ������ﵼ����ͣ��ʱ��Index=0
////        try {
////			if(mediaPlayer.isPlaying()) {
////				currentPosition = mediaPlayer.getCurrentPosition();
////			    duration = mediaPlayer.getDuration();
////			}else
////				//��ͣ��ʱ��
////				return index;
////		} catch (Exception e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////        /**********************************************************************************************************/
////        if(currentPosition < duration) {
////            for (int i = 0; i < lrcList.size(); i++) {
////                if (i < lrcList.size() - 1) {
////                    if (currentPosition < lrcList.get(i).getLrcTime() && i == 0) {
////                        index = i;
////                    }
////                    if (currentPosition > lrcList.get(i).getLrcTime()
////                            && currentPosition < lrcList.get(i + 1).getLrcTime()) {
////                        index = i;
////                    }
////                }
////                if (i == lrcList.size() - 1
////                        && currentPosition > lrcList.get(i).getLrcTime()) {
////                    index = i;
////                }
////            }
////        }
////        return index;
////    }
    private void toUpdateDuration(){
        if(mediaPlayer != null){
            int duration = mediaPlayer.getDuration();
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_DURATION);
            intent.putExtra(ACTION_UPDATE_DURATION,duration);
            sendBroadcast(intent);
        }
    }

    private void toUpdateCurrentMusic(){
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE);
        intent.putExtra(ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE,currentMusic);
        sendBroadcast(intent);
    }

    public void onCreate(){
        initMediaPlayer();

        MusicLoader musicLoader = new MusicLoader();
        musicList = musicLoader.getMusicList();
        Log.v(TAG, "OnCreate");
        super.onCreate();

//		Intent intent = new Intent(this, SongsActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

//		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//		notification = new Notification.Builder(this)
//					.setTicker("Nature")
//					.setSmallIcon(R.drawable.music_app)
//					.setContentTitle("Playing")
//					.setContentText(musicList.get(currentMusic).getTitle())
//					.setContentIntent(pendingIntent)
//					.getNotification();		
//		notification.flags |= Notification.FLAG_NO_CLEAR;
//		  
//		startForeground(1, notification);

    }
    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            //根据 一个boolean值来判断是否多行
            lrcView.setIndex(lrcIndex(lrcList ),true);
            single_lrc_view.setIndex(lrcIndex(lrcList ),false);
            //重绘
            lrcView.invalidate();
            single_lrc_view.invalidate();
            LrcHandler.postDelayed(mRunnable, 100);
        }
    };
    /**
     * 根据时间获取歌词显示的索引值
     * @return
     */
    public  int lrcIndex(List<LrcContent> lrcList ) {
        int duration = 0 ;
        //我猜想这里导致暂停的时候Index=0
        try {
            if(mediaPlayer.isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                duration = mediaPlayer.getDuration();
            }else
                //暂停的时候
                return index;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /**********************************************************************************************************/
        if(currentPosition < duration) {
            for (int i = 0; i < lrcList.size(); i++) {
                if (i < lrcList.size() - 1) {
                    if (currentPosition < lrcList.get(i).getLrcTime() && i == 0) {
                        index = i;
                    }
                    if (currentPosition > lrcList.get(i).getLrcTime()
                            && currentPosition < lrcList.get(i + 1).getLrcTime()) {
                        index = i;
                    }
                }
                if (i == lrcList.size() - 1
                        && currentPosition > lrcList.get(i).getLrcTime()) {
                    index = i;
                }
            }
        }
        return index;
    }
    public void onDestroy(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * initialize the MediaPlayer
     */
    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                mediaPlayer.seekTo(currentPosition);
//				Log.v(TAG, "[OnPreparedListener] Start at " + currentMusic + " in mode " + currentMode + ", currentPosition : " + currentPosition);
                handler.sendEmptyMessage(updateDuration);
            }
        });
        //���������⣬����һ��ʼ�ͻ�������������Щ���룬����֣���ʱ���˸�̾�ţ����Ͳ����Զ��л�����
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//				if(isPlaying){
                //���˸�̾�ţ��������Ļ��Ͳ��ܹ��Լ���ѡ��һ��������Ϊʲôѽ��
                if(!isPlaying){
                    Log.v(TAG, "[OnCompletionListener] On Completion at " + currentMusic);
                    switch (currentMode) {
                        case MODE_ONE_LOOP:
                            Log.v(TAG, "[Mode] currentMode = MODE_ONE_LOOP.");
                            mediaPlayer.start();
                            Log.i("", "onCompletion" + currentMode);
                            break;
                        case MODE_ALL_LOOP:
                            Log.v(TAG, "[Mode] currentMode = MODE_ALL_LOOP.");
                            play((currentMusic + 1) % musicList.size(), 0);
                            Log.i("", "onCompletion" + currentMode);
                            //��ʼ�����
//						((NatureBinder) natureBinder).BinderInitLrc();
                            break;
                        case MODE_RANDOM:
                            Log.v(TAG, "[Mode] currentMode = MODE_RANDOM.");
                            Log.i("", "onCompletion" + currentMode);
                            play(getRandomPosition(), 0);
                            break;
                        case MODE_SEQUENCE:
                            Log.v(TAG, "[Mode] currentMode = MODE_SEQUENCE.");
                            Log.i("", "onCompletion" + currentMode);
                            if(currentMusic < musicList.size() - 1){
                                playNext();
                            }
                            break;
                        default:
                            Log.v(TAG, "No Mode selected! How could that be ?");
                            break;
                    }
                    Log.v(TAG, "[OnCompletionListener] Going to play at " + currentMusic);
                }
            }
        });
    }

    private void setCurrentMusic(int pCurrentMusic){
        currentMusic = pCurrentMusic;
        handler.sendEmptyMessage(updateCurrentMusic);
    }

    private int getRandomPosition(){
        int random = (int)(Math.random() * (musicList.size() - 1));
        return random;
    }

    private void play(int currentMusic, int pCurrentPosition) {
        currentPosition = pCurrentPosition;
        setCurrentMusic(currentMusic);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource("/data/data/com.yuzhiyun.perfectmusic/songs/"+musicList.get(currentMusic).getUrl());
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(getApplication(),"无法播放"+e.toString(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        Log.v(TAG, "[Play] Start Preparing at " + currentMusic);


        handler.sendEmptyMessage(updateProgress);
        handler.sendEmptyMessage(updateCurrentMusic);
        handler.sendEmptyMessage(updateDuration);
        isPlaying = true;
    }

    private void stop(){
        mediaPlayer.stop();
        isPlaying = false;
    }

    private void playNext(){
        try {
//			initLrc();
        } catch (Exception e) {
            Log.e("", e + "");
        }
        switch(currentMode){
            case MODE_ONE_LOOP:
                play(currentMusic, 0);
                break;
            case MODE_ALL_LOOP:
                if(currentMusic + 1 == musicList.size()){
                    play(0,0);
                }else{
                    play(currentMusic + 1, 0);
                }
                break;
            case MODE_SEQUENCE:
                if(currentMusic + 1 == musicList.size()){
                    Toast.makeText(this, "No more song.", Toast.LENGTH_SHORT).show();
                }else{
                    play(currentMusic + 1, 0);
                }
                break;
            case MODE_RANDOM:
                play(getRandomPosition(), 0);
                break;
        }
    }

    private void playPrevious(){
        switch(currentMode){
            case MODE_ONE_LOOP:
                play(currentMusic, 0);
                break;
            case MODE_ALL_LOOP:
                if(currentMusic - 1 < 0){
                    play(musicList.size() - 1, 0);
                }else{
                    play(currentMusic - 1, 0);
                }
                break;
            case MODE_SEQUENCE:
                if(currentMusic - 1 < 0){
                    Toast.makeText(this, "No previous song.", Toast.LENGTH_SHORT).show();
                }else{
                    play(currentMusic - 1, 0);
                }
                break;
            case MODE_RANDOM:
                play(getRandomPosition(), 0);
                break;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return natureBinder;
    }

    public class myBinder extends Binder {

        public void startPlay(int currentMusic, int currentPosition){
            play(currentMusic,currentPosition);
        }
        		public void  BinderInitLrc(){
			initLrc();
		}
        public void stopPlay(){
            stop();
        }

        public void toNext(){
            playNext();
        }

        public void toPrevious(){
            playPrevious();
        }
        /**
         * 初始化歌词配置
         */
        public void initLrc(){
            //多行歌词
            lrcView=DetailActivity.lrcView;
            //一行当前歌词
            single_lrc_view= DetailActivity.single_lrc_view;

            mLrcProcess = new LrcProcess();
            //读取歌词文件
//            mLrcProcess.readLRC(musicList.get(currentMusic).getUrl());
            mLrcProcess.readLRC("/data/data/com.yuzhiyun.perfectmusic/songs/"+musicList.get(currentMusic).getLrc());
            //传回处理后的歌词文件
            lrcList = mLrcProcess.getLrcList();

            lrcView.setmLrcList(lrcList);
            single_lrc_view.setmLrcList(lrcList);
            //切换带动画显示歌词
//        lrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this,R.anim.alpha_z));
            LrcHandler =new Handler();
            LrcHandler.post(mRunnable);
        }


        /**
         * MODE_ONE_LOOP = 1;
         * MODE_ALL_LOOP = 2;
         * MODE_RANDOM = 3;
         * MODE_SEQUENCE = 4;
         */
        public void changeMode(){
            currentMode = (currentMode + 1) % 4;
            Log.v(TAG, "[NatureBinder] changeMode : " + currentMode);
            Toast.makeText(PlayService.this, MODE_DESC[currentMode], Toast.LENGTH_SHORT).show();
        }

        /**
         * return the current mode
         * MODE_ONE_LOOP = 1;
         * MODE_ALL_LOOP = 2;
         * MODE_RANDOM = 3;
         * MODE_SEQUENCE = 4;
         * @return
         */
        public int getCurrentMode(){
            return currentMode;
        }

        /**
         * The service is playing the music
         * @return
         */
        public boolean isPlaying(){
            return isPlaying;
        }

        /**
         * Notify Activities to update the current music and duration when current activity changes.
         */
        public void notifyActivity(){
            toUpdateCurrentMusic();
            toUpdateDuration();
        }

        /**
         * Seekbar changes
         * @param progress
         */
        public void changeProgress(int progress){
            if(mediaPlayer != null){
                currentPosition = progress * 1000;
                if(isPlaying){
                    mediaPlayer.seekTo(currentPosition);
                }else{
                    play(currentMusic, currentPosition);
                }
            }
        }
    }

}