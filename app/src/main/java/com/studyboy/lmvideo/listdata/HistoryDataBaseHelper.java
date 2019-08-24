package com.studyboy.lmvideo.listdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class HistoryDataBaseHelper extends SQLiteOpenHelper {

    private Context mContext;
    // 建表语句
    public static final String CREATE_FILE="create table table_video_history(path Text,name text,datetime text)";

    public HistoryDataBaseHelper (Context context, String name , SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);  // 上下文、库名、null 、版本
        mContext=context;
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        // 建立数据库同时建表
        db.execSQL(CREATE_FILE);
        Log.d(" ", "onCreate: ************************************** DataBase Create succeeded");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldversion,int newVersion){
        // 用于数据库版本更新
    }
}
