package cn.lemage_scanlib.android;


import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;

import cn.lemage_scanlib.CaptureActivity;
import cn.lemage_scanlib.camera.CameraManager;
import cn.lemage_scanlib.decode.DecodeThread;
import cn.lemage_scanlib.view.ViewfinderResultPointCallback;

/**
 * @author zhaoguangyang
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();
    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    private CaptureActivityHandler.State state;
    private final CameraManager cameraManager;

    public CaptureActivityHandler(CaptureActivity activity, CameraManager cameraManager) {
        this.activity = activity;
        this.decodeThread = new DecodeThread(activity, new ViewfinderResultPointCallback(activity.getViewfinderView()));
        this.decodeThread.start();
        this.state = CaptureActivityHandler.State.SUCCESS;
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        this.restartPreviewAndDecode();
    }

    public void handleMessage(Message message) {
        switch(message.what) {
            case 2:
                this.state = CaptureActivityHandler.State.PREVIEW;
                this.cameraManager.requestPreviewFrame(this.decodeThread.getHandler(), 1);
                break;
            case 3:
                this.state = CaptureActivityHandler.State.SUCCESS;
                this.activity.handleDecode((Result)message.obj);
            case 4:
            case 5:
            default:
                break;
            case 6:
                this.restartPreviewAndDecode();
                break;
            case 7:
                this.activity.setResult(-1, (Intent)message.obj);
                this.activity.finish();
                break;
            case 8:
                this.activity.switchFlashImg(8);
                break;
            case 9:
                this.activity.switchFlashImg(9);
        }

    }

    public void quitSynchronously() {
        this.state = CaptureActivityHandler.State.DONE;
        this.cameraManager.stopPreview();
        Message quit = Message.obtain(this.decodeThread.getHandler(), 5);
        quit.sendToTarget();

        try {
            this.decodeThread.join(500L);
        } catch (InterruptedException var3) {
            ;
        }

        this.removeMessages(3);
        this.removeMessages(2);
    }

    public void restartPreviewAndDecode() {
        if (this.state == CaptureActivityHandler.State.SUCCESS) {
            this.state = CaptureActivityHandler.State.PREVIEW;
            this.cameraManager.requestPreviewFrame(this.decodeThread.getHandler(), 1);
            this.activity.drawViewfinder();
        }

    }

    private static enum State {
        PREVIEW,
        SUCCESS,
        DONE;

        private State() {
        }
    }
}
