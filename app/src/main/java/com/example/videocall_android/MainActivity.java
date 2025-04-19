package com.example.videocall_android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.EglBase;
import org.webrtc.Camera2Enumerator;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.PeerConnectionFactory;

public class MainActivity extends AppCompatActivity {

    private SurfaceViewRenderer localView;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoCapturer capturer;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ 1. SurfaceViewRenderer 연결
        localView = findViewById(R.id.localView);

        // ✅ 2. EGL 환경 초기화
        rootEglBase = EglBase.create();
        localView.init(rootEglBase.getEglBaseContext(), null);
        localView.setZOrderMediaOverlay(true);

        // ✅ 3. PeerConnectionFactory 초기화
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions()
        );
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        // ✅ 4. 카메라 가져오기 + VideoSource 만들기
        capturer = createVideoCapturer();
        if (capturer != null) {
            videoSource = factory.createVideoSource(capturer.isScreencast());
            capturer.initialize(null, getApplicationContext(), videoSource.getCapturerObserver());
            capturer.startCapture(640, 480, 30);

            // ✅ 5. VideoTrack 만들고 SurfaceView에 연결
            localVideoTrack = factory.createVideoTrack("100", videoSource);
            localVideoTrack.addSink(localView);
        }
    }

    private VideoCapturer createVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(this);
        for (String deviceName : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null);
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (capturer != null) {
                capturer.stopCapture();
                capturer.dispose();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (videoSource != null) videoSource.dispose();
        if (localView != null) localView.release();
        if (factory != null) factory.dispose();
    }
}
