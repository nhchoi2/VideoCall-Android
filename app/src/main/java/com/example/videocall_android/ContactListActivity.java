package com.example.videocall_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videocall_android.adapter.FriendAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * 연락처 리스트를 보여주는 액티비티
 * 각 친구 항목에는 영상통화 버튼이 포함됨
 */
public class ContactListActivity extends AppCompatActivity implements FriendAdapter.OnCallClickListener {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        recyclerView = findViewById(R.id.recycler_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 샘플 연락처 리스트 (실제 구현에서는 DB 또는 Firebase에서 불러오기)
        List<String> sampleFriends = Arrays.asList("남호", "민수", "지연");

        adapter = new FriendAdapter(sampleFriends, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCallClick(@NonNull String friendName) {
        // 통화 버튼 클릭 시 호출
        Toast.makeText(this, friendName + "님에게 영상통화 요청 중...", Toast.LENGTH_SHORT).show();

        // 나중에 여기서 FCM 메시지 전송 등 추가 가능
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("roomId", "room123");  // 예시 roomId
        startActivity(intent);
    }
}
