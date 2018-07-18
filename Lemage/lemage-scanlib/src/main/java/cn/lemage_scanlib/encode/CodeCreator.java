package cn.lemage_scanlib.encode;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * @author zhaoguangyang
 */
public class CodeCreator {

    private static Bitmap logoBitmap;

    public CodeCreator() {
    }

    public static Bitmap createQRCode(String content, int w, int h, Bitmap logo) throws WriterException {
        if (TextUtils.isEmpty(content)) {
            return null;
        } else {
            int offsetX = w / 2;
            int offsetY = h / 2;
            if (logo != null) {
                Matrix matrix = new Matrix();
                float scaleFactor = Math.min((float)w * 1.0F / 5.0F / (float)logo.getWidth(), (float)h * 1.0F / 5.0F / (float)logo.getHeight());
                matrix.postScale(scaleFactor, scaleFactor);
                logoBitmap = Bitmap.createBitmap(logo, 0, 0, logo.getWidth(), logo.getHeight(), matrix, true);
            }

            int logoW = 0;
            int logoH = 0;
            if (logoBitmap != null) {
                logoW = logoBitmap.getWidth();
                logoH = logoBitmap.getHeight();
                offsetX = (w - logoW) / 2;
                offsetY = (h - logoH) / 2;
            }

            Hashtable<EncodeHintType, Object> hints = new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix matrix = (new MultiFormatWriter()).encode(content, BarcodeFormat.QR_CODE, w, h, hints);
            int[] pixels = new int[w * h];

            for(int y = 0; y < h; ++y) {
                for(int x = 0; x < w; ++x) {
                    if (x >= offsetX && x < offsetX + logoW && y >= offsetY && y < offsetY + logoH) {
                        int pixel = logoBitmap.getPixel(x - offsetX, y - offsetY);
                        if (pixel == 0) {
                            if (matrix.get(x, y)) {
                                pixel = -16777216;
                            } else {
                                pixel = -1;
                            }
                        }

                        pixels[y * w + x] = pixel;
                    } else if (matrix.get(x, y)) {
                        pixels[y * w + x] = -16777216;
                    } else {
                        pixels[y * w + x] = -1;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        }
    }
}
