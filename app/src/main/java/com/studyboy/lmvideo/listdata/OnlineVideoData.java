package com.studyboy.lmvideo.listdata;

import java.util.ArrayList;
import java.util.List;

/**
 *  获取在线视频列表，只有 path 和 name 的值
 */
public class OnlineVideoData {

    List<VideoParam>  onlineVideoList = new ArrayList<>();
    String size = " ";              // 后期判断为 " " 的话，显示unknown
    String duration = " ";
    public  List<VideoParam> getOnlineVideoData(){

        String path11 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4";
        String path3 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319222227698228.mp4";
        String path4 = "https://gslb.miaopai.com/stream/P4DnrjGZ7PzC2LfQK9k2cAKEIw39GiixIBpIHA__.mp4";  // 网络视频
        String path5 = "http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4";           // 演讲视频 4s
        String path6 = "http://vfx.mtime.cn/Video/2019/03/18/mp4/190318231014076505.mp4";
        String path7 = "http://vfx.mtime.cn/Video/2019/03/17/mp4/190317150237409904.mp4";
        String path8 = "http://vfx.mtime.cn/Video/2019/03/12/mp4/190312143927981075.mp4";
        String path9 = "http://vfx.mtime.cn/Video/2019/03/14/mp4/190314223540373995.mp4";
        String path10 = "https://www.apple.com/105/media/cn/iphone-x/2017/01df5b43-28e4-4848-bf20-490c34a926a7/films/feature/iphone-x-feature-cn-20170912_1280x720h.mp4";

        String[] path = { path11, path3 ,path4,path5,path6, path7, path8 ,path9,path10};

        for(int i = 0;i < path.length; i++ ){
            VideoParam videoParam = new VideoParam();
            // 获取 path 和name
            String name = getVideoFileName(path[i]);
            videoParam.setVideoName(name);
            videoParam.setVideoPath(path[i]);
            videoParam.setvideoSize(size);
            videoParam.setVideoTime(duration);

            onlineVideoList.add(videoParam);
        }

        return onlineVideoList;
    }

    /**
     *  根据 http 链接返回视频名字
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
}
