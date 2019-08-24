package com.studyboy.lmvideo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.studyboy.lmvideo.listViewShow.MainListAdapter;
import com.studyboy.lmvideo.listViewShow.SDFileAdapter;
import com.studyboy.lmvideo.listdata.HistoryVideoData;
import com.studyboy.lmvideo.listdata.LocalVideoData;
import com.studyboy.lmvideo.listdata.OnlineVideoData;
import com.studyboy.lmvideo.listdata.SDFileData;
import com.studyboy.lmvideo.listdata.SDFileParam;
import com.studyboy.lmvideo.listdata.VideoParam;
import com.studyboy.lmvideo.listdata.VideoPlayParam;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    private ImageView iv_OpenSDFile; //lv_openSDVideo
    /** 首页、在线、本地、历史 */
    private LinearLayout ly_Home,ly_Online,ly_Local,ly_History, ly_old; // ly_old 用于设定背景
    /** 本地或在线列表  、打开存储文件列表*/
    private ListView main_listView_video,lv_Sdfile_show;
    private TextView tv_HeadShow;
    /** 列表在线、本地、历史、用于显示*/
    List<VideoParam> onlineVideoList,localVideoList,historyVideoList;
    List<VideoParam> showVideoList = new ArrayList<VideoParam>();
    List<VideoPlayParam>  playVideoList = new ArrayList<>();

    List<SDFileParam> sdFileList = new ArrayList<>();
    /** sdFile listView  当前显示路径、根路径*/
    String  recentPath = null,rootPath = null;
    private boolean sdListIsShowing = false;

    private static final  int UPDATE_HOME = 0, UPDATE_ONLINE = 1, UPDATE_LOCAL = 2, UPDATE_HIS = 3;
    /** 当前选项名字，用于数据库删除*/
    private String videoName = null;

    /**  网络监听 */
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        // 隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null ) {
            actionBar.hide();
        }
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initUI();

        // 广播，网络监听
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        // 注册广播（动态注册的，结束要取消）
        registerReceiver(networkChangeReceiver,intentFilter);
    }

    public void  initUI(){

        // 首页、在线播放、本地、历史
        ly_Home = (LinearLayout)   findViewById(R.id.ly_home);
        ly_Online = (LinearLayout) findViewById(R.id.ly_online);
        ly_Local = (LinearLayout)  findViewById(R.id.ly_local);
        ly_History = (LinearLayout)findViewById(R.id.ly_history);
        ly_Home.setOnClickListener(this);
        ly_Online.setOnClickListener(this);
        ly_Local.setOnClickListener(this);
        ly_History.setOnClickListener(this);

        // listview 列表
        main_listView_video = (ListView) findViewById(R.id.lv_maain_show);
        // SDFile 列表
        tv_HeadShow = (TextView) findViewById(R.id.tv_headShow);
        lv_Sdfile_show = (ListView)findViewById(R.id.lv_sdfile_show);
        // 右上角本地打开
        iv_OpenSDFile= (ImageView)findViewById(R.id.iv_open_sdfile);
        iv_OpenSDFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick:**************************** 打开本地SD ");
                hideOrShowSDList(true);
                // 列表初始化.获取根目录
                rootPath = new SDFileData().getSDRoot();
                sdFileListInit(rootPath);
                sdFileListViewShow();
            }
        });
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.ly_home:
                Log.d("TAG", "onClick:**************************** 主页0 ");
                hideOrShowSDList(false);
                updateBackGround(ly_Home);
                break;
            case R.id.ly_online:
                Log.d("TAG", "onClick:**************************** 在线1 ");
                hideOrShowSDList(false);
                updateBackGround(ly_Online);
                if(onlineVideoList == null) {
                    // 获取在线视频列表
                    OnlineVideoData onlineVideoData = new OnlineVideoData();
                    onlineVideoList = onlineVideoData.getOnlineVideoData();
                }
                updateListView(UPDATE_ONLINE);
                break;
            case R.id.ly_local:
                Log.d("TAG", "onClick:**************************** 本地2 ");
                hideOrShowSDList(false);
                updateBackGround(ly_Local);
                if(localVideoList == null) {
                    // 获取本地视频列表,若已经获取则直接更新
                    LocalVideoData loadVideoData = new LocalVideoData(MainActivity.this);
                    localVideoList = loadVideoData.getVideoDataLocal();
                }
                updateListView(UPDATE_LOCAL);
                break;
            case R.id.ly_history:
                Log.d("TAG", "onClick:**************************** 历史3 ");
                hideOrShowSDList(false);
                updateBackGround(ly_History);
                updateListView(UPDATE_HIS);
                break;

        }
    }

    /**
     *  更新背景，ly_Home,ly_Online,ly_Local,ly_History;
     * @param ly_new
     */
    public void updateBackGround(LinearLayout ly_new){
        ly_new.setBackgroundResource(R.color.orange);
        // 去掉上个点击项背景
        if(ly_old != null){
            ly_old.getBackground().setAlpha(0);
        }
        ly_new.getBackground().setAlpha(255);
        ly_old = ly_new;
    }

    /**
     *  根据对应选择更新ListView 列表，在线、本地或历史
     * @param index
     */
    public void updateListView( int index){
        showVideoList.clear();
        if(index == UPDATE_ONLINE){
            Log.d("TAG", "updateListView: *****************************  UPDATE_ONLINE ");
            showVideoList.addAll(onlineVideoList);
        }
        else if(index == UPDATE_LOCAL) {
            // 直接赋值，后期会对localVideoList 产生影响
            Log.d("TAG", "updateListView: *****************************  UPDATE_LOCAL ");
            showVideoList.addAll(localVideoList);
        } else{
            Log.d(TAG, "updateListView: *****************************  UPDATE_HIS ");
            // 获取历史数据列表，实时更新，故每次都要重新获取数据
            HistoryVideoData historyVideoData = new HistoryVideoData(this);
            historyVideoList = historyVideoData.getHistoryData();
            showVideoList.addAll(historyVideoList);

        }
        showListView(index);
    }

    /**
     *  本地、在线或历史 listView 显示及监听
     */
    public void showListView(int index){
        MainListAdapter mainListAdapter = new MainListAdapter(MainActivity.this,
                R.layout.main_list_item , showVideoList);
        main_listView_video.setAdapter(mainListAdapter);
        // item 点击监听
        main_listView_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 点击 item，利用列表中的位置，传递路径
                openVideo(position);
            }
        });
        if(index == UPDATE_HIS) {
            // 是历史列表，长按删除
            main_listView_video.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    videoName = showVideoList.get(position).getVideoName();
                    showDeleteDialog();
                    return false;
                }
            });
        } else {
            // 去除长按监听
            main_listView_video.setOnItemLongClickListener( null);
        }
    }

    /**
     *  跳转至播放页面，文件列表传输，showVideoList 用于显示， playVideoList用于传输
     * @param position
     */
    public void openVideo(int position){   //
        playVideoList.clear();
        // 将显示列表的 路径、名字加载到 playVideoList 列表中
        for(int i = 0;i< showVideoList.size();i++){
            VideoPlayParam videoPlayParam = new VideoPlayParam();
            videoPlayParam.setPlayName( showVideoList.get(i).getVideoName() );
            videoPlayParam.setPlayPath( showVideoList.get(i).getVideoPath() );
            playVideoList.add(videoPlayParam);
        }
        StartOpenVideoActivty( position );
    }

    /**
     *  开始跳转播放，传输播放列表、当前选项
     * @param position
     */
    public void StartOpenVideoActivty(int position ){
        Intent intent = new Intent(MainActivity.this,OpenVideoActivity.class);
        Log.d(TAG, "StartOpenVideoActivty: *************************************** 当前列表位置 "+position);
        intent.putExtra("position",position);
        intent.putExtra("playVideoList",(Serializable)playVideoList);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /** 显示或隐藏手动打开sdfile 列表 */
    public void hideOrShowSDList(boolean shouldShow){
        if( !shouldShow){
            // 隐藏,点击首页、在线播放等地方时
            if( sdListIsShowing){
                tv_HeadShow.setVisibility(View.GONE);
                lv_Sdfile_show.setVisibility(View.GONE);
                sdListIsShowing = false;
            }
        } else {
            // 根据状态隐藏或显示
            if( !sdListIsShowing){
                // 隐藏状态，显示
                tv_HeadShow.setVisibility(View.VISIBLE);
                lv_Sdfile_show.setVisibility(View.VISIBLE);
                sdListIsShowing = true;
            } else {
                tv_HeadShow.setVisibility(View.GONE);
                lv_Sdfile_show.setVisibility(View.GONE);
                sdListIsShowing = false;
            }
        }
    }

    /**
     *  显示删除历史记录提示dialog
     */
    public void showDeleteDialog() {

        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(" TTVideo ");
        deleteDialog.setMessage("     您确定要删除该记录吗");
        deleteDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 数据库删除
                        deleteData();
                    }
                }).show();
    }
    /**
     *  数据库中删除历史记录
     */
    public void deleteData(){
        HistoryVideoData historyVideoData = new HistoryVideoData(MainActivity.this);
        historyVideoData.deleteInDataBase(videoName);
        // 刷新列表显示
        updateListView(UPDATE_HIS);
    }

    /**
     *  初始化 打开 sdFile 显示列表，
     */
    public void sdFileListInit(String filePath){
        recentPath = filePath;

        SDFileData sdFileData = new SDFileData();
        tv_HeadShow.setText(filePath);
        // 获取显示列表
        if(sdFileList != null){
            sdFileList.clear();
        }
        List<SDFileParam> fileList = sdFileData.getFileDirectory(filePath);
        sdFileList.addAll(fileList);
    }

    /**
     *  sdFileListView 加载适配器，并显示 监听
     */
    public void  sdFileListViewShow(){
        SDFileAdapter sdFileAdapter = new SDFileAdapter(this,R.layout.sdfile_show_item ,sdFileList);
        lv_Sdfile_show.setAdapter(sdFileAdapter);
        lv_Sdfile_show.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SDFileParam sdFileParam = sdFileList.get(position);
                File newFile = new File( sdFileParam.getSdFilePath() );

                // 返回根目录
                if(position == 0){
                    sdFileListInit(rootPath);
                    sdFileListViewShow();
                }
                // 返回上一级目录,SDfileName 为“ 返回上一级 ”，故加入 recentPath 作当前路径
                else if(position == 1){
//                    String parentFile = newFile.getName();
                    File parentFile = new File(recentPath);
                    if(!parentFile.exists() || parentFile.length() == 0) {
                        System.out.println("上级目录不存在");
                    }
                    else{
                        sdFileListInit( parentFile.getParent());  // 打开父路径并重新显示 listView
                        sdFileListViewShow();
                    }
                }
                // 点击文件夹或TXT文件
                else{
                    if(newFile.isDirectory()){
                        // 打开新路径 并重新显示 listView
                        sdFileListInit( sdFileParam.getSdFilePath());
                        sdFileListViewShow();
                    }
                    else{
                        // mp4 文件，返回其完整路径，和名字
                        playVideoBySD( sdFileParam.getSdFilePath(), sdFileParam.getSdFileName());
                    }
                }
            }
        });
    }

    /**
     *  根据选择的sd 卡视频，打开播放
     * @param path
     * @param name
     */
    public void playVideoBySD (String path ,String name){
        // 重设传输列表，并跳转播放
        playVideoList.clear();
        VideoPlayParam videoPlayParam = new VideoPlayParam();
        videoPlayParam.setPlayName( name );
        videoPlayParam.setPlayPath( path );
        playVideoList.add(videoPlayParam);
        StartOpenVideoActivty(0);
    }

    /**
     *  利用广播实现网络监听器， **********************************************************
     */
    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent){
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = (networkInfo != null && networkInfo.isConnected() );
            if (isConnected){
                boolean isWiFi = ( networkInfo.getType() == connectivityManager.TYPE_WIFI );
                boolean isMobile =  ( networkInfo.getType() == connectivityManager.TYPE_MOBILE );
                if(isWiFi) {
                    Log.d("MainActivity", "onReceive: ************************  WiFi 连接  ");
                } else if(isMobile) {
                    Log.d("MainActivity", "onReceive: ************************  数据 连接  ");
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setMessage("当前在使用数据中，请注意你的流量哦");
                    dialog.setCancelable(true);
                    dialog.show();
                }
            } else{
                Log.d("MainActivity", "onReceive: ************************  没有网络  ");
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setMessage("当前没有网络，无法进行在线播放哦");
                dialog.setCancelable(true);
                dialog.show();

            }
        }
    }

    /**
     *  取消注册广播接收器，否则会内存泄漏
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }
}
