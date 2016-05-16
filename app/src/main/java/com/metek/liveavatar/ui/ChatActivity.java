package com.metek.liveavatar.ui;

import android.animation.ObjectAnimator;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.metek.liveavatar.R;
import com.metek.liveavatar.User;
import com.metek.liveavatar.face.sensetime.FaceOverlapFragment;
import com.metek.liveavatar.live2d.FaceData;
import com.metek.liveavatar.live2d.FileManager;
import com.metek.liveavatar.live2d.Live2dFragment;
import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;
import com.metek.liveavatar.socket.TCPManager;
import com.metek.liveavatar.socket.receive.RecMsgFaceData;
import com.metek.liveavatar.socket.receive.RecMsgMatch;
import com.metek.liveavatar.socket.receive.RecMsgRequest;
import com.metek.liveavatar.socket.send.MsgFaceData;
import com.metek.liveavatar.socket.send.MsgLogin;
import com.metek.liveavatar.socket.send.MsgMatch;
import com.metek.liveavatar.socket.send.MsgResponse;
import com.metek.liveavatar.utils.AnimationHelper;

public class ChatActivity extends AppCompatActivity implements FaceOverlapFragment.onActionChangeListener {
    private static final String TAG = ChatActivity.class.getSimpleName();

    private String friendId;
    private boolean isChatting = false;

    private FaceOverlapFragment fragmentCamera;
//    private Live2dFragment fragmentSelf;
    private Live2dFragment fragmentTarget;

    private RelativeLayout layoutMain;
    private RelativeLayout layoutResource;
    private RelativeLayout layoutRequest;

    private ImageView btnTransform;
    private ImageView btnMatch;
    private ImageView btnCancel;
    private ImageView btnDisconnect;
    private ImageView btnResource;
    private ImageView btnOptions;
    private ImageView btnClose;
    private ImageView btnRefuse;
    private ImageView btnAccept;

    private Animation outAnim, inAnim;
    private Animation requestOutAnim, requestInAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FileManager.init(this.getApplicationContext());

//        fragmentCamera = (FaceOverlapFragment) getFragmentManager().findFragmentById(R.id.fragment_camera);
//        fragmentCamera.setOnActionChangeListener(this);
//        fragmentSelf = (Live2dFragment) getFragmentManager().findFragmentById(R.id.fragment_live2d_self);
//        fragmentTarget = (Live2dFragment) getFragmentManager().findFragmentById(R.id.fragment_live2d_target);

        layoutMain = (RelativeLayout) findViewById(R.id.layout_main);
        layoutResource = (RelativeLayout) findViewById(R.id.layout_resource);
        layoutRequest = (RelativeLayout) findViewById(R.id.layout_request);

        btnTransform = (ImageView) findViewById(R.id.btn_transform);
        btnMatch = (ImageView) findViewById(R.id.btn_match);
        btnCancel = (ImageView) findViewById(R.id.btn_cancel);
        btnDisconnect = (ImageView) findViewById(R.id.btn_disconnect);
        btnResource = (ImageView) findViewById(R.id.btn_resource);
        btnOptions = (ImageView) findViewById(R.id.btn_options);
        btnClose = (ImageView) findViewById(R.id.btn_close);
        btnRefuse = (ImageView) findViewById(R.id.btn_refuse);
        btnAccept = (ImageView) findViewById(R.id.btn_accept);

        outAnim = AnimationUtils.loadAnimation(this, R.anim.out_anim);
        inAnim = AnimationUtils.loadAnimation(this, R.anim.in_anim);
        requestOutAnim = AnimationUtils.loadAnimation(this, R.anim.request_out_anim);
        requestInAnim = AnimationUtils.loadAnimation(this, R.anim.request_in_anim);
//
//        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
//        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);



        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        fragmentCamera = new FaceOverlapFragment();
        transaction.add(R.id.chat_main, fragmentCamera);
        transaction.commit();
        fragmentCamera.setOnActionChangeListener(this);
    }

    @Override
    public void onActionChange(FaceData data) {
        if (isChatting) {
            MsgFaceData msgFaceData = new MsgFaceData(friendId, data);
            TCPManager.getManager().send(msgFaceData);
        }
//        fragmentTarget.setLive2dAction(data);
    }

    private TCPManager.ConnectListener tcpListener = new TCPManager.ConnectListener() {
        @Override
        public void onConnect(int state, MsgData data) {
            if (state != CONN_OK) return;
            if (data == null) return;

            switch (data.code) {
                case NetConst.CODE_LOGIN:
                    Log.v(TAG, "连接服务器成功");
                    MsgLogin msgLogin = new MsgLogin(User.getInstance().getUserId());
                    TCPManager.getManager().send(msgLogin);
                    break;
                case NetConst.CODE_MATCH:
                    Log.v(TAG, "匹配成功");
                    RecMsgMatch recMsgMatch = new RecMsgMatch(data);
                    friendId = recMsgMatch.getFriendid();
                    isChatting = true;
                    // TODO fragmentSelf缩小到右上角，fragmentTarget显示
                    break;
                case NetConst.CODE_REQUEST:
                    Log.v(TAG, "收到匹配请求");
                    RecMsgRequest recMsgRequest = new RecMsgRequest(data);
                    TextView tvMessage = (TextView) layoutRequest.findViewById(R.id.request_message);
                    String friendName = recMsgRequest.getFriendName();
                    String message = String.format(getResources().getString(R.string.request_message), friendName);
                    tvMessage.setText(message);

                    // TODO fragmentCamera隐藏
                    requestInAnim.setAnimationListener(new AnimationHelper() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            btnRefuse.setEnabled(true);
                            btnAccept.setEnabled(true);
                        }
                    });
                    layoutRequest.setVisibility(View.VISIBLE);
                    layoutRequest.startAnimation(requestInAnim);
                    break;
                case NetConst.CODE_SEND_FACE_DATA:
                    RecMsgFaceData recMsgFaceData = new RecMsgFaceData(data);
//                    fragmentSelf.setLive2dAction(recMsgFaceData.getFaceData());
                    break;
            }
        }
    };

    public void onClickTransform(View view) {
        Log.v(TAG, "开始转换");
        btnTransform.setVisibility(View.GONE);
        btnMatch.setVisibility(View.VISIBLE);
        btnResource.setVisibility(View.VISIBLE);
        btnOptions.setVisibility(View.VISIBLE);

        TCPManager.getManager().setConnectListener(tcpListener);
        TCPManager.getManager().connect();
        // TODO fragmentCamera执行缩小动画

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Live2dFragment fragmentTarget = new Live2dFragment();
        transaction.replace(R.id.chat_main, fragmentTarget);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();

        transaction = getFragmentManager().beginTransaction();
        fragmentCamera = new FaceOverlapFragment();
        transaction.add(R.id.chat_sub, fragmentCamera);
        transaction.commit();
        fragmentCamera.setOnActionChangeListener(this);
    }

    public void onClickMatch(View view) {
        Log.v(TAG, "开始匹配联系人");
        btnMatch.setVisibility(View.GONE);
        btnCancel.setVisibility(View.VISIBLE);

        MsgMatch msgMatch = new MsgMatch();
        TCPManager.getManager().send(msgMatch);
    }

    public void onClickCancel(View view) {
        btnCancel.setVisibility(View.GONE);
        btnMatch.setVisibility(View.VISIBLE);
        // TODO 断开匹配联系人操作
    }

    public void onClickDisconnect(View view) {
        btnDisconnect.setVisibility(View.GONE);
        btnMatch.setVisibility(View.VISIBLE);
        // TODO 断开通话
    }

    public void onClickResource(View view) {
        btnResource.setEnabled(false);
        inAnim.setAnimationListener(new AnimationHelper() {
            @Override
            public void onAnimationEnd(Animation animation) {
                btnClose.setEnabled(true);
            }
        });
        outAnim.setAnimationListener(new AnimationHelper() {
            @Override
            public void onAnimationEnd(Animation animation) {
                layoutMain.setVisibility(View.GONE);
                layoutResource.setVisibility(View.VISIBLE);
                layoutResource.startAnimation(inAnim);
            }
        });
        layoutMain.startAnimation(outAnim);
    }

    public void onClickOptions(View view) {
        btnOptions.setEnabled(false);
        // TODO 跳转设置界面
    }

    public void onClickClose(View view) {
        btnClose.setEnabled(false);
        inAnim.setAnimationListener(new AnimationHelper() {
            @Override
            public void onAnimationEnd(Animation animation) {
                btnResource.setEnabled(true);
            }
        });
        outAnim.setAnimationListener(new AnimationHelper() {
            @Override
            public void onAnimationEnd(Animation animation) {
                layoutResource.setVisibility(View.GONE);
                layoutMain.setVisibility(View.VISIBLE);
                layoutMain.startAnimation(inAnim);
            }
        });
        layoutResource.startAnimation(outAnim);
    }

    public void onClickRefuse(View view) {
        MsgResponse msgResponse = new MsgResponse(false);
        TCPManager.getManager().send(msgResponse);

        btnRefuse.setEnabled(false);
        btnAccept.setEnabled(false);
        requestOutAnim.setAnimationListener(new AnimationHelper() {
            @Override
            public void onAnimationEnd(Animation animation) {
                layoutRequest.setVisibility(View.GONE);
                // TODO fragmentCamera显示
            }
        });
        layoutRequest.startAnimation(requestOutAnim);
    }

    public void onClickAccept(View view) {
        MsgResponse msgResponse = new MsgResponse(true);
        TCPManager.getManager().send(msgResponse);
        isChatting = true;

        btnRefuse.setEnabled(false);
        btnAccept.setEnabled(false);
        requestOutAnim.setAnimationListener(new AnimationHelper() {
            @Override
            public void onAnimationEnd(Animation animation) {
                layoutRequest.setVisibility(View.GONE);
                // TODO fragmentSelf缩小到右上角，fragmentTarget显示
            }
        });
        layoutRequest.startAnimation(requestOutAnim);
    }

    public void target(View view) {
//        ScaleAnimation sa = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f);
//        TranslateAnimation ta = new TranslateAnimation(0.0f, 500f, 0.0f, 500f);
//        ta.setFillAfter(true);
//        ta.setDuration(500);
//        view.startAnimation(ta);

        ObjectAnimator sa = ObjectAnimator.ofFloat(view, "scaleX", 0.1f, 0.3f, 0.5f, 0.8f, 1.0f, 1.2f, 1.4f, 1.6f, 1.8f, 2.0f);
        sa.setDuration(2000);
        sa.setRepeatCount(2);
        sa.setRepeatMode(ObjectAnimator.REVERSE);
        sa.start();
    }
}
