package com.studyboy.lmvideo.listViewShow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.studyboy.lmvideo.R;
import com.studyboy.lmvideo.SetAboutVideo;
import com.studyboy.lmvideo.listdata.VideoPlayParam;

import java.util.List;

/**
 *  播放界面视频列表的适配器,VideoPlayParam 包含 path 、name
 *  ming 2019.08.05
 */

public class PlayListAdapter extends ArrayAdapter<VideoPlayParam> {

    private int resourceId;
    // 选中项
    private int mSelect = 0;
    SetAboutVideo setAboutVideo;
    public PlayListAdapter(Context context, int textViewResourceId, List<VideoPlayParam> objects) {

        // 上下文，子项布局id， 数据
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    // 更新点击位置
    public void changeSelect(int position){
        mSelect = position;
        // 相当于重新执行一次getView（）
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
         // 存路径，显示名字
         String path = getItem(position).getPlayPath();
         View view;
         // 避免反复加载布局
         if(convertView == null){
             view = LayoutInflater.from(getContext()).inflate(resourceId ,parent,false );
         } else {
             view = convertView;
         }
        TextView tv_videoName = (TextView) view.findViewById(R.id.tv_videoname);
        String videoName = getItem(position).getPlayName();
        // 显示视频名字
        tv_videoName.setText(videoName);

        if( position == mSelect){
            // 当前选中项背景
            view.setBackgroundResource(R.color.orangeback);
        } else {
            // 非当前选中项背景
            view.setBackgroundResource(R.color.transparent);  // transparent  orange
        }
         return view;
    }
}
