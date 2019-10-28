package com.studyboy.lmvideo.listdata;

import android.os.Environment;
import android.util.Log;

import com.studyboy.lmvideo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.ContentValues.TAG;

public class SDFileData {

    List<SDFileBean>  SDFileList = new ArrayList<>();
    
    public   List<SDFileBean> getSDFileList(){
        if( SDFileList.size() > 0){
            Log.d(TAG, "getSDFileList: ******* 有数据的呀");
        }
        return SDFileList;
    }

    /**
     *  获取该路径下的 mp4 文件或文件夹，加入 listView 中显示
     * @param filePath
     */
    public void getFileDirectory(String filePath){

        File file = new File(filePath);

//        file.getPath();
        File[] files = file.listFiles();
        
        if( files != null ) {
            List fileList = Arrays.asList(files);
            // 文件列表按名称排列
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });
            SDFileBean sdFileParam;
            // fileShowList 列表的初始化
            initShowList();
            for (int i = 0; i < files.length; i++) {
                file = files[i];
                // 是文件夹或TXT文件
                if (checkFileShape(file)) {
                    String path = file.getAbsolutePath();
                    String name = file.getName();
                    //  根据文件夹或TXT 获取对应图标
                    if (file.isDirectory()) {
                        // 图标、路径、名字
                        sdFileParam = new SDFileBean(R.drawable.main_folder, path, name);
                        SDFileList.add(sdFileParam);
                    } else {
                        sdFileParam = new SDFileBean(R.drawable.main_mp4, path, name);
                        SDFileList.add(sdFileParam);
                    }
                }
            }
        } else {
            Log.d(TAG, "getFileDirectory: ******* 空列表");
        }

    }
    

    /**
     *  判断文件类型
     * @param file
     * @return
     */
    public boolean checkFileShape(File file){

        boolean checkShape;
        String nameString  = file.getName();
        int length = nameString.length();
        // 获取文件后缀
        String endString = nameString.substring(nameString.lastIndexOf(".")+1,length) .toLowerCase();
        // 是否为文件夹
        if(file.isDirectory()){
            checkShape = true;
        }
        else {
            if(endString.equals("mp4")){
                checkShape = true;
            } else{
                checkShape = false;
            }
        }
        return  checkShape;
    }

    /**
     *  初始化显示列表，设置第一行为根目录，第二行为返回上一级目录
     */
    public void initShowList( ){
        if(SDFileList != null){
            SDFileList.clear();
        }
        SDFileBean sdFileBean = new SDFileBean(R.drawable.main_folder,getSDRoot(),"返回根目录");

        SDFileList.add(sdFileBean);
         sdFileBean = new SDFileBean(R.drawable.main_folder," ","返回上一级");

        SDFileList.add(sdFileBean);

    }

    /**
     *  获取内存卡根目录
     * @return
     */
    public String getSDRoot(){
        String SDRoot = null;
        if(!checkSDcard()){
            Log.d(TAG, "getSDRoot: ******************************   no sdcard  ");
            SDRoot = "";
            return SDRoot;
        }
        try{
            SDRoot = Environment.getExternalStorageDirectory().toString();
        } catch(Exception e){
            Log.d(TAG, "getSDRoot: ******************************   打不开诶 ");
            e.printStackTrace();
        }
        return SDRoot;
    }

    /**
     *  检查SD 是否存在
     * @return
     */
    public boolean checkSDcard(){
        String sdString = Environment.getExternalStorageState();
        if(sdString.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
}
