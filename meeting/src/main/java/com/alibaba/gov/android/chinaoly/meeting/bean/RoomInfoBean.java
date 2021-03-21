// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.alibaba.gov.android.chinaoly.meeting.bean;


import com.alibaba.gov.android.chinaoly.meeting.basebean.BaseBean;

public class RoomInfoBean extends BaseBean {
    private MeetingBean data;

    public MeetingBean getData() {
        return data;
    }

    public void setData(MeetingBean data) {
        this.data = data;
    }
}
