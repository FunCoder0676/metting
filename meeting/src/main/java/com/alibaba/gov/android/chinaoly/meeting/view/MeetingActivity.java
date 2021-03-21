package com.alibaba.gov.android.chinaoly.meeting.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.alibaba.gov.android.chinaoly.meeting.R;
import com.alibaba.gov.android.chinaoly.meeting.bean.MeetingBean;
import com.alibaba.gov.android.chinaoly.meeting.bean.MemberBean;
import com.alibaba.gov.android.chinaoly.meeting.common.UserConfig;
import com.alibaba.gov.android.chinaoly.meeting.databinding.ActivityMeetingBinding;
import com.alibaba.gov.android.chinaoly.meeting.util.DisplayUtil;
import com.alibaba.gov.android.chinaoly.meeting.util.ToastUtil;
import com.freewind.vcs.AudioStatusCallback;
import com.freewind.vcs.CameraPreview;
import com.freewind.vcs.Models;
import com.freewind.vcs.RoomClient;
import com.freewind.vcs.RoomEvent;
import com.freewind.vcs.RoomServer;
import com.freewind.vcs.bean.AudioStatusBean;
import com.ook.android.VCS_EVENT_TYPE;
import com.ook.android.ikPlayer.VcsPlayerGlSurfaceView;

import java.util.List;

public class MeetingActivity extends PermissionActivity implements RoomEvent, CameraPreview {

    RoomClient roomClient;
    private int videoH = 480;
    private int videoW = 848;
    private int fps = 20;//如果视频源来自摄像头，24FPS已经是肉眼极限，所以一般20帧的FPS就已经可以达到很好的用户体验了
    private int bitRate = 1024;

    private boolean isFront = true;//是否前置
    private int roomSdkNo;
    private static String TAG = "4444444444";
    public static final String ROOM_INFO = "room_info";
    private MeetingBean meetingBean;
    private int agc = 10000;//自动增益
    private int aec = 12;//回音消除
    private int sampleRate = 48000;

    int level = 1;//0:720P  1:1080P
    private WindowAdapter windowAdapter;

    public int spanCount = 2;

    public MemberBean mainWindowMember;//保存在主窗口的成员的信息
    public MemberBean selfMember;

    @Override
    public void onEnter(int result) {
        //如果result != 0 则表示服务器上的token已经失效，需要进行重新进入房间的逻辑
        Log.e("2222222", result + "");
        if (result != 0) {
            ToastUtil.getInstance().showLongToast("正在进行重连");
            if (binding.progressBar.getVisibility() == View.VISIBLE) {
                return;
            }
            if (meetingBean.getAccount() == null || meetingBean.getAccount().getRoom() == null) {
                return;
            }
        }
    }

    @Override
    public void onExit(int result) {
        Log.e(TAG, "你离开了会议室 result:" + result);
    }

    @Override
    public void onNotifyRoom(Models.Room room) {
        Log.e(TAG, "onNotifyRoom"
                + "  id:" + room.getId() + "  sdkNo:" + room.getSdkNo()
                + "  sharingAccId:" + room.getSharingAccId()
                + "  sharingType:" + room.getSharingType()
                + "  whiteBoard:" + room.getWhiteBoard() + "  state:" + room.getState()
                + "  type:" + room.getType());

        switch (room.getSharingType().getNumber()) {

            case Models.SharingType.ST_Desktop_VALUE://开启

                int mask = 0;
                int sdkNo = 0;
                try {
                    sdkNo = Integer.parseInt(room.getSharingSdkno());
                    mask = Integer.parseInt(room.getSharingStreamId());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                roomClient.setStreamTrack(sdkNo, mask);
                break;
        }
    }

    @Override
    public void onNotifyKickOut(String accountId) {
        Log.e(TAG, "onNotifyKickout   你被踢出了会议室");
        onBackPressed();
    }

    /**
     * Account类部分字段
     * String id;                          //id
     * int StreamId;                       //流媒体连接标识
     * String name;                        //用户名
     * String nickname;                    //昵称
     * Models.ConferenceRole role;         //参会角色
     * Models.DeviceState videoState;      //视频状态
     * Models.DeviceState audioState;      //音频状态
     * int terminalType;                   //登录终端类型：1-PC,2-Android,3-IOS,4-安卓一体机,5-录播主机
     */
    @Override
    public void onNotifyEnter(Models.Account account) {
        final int sdkNo = account.getStreamId();
        Log.e("5555555", "onNotifyEnter: 有人进入房间" + "  sdkno: " + sdkNo);

        roomClient.pickMember(account.getStreamId(), true);

        final MemberBean memberBean = new MemberBean();
        memberBean.setSdkNo(sdkNo);
        memberBean.setAccountId(account.getId());
        memberBean.setCloseVideo(account.getVideoState());
        memberBean.setMute(account.getAudioState());
        memberBean.setCloseOtherAudio(false);
        memberBean.setCloseOtherVideo(false);

        if (windowAdapter.getMemberList().isEmpty()) {
            windowAdapter.addItem(memberBean);
        } else {
            int count = 0;
            for (MemberBean s : windowAdapter.getMemberList()) {
                if (s.getSdkNo() == sdkNo) {
                    count++;
                    break;
                }
            }
            if (count == 0) {
                windowAdapter.addItem(memberBean);
            }
        }
    }

    @Override
    public void onNotifyExit(Models.Account account) {
        final int sdkNo = account.getStreamId();
        Log.e("5555555", "onNotifyExit: 有人离开房间" + "  roomSdkNo: " + sdkNo);

        windowAdapter.removeItem(sdkNo);
        if (mainWindowMember.getSdkNo() == sdkNo) {
            mainWindowMember = selfMember;
            binding.cameraTextureView.setCameraId(isFront ? 1 : 0);
            updateViewInfo();
        }
    }

    @Override
    public void onNotifyBegin(String roomId) {
        Log.e(TAG, "onNotifyBegin");
    }

    @Override
    public void onNotifyEnd(String roomId) {
        Log.e(TAG, "onNotifyEnd");
        ToastUtil.getInstance().showLongToast("主持人结束会议");
        onBackPressed();
    }

    @Override
    public void onFrame(byte[] ost, byte[] tnd, byte[] trd, int width, int height, int format, int streamId, int mask, int label) {
        Log.e("3333333333", "onFrame  " + "  clientId: " + streamId + "   " + width + " " + height + "   mask:" + mask);
        if (windowAdapter != null) {
            VcsPlayerGlSurfaceView vcsPlayerGlTextureView = getTargetSurfaceView(streamId);
            if (vcsPlayerGlTextureView != null) {
                vcsPlayerGlTextureView.update(width, height, format);
                vcsPlayerGlTextureView.update(ost, tnd, trd, format, label);
            }
        }
    }

    private VcsPlayerGlSurfaceView getTargetSurfaceView(int sdkNo) {
        VcsPlayerGlSurfaceView vcsPlayerGlTextureView = null;
        if (mainWindowMember != null && mainWindowMember.getSdkNo() == sdkNo) {
            vcsPlayerGlTextureView = binding.cameraTextureView;
        } else {
            WindowAdapter.MyViewHolder holder;
            if (sdkNo == selfMember.getSdkNo()) {
                holder = windowAdapter.getHolders().get(mainWindowMember.getSdkNo());
            } else {
                holder = windowAdapter.getHolders().get(sdkNo);
            }
            if (holder != null && holder.textureView != null) {
                vcsPlayerGlTextureView = holder.textureView;
            }
        }
        return vcsPlayerGlTextureView;
    }

    @Override
    public void onSendInfo(String info) {
        //delay: 移动40-45， wifi 30  有线 20
        // 60000270::delay=17 status=1 speed=687 buffer=0 overflow=0 */
        // 60000270=id, delay=上传到服务器之间的延迟时间,越大越不好, status=-1上传出错 >=0正常, speed=发送速度 buffer=缓冲包0-4正常 */
        Log.e(TAG, "onSendInfo" + info);
    }

    /**
     * {
     * "recvinfo": [
     * {
     * "linkid": 12340001,  对方Sdkno
     * "recv": 4127 接收包信息
     * "comp": 13,  补偿 高 网络不稳定
     * "losf": 0,   丢失包信息  高 就是网络差
     * "lrl": 6.8, //短时端到端丢包率（对方手机到你手机）
     * "lrd": 8.9 //短时下行丢包率（服务器到你）
     * }
     * ]
     * }
     */
    @Override
    public void onRecvInfo(String info) {
        Log.e(TAG, "onRecvInfo" + info);
    }

    @Override
    public void onXBitrate(int level, int bitRate) {
        switch (level) {
            case VCS_EVENT_TYPE.VCS_START_XBITRATE://自适应模式启动
                break;
            case VCS_EVENT_TYPE.VCS_BITRATE_RECOVERED://恢复码率
                break;
            case VCS_EVENT_TYPE.VCS_BITRATE_HALF_BITRATE://降为1/2
                break;
            case VCS_EVENT_TYPE.VCS_BITRATE_QUARTER_BITRATE://降为1/4
                break;

        }
    }

    @Override
    public void onNotifyAccount(Models.Account account) {
        Log.e(TAG, "onNotifyAccount" + "   delay:" + account.getDelay()
                + "  id:" + account.getId() + "  streamId:" + account.getStreamId()
                + "  name:" + account.getName() + "  nickname:" + account.getNickname()
                + "  videoState:" + account.getVideoState() + "  audioState:" + account.getAudioState() + "  role:" + account.getRole());

        StringBuilder streamLog = new StringBuilder("StreamList:   streamId:" + account.getStreamId());
        for (Models.Stream stream : account.getStreamsList()) {
//            streamLog.append("\n  id：").append(stream.getId()).append("   angle:").append(stream.getAngle()).append("    channel:").append(stream.getChannel()).append("    name:").append(stream.getName()).append("   type:").append(stream.getType()).append("   channelType:").append(stream.getChannelType());
        }
        Log.e(TAG, streamLog.toString());

        List<MemberBean> memberBeans = windowAdapter.getMemberList();
        int size = memberBeans.size();

        boolean isChange = false;

        for (int i = 0; i < size; i++) {
            MemberBean memberBean = memberBeans.get(i);
            if (memberBean.getAccountId().equals(account.getId())) {
                if (memberBean.getCloseVideo() != account.getVideoState()) {//状态发送改变
                    isChange = true;
                    memberBean.setCloseVideo(account.getVideoState());
                }
                if (memberBean.getMute() != account.getAudioState()) {
                    isChange = true;
                    memberBean.setMute(account.getAudioState());
                }
                break;
            }
        }
        if (isChange) {
            // TODO: 2019/10/29 用notifyDataSetChanged会黑屏一下，可以直接拿到控件去控制
            windowAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onNotifyMyAccount(RoomServer.MyAccountNotify notify) {
        //sdk已经把数据同步到RoomClient的account里面了，
        Models.Account account = notify.getAccount();
        Log.e(TAG, "onNotifyMyAccount"
                + "  id:" + account.getId() + "  streamId:" + account.getStreamId()
                + "  name:" + account.getName() + "  nickname:" + account.getNickname()
                + "  hasVideo:" + account.hasVideoState() + "   hasAudio:" + account.hasAudioState()
                + "  videoState:" + account.getVideoState() + "  audioState:" + account.getAudioState() + "  role:" + account.getRole());
    }

    @Override
    public void onNotifyStreamChanged(RoomServer.StreamNotify streamNotify) {
        Log.e(TAG, "onNotifyStreamChanged:  "
                + "  sdkNo:" + streamNotify.getSdkNo()
                + "  operation:" + streamNotify.getOperation()
                + "  streamName:" + streamNotify.getStream().getName()
                + "  channelType:" + streamNotify.getStream().getChannelType()
                + "  type:" + streamNotify.getStream().getType());

        streamNotify.getAccountId(); //判断哪个用户

        switch (streamNotify.getOperation().getNumber()) {
            case Models.Operation.Operation_Remove_VALUE://流关闭

                break;
            case Models.Operation.Operation_Add_VALUE://流新增

                break;
            case Models.Operation.Operation_Update_VALUE://改变

                break;
        }
    }

    //透传消息
    @Override
    public void onNotifyPassThough(RoomServer.PassthroughNotify passthroughNotify) {
        Log.e(TAG, "onNotifyPassThough:  " + passthroughNotify.getMessage());
    }

    //主持人会控,只有被控制的人会收到回调
    @Override
    public void onNotifyHostCtrlStream(RoomServer.HostCtrlStreamNotify hostCtrlStreamNotify) {
    }

    /**
     * 网络丢包状态，网络差的时候才会回调
     * 0    0% - 8%
     * -1   8% - 15%
     * -2   15% - 30%
     * -3   >=30% 丢包
     */
    @Override
    public void onRecvStatus(int i, int streamId) {
    }

    /**
     * 测速结果事件
     *
     * @param s 返回格式
     *          upld::recv=191 miss=10 losf=18 speed=2029127 delay=23
     *          down::recv=1164 miss=41 losf=67 speed=2078873 delay=22
     */
    @Override
    public void onTestSpeed(String s) {
    }

    /**
     * 聊天消息事件
     */
    @Override
    public void onNotifyChat(RoomServer.ChatNotify chatNotify) {
        ToastUtil.getInstance().showLongToast(chatNotify.getAccountName() + ":" + chatNotify.getMessage());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initView();
        initVcsApi();
        binding.cameraTextureView.setZOrderOnTop(false);
        binding.cameraTextureView.setZOrderMediaOverlay(false);

    }

    ActivityMeetingBinding binding;

    private void initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_meeting);

        binding.sendMsgBtn.setOnClickListener(v ->
                roomClient.sendChatMsg(null, "你好")
        );
        Intent intent = getIntent();
        meetingBean = (MeetingBean) intent.getSerializableExtra(ROOM_INFO);
        roomSdkNo = Integer.parseInt(meetingBean.getSdk_no());

        if (level == 0) {
            videoH = 480;
            videoW = 640;
            bitRate = 512;
        } else {
            videoH = 720;
            videoW = 1280;
            bitRate = 900;
        }
        windowAdapter = new WindowAdapter(MeetingActivity.this);
        binding.windowRcview.setAdapter(windowAdapter);
        binding.windowRcview.getRecycledViewPool().setMaxRecycledViews(0, 0);
        binding.windowRcview.setLayoutManager(new GridLayoutManager(this, spanCount));

        binding.clientTv.setText(UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no());

        windowAdapter.setOnItemClickListener((view, memberBean) -> {
            clickWindow(memberBean);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (roomClient != null) {
            roomClient.onResumeCamera();
        }
    }

    //初始化API
    private void initVcsApi() {
        roomClient = new RoomClient(this, roomSdkNo);

        roomClient.setRoomEvent(this);//设置会议回调
        roomClient.setAudioEncodeType(VCS_EVENT_TYPE.AUDIO_ENCODE_OPUS);
        roomClient.setAccount(
                UserConfig.getUserInfo().getData().getAccount().getId(),
                UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no(),
                UserConfig.getUserInfo().getData().getAccount().getName(),
                UserConfig.getUserInfo().getData().getAccount().getNickname(),
                UserConfig.getUserInfo().getData().getAccount().getPortrait());
        roomClient.setRoom(meetingBean.getRoom().getId(), roomSdkNo, meetingBean.getSession());
        roomClient.setStreamAddr(meetingBean.getStream_host(), meetingBean.getStream_port());
        roomClient.setMeetingAddr(meetingBean.getMeeting_host(), meetingBean.getMeeting_port());

        roomClient.setAudioStatusCallback(new AudioStatusCallback() {
            @Override
            public void onAudioStatus(List<AudioStatusBean> list) {
                Log.e("2222222222222", "onAudioStatus==" + list.toString());
            }

            @Override
            public void onSpeechStatus(boolean b) {

            }
        });

        roomClient.useMultiStream(true);//设置开启多流
        roomClient.setMinEncoderSoft(false);//在setVideoOutput前设置


        roomClient.setResolutionSize(videoW, videoH);
        //输出分辨率宽必须是16的倍数,高必须是2的倍数,否则容易出现绿边等问题
        //小码流高（会根据setVideoOutput设置的宽高自动计算宽，一定要放在setVideoOutput方法之前设置），码流，帧率
//        roomClient.setMinVideoOutput(360, 500, 15);
        roomClient.setVideoOutput(videoW, videoH, fps, bitRate);//设置视频分辨率宽高，帧率，码率
        roomClient.setAudioSampleRate(sampleRate);//设置采样率
        roomClient.setAgcAec(agc, aec);//设置AGC,AEC
        roomClient.setFps(fps);//设置帧率

        roomClient.openCamera(null, this);//设置预览view

        roomClient.enableXDelay(true);//自适应延迟
        roomClient.useHwDecoder(true);//是否硬解码
        roomClient.setDefaultSendSelfAudio(true);
        roomClient.setDefaultSendSelfVideo(true);
        selfMember = new MemberBean();
        selfMember.setAccountId(UserConfig.getUserInfo().getData().getAccount().getId());
        selfMember.setSdkNo(Integer.parseInt(UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no()));
        selfMember.setMute(Models.DeviceState.DS_Active);
        selfMember.setCloseVideo(Models.DeviceState.DS_Active);

        mainWindowMember = selfMember;

        roomClient.open();
    }

    public void streamKickOut(String accId) {
        roomClient.streamKickOut(accId);
    }

    /**
     * 小窗口点击事件
     */
    private void clickWindow(MemberBean memberBean) {
        if (windowAdapter != null) {
            WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(memberBean.getSdkNo());
            if (mainWindowMember == memberBean) {//点击的小窗口正显示在大窗口
                mainWindowMember = selfMember;
            } else {


                updateViewInfoSmallOld(mainWindowMember);//恢复原先小窗口的信息，用先前的memberBean
                mainWindowMember = memberBean;
            }
            updateViewInfo();
            updateViewInfoSmall(memberBean, holder);
        }
    }

    /**
     * 更新主画面的状态信息，昵称，音视频状态等
     */
    private void updateViewInfo() {
        if (mainWindowMember == null) {
            return;
        }

        binding.clientTv.setText(mainWindowMember.getSdkNo() + "");
        binding.closePreviewTv.setVisibility(mainWindowMember.isCloseVideo() ? View.VISIBLE : View.GONE);
        binding.closePreviewTv.setText(mainWindowMember.getSdkNo() + "\n" + "视频已关闭");
    }

    /**
     * 更新小窗口的状态信息
     */
    private void updateViewInfoSmall(MemberBean memberBean, WindowAdapter.MyViewHolder holder) {
        if (holder == null) {
            return;
        }
        if (mainWindowMember != selfMember) {//大窗口不是预览，小窗口显示自己的
            holder.nameTv.setText(roomClient.getAccount().getStreamId() + "");

            //大窗口设置大流
            Models.Account account = roomClient.getAccountList().get(mainWindowMember.getSdkNo());
            if (account != null) {
                if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded) {
                    for (Models.Stream stream : account.getStreamsList()) {
                        if (stream.getId() == 1) {//取id=1的流为默认流
                            roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Main);
                            break;
                        }
                    }
                } else {
                    roomClient.pickStreamMain(account.getId());
                }
            }
        } else {
            if (memberBean == null) {
                return;
            }
            holder.nameTv.setText(memberBean.getSdkNo() + "");

            //恢复为小码流
            Models.Account account = roomClient.getAccountList().get(memberBean.getSdkNo());
            if (account != null) {
                if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded) {//录播主机
                    for (Models.Stream stream : account.getStreamsList()) {
                        if (stream.getId() == 1) {
                            roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Sub);
                            break;
                        }
                    }
                } else {
                    roomClient.pickStreamSub(account.getId());
                }
            }
        }
    }

    /**
     * 恢复原先小窗口的信息
     */
    private void updateViewInfoSmallOld(MemberBean memberBean) {
        if (memberBean == null) {
            return;
        }
        WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(memberBean.getSdkNo());
        if (holder != null) {
            holder.nameTv.setText(memberBean.getSdkNo() + "");
//            holder.textureView.setMirror(0);
        }
        //恢复小流
        Models.Account account = roomClient.getAccountList().get(memberBean.getSdkNo());
        if (account != null) {
            if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded) {
                for (Models.Stream stream : account.getStreamsList()) {
                    if (stream.getId() == 1) {
                        roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Sub);
                        break;
                    }
                }
            } else {
                roomClient.pickStreamSub(account.getId());
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (roomClient != null) {
            roomClient.close();//退出释放
        }
        super.onDestroy();
    }


    public void changeSize() {
        int width, height;
        if (isLand) {
            height = (DisplayUtil.getInstance().getMobileWidth(this) / spanCount) * 9 / 16;
        } else {
            height = (DisplayUtil.getInstance().getMobileWidth(this) / spanCount) * 16 / 9;
        }
        width = DisplayUtil.getInstance().getMobileWidth(this) / spanCount;

        for (WindowAdapter.MyViewHolder holder : windowAdapter.getHolders().values()) {
            holder.frameLayout.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isLand = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        changeSize();
    }

    public boolean isLand = false;

    @Override
    public void onPreviewFrame(byte[] yuv, int width, int height, long stamp, int format, int angle) {
        VcsPlayerGlSurfaceView vcsPlayerGlSurfaceView = getTargetSurfaceView(selfMember.getSdkNo());
        if (vcsPlayerGlSurfaceView != null) {
            vcsPlayerGlSurfaceView.update(width, height, format);
            vcsPlayerGlSurfaceView.update(yuv, format);
            vcsPlayerGlSurfaceView.setLANDSCAPE(angle);
            vcsPlayerGlSurfaceView.setCameraId(isFront ? 1 : 0);
        }
    }
}