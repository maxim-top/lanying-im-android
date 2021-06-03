//package top.maxim.im.rtc;
//
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Point;
//import android.os.Build;
//import android.view.View;
//import android.widget.RelativeLayout;
//
//import org.webrtc.EglBase;
//import org.webrtc.RendererCommon;
//import org.webrtc.VideoTrack;
//
//import top.maxim.im.R;
//import top.maxim.im.common.base.BaseTitleActivity;
//import top.maxim.im.common.view.Header;
//import top.maxim.im.rtc.view.RTCRenderView;
//import top.maxim.im.rtc.webrtcmodule.PeerConnectionParameters;
//import top.maxim.im.rtc.webrtcmodule.RtcListener;
//import top.maxim.im.rtc.webrtcmodule.WebRtcClient;
//
//public class RTCActivity extends BaseTitleActivity {
//
//    public static void openVideoCall(Context context) {
//        context.startActivity(new Intent(context, RTCActivity.class));
//    }
//
//    private RelativeLayout mContainer;
//
//    private RTCRenderView mLocalView;
//
//    private WebRtcClient mClient;
//
//    private EglBase rootEglBase;
//
//    @Override
//    protected Header onCreateHeader(RelativeLayout headerContainer) {
//        return new Header.Builder(this, headerContainer).build();
//    }
//
//    @Override
//    protected View onCreateView() {
//        hideHeader();
//        View view = View.inflate(this, R.layout.activity_rtc, null);
//        mLocalView = view.findViewById(R.id.rtc_local);
//        mContainer = view.findViewById(R.id.rtc_container);
//        return view;
//    }
//
//    @Override
//    protected void initDataForActivity() {
//        super.initDataForActivity();
//        createWebRtcClient();
//    }
//
//    //创建配置参数
//    private PeerConnectionParameters createPeerConnectionParameters() {
//        //获取webRtc 音视频配置参数
//        Point displaySize = new Point();
//        this.getWindowManager().getDefaultDisplay().getSize(displaySize);
//        displaySize.set(480, 320);
//        return new PeerConnectionParameters(true, false,
//                false, displaySize.x, displaySize.y, 30,
//                0, "VP8",
//                true, false, 0, "OPUS",
//                false, false, false, false, false, false,
//                false, false, false, false);
//    }
//
//    //创建webRtcClient
//    private void createWebRtcClient(){
//        //配置参数
//        PeerConnectionParameters peerConnectionParameters = createPeerConnectionParameters();
//        //创建视频渲染器
//        rootEglBase = EglBase.create();
//        //WebRtcClient对象
//        mClient = new WebRtcClient(getApplicationContext(),
//                rootEglBase,
//                peerConnectionParameters,
//                new RtcListener() {
//                    @Override
//                    public void onCallStart(String id) {
//                        runOnUiThread(() -> {
////                            try {
////                                mClient.sendMessage(id, "init", null);
////                            } catch (JSONException e) {
////                                e.printStackTrace();
////                            }
//                            mClient.createAndJoinRoom("22222", Build.BRAND);
//                            startCamera();
//                        });
//                    }
//
//                    @Override
//                    public void onAddRemoteStream(String peerId, VideoTrack videoTrack) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ////UI线程执行
//                                //构建远端view
//                                RTCRenderView remoteView = new RTCRenderView(RTCActivity.this);
//                                //初始化渲染源
//                                remoteView.init(rootEglBase.getEglBaseContext(), null);
//                                //填充模式
//                                remoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//                                remoteView.setZOrderMediaOverlay(true);
//                                remoteView.setEnableHardwareScaler(false);
//                                remoteView.setMirror(true);
//                                //控件布局
//                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(360,360);
//                                layoutParams.topMargin = 20;
//                                mContainer.addView(remoteView,layoutParams);
//                                //添加数据
//                                //VideoTrack videoTrack = mediaStream.videoTracks.get(0);
//                                videoTrack.addSink(remoteView);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onRemoveRemoteStream(String peerId) {
//
//                    }
//                },
//                "http://172.31.238.114:3018");
//    }
//
//
//    //开启摄像头
//    private void startCamera(){
//        //初始化渲染源
//        mLocalView.init(rootEglBase.getEglBaseContext(), null);
//        //填充模式
//        mLocalView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//        mLocalView.setZOrderMediaOverlay(true);
//        mLocalView.setEnableHardwareScaler(false);
//        mLocalView.setMirror(true);
//        mLocalView.setBackground(null);
//        //启动摄像头
//        mClient.startCamera(mLocalView, WebRtcClient.FONT_FACTING);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mLocalView.release();
//        mLocalView = null;
//    }
//
//    @Override
//    public void finish() {
//        super.finish();
//        mClient.exitRoom();
//    }
//}
