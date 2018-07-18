package cn.lemage_scanlib.decode;

import android.os.Handler;
import android.os.Looper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import cn.lemage_scanlib.CaptureActivity;

/**
 * @author zhaoguangyang
 */
public final class DecodeThread extends Thread {

    private final CaptureActivity activity;
    private final Hashtable<DecodeHintType, Object> hints;
    private final Vector<BarcodeFormat> decodeFormats;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    public DecodeThread(CaptureActivity activity, ResultPointCallback resultPointCallback) {
        this.activity = activity;
        this.handlerInitLatch = new CountDownLatch(1);
        this.hints = new Hashtable();
        this.decodeFormats = new Vector();
        this.decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
        this.decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        this.decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        this.hints.put(DecodeHintType.POSSIBLE_FORMATS, this.decodeFormats);
        this.hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        this.hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
    }

    public Handler getHandler() {
        try {
            this.handlerInitLatch.await();
        } catch (InterruptedException var2) {
            ;
        }

        return this.handler;
    }

    public void run() {
        Looper.prepare();
        this.handler = new DecodeHandler(this.activity, this.hints);
        this.handlerInitLatch.countDown();
        Looper.loop();
    }
}
