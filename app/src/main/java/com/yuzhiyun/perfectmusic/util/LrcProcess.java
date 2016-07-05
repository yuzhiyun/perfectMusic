package com.yuzhiyun.perfectmusic.util;
/**
 * 处理歌词类*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class LrcProcess {
    private List<LrcContent> lrcList; //List集合存放歌词内容对象
    private LrcContent mLrcContent;     //声明一个歌词内容对象
    /**
     * 无参构造函数用来实例化对象
     */
    public LrcProcess() {
        mLrcContent = new LrcContent();
        lrcList = new ArrayList<LrcContent>();
    }

    /**
     * 读取歌词
     * @param path
     * @return
     */
    public String readLRC(String path) {
        //定义一个StringBuilder对象，用来存放歌词内容
        StringBuilder stringBuilder = new StringBuilder();
        /**我这里有两个路径
         * 第一种示范
         * 11-04 12:01:03.056: I/(7766): 路径/storage/emulated/0/Music/一半.lrc
         * 11-04 12:01:03.056: I/(7766): 路径/storage/emulated/0/Music/一半.mp3
         * 第二种示范
         * 11-04 12:05:07.806: I/(8923): 路径/storage/sdcard1/MIUI/music/lyric/极限爱恋_郑爽,张翰.lrc
         * 11-04 12:05:15.606: I/(8923): 路径/storage/sdcard1/MIUI/music/mp3_hd/极限爱恋_郑爽,张翰.mp3
         * 如果是arm格式会有问题，是mp3格式的
         */
        Log.i("", "路径"+path);
        path=path.replace(".mp3", ".lrc");
        path=path.replace("mp3_hd", "lyric");

        Log.i("", "路径"+path);
        //path的修改很重要
        File f = new File(path);
        try {
            //创建一个文件输入流对象
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "GBK");
            BufferedReader br = new BufferedReader(isr);
            String s = "";
            while((s = br.readLine()) != null) {
                //替换字符
                s = s.replace("[", "");
                s = s.replace("]", "@");

                //分离“@”字符
                String splitLrcData[] = s.split("@");
                if(splitLrcData.length > 1) {
                    mLrcContent.setLrcStr(splitLrcData[1]);

                    //处理歌词取得歌曲的时间
                    int lrcTime = time2Str(splitLrcData[0]);

                    mLrcContent.setLrcTime(lrcTime);

                    //添加进列表数组
                    lrcList.add(mLrcContent);

                    //新创建歌词内容对象
                    mLrcContent = new LrcContent();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            stringBuilder.append("木有歌词文件，赶紧去下载！..."+e);
            /**10-25 16:40:43.657:
             * E/readLRC错误(13578): 木有歌词文件，赶紧去下载！
             * ...java.io.FileNotFoundException: /storage/emulated/0/kgmusic/download/少女时代 - gee.lrc:
             * open failed: ENOENT (No such file or directory)*/

        } catch (IOException e) {
            e.printStackTrace();
            stringBuilder.append("木有读取到歌词哦！"+e);
        }
        Log.e("readLRC错误", stringBuilder.toString());
        return stringBuilder.toString();
    }

    /**
     * 解析歌词时间
     * 歌词内容格式如下：
     * [00:02.32]陈奕迅
     * [00:03.43]好久不见
     * [00:05.22]歌词制作  王涛
     * @param timeStr
     * @return
     */
    public int time2Str(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");

        String timeData[] = timeStr.split("@"); //将时间分隔成字符串数组

        //分离出分、秒并转换为整型
        int minute = Integer.parseInt(timeData[0]);
        int second = Integer.parseInt(timeData[1]);
        int millisecond = Integer.parseInt(timeData[2]);

        //计算上一行与下一行的时间转换为毫秒数
        int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
        return currentTime;
    }
    public List<LrcContent> getLrcList() {
        return lrcList;
    }
}  
