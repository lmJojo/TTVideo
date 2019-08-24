package com.studyboy.lmvideo;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  OpenVideoActivity 相关的调用方法
 *  ming 2019.08.04
 */

public class SetAboutVideo {

    // 根据手机的分辨率从 dp 的单位 转成为 px(像素) ,用于坐标 位移偏移量
    public static int dip2px(Context context, float dpValue) {
        // 获取当前手机的像素密度
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f); // 四舍五入取整
    }

    // 根据获取的时间设定格式
    public static void updateTimeWithFormat(TextView textview, int millsecond){
        int second = millsecond/1000;
        int hh = second/3600;
        int mm = second %3600/60;
        int ss = second %60;
        String str = null;
        if(hh != 0){
            str = String.format("%02d:%02d:%02d",hh,mm,ss);

        } else {
            str = String.format("%02d:%02d",mm,ss);
        }
        textview.setText(str);
    }
    /** 获取时间 */
    public  static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String str = sdf.format(date);
        return str;
    }
    /**
     *  根据 本地路径或 http 链接返回视频名字
     * @param path
     * @return
     */
    public static String getVideoFileName(String path){
        // 获取" / " 最后出现的位置，没有则返回 -1+1 =0
        String name = null;
        int startPosition = path.lastIndexOf("/")+1;
        if(startPosition > 0) {
            name = path.substring(startPosition, path.length());
        } else {
            // 没有符号“ / ”，直接返回 path
            name = path;
        }
        return name;
    }

    /**
     *   视频播放列表初始化 List<String> String[]
     */
    public static List<String>  getVideoNameArray(  ){
        Log.d("TAG", ":************   数据初始化   **************");

        String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/studyboy.mp4";
        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/oceanWorld.mp4";  // 海洋世界

        String path11 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4";
        String path3 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319222227698228.mp4";
        String path4 = "https://gslb.miaopai.com/stream/P4DnrjGZ7PzC2LfQK9k2cAKEIw39GiixIBpIHA__.mp4";  // 网络视频
        String path5 = "http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4";           // 演讲视频 4s
        String path6 = "http://vfx.mtime.cn/Video/2019/03/18/mp4/190318231014076505.mp4";
        String path7 = "http://vfx.mtime.cn/Video/2019/03/17/mp4/190317150237409904.mp4";
        String path8 = "http://vfx.mtime.cn/Video/2019/03/12/mp4/190312143927981075.mp4";
        String path9 = "http://vfx.mtime.cn/Video/2019/03/14/mp4/190314223540373995.mp4";
        String path10 = "https://www.apple.com/105/media/cn/iphone-x/2017/01df5b43-28e4-4848-bf20-490c34a926a7/films/feature/iphone-x-feature-cn-20170912_1280x720h.mp4";
        String[] videoName = { path1,path2,path11, path3 ,path4,path5,path6, path7, path8 ,path9,path10};
        List<String> videoNameList = new ArrayList<>();
        Log.d("TAG", ":************   产生了   **************");
        for(int i = 0;i < videoName.length; i++ ){
            videoNameList.add(videoName[i]);
        }
      return videoNameList;
}
}

