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
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.RTCConfiguration;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.SdpObserver;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private SurfaceViewRenderer localView;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoCapturer capturer;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;
    private AudioSource audioSource;
    private AudioTrack localAudioTrack;
    private PeerConnection loopbackPeer;

    private SurfaceViewRenderer remoteView;
    private PeerConnection peerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        } else {
            setContentView(R.layout.activity_main);
            startVideo();
        }
    }

    private void listenForIceCandidates() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference candidateRef = database.getReference("rooms/room123/candidates/userB");

        candidateRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot snapshot, String previousChildName) {
                String[] parts = snapshot.getValue(String.class).split("\\|");
                if (parts.length == 3) {
                    IceCandidate candidate = new IceCandidate(parts[0], Integer.parseInt(parts[1]), parts[2]);
                    peerConnection.addIceCandidate(candidate);
                }
            }

            @Override public void onChildChanged(com.google.firebase.database.DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(com.google.firebase.database.DataSnapshot snapshot) {}
            @Override public void onChildMoved(com.google.firebase.database.DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(com.google.firebase.database.DatabaseError error) {}
        });
    }

    private VideoCapturer createVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(this);
        for (String deviceName : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }
        // fallback to other camera
        for (String deviceName : enumerator.getDeviceNames()) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setContentView(R.layout.activity_main);
                startVideo();
            } else {
                finish(); // 권한 거부 시 앱 종료
            }
        }
    }

    private void startVideo() {
        // ✅ 1. SurfaceViewRenderer 연결
        localView = findViewById(R.id.localView);

        // ✅ 2. EGL 환경 초기화
        rootEglBase = EglBase.create();
        localView.init(rootEglBase.getEglBaseContext(), null);

        // Initialize remoteView
        remoteView = findViewById(R.id.remoteView);
        remoteView.init(rootEglBase.getEglBaseContext(), null);
        remoteView.setZOrderMediaOverlay(false);

        // ✅ 3. PeerConnectionFactory 초기화
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions()
        );
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        // Create PeerConnection
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        peerConnection = factory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}

            @Override
            public void onIceConnectionReceivingChange(boolean b) {}

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                // TODO: send this candidate to the remote peer via signaling
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}

            @Override
            public void onAddStream(org.webrtc.MediaStream mediaStream) {}

            @Override
            public void onRemoveStream(org.webrtc.MediaStream mediaStream) {}

            @Override
            public void onDataChannel(org.webrtc.DataChannel dataChannel) {}

            @Override
            public void onRenegotiationNeeded() {}

            @Override
            public void onAddTrack(org.webrtc.RtpReceiver rtpReceiver, org.webrtc.MediaStream[] mediaStreams) {
                if (!mediaStreams[0].videoTracks.isEmpty()) {
                    mediaStreams[0].videoTracks.get(0).addSink(remoteView);
                }
            }
        });

        // ✅ 4. 카메라 가져오기 + VideoSource 만들기
        capturer = createVideoCapturer();
        if (capturer != null) {
            SurfaceTextureHelper surfaceTextureHelper =
                    SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());

            videoSource = factory.createVideoSource(false);
            capturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
            capturer.startCapture(640, 480, 30);

            // ✅ 5. VideoTrack 만들고 SurfaceView에 연결
            localVideoTrack = factory.createVideoTrack("100", videoSource);
            localVideoTrack.addSink(localView);

            // ✅ 6. 오디오 트랙 추가
            audioSource = factory.createAudioSource(new org.webrtc.MediaConstraints());
            localAudioTrack = factory.createAudioTrack("101", audioSource);

            // ✅ 7. LocalTrack → PeerConnection에 추가
            peerConnection.addTrack(localVideoTrack, Arrays.asList("streamId"));
            peerConnection.addTrack(localAudioTrack, Arrays.asList("streamId"));

            // ✅ 8. 루프백 Peer 생성
            loopbackPeer = factory.createPeerConnection(new PeerConnection.RTCConfiguration(new ArrayList<>()), new PeerConnection.Observer() {
                @Override
                public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

                @Override
                public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}

                @Override
                public void onIceConnectionReceivingChange(boolean b) {}

                @Override
                public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                    peerConnection.addIceCandidate(iceCandidate);
                }

                @Override
                public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}

                @Override
                public void onAddStream(org.webrtc.MediaStream mediaStream) {}

                @Override
                public void onRemoveStream(org.webrtc.MediaStream mediaStream) {}

                @Override
                public void onDataChannel(org.webrtc.DataChannel dataChannel) {}

                @Override
                public void onRenegotiationNeeded() {}

                @Override
                public void onAddTrack(org.webrtc.RtpReceiver rtpReceiver, org.webrtc.MediaStream[] mediaStreams) {
                    if (!mediaStreams[0].videoTracks.isEmpty()) {
                        mediaStreams[0].videoTracks.get(0).addSink(remoteView);
                    }
                }
            });

            // ✅ 9. Offer/Answer 교환 (로컬 루프백)
            peerConnection.createOffer(new SdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    peerConnection.setLocalDescription(new SdpObserverAdapter(), sessionDescription);
                    loopbackPeer.setRemoteDescription(new SdpObserverAdapter(), sessionDescription);

                    // ✅ Firebase에 Offer 쓰기
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference offerRef = database.getReference("rooms/room123/offer");
                    offerRef.setValue(sessionDescription.description);

                    loopbackPeer.createAnswer(new SdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {
                            loopbackPeer.setLocalDescription(new SdpObserverAdapter(), sessionDescription);
                            peerConnection.setRemoteDescription(new SdpObserverAdapter(), sessionDescription);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference answerRef = database.getReference("rooms/room123/answer");
                            answerRef.setValue(sessionDescription.description);
                        }

                        @Override public void onSetSuccess() {}
                        @Override public void onSetFailure(String s) {}
                        @Override public void onCreateFailure(String s) {}
                    }, new org.webrtc.MediaConstraints());
                }

                @Override public void onSetSuccess() {}
                @Override public void onSetFailure(String s) {}
                @Override public void onCreateFailure(String s) {}
            }, new org.webrtc.MediaConstraints());
        }

        listenForOffer();
        listenForAnswer();
    }

    private void listenForAnswer() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference answerRef = database.getReference("rooms/room123/answer");

        answerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String sdp = snapshot.getValue(String.class);
                    SessionDescription remoteAnswer = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
                    peerConnection.setRemoteDescription(new SdpObserverAdapter(), remoteAnswer);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void listenForOffer() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference offerRef = database.getReference("rooms/room123/offer");

        offerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String sdp = snapshot.getValue(String.class);
                    SessionDescription remoteOffer = new SessionDescription(SessionDescription.Type.OFFER, sdp);
                    peerConnection.setRemoteDescription(new SdpObserverAdapter(), remoteOffer);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private static class SdpObserverAdapter implements SdpObserver {
        @Override public void onCreateSuccess(SessionDescription sessionDescription) {}
        @Override public void onSetSuccess() {}
        @Override public void onCreateFailure(String s) {}
        @Override public void onSetFailure(String s) {}
    }
}
