package com.example.videocall_android;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 애플리케이션이 시작될 때 한 번만 실행되는 초기화 코드 (예: Firebase 초기화, 로그 설정 등)
    }
}
