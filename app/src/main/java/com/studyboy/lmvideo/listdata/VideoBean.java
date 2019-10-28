package com.studyboy.lmvideo.listdata;

/**
 *  主页视频列表 listView 显示类型 ，包括 名字、 大小、时长、路径
 *  图标已固定，路径不显示
 */
public class VideoBean {

//    private int imageId;
    private String videoPath = null;
    private String videoName = null;
    private String videoSize = null;
    private String videoTime = null;

//    public void setImageId(int imageId) {
//        this.imageId = imageId;
//    }

    public void setVideoTime(String videoTime) {
        this.videoTime = videoTime;
    }
    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }
    public void setvideoSize(String videoDate) {
        this.videoSize = videoDate;
    }
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

//    public int getImageId() {
//        return imageId;
//    }
    public String getVideoTime() {
        return videoTime;
    }
    public String getVideoName() {
        return videoName;
    }
    public String getVideoSize() {
        return videoSize;
    }
    public String getVideoPath() {
        return videoPath;
    }
}
