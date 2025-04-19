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

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "call_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Extract data with fallback
        Map<String, String> data = remoteMessage.getData();
        String title = data.containsKey("title") ? data.get("title") : "수신된 영상통화";
        String message = data.containsKey("message") ? data.get("message") : "영상통화가 도착했습니다.";

        // roomId를 데이터에서 가져오거나 기본값 설정
        String roomId = data.containsKey("roomId") ? data.get("roomId") : "room123";

        // 수신된 알림을 클릭했을 때 호출 화면으로 이동 + roomId 전달
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtra("roomId", roomId); // ✅ roomId를 전달
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Ringtone for call
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        // Build notification with full screen intent and blurred/gradient styling
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_call)  // Provide a suitable icon
                .setContentTitle(title)
                .setContentText(message != null ? message : "영상통화가 도착했습니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setSound(soundUri)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Channel setup for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "영상통화 수신 알림",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setDescription("영상통화 수신 시 전체화면 알림 제공");
            notificationManager.createNotificationChannel(channel);
        }

        // Show notification with unique ID
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
