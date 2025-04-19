package com.example.videocall_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class IncomingCallActivity extends AppCompatActivity {

    private ConstraintLayout backgroundLayout;
    private Button acceptButton, declineButton;
    private TextView callerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        backgroundLayout = findViewById(R.id.incoming_call_background);
        acceptButton = findViewById(R.id.button_accept);
        declineButton = findViewById(R.id.button_decline);
        callerName = findViewById(R.id.text_caller_name);

        // Example caller info
        callerName.setText("남호님이 영상통화를 요청했습니다");

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
