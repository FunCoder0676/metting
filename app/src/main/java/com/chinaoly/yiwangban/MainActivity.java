package com.chinaoly.yiwangban;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.alibaba.gov.android.chinaoly.meeting.bean.RoomInfoBean;
import com.alibaba.gov.android.chinaoly.meeting.bean.UserInfoBean;
import com.alibaba.gov.android.chinaoly.meeting.common.UserConfig;
import com.alibaba.gov.android.chinaoly.meeting.http.HttpCallBack;
import com.alibaba.gov.android.chinaoly.meeting.util.Requester;
import com.alibaba.gov.android.chinaoly.meeting.view.MeetingActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        startActivity(new Intent(this, WebActivity.class));

        TextView textView = findViewById(R.id.client_tv);


        textView.setOnClickListener(v ->
                login()
        );
    }


    public void login() {
        Requester.login("15267890676", "67781395", new HttpCallBack<UserInfoBean>() {
            @Override
            public void onSucceed(UserInfoBean data) {
                UserConfig.setSpAdmin("15267890676");
                UserConfig.setSpPassword("67781395");

                UserConfig.updateUserInfo(data);
                UserConfig.setRequestToken(data.getData().getToken());

                meeting();
            }

            @Override
            protected void onComplete(boolean success) {
            }
        });

    }

    public void meeting() {
        Requester.enterMeeting(MainActivity.this, "915105013005", "", new HttpCallBack<RoomInfoBean>() {
            @Override
            public void onSucceed(RoomInfoBean data) {
                startActivity(new Intent(MainActivity.this, MeetingActivity.class)
                        .putExtra(MeetingActivity.ROOM_INFO, data.getData())
                );
            }

            @Override
            protected void onComplete(boolean success) {

            }
        });
    }
}