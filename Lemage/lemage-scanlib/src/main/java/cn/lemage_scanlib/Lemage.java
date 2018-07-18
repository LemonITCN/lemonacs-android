package cn.lemage_scanlib;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.lemage_scanlib.bean.ZxingConfig;
import cn.lemage_scanlib.common.Constant;
import cn.lemage_scanlib.encode.CodeCreator;

import static android.graphics.Bitmap.createBitmap;

/**
 * 扫描相关（扫描二维码，条形码，生成二维码，条形码）统一入口类
 * @author zhaoguangyang
 */
public class Lemage {

    private final static String TAG = "Lemage";

    /**
     * 扫描入口函数
     * @param mContext
     * @param isPlayBeep            扫描时是否需要声音
     * @param isScanShake           扫描时是否需要震动
     * @param isShowbottomLayout   扫描界面是否显示底部条（打开闪光灯按钮，去相册按钮）
     * @param isShowFlashLight     扫描界面是否显示打开闪光灯按钮
     * @param isShowAlbum           扫描界面是否显示去相册按钮
     * @param themeColor            扫描框四个角的颜色和扫描条的颜色
     * @param scanWidth             扫描框的宽度
     * @param scanHeight            扫描框的高度
     */
    public static void startScan(Context mContext, boolean isPlayBeep, boolean isScanShake, boolean isShowbottomLayout, boolean isShowFlashLight, boolean isShowAlbum, int themeColor, int scanWidth, int scanHeight, ScanResultCallback scanResultCallback) {
        Intent intent = new Intent(mContext, CaptureActivity.class);
        ZxingConfig config = new ZxingConfig();
        config.setPlayBeep(isPlayBeep);
        config.setShake(isScanShake);
        config.setShowbottomLayout(isShowbottomLayout);
        config.setShowFlashLight(isShowFlashLight);
        config.setShowAlbum(isShowAlbum);
        config.setThemeColor(themeColor);
        config.setScanWidth(scanWidth);
        config.setScanHeight(scanHeight);
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        CaptureActivity.setScanResultCallback(scanResultCallback);
        mContext.startActivity(intent);
    }


    /**
     * 根据字符串生成二维码
     * @param mContext
     * @param str          需要生成二维码的字符串
     * @param width        生成的二维码图片宽
     * @param height       生成的二维码图片高
     * @return
     */
    public static void createQRCode(Context mContext, String str, int width, int height, CreateCodeCallback createCodeCallback) {
        Bitmap bitmap = null;
//        Bitmap logo = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.scan_light);
//        Bitmap logo = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.scan_light);
        Bitmap logo = createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        try {
            bitmap = CodeCreator.createQRCode(str, width, height, logo);
        } catch (WriterException e) {
            e.printStackTrace();
        }
//        if(bitmap != null) {
//            imageview.setImageBitmap(bitmap);
//        }
        createCodeCallback.createQRcodeFinishResult(bitmap);
    }


    /**
     * 根据字符串生成条形码
     * @param mContext
     * @param str                           需要生成二维码的字符串
     * @param width                         生成的二维码图片宽
     * @param height                        生成的二维码图片高
     * @param isShowText                   生成的条形码下面是否显示文字
     * @param createCodeCallback
     */
    public static void createStripeCode(Context mContext, String str, int width, int height, boolean isShowText, CreateCodeCallback createCodeCallback) {
        if(TextUtils.isEmpty(str)) return;
        if(isContainChinese(str)) return;
        Bitmap bitmap = createBarcode(str, width, height, isShowText);
        createCodeCallback.createQRcodeFinishResult(bitmap);
    }

    /**
     * 扫描本地条形码，二维码
     * @param mContext
     * @param scanBitmap   要扫描的图片
     * @param scanResultCallback
     */
    public static void scanLocalPhoto(Context mContext, Bitmap scanBitmap, ScanResultCallback scanResultCallback) {
        if(scanBitmap == null) {
            return;
        }
        String strResult = scanningImage(scanBitmap);
        if(!TextUtils.isEmpty(strResult)) {

        }else {
            strResult = "出错了";
        }
        scanResultCallback.scanFinishResult(strResult);
    }

    /**
     * 扫描本地条形码，二维码
     * @param mContext
     * @param path                要扫描的图片路径
     * @param scanResultCallback
     */
    public static void scanLocalPhoto(Context mContext, String path, ScanResultCallback scanResultCallback) {
        String strResult = scanningImage(path);
        if(!TextUtils.isEmpty(strResult)) {

        }else {
            strResult = "出错了";
        }
        scanResultCallback.scanFinishResult(strResult);
    }

    /**
     * 扫描二维码图片的方法
     * @param scanBitmap
     * @return
     */
    private static String scanningImage(Bitmap scanBitmap) {
        String content = "";
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码

        float sampleSize = (float) 200 / scanBitmap.getWidth();  // 得到原图片的缩小比例
        if (sampleSize <= 0)
            sampleSize = 1;
        float scaleSize = (float) new BigDecimal(sampleSize).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  // 保留小数点后2位
        Matrix matrix = new Matrix();
        matrix.postScale(scaleSize, scaleSize);
        // 得到新缩放后的图片
        Bitmap bitmap = Bitmap.createBitmap(scanBitmap, 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight(), matrix, false);

        if(bitmap == null) return content;

        byte[] data = rgb2YUV(scanBitmap);
        LuminanceSource source1 = new PlanarYUVLuminanceSource(
                data,
                scanBitmap.getWidth(),
                scanBitmap.getHeight(),
                0,
                0,
                scanBitmap.getWidth(),
                scanBitmap.getHeight(),
                false);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source1));
        MultiFormatReader reader1 = new MultiFormatReader();
        Result result1 = null;
        try {
            result1 = reader1.decode(binaryBitmap);
            content = result1.getText();
            // 处理解析结果，防止有乱码
            content = recode(content);
            Log.e("123content", content);
        } catch (NotFoundException e1) {
            e1.printStackTrace();
        }
        return content;
    }


    /**
     * 扫描二维码图片的方法
     * @param path
     * @return
     */
    private static String scanningImage(String path) {
        String content = "";
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码\


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        BitmapFactory.decodeFile(path, options);  // 预加载
        Log.e(TAG, "原图大小 ： 宽 == " + options.outWidth + "   高 == " + options.outHeight);
        options.inJustDecodeBounds = false; // 获取新的大小

        int sampleSize = (int) (options.outWidth / (float) 200);

        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        Log.e(TAG, "新图大小 ： 宽 == " + scanBitmap.getWidth() + "   高 == " + scanBitmap.getHeight());
        if(scanBitmap == null) return content;


        byte[] data = rgb2YUV(scanBitmap);
        LuminanceSource source1 = new PlanarYUVLuminanceSource(
                data,
                scanBitmap.getWidth(),
                scanBitmap.getHeight(),
                0,
                0,
                scanBitmap.getWidth(),
                scanBitmap.getHeight(),
                false);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source1));
        MultiFormatReader reader1 = new MultiFormatReader();
        Result result1 = null;
        try {
            result1 = reader1.decode(binaryBitmap);
            content = result1.getText();
            // 处理解析结果，防止有乱码
            content = recode(content);
            Log.e("123content", content);
        } catch (NotFoundException e1) {
            e1.printStackTrace();
        }
        return content;
    }


    /**
     * 解析Bitmap
     * @param bitmap
     * @return
     */
    private static byte[] rgb2YUV(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int len = width * height;
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = pixels[i * width + j] & 0x00FFFFFF;

                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;

                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);

                yuv[i * width + j] = (byte) y;
//                yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
//                yuv[len + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
        return yuv;
    }


    private static String recode(String str) {
        String format = "";
        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
            if (ISO) {
                format = new String(str.getBytes("ISO-8859-1"), "GB2312");
                Log.e(TAG, "ISO8859-1 ================= " + format);
            } else {
                format = str;
                Log.i(TAG, "stringExtra ==================== " + str);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return format;
    }



    /**
     * 判断字符串中是否有中文
     * @param str
     * @return
     */
    public static boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }



    /**
     * 绘制条形码
     * @param content 要生成条形码包含的内容
     * @param widthPix 条形码的宽度
     * @param heightPix 条形码的高度
     * @param isShowText  否则显示条形码包含的内容
     * @return 返回生成条形的位图
     */
    private static Bitmap createBarcode( String content, int widthPix, int heightPix, boolean isShowText) {
        if (TextUtils.isEmpty(content)){
            return null;
        }
        //配置参数
        Map<EncodeHintType,Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 容错级别 这里选择最高H级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            // 图像数据转换，使用了矩阵转换 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
//             下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000; // 黑色
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;// 白色
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
            if (isShowText){
                bitmap = showContent(bitmap,content);
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }



    /**
     * 显示条形的内容
     * @param bCBitmap 已生成的条形码的位图
     * @param content  条形码包含的内容
     * @return 返回生成的新位图,它是 方法createQRCode()返回的位图与新绘制文本content的组合
     */
    private static Bitmap showContent(Bitmap bCBitmap , String content){
        if (TextUtils.isEmpty(content) || null == bCBitmap){
            return null;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);//设置填充样式
        paint.setTextSize(23);
//        paint.setTextAlign(Paint.Align.CENTER);
        //测量字符串的宽度
        int textWidth = (int) paint.measureText(content);
        Paint.FontMetrics fm = paint.getFontMetrics();
        //绘制字符串矩形区域的高度
        int textHeight = (int) (fm.bottom - fm.top);
        // x 轴的缩放比率
        float scaleRateX = bCBitmap.getWidth() / textWidth;
        paint.setTextScaleX(scaleRateX);
        //绘制文本的基线
        int baseLine = bCBitmap.getHeight() + textHeight;
        //创建一个图层，然后在这个图层上绘制bCBitmap、content
        Bitmap  bitmap = Bitmap.createBitmap(bCBitmap.getWidth(),bCBitmap.getHeight() + 2 * textHeight,Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas();
        canvas.drawColor(Color.WHITE);
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(bCBitmap, 0, 0, null);
//        canvas.drawText(content,bCBitmap.getWidth() / 10, baseLine,paint);
        paint.setTextAlign(Paint.Align.CENTER);  // 设置从中心点开始写文字，默认是从文字左边开始写
        canvas.drawText(content,bCBitmap.getWidth() / 2, baseLine, paint);
//        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.save();
        canvas.restore();
        return bitmap;
    }
}
