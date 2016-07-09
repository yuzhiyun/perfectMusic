package com.yuzhiyun.perfectmusic.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yuzhiyun.perfectmusic.util.MusicLoader;
import com.yuzhiyun.perfectmusic.util.MusicLoader.MusicInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liangx on 2015/11/20.
 */
public class DBservice {

    private SQLiteDatabase db;


    public DBservice() {
        this.db = SQLiteDatabase.openDatabase("/data/data/com.yuzhiyun.perfectmusic/databases/music.db",null,SQLiteDatabase.OPEN_READWRITE);
    }

    public List<MusicLoader.MusicInfo> getList_data(){

        List<MusicLoader.MusicInfo> list = new ArrayList<MusicInfo>();

        Cursor cursor = db.rawQuery("select * from music",null);
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            int count = cursor.getCount();
            for (int i = 0;i < count;i++)
            {
                cursor.moveToPosition(i);
                MusicInfo list_data = new MusicInfo();
                list_data.setId(cursor.getInt(cursor.getColumnIndex("id")));
                list_data.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                list_data.setAlbum(cursor.getString(cursor.getColumnIndex("album")));
                list_data.setDuration(cursor.getInt(cursor.getColumnIndex("duration")));
                list_data.setSize(cursor.getInt(cursor.getColumnIndex("size")));
                list_data.setArtist(cursor.getString(cursor.getColumnIndex("artist")));
                list_data.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                list_data.setLrc(cursor.getString(cursor.getColumnIndex("lrc")));
                list.add(list_data);
                Log.i("数据库内容",list_data.getTitle()+"歌qu位置"+list_data.getUrl()+"歌词"+list_data.getLrc()+"时长"+list_data.getDuration());
                if(2==list.size())
                    return list;
            }
//            由于只有两首歌，但是数据库里面包含多首歌的信息，所以就只取数据库中两首歌的信息算了

        }
        return list;

    }
}
