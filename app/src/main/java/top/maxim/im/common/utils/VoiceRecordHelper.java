
package top.maxim.im.common.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Description : 语音录制帮助类 Created by Mango on 2018/11/11.
 */
public class VoiceRecordHelper {

    /* 语音录制帮助类标记 */
    private String TAG = "VoiceRecordHelper";

    /* 语音录制 */
    private MediaRecorder mMediaRecorder;

    /* 语音录制handler */
    private WeakHandler handler;

    /* 系统声音管理类 */
    private AudioManager mAudioManager;

    /* 系统声音改变监听 */
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;

    /* 是否需要回调声音分贝 默认: 不需要 */
    private boolean mIsNeedCallBackSoundDB = false;

    /**
     * 用于监听声音分贝的回调
     */
    public interface OnCallBackSoundDecibel {
        /**
         * 回调当前声音分贝
         * 
         * @param decibel 声音分贝
         */
        void callBackSoundDecibel(float decibel);
    }

    private OnCallBackSoundDecibel mOnCallBackSoundDecibel;

    public void setCallBackSoundDecibel(OnCallBackSoundDecibel callBack) {
        this.mOnCallBackSoundDecibel = callBack;
        this.mIsNeedCallBackSoundDB = true;
    }

    public VoiceRecordHelper(Activity context) {
        handler = new WeakHandler(context);
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        initListener();
    }

    private void initListener() {
        // 系统声音改变监听
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                Log.d(TAG, "Audio Focus Change=" + focusChange);
                // 暂时失去了音频焦点，但很快会重新得到焦点
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    Log.e(TAG, focusChange + "");
                    // 你已经得到焦点了
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // 永久失去焦点
                    Log.e(TAG, focusChange + "");
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    Log.e(TAG, focusChange + "");
                }
            }
        };
    }

    /**
     * 开始录制语音
     * 
     * @param voicePath 语音录制的路径
     */
    public void startVoiceRecord(final String voicePath) {
        if (TextUtils.isEmpty(voicePath)) {
            return;
        }
        boolean isFocus = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        if (isFocus) {
            Log.d(TAG, "file name = " + voicePath);
            try {
                if (mMediaRecorder != null) {
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }
            } catch (Exception e) {
                Log.d(TAG, "stopRecord" + e.getMessage());
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mMediaRecorder = new MediaRecorder();
                        // 第1步：设置音频来源（MIC表示麦克风）
                        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        // 第2步：设置音频输出格式（默认的输出格式）
                        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                        // 第3步：设置音频编码方式（默认的编码方式）
                        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        // 第4步：指定音频输出文件
                        mMediaRecorder.setOutputFile(voicePath);
                        // 第5步：调用prepare方法
                        try {
                            mMediaRecorder.prepare();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "message = " + e.getMessage());
                        }
                        // 第6步：调用start方法开始录音
                        mMediaRecorder.start();

                        // 获取声音分贝
                        if (mIsNeedCallBackSoundDB) {
                            updateMicStatus();
                        }
                    } catch (Exception e) {
                        mMediaRecorder = null;
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "request Audio Focus failed.");
        }
    }

    /**
     * 停止录音，并是否删除文件
     * 
     * @param delete 是否删除文件
     * @param fileName 语音路径
     */
    public void stopVoiceRecord(boolean delete, final String fileName) {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mMediaRecorder = null;
        }
        if (delete) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(fileName)) {
                        File file = new File(fileName);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            }, 1000);
        }
        resetMusic();
    }

    /**
     * 录制语音完成后放弃焦点获取
     */
    private void resetMusic() {
        if (mAudioManager != null && mOnAudioFocusChangeListener != null) {
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };

    private static final int BASE = 100;

    /* 间隔取样时间 */
    private static final int SPACE = 300;

    /**
     * 更新声音分贝
     */
    private void updateMicStatus() {
        if (mMediaRecorder != null) {
            double ratio = (double)mMediaRecorder.getMaxAmplitude() / BASE;
            float db = 0;// 分贝
            if (ratio > 1)
                db = (float)(20 * Math.log10(ratio));
            handler.postDelayed(mUpdateMicStatusTimer, SPACE);

            if (null != mOnCallBackSoundDecibel) {
                mOnCallBackSoundDecibel.callBackSoundDecibel(db);
            }
        }
    }

}
