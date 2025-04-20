package com.example.videocall_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * 수신된 영상 통화를 처리하는 액티비티입니다.
 * 사용자에게 전화 수신 화면을 보여주고, 수락 또는 거절 기능을 제공합니다.
 */
public class IncomingCallActivity extends AppCompatActivity {

    public ConstraintLayout backgroundLayout;
    public ImageButton acceptButton, declineButton;
    public TextView callerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        // 레이아웃 컴포넌트 연결
        backgroundLayout = findViewById(R.id.incoming_call_background);
        acceptButton = findViewById(R.id.button_accept);
        declineButton = findViewById(R.id.button_decline);
        callerName = findViewById(R.id.text_caller_name);

        // 기본 표시 이름 설정 (실제 구현 시에는 발신자 이름 받아와야 함)
        callerName.setText(getString(R.string.incoming_call_message));

        // 푸시 알림 또는 인텐트로부터 전달된 roomId 받아오기
        String roomId = getIntent().getStringExtra("roomId");
        android.util.Log.d("IncomingCall", "roomId: " + roomId);

        // 블러 처리된 배경 이미지 설정
        backgroundLayout.setBackgroundResource(R.drawable.blurred_background);

        // 통화 수락 버튼 클릭 시 MainActivity(영상통화 화면)로 이동
        acceptButton.setOnClickListener(v -> {
            Intent intent = new Intent(IncomingCallActivity.this, MainActivity.class);
            intent.putExtra("roomId", roomId); // roomId 전달
            startActivity(intent);
            finish();
        });

        // 통화 거절 버튼 클릭 시 현재 액티비티 종료
        declineButton.setOnClickListener(v -> {
            finish();
        });
    }
}
