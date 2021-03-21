/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.alibaba.gov.android.chinaoly.meeting.http;

import com.alibaba.gov.android.chinaoly.meeting.basebean.BaseBean;
import com.alibaba.gov.android.chinaoly.meeting.common.Constants;
import com.alibaba.gov.android.chinaoly.meeting.common.UserConfig;
import com.alibaba.gov.android.chinaoly.meeting.util.ToastUtil;


public abstract class HttpCallBack<T extends BaseBean> {
    public void onSucceed(T data) {
    }

    protected void onNetError() {
        ToastUtil.getInstance().showLongToast(Constants.MSG_NET_ERROR);
    }

    protected void onServerError(T data) {
        if (data != null) {
            if (data.getCode() == 598 || data.getCode() == 401 || data.getCode() == 599 || data.getMsg().equals("Token invalid.")) {
                ToastUtil.getInstance().showLongToast("账号信息已过期，请重新登陆");
                UserConfig.setRequestToken("");
                UserConfig.updateUserInfo(null);
//                Intent intent;
//                intent = new Intent(AppGlobals.get(), LoginActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                AppGlobals.get().getApplicationContext().startActivity(intent);
            } else {
                ToastUtil.getInstance().showLongToast(data.getMsg());
            }
        }
    }

    protected void onComplete(boolean success) {

    }
}
