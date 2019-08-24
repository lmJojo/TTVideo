package com.studyboy.lmvideo.listViewShow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.studyboy.lmvideo.R;
import com.studyboy.lmvideo.listdata.VideoParam;

import java.util.List;

/**
 *  主界面视频列表的适配器，设定名字，大小，时长， 图标已固定，路径不显示
 *  ming 2019.08.07
 */
public class MainListAdapter extends ArrayAdapter<VideoParam> {

    private int resourceId;
    public MainListAdapter(Context context, int textViewResourceId, List<VideoParam> objects) {

        // 上下文，子项布局id， 数据
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        VideoParam videoParam = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();

            viewHolder.videoName =  (TextView)view.findViewById (R.id.main_list_name);
            viewHolder.videoSize =  (TextView)view.findViewById (R.id.main_list_size);
            viewHolder.videoTime =  (TextView)view.findViewById (R.id.main_list_time);
            // 将 viewHolder 保存在view 中
            view.setTag(viewHolder);
        } else {
            view = convertView;
            // 重新获取viewHolder
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.videoName.setText( videoParam.getVideoName() );
        setSizeOrTime( videoParam.getVideoSize(), viewHolder.videoSize);
        setSizeOrTime( videoParam.getVideoTime(), viewHolder.videoTime);
        return view;
    }
    // 定义内部类，用于对控件的实例进行缓存 ,videoPath 不显示出来
    class ViewHolder {

        TextView  videoName,videoSize,videoTime;
    }

    public void setSizeOrTime(String str,TextView textView){
        // 大小或时长的设置,为空时，设定为 *****************   一个空格  ****************************************
        if(str.equals(" ")){
            textView.setText("<unknown>");
        }else {
            textView.setText( str );
        }
    }
}
