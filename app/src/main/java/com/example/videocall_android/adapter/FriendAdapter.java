package com.example.videocall_android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videocall_android.R;

import java.util.List;

/**
 * 이 어댑터는 친구 목록을 리사이클러뷰에 표시하며,
 * 사용자가 통화 버튼을 클릭할 때 해당 친구의 이름을 콜백으로 전달합니다.
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final List<String> friendList;
    private final OnCallClickListener listener;

    public interface OnCallClickListener {
        void onCallClick(@NonNull String friendName);
    }

    public FriendAdapter(List<String> friendList, OnCallClickListener listener) {
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        String friendName = friendList.get(position);
        holder.textName.setText(friendName);
        holder.buttonCall.setOnClickListener(v -> listener.onCallClick(friendName));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        ImageButton buttonCall;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            buttonCall = itemView.findViewById(R.id.button_call);
        }
    }
}