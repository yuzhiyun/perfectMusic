package com.yuzhiyun.perfectmusic.ui.customView;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.yuzhiyun.perfectmusic.util.LrcContent;


/**
 * 自定义歌词view
 * */
public class LrcView extends TextView {

    private float width;        //歌词视图宽度
    private float height;       //歌词视图高度
    private Paint currentPaint; //当前画笔对象
    private Paint notCurrentPaint;  //非当前画笔对象
    private float textHeight = 70;  //文本高度
    private float textSize = 40;        //文本大小
    private int index = 0;      //list集合下标
    private List<LrcContent> mLrcList = new ArrayList<LrcContent>();
    private boolean isMutiplyLine;////判断是否是多行歌词的显示


    public void setMutiplyLine(boolean isMutiplyLine) {
        this.isMutiplyLine = isMutiplyLine;
    }

    public void setmLrcList(List<LrcContent> mLrcList) {
        this.mLrcList = mLrcList;
    }

    public LrcView(Context context) {
        super(context);
        init();
    }
    public LrcView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setFocusable(true);     //设置可对焦

        //高亮部分
        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);    //设置抗锯齿，让文字美观饱满
        currentPaint.setTextAlign(Paint.Align.CENTER);//设置文本对齐方式

        //非高亮部分
        notCurrentPaint = new Paint();
        notCurrentPaint.setAntiAlias(true);
        notCurrentPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * 绘画歌词
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(canvas == null) {
            return;
        }

        currentPaint.setColor(Color.argb(210, 251, 248, 29));
        notCurrentPaint.setColor(Color.WHITE);

        currentPaint.setTextSize(textSize+20);
        currentPaint.setTypeface(Typeface.SERIF);

        notCurrentPaint.setTextSize(textSize);
        notCurrentPaint.setTypeface(Typeface.DEFAULT);

        try {
            setText("");

            //判断是否是多行歌词的显示
//            Log.i("", "是否多行歌词"+isMutiplyLine);
            if(isMutiplyLine) {
                //画出当前 行
                canvas.drawText(mLrcList.get(index).getLrcStr(), width / 2, height / 2, currentPaint);
                float tempY = height / 2;
                //画出本句之前的句子
                for(int i = index - 1; i >= 0; i--) {
                    //向上推移
                    tempY = tempY - textHeight;
                    canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
                }
                tempY = height / 2;
                //画出本句之后的句子
                for(int i = index + 1; i < mLrcList.size(); i++) {
                    //往下推移
                    tempY = tempY + textHeight;
                    canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
                }

            }
            else{
//            	canvas.drawText(mLrcList.get(index).getLrcStr(), 100, 100, currentPaint);
                setTextColor(Color.WHITE);

                setTextSize(30);
                setText(mLrcList.get(index).getLrcStr());
            }

        } catch (Exception e) {
            setText("这里是onDraw...木有歌词文件，赶紧去下载..."+"\n"+e);
        }
    }

    /**
     * 当view大小改变的时候调用的方法
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    public void setIndex(int index,boolean isMutiplyLine) {
        this.index = index;
        setMutiplyLine(isMutiplyLine) ;
    }


}
