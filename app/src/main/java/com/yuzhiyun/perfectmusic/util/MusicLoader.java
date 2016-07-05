package com.yuzhiyun.perfectmusic.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;


import com.yuzhiyun.perfectmusic.db.DBservice;

import java.util.ArrayList;
import java.util.List;

public class MusicLoader {
	
	private static final String TAG = "com.example.nature.MusicLoader";
	
	private static List<MusicInfo> musicList = new ArrayList<MusicInfo>();
	
	private static MusicLoader musicLoader;
	
	private static ContentResolver contentResolver;
	
	private Uri contentUri = Media.EXTERNAL_CONTENT_URI;
	
	private String[] projection = {
			Media._ID,
			Media.TITLE,
			Media.DATA,
			Media.ALBUM,
			Media.ARTIST,
			Media.DURATION,
			Media.SIZE
	};

	public List<MusicInfo> getMusicList(){
		DBservice dBservice=new DBservice();
		musicList=dBservice.getList_data();
		return musicList;
	}
	
	public Uri getMusicUriById(long id){
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		return uri;
	}	

	public static class MusicInfo {
		private long id;
		private String title;
		private String album;
		private int duration;
		private long size;
		private String artist;
		private String url;
		private String image;
		private String lrc;
		public MusicInfo(){
			
		}
		
		public MusicInfo(long pId, String pTitle){
			id = pId;
			title = pTitle;
		}


		public String getLrc() {
			return lrc;
		}

		public void setLrc(String lrc) {
			this.lrc = lrc;
		}

		public String getArtist() {
			return artist;
		}

		public void setArtist(String artist) {
			this.artist = artist;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}		

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getAlbum() {
			return album;
		}

		public void setAlbum(String album) {
			this.album = album;
		}

		public int getDuration() {
			return duration;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}	

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

	}
}
