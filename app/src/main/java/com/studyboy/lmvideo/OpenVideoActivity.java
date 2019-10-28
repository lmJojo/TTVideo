package com.studyboy.lmvideo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.studyboy.lmvideo.listViewShow.PlayListAdapter;
import com.studyboy.lmvideo.listdata.HistoryVideoData;
import com.studyboy.lmvideo.listdata.VideoPlayParam;

import java.util.ArrayList;
import java.util.List;


public class OpenVideoActivity extends AppCompatActivity implements View.OnClickListener,MediaPlayer.OnPreparedListener{
    public static final String TAG = "OpenVideoActivity";
    // 定义多媒体播放器
    private MediaPlayer mMediaPlayer ;
    private SurfaceView sv_View;
    private SurfaceHolder holder;

    String path = null ,fileName = null;
    // 定义缓存容器
    private SharedPreferences sp;

    // 顶部栏布局、底部栏布局
    private LinearLayout ly_bottom__Control;
    private RelativeLayout ly_top_Control;
    // 图片控件全屏 、悬浮窗、暂停 、上一个、下一个
    private  ImageView iv_FullScreen,iv_SmallWindow ,pauseOrPlay_Image, last_Image, next_Image;
    // 图片控件 返回、关闭、刷新
    private ImageView back_Image, close_Image, replay_Image;
    // 视频总时间、已播放时间 、播放列表控件
    private  TextView tv_recentTime , tv_totalTime ,tv_showList;
    // 播放列表控件 、listview显示列表 、进度条
    private TextView tv_Playlist;
    ListView listView;
    private SeekBar seekBar_Progress;

    private RelativeLayout ly_Loading_background;

    /** 判断上下控制栏是否显示 */
    private boolean isShowing = true;
    private boolean isFullScreen = false;
    /** 播放列表是否显示*/
    private boolean list_IsShow = false;

    /** 手指按下的横纵坐标、坐标偏移量 */
    private int mXpos = 0,mYpos = 0;
    private int mOffset = 0;

    public static final int UPDATE_UI = 1;

    Button btn_Open ; // 临时 使用
    SetAboutVideo setAboutVideo;
    PlayListAdapter playListAdapter;
    private List<VideoPlayParam> videoPlayList = new ArrayList<>();
    private int mSelect = 0; // 列表播放位置
    /** 悬浮窗服务绑定 、是否绑定*/
    private VideoFloatService.FloatWindowBinder floatWindowBinder;
    private ServiceConnection  connection;
    private boolean isConnected = false ,mediaIsShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_video_layout);
        // 隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null ) {
            actionBar.hide();
        }
        // 一段时间不操作，则隐藏控制条
        mHandler.postDelayed(mHide,5000);
        // 隐藏下标题栏
//        getWindow().setFlags(0x02000000, 0x02000000);
        // 全屏显示
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        mSelect = intent.getIntExtra("position",0);
        videoPlayList = (List<VideoPlayParam>) intent.getSerializableExtra("playVideoList");
        // 根据传过来的数组及位置获取当前 路径
        path = videoPlayList.get(mSelect).getPlayPath();

        initUI();
        listViewShow();

        fileName = setAboutVideo.getVideoFileName(path);
        // 设置位移偏移量的临界值
        mOffset = setAboutVideo.dip2px(this, 10);
        //  获取容器缓冲区的存储位置   私有模式
        sp = getSharedPreferences("config", MODE_PRIVATE);
        // 设置容器  注册回调
        holder = sv_View.getHolder();
        initMediaView();

        // service 绑定回调
        connection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                // 出现异常，自动解绑才调用，获取路径和当前播放位置
                Log.d(TAG, "onServiceDisconnected: ********************************* activity 异常解除绑定 ");
            }
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // FloatWindowBinder 的实例获取
                Log.d(TAG, "onServiceDisconnected: ********************************* activity 绑定成功 ");
                floatWindowBinder = (VideoFloatService.FloatWindowBinder)service;
                VideoFloatService fService = floatWindowBinder.getService();  // onFloatClick  FloatClickListener
                fService.setOnFloatListener(new VideoFloatService.FloatClickListener(){
                    @Override
                     public void onFloatClick(String floatPath,int foatPosition,boolean shouldFull){
                       // 回调，获取位置和路径
                        playServicePosition(floatPath,foatPosition,shouldFull);
                     }
                });
            }
        };
    }

    /**
     *  界面初始化
     */
    public void initUI(){

        sv_View = (SurfaceView) findViewById(R.id.sv_view);
/**************************  全屏与悬浮窗 须加入  ******************************************/
        iv_FullScreen = (ImageView) findViewById(R.id.iv_fullscreen);
        iv_SmallWindow = (ImageView) findViewById(R.id.iv_floatWindow);
        iv_FullScreen.setOnClickListener(this);
        iv_SmallWindow.setOnClickListener(this);
        seekBar_Progress = (SeekBar) findViewById(R.id.seekBar_progress) ;

        // 图片控件 暂停、上一个、下一个、返回、关闭、刷新
        pauseOrPlay_Image = (ImageView) findViewById(R.id.pauseOrPlay_image);
        last_Image = (ImageView)findViewById(R.id.last_image);
        next_Image = (ImageView)findViewById(R.id.next_image);
        back_Image = (ImageView)findViewById(R.id.back_image);
        close_Image = (ImageView)findViewById(R.id.close_image);
        replay_Image = (ImageView)findViewById(R.id.replay_image);
        pauseOrPlay_Image.setOnClickListener(this);
        last_Image.setOnClickListener(this);
        next_Image.setOnClickListener(this);
        back_Image.setOnClickListener(this);
        close_Image.setOnClickListener(this);
        replay_Image.setOnClickListener(this);

        // 视频总时长与播放时间
        tv_totalTime = (TextView)findViewById(R.id.tv_total_time);
        tv_recentTime = (TextView)findViewById(R.id.tv_recent_time) ;

        // 设置顶部和底部控制栏 的背景透明度
        ly_bottom__Control =  (LinearLayout) findViewById(R.id.ly_bottom_control);
        ly_bottom__Control.getBackground().setAlpha(100);
        ly_top_Control = (RelativeLayout) findViewById(R.id.ly_top_control) ;
        ly_top_Control.getBackground().setAlpha(100);;

        tv_showList = (TextView) findViewById( R.id.tv_showlist);
        tv_showList.getBackground().setAlpha(50);
        // 点击出现播放列表
        tv_showList.setOnClickListener(this);

        // 视频加载界面
        ly_Loading_background = (RelativeLayout) findViewById(R.id.ly_loading_background);
    }

    /**
     *  界面Button、ImageView 等控件监听
     * @param v
     */
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.tv_showlist:
                // 显示或隐藏播放列表
                showPlaylist();
                break;
            case R.id.pauseOrPlay_image:
                pauseOrPlayVideo();
                break;
            case R.id.last_image:
                // 上一个视频
                lastOrNextVideo(-1);
                break;
            case R.id.next_image:
                // 下一个视频
                lastOrNextVideo(1);
                break;
            case R.id.iv_floatWindow:
                /******************************   api 23 以上 悬浮窗权限申请   ******************************/
                requestOverlayPermission();
                Log.d( TAG, "onClick:***************************  点击悬浮窗  ");
                openFloatWindows();
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case R.id.iv_fullscreen:
                Log.d( TAG, "onClick:***************************  点击全屏  ");
                setFullScreen();
//                fullScreenVideo();
                break;
            case R.id.back_image:
                Log.d( TAG,  "onClick:***************************  点击返回  ");
                finish();
                break;
            case R.id.replay_image:
                Log.d( TAG,  "onClick:***************************  点击刷新  ");
                playNewVideo(path);
                break;
            case R.id.close_image:
                Log.d( TAG,  "onClick:***************************  点击关闭  ");
                finish();
                break;
        }
    }

    /**
     *  利用 service 打开悬浮窗
     */
    public void openFloatWindows(){
        int position = getPausePosition();;

        if(!isConnected){
            // 打开服务，在服务中实现悬浮窗，传递路径和当前播放位置
            Intent startIntent = new Intent(OpenVideoActivity.this, VideoFloatService.class);
            startIntent.putExtra("videoPath",path);
            startIntent.putExtra("position",position);
            startService(startIntent);
           // 绑定服务
            Log.d(TAG, "openFloatWindows:**************************  尝试绑定  ");
            Intent serviceIntent = new Intent(OpenVideoActivity.this, VideoFloatService.class);
            bindService(serviceIntent,connection,BIND_AUTO_CREATE);
            isConnected = true;
        } else {
            if(floatWindowBinder.isShowing()){
                Log.d(TAG, "openFloatWindows: **************************  悬浮窗在显示中 ");
            }
            else {
                floatWindowBinder.openFloatWindow(path,position);
            }
        }
        Intent i= new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //android123提示如果是服务里调用，必须加入new task标识
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);

    }

    /**
     *  若服务还在进行中，解绑并停止服务
     */
    public void stopFloatService(){
        if(isConnected) {
            unbindService(connection);
            Intent stopIntent = new Intent(OpenVideoActivity.this, VideoFloatService.class);
            stopService(stopIntent);
            isConnected = false;
        }
    }

    /**
     *  根据悬浮窗传递回来的路径及位置继续播放
     * @param floatPath
     * @param floatPosition
     */
    public void  playServicePosition(String floatPath, int floatPosition, boolean shouldFull){
        // 全屏继续播放
        if(shouldFull) {
            // 处于播放页面
            if (mediaIsShow) {
                // 路径不同
                if (!floatPath.equals(path)) {
                    playNewVideo(floatPath);
                }
                mMediaPlayer.seekTo(floatPosition);
                setAboutVideo.updateTimeWithFormat( tv_recentTime,floatPosition);
                pauseOrPlay_Image.setImageResource(R.drawable.pause_btn_style);
                mMediaPlayer.start();
            } else {
                // 后台
                path = floatPath;
                mMediaPlayer.seekTo(floatPosition);
                setAboutVideo.updateTimeWithFormat( tv_recentTime,floatPosition);
            }
        }
        // 关闭悬浮窗
        else {
           if(floatPath.equals(path) && mediaIsShow ) {
                // 视频路径相同，且还在播放页面
                Log.d(TAG, "openFloatWindows:**************************  跳转到悬浮窗进度  ");
               mMediaPlayer.seekTo(floatPosition);
               setAboutVideo.updateTimeWithFormat( tv_recentTime,floatPosition);
            }
        }
        stopFloatService(); // 停止服务
    }

    /**
     * 暂停并获取暂停播放的位置
     * @return
     */
    public int getPausePosition () {
        int position = 0;
        // 先暂停播放页面
        if (mMediaPlayer.isPlaying()) {
            // 暂停播放
            pauseOrPlay_Image.setImageResource(R.drawable.play_btn_style);
            mMediaPlayer.pause();
        } else { }
        try {
            // 在prepare状态是，获取当前播放位置会出异常
            position = mMediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            Log.d(TAG, "onClick:***********************  在准备中，进度保持为0  ");
            e.printStackTrace();
        }
        return position;
    }
    /**
     *  显示或隐藏播放列表 listView
     */
    public void showPlaylist(){
        if(!list_IsShow) {
            listView.setVisibility(View.VISIBLE);
            // 背景透明度
            listView.getBackground().setAlpha(100);
            list_IsShow = true;
        } else {
            listView.setVisibility(View.GONE);
            list_IsShow = false;
        }
    }

    /**
     * 切换上一个或下一个视频,列表循环
     * @param index
     */
    public void lastOrNextVideo(int index){
        mSelect = mSelect+index;
        if(mSelect >= videoPlayList.size()){
            mSelect = 0;
        }
        else {
            if(mSelect < 0) {
                mSelect = videoPlayList.size()-1;
            }
        }
        path = videoPlayList.get(mSelect).getPlayPath();
        playNewVideo(path);
        // 更新播放列表当前位置
        playListAdapter.changeSelect(mSelect);
    }

    /**
     *  暂停或继续播放
     */
    public void pauseOrPlayVideo(){
        if(mMediaPlayer.isPlaying()){
            // 暂停播放
            pauseOrPlay_Image.setImageResource(R.drawable.play_btn_style);
            mMediaPlayer.pause();
        } else {
            pauseOrPlay_Image.setImageResource(R.drawable.pause_btn_style);
            // 继续播放
            mMediaPlayer.start();
        }
    }

    private boolean isFull = false;
    /**
     * 全屏设置
     */
    private void setFullScreen() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            if(!isFull) {
                v.setSystemUiVisibility(View.GONE);
            }else {
                v.setSystemUiVisibility(View.VISIBLE);
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            if(!isFull) {
                //for new api versions.
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
                // 图标替换
                iv_FullScreen.setImageResource(R.drawable.player_fullscreen_press2);
                Log.d(TAG, "setFullScreen: ***************** 隐藏");
            }else{
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
                // 图标替换
                iv_FullScreen.setImageResource(R.drawable.player_fullscreen_normal2 );
                Log.d(TAG, "setFullScreen: ********************** 显示");
            }
        }
        isFull = !isFull;

    }


    /**
     *  切换视频，打开新文件
     * @param newPath
     */
    public void playNewVideo(String newPath){
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
        fileName = newPath.substring(newPath.lastIndexOf("/")+1,newPath.length());
        Log.d("","*********************  打开新视频 ************" + fileName );
        seekBar_Progress.setProgress(0);
        seekBar_Progress.setSecondaryProgress(0);
        openVideo(newPath);
    }

    /**
     *  seekBar 接口方法重写 ，进度条监听
     *  surfaceView 接口方法重写 ，surfaceView变化监听
     *  mediaPlayer 状态监听
     */
    public void initMediaView(){

        // surfaceView 接口方法重写 ,回调
        sv_View.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed: ****************************************  surface被销毁 ");
                mediaIsShow = false;
                if(mMediaPlayer != null){
                    // 停止运行，移除 Message ,以免崩溃
                    timeHandler.removeMessages(UPDATE_UI);
                    // 记录当前播放到哪个位置
                    int position = mMediaPlayer.getCurrentPosition();
                    // 创建缓存容器
                    SharedPreferences.Editor editor  = sp.edit();
                    // 将当前位置存储在缓冲容器中
                    editor.putString("path",path);
                    editor.putInt("position", position);
                    // 异步存储，使用commit()则直接写入内存
                    editor.apply();
                    mMediaPlayer.stop();//停止播放
                    mMediaPlayer.reset();
                    mMediaPlayer.release();//释放资源
                }
            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated: **************************************** surface被创建 ");
                mMediaPlayer = new MediaPlayer();
                mediaIsShow = true;
                openVideo(path);
                // 设定播放列表的当前位置背景
                playListAdapter.changeSelect(mSelect);
                // 设定暂停的image 样式
                pauseOrPlay_Image.setImageResource(R.drawable.pause_btn_style);
//                // 同一个文件，则把播放的位置定为上次播放的位置
                if(path.equals( sp.getString("path","") ) ){
                    mMediaPlayer.seekTo(sp.getInt("position", 0));// 如果打开就从上一次停止的时候播放
                };
                Log.d(TAG,"surfaceCreated :*****************  000 *******" + sp.getString("path","") +
                        "   "+sp.getInt("position", 0));
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });
        // 进度条接口重写，回调
        seekBar_Progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser){

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){
                // 移除 Message ,避免重复刷新时间
                timeHandler.removeMessages(UPDATE_UI);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
                // 进度条停止拖动的位置
                int progress = seekBar.getProgress();
                // 拖动播放器到指定位置
                mMediaPlayer.seekTo(progress);
                // 重置显示已播放的进度时间
                setAboutVideo.updateTimeWithFormat(tv_recentTime,mMediaPlayer.getCurrentPosition());
                // 重新开始刷新进度
                timeHandler.sendEmptyMessage(UPDATE_UI);
            }
        });
    }

    /**
     *  将该记录写入数据库,再根据路径打开视频
     * @param filePath
     */
    public void openVideo( String filePath){
        saveHistory();
        seekBar_Progress.setMax(0);
        // 设定 暂停的image 样式
        pauseOrPlay_Image.setImageResource(R.drawable.pause_btn_style);
        String videoPath = filePath;
        try{
            mMediaPlayer = new MediaPlayer();
            // 设置多媒体的数据源
            mMediaPlayer.setDataSource(videoPath);
            // 指定多媒体的内容实在holder里面显示
            mMediaPlayer.setDisplay(holder);
            // 充满屏幕显示，保持比例，如果屏幕比例不对，则进行裁剪
            mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            // mMediaPlayer.setLooping(true); // 循环播放
            // 加载界面
            ly_Loading_background.setVisibility(View.VISIBLE);
            // 准备播放
            mMediaPlayer.prepareAsync ();
            mMediaPlayer.setOnPreparedListener(this);
        } catch (Exception e){
            e.printStackTrace();
            Log.d(" OpenVideoActivity", "openVideo: ********************************* openVideo 出错了  **********************");
        }
    }

    /**
     *  监听视频准备完成，接口 MediaPlayer.OnPreparedListener
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp){
        //*********************************************************** 线程不同步，？？
        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                // 第一帧开始渲染,去掉imageView
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // 去掉加载界面
                    ly_Loading_background.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });
        mp.start();
        // 如果是上次的文件，则把播放的位置定为上次播放的位置
        if(path.equals( sp.getString("path","") ) ){
            mMediaPlayer.seekTo(sp.getInt("position", 0));//如果打开就从上一次停止的时候播放
        };
        Log.d("","*********************  onPrepared  ************  " + fileName );
        // 进度条初始化
        seekBarInit();
        // 视频播放监听
        myMediaVideoListener( );

    }

    /**
     *  视频状态监听：播放完成、出现错误、缓冲更新、尺寸变化；开始渲染第一帧、网络监听
     */
    public void myMediaVideoListener( ) {
        // 播放完成监听回调
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
               // 下一个视频
                lastOrNextVideo(1);
                // 暂停
            }
        });
        // 出现 error 监听回调
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                Log.d("TAG", "onBufferingUpdate: ******************  error 出错了  *************");
                Toast.makeText(OpenVideoActivity.this,"*  error 出错了  *",Toast.LENGTH_LONG).show();
                return false;
            }
        });
        // 缓存进度更新回调
        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Log.d("TAG", "onBufferingUpdate: ****************** "+  percent+"% ***************");
                int total = mMediaPlayer.getDuration();
                // 缓存进度
                seekBar_Progress.setSecondaryProgress(percent*total/100);
            }
        });
        // 尺寸变化回调
        mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

            }
        });
        // 监听卡
        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch(what){
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                         // 第一帧开始渲染,去掉加载界面imageView
                        ly_Loading_background.setVisibility(View.GONE);
                        break;
                    case  MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.d("TAG ", "onInfo: ****************************  卡了");
                        Toast.makeText(getApplicationContext(), "网络有点卡哦",Toast.LENGTH_LONG).show();

                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Toast.makeText(getApplicationContext(), "网络已恢复",Toast.LENGTH_LONG).show();
                        break;

                }
                return false;
            }
        });

    }

    /**
     *  利用 handle 刷新进度条
     */
    private Handler timeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == UPDATE_UI){
                int currentDuratin = mMediaPlayer.getCurrentPosition();
                seekBar_Progress.setProgress(currentDuratin);
                // 刷新进度显示
                setAboutVideo.updateTimeWithFormat(tv_recentTime,currentDuratin);
                // 每0.5s 刷新一次
                timeHandler.sendEmptyMessageDelayed(UPDATE_UI,500);
            }
        }
    };

    /**
     *  进度条初始化
     */
    public void seekBarInit(){
        int total = mMediaPlayer.getDuration();
        // 显示视频总时长
        setAboutVideo.updateTimeWithFormat( tv_totalTime,total);
        seekBar_Progress.setMax(total);
        // 开始更新进度条
        timeHandler.sendEmptyMessage(UPDATE_UI);
    }

    /**
     * listView 加载适配器,及其显示 、点击监听
     */
    public void listViewShow(){

        listView = findViewById(R.id.videoName_list);
        Log.d("TAG", ":************   初始化没问题   **************");
        // 加载适配器
        playListAdapter = new PlayListAdapter(OpenVideoActivity.this, R.layout.video_show_item , videoPlayList);
        listView.setAdapter(playListAdapter);
        // 点击监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取当前位置，便于上一个，下一个视频切换
                mSelect = position;
                path = videoPlayList.get(position).getPlayPath();
                // 修改选项背景颜色
                playListAdapter.changeSelect(position);
                playNewVideo(path);
                Log.d("TAG", "onItemClick:**************************  "+path+ " 序号："+position);
            }
        });
    }

    /**
     *  播放列表数据初始化
     */
    public void videoNameListInit(){
        if(videoPlayList != null){
            videoPlayList.clear();
        }
        Log.d("TAG", ":************   初始化成功   **************");
    }

    /**
     *  触摸屏幕 隐藏或显示底部和顶部控制栏
     */
    public void showOrHide(){
        // 正在显示，将其隐藏
        if( isShowing ){
            ly_bottom__Control.setVisibility(View.GONE);
            ly_top_Control.setVisibility(View.GONE);
            isShowing = false;
            // 隐藏控制栏将播放列表也隐藏
            listView.setVisibility(View.GONE);
            list_IsShow = false;
        } else {
            // 正在隐藏，将其显示
            ly_bottom__Control.setVisibility(View.VISIBLE);
            ly_top_Control.setVisibility(View.VISIBLE);
            isShowing = true ;
            mHandler.removeCallbacks(mHide);
            mHandler.postDelayed(mHide,5000);

        }
    }

    private Handler mHandler = new Handler(); // 声明一个处理器对象
    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            showOrHide(); // 显示或者隐藏顶部与底部视图
        }
    };

    /**
     * 屏幕触摸监听，控制 底部和顶部控制栏
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN :
                // 获取按下位置坐标
                mXpos = (int)event.getX();
                mYpos = (int)event.getY();
                break;
            case MotionEvent.ACTION_UP:
                // 坐标偏移量的绝对值
                float x = Math.abs( event.getX()-mXpos );
                float y = Math.abs( event.getY()-mYpos );
                if(x<mOffset && y<mOffset){
                    showOrHide();
                }
        }
        return super.onTouchEvent(event);
    }

    /**
     *  将播放记录写入数据库
     */
    public void saveHistory(){
        HistoryVideoData hisData = new HistoryVideoData(this);
        VideoPlayParam history = videoPlayList.get(mSelect);
        hisData.saveHistoryToDB(history);
    }


    @Override
    public void finish() {
        stopFloatService(); // 停止服务
        super.finish();
        Log.d(TAG ,"******************************  onFinish");

    }


    private static final int REQUEST_CODE = 1111;

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE);
            } else {

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult: ******* 成功获取权限");
        }
    }
}
