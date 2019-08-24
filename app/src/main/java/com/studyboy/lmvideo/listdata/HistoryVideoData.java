package com.studyboy.lmvideo.listdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.studyboy.lmvideo.SetAboutVideo;
import java.util.ArrayList;
import java.util.List;

/**
 *  用于获取历史数据列表，包含本地的和在线播放的;主要是数据库的增删查改
 *  ming 2019.08.12
 */
public class HistoryVideoData {
    private static final String TAG = "HistoryVideoData";
    private HistoryDataBaseHelper dbHelper;
    private Context context;
    List<VideoParam> historyList = new ArrayList<>();

    public HistoryVideoData(Context context){
        this.context = context;
    }

    /**
     *  数据库中获取历史数据
     * @return
     */
    public  List<VideoParam> getHistoryData(){
        historyList.clear();
        String videoPath = null;
        String videoName = null;
        String datetime = null;

        dbHelper = new HistoryDataBaseHelper(context,"videoHistory.db",null,1);
        // 若没有 file.db 数据库，则创建
        Cursor cursor = null;
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();
//            db = context.openOrCreateDatabase("videoHistory.db",MODE_PRIVATE,null);
            if (db != null) {
                cursor = db.query("table_video_history",null,null,null,
                        null,null,"datetime desc");  //   按datatime 降序排序
                Log.d(TAG, "updateListView: ********************* ***********  查询完成 ");
                if (cursor != null){
                    if(cursor.moveToFirst()) {
                        do {
                            Log.d(TAG, "updateListView: *********************  有数据 ************ ");
                            // 查询得到的数据加载到 filelist 中，用于listView 显示
                            videoPath = cursor.getString(cursor.getColumnIndex("path"));
                            videoName = cursor.getString(cursor.getColumnIndex("name"));
                            datetime = cursor.getString(cursor.getColumnIndex("datetime"));

                            addList(videoPath,videoName,datetime);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                    cursor = null;
                }
                db.close();
                db = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null){
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor = null;
            try {
                if (db != null){
                    db.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            db = null;
        }
        if( historyList.size()== 0){
//            addList(" 暂无","暂无 ","不知道诶");
            Log.d(TAG, "updateListView: *************************************** 暂无数据 ");
        }
        return historyList;
    }

    /**
     *  将查询到的数据添加到  historyList
     * @param path
     * @param name
     * @param datetime
     */
    public void addList(String path,String name,String datetime){
        VideoParam videoParam = new VideoParam();
        videoParam.setVideoPath(path);
        videoParam.setVideoName(name);
        videoParam.setVideoTime(" ");      // 后面判断为 " " 的话，显示unknown
        videoParam.setvideoSize(datetime); // 历史记录的时间显示在 视频大小的位置
        historyList.add(videoParam);
    }

    /** 数据库中删除数据 ,根据长按获取的 name */
    public void deleteInDataBase(String name){

        dbHelper = new HistoryDataBaseHelper(context,"videoHistory.db",null,1);
        // 若没有 file.db 数据库，则创建
        SQLiteDatabase db = null;
        try{
            db = dbHelper.getWritableDatabase();
            Log.d(TAG, "deleteInDataBase: ****************************  准备删除 ");
            if(db != null) {
                db.delete("table_video_history", "name = ? ", new String[]{name});
//            Toast.makeText(getActivity(), "删除成功 " , Toast.LENGTH_SHORT).show();
                Log.d(TAG, "deleteInDataBase: ****************************  删除成功 ");
                db.close();
                db = null;
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try{
                if( db != null){
                    db.close();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            db = null;
        }
    }

    /**
     * 保存到数据库
     */
    public void saveHistoryToDB(VideoPlayParam historyParam) {
        String videoPath = null;
        String videoName = null;
        String dateTime = null;
        videoPath = historyParam.getPlayPath();
        videoName = historyParam.getPlayName();
        dateTime = SetAboutVideo.getTime();

        deleteInDataBase(videoName); // 删掉原有可能有的记录，再保存

        dbHelper = new HistoryDataBaseHelper(context ,"videoHistory.db",null,1);
        // 若没有 file.db 数据库，则创建
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put("path",videoPath);
        values.put("name", videoName);
        values.put("datetime", dateTime);
        try {
            db = dbHelper.getWritableDatabase();
            if(db != null){
                db.insert("table_video_history", null, values);
                Log.d(TAG, "saveHistoryToDB: ****************************** 历史记录保存成功 "+videoName);
                values.clear();
                db.close();
                db = null;
            }
            values = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try{
                if( db != null){
                    db.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            db = null;
        }
    }


}
