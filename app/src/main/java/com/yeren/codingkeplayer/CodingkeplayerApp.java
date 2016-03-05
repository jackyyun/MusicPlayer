package com.yeren.codingkeplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lidroid.xutils.DbUtils;
import com.yeren.codingkeplayer.Utils.Constant;

/**
 * Created by Administrator on 2016/2/19.
 */
public class CodingkeplayerApp extends Application {
    public static SharedPreferences sp;
    public static DbUtils dbUtils;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        dbUtils = DbUtils.create(getApplicationContext(),Constant.DB_NAME);
        context = getApplicationContext();
    }
}
