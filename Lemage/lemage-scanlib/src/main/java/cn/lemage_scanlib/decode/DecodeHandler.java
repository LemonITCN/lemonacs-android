package cn.lemage_scanlib.decode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Map;

import cn.lemage_scanlib.CaptureActivity;

/**
 * @author zhaoguangyang
 */
public final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();
    private final CaptureActivity activity;
    private final MultiFormatReader multiFormatReader = new MultiFormatReader();
    private boolean running = true;

    DecodeHandler(CaptureActivity activity, Map<DecodeHintType, Object> hints) {
        this.multiFormatReader.setHints(hints);
        this.activity = activity;
    }

    public void handleMessage(Message message) {
        if (this.running) {
            switch(message.what) {
                case 1:
                    this.decode((byte[])((byte[])message.obj), message.arg1, message.arg2);
                    break;
                case 5:
                    this.running = false;
                    Looper.myLooper().quit();
            }

        }
    }

    private void decode(byte[] data, int width, int height) {
        Result rawResult = null;
        byte[] rotatedData = new byte[data.length];

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }

        PlanarYUVLuminanceSource source = this.activity.getCameraManager().buildLuminanceSource(rotatedData, height, width);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                rawResult = this.multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException var13) {
                ;
            } finally {
                this.multiFormatReader.reset();
            }
        }

        Handler handler = this.activity.getHandler();
        Message message;
        if (rawResult != null) {
            if (handler != null) {
                message = Message.obtain(handler, 3, rawResult);
                message.sendToTarget();
            }
        } else if (handler != null) {
            message = Message.obtain(handler, 2);
            message.sendToTarget();
        }

    }
}
