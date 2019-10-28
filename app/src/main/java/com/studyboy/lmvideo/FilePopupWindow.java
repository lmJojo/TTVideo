package com.studyboy.lmvideo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.studyboy.lmvideo.listViewShow.SDFileAdapter;
import com.studyboy.lmvideo.listdata.SDFileData;
import com.studyboy.lmvideo.listdata.SDFileBean;
import com.studyboy.lmvideo.util.DimenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilePopupWindow {

      private String TAG = "FilePopupWindow";
      private Context mContext;
      // 用于设定位置
      private View  positionVIew;
      private int width,height;

      public FilePopupWindow( Context mContext,View positionVIew){
            this.mContext = mContext;
            this.positionVIew = positionVIew;
            init();
      }

      private View contentView;
      private PopupWindow mPopWindow;
      private TextView tv_path;
      private ListView lv_sdFile;

     /**
      *  初始化
      */
    public void init(){
          Log.d(TAG, "init: ************ 界面初始化");
          if( mPopWindow == null ){
              contentView = LayoutInflater.from( mContext ).inflate( R.layout.layout_popwindow, null );
              tv_path = (TextView)contentView.findViewById( R.id.tv_path );
              lv_sdFile = (ListView) contentView.findViewById( R.id.video_sdfile_list );

              // 获取数据并加载到适配器
//              checkPermission();
              getRootData();
              showSdFileList();

              width = DimenUtil.dip2px( mContext,400 );
              height = DimenUtil.dip2px( mContext,686 );
              mPopWindow = new PopupWindow( width, height );
//              mPopWindow = new PopupWindow( WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.MATCH_PARENT );

              mPopWindow.setContentView( contentView );
              mPopWindow.setOutsideTouchable(true);
              mPopWindow.setTouchable(true);
              mPopWindow.setFocusable(true);

          } else {

          }

          mPopWindow.showAsDropDown( positionVIew ,0 ,0);
          mPopWindow.setHeight( height );
          mPopWindow.setWidth( width );
          Log.d(TAG, "init: ******* 显示 popupWindow");
      }

      private SDFileData mSDFileData;
      private String rootPath, currentPath;
      private List<SDFileBean> SDFileList = new ArrayList<>();
      private SDFileAdapter mAdapter;

    /**
     * 获取根目录
     */
    public void getRootData(){
        if( mSDFileData == null ){
            mSDFileData = new SDFileData();
        }
        rootPath = mSDFileData.getSDRoot();
        getLiatData( rootPath );
    }

    public void getLiatData(String path){
        currentPath = path ;
        tv_path.setText("路径："+ currentPath);
        // 获取数据列表
        mSDFileData.getFileDirectory( path );
        SDFileList = mSDFileData.getSDFileList();

        if( mAdapter != null ){
            mAdapter.ChangeListData( SDFileList );
        }
    }

    /**
     *  列表加载适配器
     */
    public void showSdFileList(){

        mAdapter = new SDFileAdapter(mContext, R.layout.sdfile_show_item , SDFileList);
        lv_sdFile.setAdapter( mAdapter );
        lv_sdFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Itempoint( position );
            }
        });

    }


    /**
     *  列表item 点击
     */
    public void Itempoint(int position ){
        // 点击项路径
        String  itemPath = SDFileList.get( position ).getSdFilePath();
        File newFile = new File( itemPath );

        // 返回根目录
        if(position == 0){
            getLiatData( rootPath );
        }
        // 返回上一级目录,SDfileName 为“ 返回上一级 ”，故加入 recentPath 作当前路径
        else if(position == 1){

            File currentFile = new File( currentPath );
            if(!currentFile.exists() || currentFile.length() == 0 || currentPath.equals(rootPath)) {
               Toast.makeText( mContext,"上级目录不存在",Toast.LENGTH_SHORT ).show();
            }
            else{
                getLiatData( currentFile.getParent());
            }
        }
        // 点击文件夹或mp4 文件
        else{

            if(newFile.isDirectory()){
                getLiatData( itemPath );
            }
            else{
                // mp4 文件，返回其完整路径
                Intent intent = new Intent();
                long fileLength = newFile.length();

                    // 接口回调
                    mListener.onFilePath( SDFileList.get( position ));


            }
        }
    }

    /**
     *  回调接口
     */
    public interface OnWindowListen{
        void onFilePath( SDFileBean sdFileBean );
    }
    private OnWindowListen mListener;
    public void setOnWindowlisten(OnWindowListen mListener){
        this.mListener = mListener;
    }


    public void checkPermission(){

        if (Build.VERSION.SDK_INT >= 23) {
            // 检查权限
            int readCheck = mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE );
            int writeCheck = mContext.checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE );
            if( readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED ){
                // 已有权限
                getRootData();
            } else {
                // 没有权限 ，api > 23 还需要手动申请权限
                ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

        }
    }

}
