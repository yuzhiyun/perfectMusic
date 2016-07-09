package com.yuzhiyun.perfectmusic.ui.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.yuzhiyun.perfectmusic.Application.App;
import com.yuzhiyun.perfectmusic.R;
import com.yuzhiyun.perfectmusic.service.PlayService;
import com.yuzhiyun.perfectmusic.service.PlayService.myBinder;
import com.yuzhiyun.perfectmusic.util.Constant;
import com.yuzhiyun.perfectmusic.util.FormatHelper;
import com.yuzhiyun.perfectmusic.util.ImageTools;
import com.yuzhiyun.perfectmusic.util.MusicLoader;
import com.yuzhiyun.perfectmusic.util.MusicLoader.MusicInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

        public static final String TAG = "com.yuzhiyun.perfectmusic.ui.Activity";

        private ListView lvSongs;
        private ExpandableListView groupLvSongs;
        private SeekBar pbDuration;
        private TextView tvCurrentMusic;
        private List<MusicLoader.MusicInfo> musicList;
        private int currentMusic; // The music that is playing.
        private int currentPosition; // The position of the music is playing.
        private int currentMax;
        // 歌手图片
        Bitmap bmp = null;
        Bitmap bm2;
        private Button btnStartStop;
        private Button btnNext;
        private ImageView btnDetail;

        private ProgressReceiver progressReceiver;
        private myBinder natureBinder;

        private ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                natureBinder = (myBinder) service;
            }
        };

        private void connectToNatureService() {
            Intent intent = new Intent(MainActivity.this, PlayService.class);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
//            Log.e(TAG, "OnCreate");

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            /**
             * tab的使用
             *
             * */
//            TabHost tabHost = (TabHost) findViewById(R.id.myTabHost);
            // 如果不是继承TabActivity，则必须在得到tabHost之后，添加标签之前调用tabHost.setup()
//            tabHost.setup();
            // 这里content的设置采用了布局文件中的view
//            tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("歌          曲")
//                    .setContent(R.id.lvSongs));
//            tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("专          辑")
//                    .setContent(R.id.list));

            MusicLoader musicLoader = new MusicLoader();
            musicList = musicLoader.getMusicList();

            connectToNatureService();
            initComponents();
            initListener();
        }

    private void initListener() {
        btnDetail.setOnClickListener(this);
        btnStartStop.setOnClickListener(this);
        btnNext.setOnClickListener(this);
    }

    public void sortByArtistName(List<MusicInfo> musicList) {

            // List<MusicInfo> musicList=new ArrayList<MusicInfo>();
            List<String> groupName = new ArrayList<String>();
            List<List<MusicInfo>> musicGroupBySinger = new ArrayList<List<MusicInfo>>();
            // 第一个特殊
            groupName.add(musicList.get(0).getArtist());
            List<MusicInfo> musicListWithSameSinger = new ArrayList<MusicInfo>();
            musicListWithSameSinger.add(musicList.get(0));
            musicGroupBySinger.add(musicListWithSameSinger);
            for (int i = 1; i < musicList.size(); i++) {
                boolean flag = false;
                for (int j = 0; j < groupName.size(); j++) {
                    // if该歌手名字已经存在
                    if (musicList.get(i).getArtist().equals(groupName.get(j))) {
                        flag = true;
                        musicGroupBySinger.get(j).add(musicList.get(i));
                        break;
                    }
                }
                if (!flag) {
                    groupName.add(musicList.get(i).getArtist());
                    List<MusicInfo> musicListWithSameSinger2 = new ArrayList<MusicInfo>();
                    musicListWithSameSinger2.add(musicList.get(i));
                    musicGroupBySinger.add(musicListWithSameSinger2);

                }
            }
        }

        public void onResume() {
//            Log.e(TAG, "OnResume register Progress Receiver");
            super.onResume();
            registerReceiver();
            if (natureBinder != null) {
                if (natureBinder.isPlaying()) {
                    btnStartStop.setBackgroundResource(R.drawable.pause);
                } else {
                    btnStartStop.setBackgroundResource(R.drawable.play);
                }
                /*********************************************************************************************/
                // 更新歌名和时长
                natureBinder.notifyActivity();
            }
        }

        public void onPause() {
            // Log.e(TAG, "OnPause unregister Progress Receiver");
            super.onPause();
            unregisterReceiver(progressReceiver);
        }

        public void onDestroy() {
            // Log.e(TAG, "OnDestroy");
            super.onDestroy();
            if (natureBinder != null) {
                unbindService(serviceConnection);
            }
            bmp=null;
            bm2=null;
        }

        private void initComponents() {
            pbDuration = (SeekBar) findViewById(R.id.pbDuration);
            pbDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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

            tvCurrentMusic = (TextView) findViewById(R.id.tvCurrentMusic);
            // 默认歌曲
            tvCurrentMusic.setText(musicList.get(currentMusic).getTitle());
            btnStartStop = (Button) findViewById(R.id.btnStartStop);

            btnNext = (Button) findViewById(R.id.btnNext);


            btnDetail = (ImageView) findViewById(R.id.btnDetail);

            getArtistBmp(currentMusic);
            btnDetail.setImageBitmap(bmp);
            // lvSongs绑定数据
            final MusicAdapter adapter = new MusicAdapter();
            lvSongs = (ListView) findViewById(R.id.lvSongs);
            lvSongs.setAdapter(adapter);
            lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    currentMusic = position;
                    getArtistBmp(position);
                    btnDetail.setImageBitmap(bmp);
                    Log.i("你的选择 ",
                            "歌曲第" + currentMusic + "首 "
                                    + musicList.get(currentMusic).getTitle());
                    for (int i = 0; i < musicList.size(); i++) {
                        Log.i("musicList", "歌曲第" + i + "首是 "
                                + musicList.get(i).getTitle());
                    }
                    natureBinder.startPlay(currentMusic, 0);
                    // 按钮背景设置为正在播放
                    if (natureBinder.isPlaying()) {
                        btnStartStop.setBackgroundResource(R.drawable.pause);
                    }
                }
            });
            lvSongs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {
                    final int pos = position;

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);
                    builder.setTitle("删除该条目");
                    builder.setMessage("确认要删除该条目吗?");
                    builder.setPositiveButton("删除",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    MusicLoader.MusicInfo music = musicList.remove(pos);
                                    adapter.notifyDataSetChanged();
                                    // 判断sd卡是否可以读写
                                    // File sd =
                                    // Environment.getExternalStorageDirectory();
                                    // boolean can_write = sd.canWrite();
                                    // Log.e("", can_write+"sd");
                                    /**
                                     * 使用系统文件管理器来显示该文件位置
                                     * */
                                    File file = new File(music.getUrl());
                                    // String path=file.getParent();
                                    File parentFlie = new File(file.getParent());
                                    Intent intent = new Intent(
                                            Intent.ACTION_GET_CONTENT);
                                    intent.setDataAndType(Uri.fromFile(parentFlie),
                                            "*/*");
                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                    startActivity(intent);

                                }
                            });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                    return false;
                }
            });
            // groupLvSongs绑定数据
//            MyExAdapter ExlvSongsAdapter = new MyExAdapter(this, musicList);
//            groupLvSongs = (ExpandableListView) findViewById(R.id.list);
//            groupLvSongs.setAdapter(ExlvSongsAdapter);
            // groupLvSongs.setOnChildClickListener(new On)
        }
    // 获取歌手图片
    private void getArtistBmp(int currentMusic) {
        if(currentMusic<2) {
            bmp = BitmapFactory.decodeResource(getResources(), Constant.array_artist[currentMusic]);
        }
    }

    /**
         * 动态注册Receiver
         * */
        private void registerReceiver() {
            progressReceiver = new ProgressReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PlayService.ACTION_UPDATE_PROGRESS);
            intentFilter.addAction(PlayService.ACTION_UPDATE_DURATION);
            intentFilter.addAction(PlayService.ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE);
            registerReceiver(progressReceiver, intentFilter);
        }



    class MusicAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return musicList.size();
            }

            @Override
            public Object getItem(int position) {
                return musicList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return musicList.get(position).getId();
            }

            @Override
            public View getView(final int position, View convertView,
                                ViewGroup parent) {
                // 优化listView
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(
                            R.layout.music_item, null);
                    ImageView pImageView = (ImageView) convertView
                            .findViewById(R.id.albumPhoto);
                    TextView pTitle = (TextView) convertView
                            .findViewById(R.id.title);
                    TextView pDuration = (TextView) convertView
                            .findViewById(R.id.duration);
                    TextView pArtist = (TextView) convertView
                            .findViewById(R.id.artist);
                    viewHolder = new ViewHolder(pImageView, pTitle, pDuration,
                            pArtist);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                /**之后可以删除，这里的if是因为图片暂时不够，只配了两张*/
                getArtistBmp(position);
                if(position<2) {
                    viewHolder.imageView.setImageBitmap(bmp);
                }
                // viewHolder.imageView.setImageResource(musicList.get(position));
                viewHolder.title.setText(musicList.get(position).getTitle());
                viewHolder.duration.setText(FormatHelper.formatDuration(musicList
                        .get(position).getDuration()));
                viewHolder.artist.setText(musicList.get(position).getArtist());
                return convertView;
            }
        }

        class ViewHolder {

            ImageView imageView;
            TextView title;
            TextView duration;
            TextView artist;

            public ViewHolder(ImageView pImageView, TextView pTitle,
                              TextView pDuration, TextView pArtist) {
                imageView = pImageView;
                title = pTitle;
                duration = pDuration;
                artist = pArtist;
            }

        }

        /**
         * 按歌手分类的listview 对应的Adapter，自定义ExpandableListView的适配器
         * */
        public class MyExAdapter extends BaseExpandableListAdapter {
            private Context context;
            // 数据
            // private List<GroupMusic> group;
            private List<MusicInfo> musicList = new ArrayList<MusicInfo>();
            private List<String> groupName = new ArrayList<String>();
            private List<List<MusicInfo>> musicGroupBySinger = new ArrayList<List<MusicInfo>>();

            public MyExAdapter(Context context, List<MusicInfo> group) {
                super();
                this.context = context;
                musicList = group;

                sortByArtistName();
            }

            // 根据歌手分类
            @SuppressWarnings("null")
            public void sortByArtistName() {
                // 第一个特殊
                groupName.add(musicList.get(0).getArtist());
                List<MusicInfo> musicListWithSameSinger = new ArrayList<MusicInfo>();
                musicListWithSameSinger.add(musicList.get(0));
                musicGroupBySinger.add(musicListWithSameSinger);
                for (int i = 1; i < musicList.size(); i++) {
                    boolean flag = false;
                    for (int j = 0; j < groupName.size(); j++) {
                        // if该歌手名字已经存在
                        if (musicList.get(i).getArtist().equals(groupName.get(j))) {
                            flag = true;
                            musicGroupBySinger.get(j).add(musicList.get(i));
                            break;
                        }
                    }
                    if (!flag) {
                        groupName.add(musicList.get(i).getArtist());
                        List<MusicInfo> musicListWithSameSinger2 = new ArrayList<MusicInfo>();
                        musicListWithSameSinger2.add(musicList.get(i));
                        musicGroupBySinger.add(musicListWithSameSinger2);
                    }
                }
            }

            @Override
            public int getGroupCount() {

                return musicGroupBySinger.size();
            }

            // 返回在指定Group的Child数目
            @Override
            public int getChildrenCount(int groupPosition) {
                return musicGroupBySinger.get(groupPosition).size();
            }

            @Override
            public Object getGroup(int groupPosition) {
                // TODO Auto-generated method stub
                return musicGroupBySinger.get(groupPosition);
            }

            @Override
            public MusicInfo getChild(int groupPosition, int childPosition) {
                // TODO Auto-generated method stub
                return musicGroupBySinger.get(groupPosition).get(childPosition);
            }

            @Override
            public long getGroupId(int groupPosition) {
                // TODO Auto-generated method stub
                return groupPosition;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                // TODO Auto-generated method stub
                return childPosition;
            }

            @Override
            public boolean hasStableIds() {
                // TODO Auto-generated method stub
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded,
                                     View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    convertView = inflater.inflate(R.layout.groups, null);
                }

                TextView title = (TextView) convertView.findViewById(R.id.tvSinger);
                title.setText(groupName.get(groupPosition));// 设置大组成员名称

                // ImageView image = (ImageView)
                // convertView.findViewById(R.id.tubiao);// 是否展开大组的箭头图标
                // if (isExpanded)// 大组展开时的箭头图标
                // image.setBackgroundResource(R.drawable.group_unfold_arrow);
                // else
                // // 大组合并时的箭头图标
                // image.setBackgroundResource(R.drawable.group_fold_arrow);

                return convertView;

            }

            @Override
            public View getChildView(final int groupPosition,
                                     final int childPosition, boolean isLastChild, View convertView,
                                     ViewGroup parent) {
                int position = musicList.indexOf(getChild(groupPosition,
                        childPosition));
                // 优化listView
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(
                            R.layout.music_item, null);
                    ImageView pImageView = (ImageView) convertView
                            .findViewById(R.id.albumPhoto);
                    TextView pTitle = (TextView) convertView
                            .findViewById(R.id.title);
                    TextView pDuration = (TextView) convertView
                            .findViewById(R.id.duration);
                    TextView pArtist = (TextView) convertView
                            .findViewById(R.id.artist);
                    viewHolder = new ViewHolder(pImageView, pTitle, pDuration,
                            pArtist);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                // viewHolder.imageView.setImageResource(R.drawable.music);

//                bmp = MusicIconLoader.getInstance().load(
//                        musicList.get(position).getImage());
                // 修建成圆形
//                bmp = ImageTools.createCircleBitmap(bmp,
//                        (int) (App.sScreenWidth * 0.35));
                // bmp=ImageTools.scaleBitmap(bmp, (int)(App.sScreenWidth*0.1));
                viewHolder.imageView.setImageBitmap(bmp);
                // viewHolder.imageView.setImageResource(musicList.get(position));
                viewHolder.title.setText(musicList.get(position).getTitle());
                viewHolder.duration.setText(FormatHelper.formatDuration(musicList
                        .get(position).getDuration()));
                viewHolder.artist.setText(musicList.get(position).getArtist());
                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // currentMusic = getChild(groupPosition, childPosition);
                        /**
                         * musicList.indexOf(）必须要change mpdifier of groupPosition,
                         * childPosition to final
                         * */
                        // 根据获取指定的child获取它在musicList中的下标，这样就可以定位到对应的音乐进行播放了。
                        currentMusic = musicList.indexOf(getChild(groupPosition,
                                childPosition));
                        natureBinder.startPlay(currentMusic, 0);
                        // 按钮背景设置为正在播放
                        if (natureBinder.isPlaying()) {
                            btnStartStop.setBackgroundResource(R.drawable.pause);
                        }
                        // 获取歌手图片
//                        bmp = MusicIconLoader.getInstance().load(
//                                musicList.get(currentMusic).getImage());

                        btnDetail.setImageBitmap(bmp);

                    }
                });
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                // 子选项是否可选
                return true;
            }

        }



        /**
         * 开始，下一首，详细按钮
         * */
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStartStop:
                    play(currentMusic, R.id.btnStartStop);
                    break;
                case R.id.btnNext:
                    natureBinder.toNext();
                    break;
                case R.id.btnDetail:

                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(DetailActivity.MUSIC_LENGTH, currentMax);
                    intent.putExtra(DetailActivity.CURRENT_MUSIC, currentMusic);
                    intent.putExtra(DetailActivity.CURRENT_POSITION, currentPosition);
                    startActivity(intent);
                    break;
            }
        }

        private void play(int position, int resId) {
            if (natureBinder.isPlaying()) {
                natureBinder.stopPlay();
                btnStartStop.setBackgroundResource(R.drawable.play);
            } else {
                natureBinder.startPlay(position, currentPosition);
                btnStartStop.setBackgroundResource(R.drawable.pause);
            }
        }

        class ProgressReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (PlayService.ACTION_UPDATE_PROGRESS.equals(action)) {
                    // Log.e("onReceive", "更新进度条");
                    int progress = intent.getIntExtra(
                            PlayService.ACTION_UPDATE_PROGRESS, 0);
                    if (progress > 0) {
                        currentPosition = progress; // Remember the current position
                        pbDuration.setProgress(progress / 1000);
                    }
                } else if (PlayService.ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE
                        .equals(action)) {
                    // Log.e("onReceive", "更新歌名，歌手图片");
                    currentMusic = intent.getIntExtra(
                            PlayService.ACTION_UPDATE_CURRENT_MUSIC_AND_IMAGE, 0);
                    tvCurrentMusic.setText(musicList.get(currentMusic).getTitle());
                    // 同时可以更新歌手图片
                    // 获取歌手图片
//                    bmp = MusicIconLoader.getInstance().load(
//                            musicList.get(currentMusic).getImage());
//                     修建为圆形
//                    bmp = ImageTools.createCircleBitmap(bmp,
//                            (int) (App.sScreenWidth * 0.5));
//                    btnDetail.setImageBitmap(bmp);
                } else if (PlayService.ACTION_UPDATE_DURATION.equals(action)) {
                    currentMax = intent.getIntExtra(
                            PlayService.ACTION_UPDATE_DURATION, 0);
                    int max = currentMax / 1000;
//                    Log.v(TAG, "[Main ProgressReciver] Receive duration : " + max);
                    pbDuration.setMax(currentMax / 1000);

                }
            }
        }
    }

