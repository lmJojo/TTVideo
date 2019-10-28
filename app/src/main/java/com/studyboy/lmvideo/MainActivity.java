package com.studyboy.lmvideo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.studyboy.lmvideo.listViewShow.MainListAdapter;
import com.studyboy.lmvideo.listViewShow.SDFileAdapter;
import com.studyboy.lmvideo.listdata.HistoryVideoData;
import com.studyboy.lmvideo.listdata.LocalVideoData;
import com.studyboy.lmvideo.listdata.OnlineVideoData;
import com.studyboy.lmvideo.listdata.SDFileData;
import com.studyboy.lmvideo.listdata.SDFileBean;
import com.studyboy.lmvideo.listdata.VideoBean;
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
    private ListView main_listView_video;

    /** 列表在线、本地、历史、用于显示*/
    List<VideoBean> onlineVideoList,localVideoList,historyVideoList;
    List<VideoBean> showVideoList = new ArrayList<VideoBean>();
    List<VideoPlayParam>  playVideoList = new ArrayList<>();

    /** sdFile listView  当前显示路径、根路径*/
    String  recentPath = null,rootPath = null;
    private boolean sdListIsShowing = false;

    private static final  int UPDATE_HOME = 0, UPDATE_ONLINE = 1, UPDATE_LOCAL = 2, UPDATE_HIS = 3;
    /** 当前选项名字，用于数据库删除*/
    private String videoName = null;
    private ImageView main_view;

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
        getPermission();

        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initUI();

        // 广播，网络监听
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        // 注册广播（动态注册的，结束要取消）
        registerReceiver(networkChangeReceiver,intentFilter);

        ly_Home.performClick();
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

        main_view = ( ImageView ) findViewById(R.id.main_iv);
        // listview 列表
        main_listView_video = (ListView) findViewById(R.id.lv_maain_show);

        // 右上角本地打开
        iv_OpenSDFile= (ImageView)findViewById(R.id.iv_open_sdfile);
        iv_OpenSDFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick:**************************** 打开本地SD ");
                initPopWindow();

            }
        });
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.ly_home:
                Log.d("TAG", "onClick:**************************** 主页0 ");
                updateBackGround(ly_Home);
                main_view.setVisibility( View.VISIBLE);
                break;
            case R.id.ly_online:
                Log.d("TAG", "onClick:**************************** 在线1 ");
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
        ly_new.setSelected(true);
        // 去掉上个点击项背景
        if(ly_old != null){
            ly_old.setSelected(false);
//            ly_old.getBackground().setAlpha(0);
            Log.d(TAG, "updateBackGround: *********** 上个背景");
        }
//        ly_new.getBackground().setAlpha(255);
        ly_old = ly_new;
        if(main_view.getVisibility() == View.VISIBLE){
            main_view.setVisibility(View.GONE);
        }
    }

    /**
     *  根据对应选择更新ListView 列表，在线、本地或历史
     * @param index
     */
    public void updateListView( int index){
        showVideoList.clear();
        if(index == UPDATE_ONLINE){
            Log.d("TAG", "updateListView: ***************  UPDATE_ONLINE ");
            showVideoList.addAll(onlineVideoList);
        }
        else if(index == UPDATE_LOCAL) {
            // 直接赋值，后期会对localVideoList 产生影响
            Log.d("TAG", "updateListView: *****************  UPDATE_LOCAL ");
            showVideoList.addAll(localVideoList);
        } else{
            Log.d(TAG, "updateListView: *****************  UPDATE_HIS ");
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
        Log.d(TAG, "StartOpenVideoActivty: *************************************** 当前列表位置 "+ position);
        intent.putExtra("position",position);
        intent.putExtra("playVideoList",(Serializable)playVideoList);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        updateListView( UPDATE_HIS );
    }


    private FilePopupWindow mFilePopWindow;
    public void initPopWindow(){
        if( mFilePopWindow == null ){
            mFilePopWindow = new FilePopupWindow(MainActivity.this, iv_OpenSDFile );
            mFilePopWindow.setOnWindowlisten(new FilePopupWindow.OnWindowListen() {
                @Override
                public void onFilePath(SDFileBean sdFileBean) {
                    // mp4 文件，返回其完整路径，和名字
                    playVideoBySD( sdFileBean.getSdFilePath(), sdFileBean.getSdFileName());
                }
            });
        } else {
            mFilePopWindow.init();
        }
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
     *  获取本地读取权限
     */
    public void getPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            // 检查权限
            int readCheck = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeCheck = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED) {
                // 已有权限
                Log.d(TAG, "getPermission:******** 已有权限 ");
            } else {
                // api > 23 还需要手动申请权限
                ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "onRequestPermissionsResult: ******* 成功获取权限");
            if( mFilePopWindow != null){
                mFilePopWindow.getRootData();
            }
        } else {
            Toast.makeText(MainActivity.this,"获取本地读写权限失败",Toast.LENGTH_SHORT );
        }
    }


    private  long time = 0;
    private Toast mToast = null;
    /**
     * 再按一次退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis() - time > 2000 ){
                mToast = Toast.makeText( MainActivity.this,"再按一次退出程序",Toast.LENGTH_SHORT );
                mToast.show();
                time = System.currentTimeMillis();
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        if( mToast != null ){
            mToast.cancel();
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
