package com.example.videocall_android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Firebase 클라우드 메시지를 수신하고 영상통화 알림을 처리하는 서비스
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "call_notifications";  // 알림 채널 ID

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 🔔 메시지 데이터 추출
        Map<String, String> data = remoteMessage.getData();
        String title = data.containsKey("title") ? data.get("title") : "수신된 영상통화";
        String message = data.containsKey("message") ? data.get("message") : "영상통화가 도착했습니다.";

        // 🧩 roomId 값을 전달받거나 기본값 설정
        String roomId = data.containsKey("roomId") ? data.get("roomId") : "room123";

        // 👉 알림 클릭 시 IncomingCallActivity로 이동 (roomId 전달)
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtra("roomId", roomId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // 🔊 알림음 설정 (벨소리)
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        // 📲 전체화면 알림 빌드
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_call)  // 알림 아이콘
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setSound(soundUri)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // 🧱 안드로이드 8.0 이상에서는 알림 채널 생성 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "영상통화 수신 알림",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);  // LED 불빛 활성화
            channel.enableVibration(true);  // 진동 활성화
            channel.setDescription("영상통화 수신 시 전체화면 알림 제공");
            notificationManager.createNotificationChannel(channel);
        }

        // 📣 알림 표시 (고유 ID 사용하여 매번 새 알림 표시)
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
