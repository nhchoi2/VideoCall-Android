package com.example.videocall_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class IncomingCallActivity extends AppCompatActivity {

    public ConstraintLayout backgroundLayout;
    public ImageButton acceptButton, declineButton;
    public TextView callerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        backgroundLayout = findViewById(R.id.incoming_call_background);
        acceptButton = findViewById(R.id.button_accept);
        declineButton = findViewById(R.id.button_decline);
        callerName = findViewById(R.id.text_caller_name);

        // Example caller info
        callerName.setText(getString(R.string.incoming_call_message));

        // 푸시 알림에서 전달된 roomId 값을 인텐트에서 가져옴
        String roomId = getIntent().getStringExtra("roomId");
        android.util.Log.d("IncomingCall", "roomId: " + roomId);

        // Set blurred background drawable
        backgroundLayout.setBackgroundResource(R.drawable.blurred_background);

        acceptButton.setOnClickListener(v -> {
            // Proceed to call screen
            Intent intent = new Intent(IncomingCallActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        declineButton.setOnClickListener(v -> {
            // Just close the activity for now
            finish();
        });
    }
}
