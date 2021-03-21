// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.alibaba.gov.android.chinaoly.meeting.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.gov.android.chinaoly.meeting.R;
import com.alibaba.gov.android.chinaoly.meeting.bean.MemberBean;
import com.alibaba.gov.android.chinaoly.meeting.util.DisplayUtil;
import com.ook.android.ikPlayer.VcsPlayerGlSurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WindowAdapter extends RecyclerView.Adapter<WindowAdapter.MyViewHolder> implements View.OnClickListener {
    private List<MemberBean> memberList;
    private HashMap<Integer, MyViewHolder> holders;
    private Context context;

    public HashMap<Integer, MyViewHolder> getHolders() {
        return holders == null ? new HashMap<>() : holders;
    }

    public MyViewHolder getHolder(Integer integer) {
        MyViewHolder myViewHolder = null;
        return myViewHolder;
    }

    public List<MemberBean> getMemberList() {
        return memberList;
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    //define interface
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, MemberBean memberBean);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (MemberBean) v.getTag());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public WindowAdapter(Context context) {
        this.memberList = new ArrayList<>();
        this.holders = new HashMap<>();
        this.context = context;
    }

    public void addItem(MemberBean memberBean) {
        memberList.add(memberBean);
        notifyItemInserted(memberList.size());
        notifyItemRangeChanged(memberList.size() - 1, 1);//通知数据与界面重新绑定
    }

    public void removeItem(int clientId) {
        if (memberList.size() < 1) {
            return;
        }
        for (int position = 0; position < memberList.size(); position++) {
            if (clientId == memberList.get(position).getSdkNo()) {
//                holders.get(clientId).itemFl.removeView(holders.get(clientId).meetingGLSurfaceView);
                holders.remove(clientId);
                notifyItemRemoved(position);
                memberList.remove(position);
                notifyItemRangeChanged(position, memberList.size() - position);//通知数据与界面重新绑定
                break;
            }
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_window, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final MemberBean memberBean = memberList.get(position);
        holder.nameTv.setText(memberBean.getSdkNo() + "");
        holder.itemView.setTag(memberBean);
        holders.put(memberBean.getSdkNo(), holder);
    }

    @Override
    public int getItemCount() {
        return memberList == null ? 0 : memberList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public VcsPlayerGlSurfaceView textureView;
        public TextView nameTv;
        public FrameLayout frameLayout;

        MyViewHolder(View convertView) {
            super(convertView);

            textureView = convertView.findViewById(R.id.gl_view);
            textureView.setZOrderOnTop(true);
            textureView.setZOrderMediaOverlay(true);
            frameLayout = convertView.findViewById(R.id.fl_view);
            nameTv = convertView.findViewById(R.id.id_tv);

            int width, height;

            height = (DisplayUtil.getInstance().getMobileWidth(context) / ((MeetingActivity) context).spanCount) * 16 / 9;
            width = DisplayUtil.getInstance().getMobileWidth(context) / ((MeetingActivity) context).spanCount;

            frameLayout.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        }
    }
}