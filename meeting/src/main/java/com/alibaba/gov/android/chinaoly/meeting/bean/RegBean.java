/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.alibaba.gov.android.chinaoly.meeting.bean;


import com.alibaba.gov.android.chinaoly.meeting.basebean.BaseBean;

public class RegBean extends BaseBean {
    private String addr;
    private int port;


    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
