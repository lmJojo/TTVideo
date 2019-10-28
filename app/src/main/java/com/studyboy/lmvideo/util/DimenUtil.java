package com.studyboy.lmvideo.util;

import android.content.Context;

public class DimenUtil {

    public static float density = 1;
    public static int dip2px(Context context, float dpValue) {
        density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }
}
