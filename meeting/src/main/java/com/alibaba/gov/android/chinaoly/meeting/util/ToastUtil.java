/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.alibaba.gov.android.chinaoly.meeting.util;

import android.widget.Toast;


public class ToastUtil {
    private static ToastUtil instance;

    private ToastUtil() {
    }

    public static ToastUtil getInstance() {
        if (instance == null) {
            synchronized (ToastUtil.class) {
                if (instance == null) {
                    instance = new ToastUtil();
                }
            }
        }
        return instance;
    }

    public void showLongToast(Object obj) {
        showToast(obj, Toast.LENGTH_LONG);
    }

    private static void showToast(Object obj, int time) {
        Toast.makeText(AppGlobals.get(), null == obj ? "Unknow Error" : obj.toString(), time).show();
    }

    public void showShortToast(Object obj) {
        showToast(obj, Toast.LENGTH_SHORT);
    }
}
