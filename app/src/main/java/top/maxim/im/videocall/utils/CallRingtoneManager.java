package top.maxim.im.videocall.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

import top.maxim.im.R;

/**
 * Description : 音视频音效
 * Created by Mango on 5/30/21.
 */
public class CallRingtoneManager {

    private AudioManager audioManager;

    private MediaPlayer mediaPlayer;

    private Context mContext;

    public CallRingtoneManager(Context context){
        mContext = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(null);
    }

    /**
     * 开启铃声
     *
     * @param incoming 是否是接受
     */
    public void ringing(boolean incoming) {
        try {
            AssetFileDescriptor file = mContext.getResources()
                    .openRawResourceFd(
                            incoming ? R.raw.video_call_receiver : R.raw.video_call_send);
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                    file.getLength());
            file.close();
            mediaPlayer.setVolume(0.8f, 0.8f);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止响铃
     */
    public void stopRinging() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(0.7f, 0.7f);
            mediaPlayer.stop();
        }
    }

    /**
     * 释放
     */
    public void release() {
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setMicrophoneMute(false);
        }
    }
}
