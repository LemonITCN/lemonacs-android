package cn.lemage_scanlib.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import java.io.Closeable;

/**
 * @author zhaoguangyang
 */
public class BeepManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, Closeable {

    private static final String TAG = BeepManager.class.getSimpleName();
    private static final float BEEP_VOLUME = 0.1F;
    private static final long VIBRATE_DURATION = 200L;
    private final Activity activity;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;

    public BeepManager(Activity activity) {
        this.activity = activity;
        this.mediaPlayer = null;
        this.updatePrefs();
    }

    public boolean isPlayBeep() {
        return this.playBeep;
    }

    public void setPlayBeep(boolean playBeep) {
        this.playBeep = playBeep;
    }

    public boolean isVibrate() {
        return this.vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public synchronized void updatePrefs() {
        if (this.playBeep && this.mediaPlayer == null) {
            this.activity.setVolumeControlStream(3);
            this.mediaPlayer = this.buildMediaPlayer(this.activity);
        }

    }

    @SuppressLint({"MissingPermission"})
    public synchronized void playBeepSoundAndVibrate() {
        if (this.playBeep && this.mediaPlayer != null) {
            this.mediaPlayer.start();
        }

        if (this.vibrate) {
            Vibrator vibrator = (Vibrator)this.activity.getSystemService("vibrator");
            vibrator.vibrate(200L);
        }

    }

    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_RINGTONE);
    }

    private MediaPlayer buildMediaPlayer(Context activity) {
        // 不用资源文件，获取手机系统的蜂鸣声
        MediaPlayer mediaPlayer = MediaPlayer.create(activity, getSystemDefultRingtoneUri());
        if(mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setAudioStreamType(3);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setVolume(0.1F, 0.1F);
//            try {
//                mediaPlayer.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//                mediaPlayer.release();
//                return null;
//            }
        }
        return mediaPlayer;

//        MediaPlayer mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(3);
//        mediaPlayer.setOnCompletionListener(this);
//        mediaPlayer.setOnErrorListener(this);
//
//        try {
//            AssetFileDescriptor file = activity.getResources().openRawResourceFd(raw.beep);
//
//            try {
//                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
//            } finally {
//                file.close();
//            }
//
//            mediaPlayer.setVolume(0.1F, 0.1F);
//            mediaPlayer.prepare();
//            return mediaPlayer;
//        } catch (IOException var8) {
//            Log.w(TAG, var8);
//            mediaPlayer.release();
//            return null;
//        }
    }

    public void onCompletion(MediaPlayer mp) {
        mp.seekTo(0);
    }

    public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
        if (what == 100) {
            this.activity.finish();
        } else {
            mp.release();
            this.mediaPlayer = null;
            this.updatePrefs();
        }

        return true;
    }

    public synchronized void close() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }

    }
}
