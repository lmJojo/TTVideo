package com.studyboy.lmvideo;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


public class VideoFloatService extends Service {
    public static final String TAG = "VideoFloatService";
    private WindowManager windowManager;
    private WindowManager.LayoutParams wmParams;
    /** 声明一个内容视图对象 */
    private View mContentView;

    /** 当前X、Y 坐标，上次触摸点坐标*/
    private int recentX,recentY;
    private int lastX,lastY;
    Boolean isShowing = false,iconIsShowing = true;

    private ImageView iv_Close, iv_Pause,iv_Full  ;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer; // 定义多媒体播放器   可以播放音频也可以播放视频
    private SurfaceHolder holder;
    private String path = null;
    private int position = 0;

    // 定义缓存容器
    private SharedPreferences sp;

    private FloatWindowBinder fwBinder = new FloatWindowBinder();

    /**
     *  用于与活动绑定
     */
    class FloatWindowBinder extends Binder{

        public VideoFloatService getService(){
            return VideoFloatService.this;
        }
        public boolean isShowing(){
            Log.d(TAG, "onUnbind: **********************************  悬浮窗是否打开 ");
            // 判断悬浮窗是否打开
            return isShowing;
        }
        public void openFloatWindow( String videoPath, int videoPosition){
            FloatWindow( videoPath, videoPosition);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onUnbind: **********************************  service 绑定 ");
        return fwBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // 解除绑定时调用
        Log.d(TAG, "onUnbind: **********************************  service 解除绑定 ");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //   path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/studyboy.mp4";
        String videoPath = intent.getStringExtra("videoPath");
        int videoPosition = intent.getIntExtra("position",0);
        //  获取容器缓冲区的存储位置   私有模式
        sp = getSharedPreferences("config", MODE_PRIVATE);
        FloatWindow(videoPath, videoPosition);
        return super.onStartCommand(intent, flags, startId);
    }

    public void FloatWindow(String videoPath, int videoPosition){

        // 延时隐藏控制栏
        mHandler.postDelayed( mHide,4000 );

        path = videoPath;
        position = videoPosition;
        initFloatWindow();
        initContentView();

        // 添加视图到悬浮窗
        windowManager.addView(mContentView,wmParams);
        isShowing = true;
    }

    /**
     *  悬浮窗初始化 及 参数设置
     */
    public void initFloatWindow(){

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        // 设置LayoutParam
        wmParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;   // 不占用焦点
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.alpha = 1.0f;    // 不透明  500 ; //  100 ; //
        wmParams.width =  500 ; // WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = 350 ; // WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.x = 300;        // 初始位置
        wmParams.y = 300;

    }
    /**
     *  内容视图获取，SurfaceView 等初始化 、监听
     */
    public void initContentView(){
        // 获取内容视图对象
        mContentView = LayoutInflater.from(this).inflate(R.layout.float_window_layout,null);

        iv_Close = (ImageView) mContentView.findViewById(R.id.iv_close);
        iv_Pause = (ImageView) mContentView.findViewById(R.id.iv_pause);
        iv_Full=  (ImageView) mContentView.findViewById(R.id.iv_full);
        mContentView.setOnTouchListener(new FloatOnTouchListener());

        iv_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onTouch: ************************************* 点击关闭图标 ");
                closeFloatWindow();
                // 回调，传递数据,不返回全屏
                fListener.onFloatClick(path,position,false);
            }
        });
        iv_Pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onTouch: ************************************* 点击播放暂停图标 ");
                pauseOrPlay();
            }
        });
        iv_Full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onTouch: ************************************* 点击全屏图标 ");
                FullWindow();
            }
        });

        surfaceView = (SurfaceView) mContentView.findViewById(R.id.float_surface_view);
        holder =  surfaceView.getHolder();
        // 设置容器   注册回调,监听surfaceView 的变化
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "onTouch: ************************************* surfaceView 被创建 ");
                openVideo(path,position);
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "onTouch: ************************************* surfaceView 被销毁 ");
                if(mediaPlayer!=null){
                    // 回调，传递路径及当前播放位置
                    position = getPosition();

                    //创建缓存容器
                    SharedPreferences.Editor editor = sp.edit();
                    //将当前位置存储在缓冲容器中
                    editor.putString("path",path);
                    editor.putInt("position", position);
                    // 异步存储，使用commit()则直接写入内存
                    editor.apply();
                    mediaPlayer.stop();//停止播放
                    mediaPlayer.reset();
                    mediaPlayer.release();//释放资源

                }
            }
        });


    }

    /**
     *  根据路径和位置播放视频
     * @param filePath
     * @param videoPosition
     */
    public void openVideo( String filePath,int videoPosition){
        String videoPath = filePath;
        try{
            mediaPlayer = new MediaPlayer();
            // 指定多媒体的内容实在holder里面显示
            mediaPlayer.setDisplay(holder);
            // 设置多媒体的数据源
            mediaPlayer.setDataSource(videoPath);
            // 准备播放
            mediaPlayer.prepare();
            Log.d(TAG, "onTouch: ************************************* 执行播放 ");
        } catch (Exception e){
            Log.d(TAG, "onTouch: ************************************* 播放失败 ");
            e.printStackTrace();
        }
        // 开始播放视频
        mediaPlayer.start();
        mediaPlayer.seekTo(videoPosition);
    }
    /** 暂停或继续播放 */
    public void pauseOrPlay(){
        if(mediaPlayer.isPlaying()){
            // 暂停播放
            iv_Pause.setImageResource(R.drawable.service_pause);
            mediaPlayer.pause();
        } else {
            iv_Pause.setImageResource(R.drawable.service_play);
            // 继续播放
            mediaPlayer.start();
        }
    }

    /**
     *  获取MediaPlayer 播放的位置
     * @return
     */
    public int getPosition() {
        if (mediaPlayer != null) {
            try {
                position = mediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return position;
    }
    /**
     *  恢复全屏
     */
    public void  FullWindow(){
        Intent service = new Intent();
        service.setClass(this, OpenVideoActivity.class);
        service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(service);

        closeFloatWindow();
//        // 回调，传递数据,返回全屏
//        fListener.onFloatClick(path,position,true);
    }
    /**
     * 关闭悬浮窗，服务在  OpenVideoActivity 中停止
     */
    public void closeFloatWindow(){
        if(windowManager != null){
            Log.d(TAG, "closeFloatWindow: **********************************  移除contentView");
            windowManager.removeView(mContentView);   // 移除，surface 被摧毁，回调
            windowManager = null;
            isShowing = false;
        }
    }

    /**
     *  触摸监听
     */
    private class FloatOnTouchListener implements View.OnTouchListener{
        int downX,downY;
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // 获取当前位置的X、Y 坐标,包括按下、移动、松开
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    lastX = (int)event.getRawX();
                    lastY = (int)event.getRawY();
                    downX = lastX;
                    downY = lastY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "onTouch: ************************************* 这是移动 ");
                    recentX = (int)event.getRawX();
                    recentY = (int)event.getRawY();
                    // X 、Y 的位移
                    int moveX = recentX - lastX;
                    int moveY = recentY - lastY;
                    updateViewPosition(moveX,moveY);
                    lastX = recentX;
                    lastY = recentY;;
                    break;
                case MotionEvent.ACTION_UP:
                    recentX = (int)event.getRawX();
                    recentY = (int)event.getRawY();
                    // X 、Y 的位移
                    moveX = recentX - downX;
                    moveY = recentY - downY;
                    if( Math.abs(moveX)<3 && Math.abs(moveY)<3 ){
                        Log.d(TAG, "onTouch: ************************************* 这是点击 ");
                        hideOrShow();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    public void hideOrShow(){
        if(iconIsShowing){
            iv_Close.setVisibility(View.GONE);
            iv_Full.setVisibility(View.GONE);
            iv_Pause.setVisibility(View.GONE);
            iconIsShowing = false;
        }
        else {
            iv_Close.setVisibility(View.VISIBLE);
            iv_Full.setVisibility(View.VISIBLE);
            iv_Pause.setVisibility(View.VISIBLE);
            iconIsShowing = true;
            mHandler.removeCallbacks(mHide);
            mHandler.postDelayed( mHide,4000 );
        }
    }

    private Handler mHandler = new Handler(); // 声明一个处理器对象
    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            hideOrShow(); // 显示或者隐藏顶部与底部视图
        }
    };

    /**
     *  更新悬浮窗位置
     * @param moveX
     * @param moveY
     */
    public void updateViewPosition(int moveX,int moveY){
        wmParams.x = wmParams.x + moveX;
        wmParams.y = wmParams.y + moveY;
        // 更新布局位置参数
        windowManager.updateViewLayout(mContentView,wmParams);

    }

    /**
     *   接口回调，传递文件名、当前播放位置
     */
    private FloatClickListener fListener;
    public void setOnFloatListener (FloatClickListener fListener){
        this.fListener = fListener;
    }
    public interface FloatClickListener{
        void onFloatClick(String name,int progress,boolean shouldFull);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ******************************************** 关闭service");
        closeFloatWindow();
        super.onDestroy();

    }



}
