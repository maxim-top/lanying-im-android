
package top.maxim.im.push;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Vibrator;
import android.support.annotation.RawRes;
import android.util.SparseIntArray;

import top.maxim.im.R;

/**
 * Description : 音频播放
 */
public class SoundManager {
    private static final String TAG = SoundManager.class.getSimpleName();

    private static SoundManager mInstance;

    /**
     * 播放语音消息的MediaPlayer
     */
    private MediaPlayer mSoundPlayer;

    /**
     * 播放简短的提示音 外放扬声器
     */
    private SoundPool mSoundPool;

    /* 外放的声音缓存 */
    private SparseIntArray soundPoolCache = new SparseIntArray();

    /**
     * 播放简短的提示音 耳机播放
     */
    private SoundPool mSoundWirePool;

    /* 耳机播放的声音缓存 */
    private SparseIntArray soundWirePollCache = new SparseIntArray();

    private AudioManager mAudioManager;

    private SoundManager() {
        mSoundPool = new SoundPool(4, AudioManager.STREAM_RING, 100);
        mSoundWirePool = new SoundPool(4, AudioManager.STREAM_VOICE_CALL, 100);
    }

    public static SoundManager getInstance() {
        if (mInstance == null) {
            mInstance = new SoundManager();
        }
        return mInstance;
    }

    /**
     * SoundPool播放简短的音效
     *
     * @param resId 音效的资源ID
     */
    private void playShortVoice(Context context, @RawRes int resId) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager)context.getApplicationContext()
                    .getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioManager == null
                || mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            // 如果不是铃音模式 在静音或者振动下 不需要响铃
            return;
        }

        // 是否是耳机播放
        boolean isWired = mAudioManager.isWiredHeadsetOn();
        float audioMaxVolume = mAudioManager.getStreamMaxVolume(
                isWired ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_RING);
        float audioCurrentVolume = mAudioManager.getStreamVolume(
                isWired ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_RING);
        final float volumeRatio = audioCurrentVolume / audioMaxVolume;

        int soundIds = -1;
        if (isWired) {
            // 耳机播放
            if (soundWirePollCache.get(resId) != 0) {
                soundIds = soundWirePollCache.get(resId);
                // 耳机播放
                mSoundWirePool.play(soundIds, volumeRatio, volumeRatio, 1, 0, 1);
            } else {
                soundIds = mSoundWirePool.load(context, resId, 1);
                mSoundWirePool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        if (status == 0) {
                            soundPool.play(sampleId, volumeRatio, volumeRatio, 1, 0, 1);
                        }
                    }
                });
                soundWirePollCache.put(resId, soundIds);
            }

        } else {
            // 外放
            if (soundPoolCache.get(resId) != 0) {
                soundIds = soundPoolCache.get(resId);
                // 扬声器播放
                mSoundPool.play(soundIds, volumeRatio, volumeRatio, 1, 0, 1);

            } else {
                soundIds = mSoundPool.load(context, resId, 1);
                mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        if (status == 0) {
                            soundPool.play(sampleId, volumeRatio, volumeRatio, 1, 0, 1);
                        }
                    }
                });
                soundPoolCache.put(resId, soundIds);
            }

        }
    }

    /**
     * 播放收到消息的音效
     */
    void playPromitReceMsgVoice(Context context) {
        playShortVoice(context, R.raw.message_received);
    }

    /**
     * 播放发送消息的音效
     */
    public void playPromitSendMsgVoice(Context context) {
        playShortVoice(context, R.raw.message_send);
    }

    /**
     * 振动 RINGER_MODE_SILENT 没有铃声 没有振动 RINGER_MODE_VIBRATE 没有铃声 只有振动
     * RINGER_MODE_NORMAL 有铃声 有振动
     */
    void vibrate(Context context) {
        if (mAudioManager == null
                || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            // 铃声模式下 振动的开关状态无法获取到 永远振动 静音模式下可以区分振动是否的打开
            return;
        }
        Vibrator vibrator = (Vibrator)context.getApplicationContext()
                .getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(350);
        }
    }

    public void stop() {
        if (mSoundPlayer != null && mSoundPlayer.isPlaying()) {
            mSoundPlayer.stop();
            mSoundPlayer.release();
            mSoundPlayer = null;
        }
    }

    /**
     * 播放语音消息
     *
     * @param filePath 语音文件路径
     * @param listener 播放完成的回调
     */
    public void playVoice(String filePath, MediaPlayer.OnCompletionListener listener) {
        try {
            stop();
            // amr player
            mSoundPlayer = new MediaPlayer();
            mSoundPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
            mSoundPlayer.setDataSource(filePath);
            mSoundPlayer.setOnCompletionListener(listener);
            mSoundPlayer.prepare();
            mSoundPlayer.start();
        } catch (Exception e) {
            listener.onCompletion(mSoundPlayer);
        }
    }
}
