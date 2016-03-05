package com.yeren.codingkeplayer.Utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;

import com.yeren.codingkeplayer.CodingkeplayerApp;

/**
 * Created by Administrator on 2016/2/23.
 */
public class AppUtils {
    public static void hideInputMethod(View view){
        InputMethodManager imm = (InputMethodManager) CodingkeplayerApp.
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm.isActive()){
            imm.hideSoftInputFromInputMethod(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
