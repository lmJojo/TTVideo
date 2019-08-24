package com.studyboy.lmvideo.listdata;

/**
 *  用于本地手动打开的 listView 类型显示
 */
public class SDFileParam {

    private int ImageId;
    private String sdFilePath;
    private String sdFileName;

    public SDFileParam(int imageId,String sdFilePath,String sdFileName){
        this.ImageId = imageId;
        this.sdFilePath = sdFilePath;
        this.sdFileName = sdFileName;
    }
    public int getImageId() {
        return ImageId;
    }
    public String getSdFilePath() {
        return sdFilePath;
    }
    public String getSdFileName() {
        return sdFileName;
    }



}
