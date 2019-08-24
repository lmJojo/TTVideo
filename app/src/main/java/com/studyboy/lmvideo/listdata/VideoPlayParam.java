package com.studyboy.lmvideo.listdata;

import java.io.Serializable;

/**
 *  列表传输到 OpenVideoActivity 的类型，包含路径、名字；利用 Serializable
 *  ming 2019.08.08
 */
public class VideoPlayParam implements Serializable {

    private String playPath;
    private String playName;


    public void setPlayPath(String playPath) {
        this.playPath = playPath;
    }
    public void setPlayName(String playName) {
        this.playName = playName;
    }

    public String getPlayPath() {
        return playPath;
    }
    public String getPlayName() {
        return playName;
    }


}
