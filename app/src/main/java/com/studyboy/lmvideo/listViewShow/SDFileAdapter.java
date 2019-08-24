package com.studyboy.lmvideo.listViewShow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.studyboy.lmvideo.R;
import com.studyboy.lmvideo.listdata.SDFileParam;

/**
 *  用于显示 SD卡 的 listview 适配器
 *  ming 2019.08.09
 */

public class SDFileAdapter extends ArrayAdapter<SDFileParam> {

    private int resourceId;

    public SDFileAdapter(Context context, int textViewResourceId, List<SDFileParam> objects) {

        // 上下文，子项布局id， 数据
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SDFileParam sdFileParam = getItem(position);
        View view;
        ViewHolder viewHolder;

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.iv_Sdfile_icon = (ImageView)view.findViewById(R.id.iv_sdfile_icon);
            viewHolder.tv_Sdfile_name = (TextView) view.findViewById(R.id.tv_sdfile_name);
            // 将 viewHolder 保存在view 中
            view.setTag(viewHolder);
        } else {
            view = convertView;
            // 重新获取viewHolder
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.iv_Sdfile_icon.setImageResource(sdFileParam.getImageId());
        viewHolder.tv_Sdfile_name.setText(sdFileParam.getSdFileName());
        return view;
    }
    // 定义内部类，用于对控件的实例进行缓存 ,path 不显示出来
    class  ViewHolder {
        ImageView iv_Sdfile_icon;
        TextView tv_Sdfile_name;
    }


}
