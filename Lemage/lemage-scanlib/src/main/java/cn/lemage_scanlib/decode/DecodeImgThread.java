package cn.lemage_scanlib.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author zhaoguangyang
 */
public class DecodeImgThread extends Thread {

    private String imgPath;
    private DecodeImgCallback callback;

    public DecodeImgThread(String imgPath, DecodeImgCallback callback) {
        this.imgPath = imgPath;
        this.callback = callback;
    }

    public void run() {
        super.run();
        if (!TextUtils.isEmpty(this.imgPath) && this.callback != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(this.imgPath, options);
            options.inJustDecodeBounds = false;
            int sampleSize = (int)((float)options.outHeight / 400.0F);
            if (sampleSize <= 0) {
                sampleSize = 1;
            }

            options.inSampleSize = sampleSize;
            Bitmap scanBitmap = BitmapFactory.decodeFile(this.imgPath, options);
            MultiFormatReader multiFormatReader = new MultiFormatReader();
            Hashtable<DecodeHintType, Object> hints = new Hashtable(2);
            Vector<BarcodeFormat> decodeFormats = new Vector();
            if (decodeFormats == null || decodeFormats.isEmpty()) {
                decodeFormats = new Vector();
                decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
                decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
                decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
            }

            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
            multiFormatReader.setHints(hints);
            Result rawResult = null;

            try {
                rawResult = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(scanBitmap))));
                Log.i("解析结果", rawResult.getText());
            } catch (Exception var9) {
                var9.printStackTrace();
            }

            if (rawResult != null) {
                this.callback.onImageDecodeSuccess(rawResult);
            } else {
                this.callback.onImageDecodeFailed();
            }

        }
    }
}
