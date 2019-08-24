package com.studyboy.lmvideo.listdata;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *  利用ContentResolver 内容提供器查找本地视频及其信息，加载到localVideoList 集合中
 *  ming 2019.08.07
 */

public class LocalVideoData {

    private static String[] mVideoColumn = new String[]{
            MediaStore.Video.Media.DISPLAY_NAME ,   //  名字（固定名称，不可修改）
            MediaStore.Video.Media.DURATION ,       //  时长
            MediaStore.Video.Media.SIZE ,           //  大小
            MediaStore.Video.Media.DATA             //  路径
    };
    List<VideoParam> localVideoList = new ArrayList<>();
    private Context context;
    public LocalVideoData(Context context){
        // 有参构造函数,用于获取 ContentResolver 所需的上下文
        this.context = context;
    }

    public  List<VideoParam> getVideoDataLocal(){

        // 获取内容提供器,获取名字、时长、大小、路径
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = null;
        try {
             cursor = contentResolver.query(uri, mVideoColumn, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    VideoParam videoParam = new VideoParam();
                    videoParam.setVideoName(cursor.getString(0));  // 名字
                    // 单位毫秒，转为时间格式
                    int duration = cursor.getInt(1);
                    videoParam.setVideoTime(setTimeWithFormat(duration));       // 时长
                    // 单位为 B，须转为M
                    int size = cursor.getInt(2);
                    videoParam.setvideoSize(setSizeWithFormat(size));            // 大小
                    videoParam.setVideoPath(cursor.getString(3));  // 路径
                    if( (duration/1000) %60 >2) {                               // 时长大于2s
                        localVideoList.add(videoParam);
                    }
                }
            }
            cursor.close();
            cursor = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                 if(cursor != null){
                     cursor.close();
                 }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return localVideoList;
    }

    /**
     *  根据获取的时间设定格式
     */
    public static String setTimeWithFormat(int millsecond){
        int second = millsecond/1000;
        int hh = second/3600;
        int mm = second % 3600 /60;
        int ss = second % 60;
        String str = null;
        if(hh != 0){
            str = String.format("%02d:%02d:%02d",hh,mm,ss);

        } else {
            str = String.format("%02d:%02d",mm,ss);
        }
        return str;
    }

    /**
     * 根据获取的文件大小设定格式      // size = df.format("%d.%02d",MB,KB)+"MB"; // %02d 为不够两位补0，多了没用
     */
    public static String setSizeWithFormat(int b){
        String size = null;
        DecimalFormat df = new DecimalFormat("####0.00");
        double GB = (double) b /(1024*1024*1024);
        double MB = (double) b / (1024*1024);
        double KB = (double) b / 1024;
        if(GB>1){
            size = df.format(GB)+"GB";
        }
        else if(MB >1){
            size = df.format(MB)+"MB";
        }
        else {
            size = df.format(KB)+"KB";
        }
        return size;
    }
}
