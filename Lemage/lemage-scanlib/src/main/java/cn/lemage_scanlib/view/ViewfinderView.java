package cn.lemage_scanlib.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.View;

import com.google.zxing.ResultPoint;

import java.util.ArrayList;
import java.util.List;

import cn.lemage_scanlib.R;
import cn.lemage_scanlib.camera.CameraManager;

/**
 * 取景框
 * @author zhaoguangyang
 */
public class ViewfinderView extends View {

    private final String TAG = "ViewfinderView";

    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 160;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;
    private CameraManager cameraManager;
    private final Paint paint = new Paint(1);
    private Bitmap resultBitmap;
    private int maskColor;
    private int resultColor;
    private int resultPointColor;
    private int statusColor;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;
    private int scanLineTop;
    // 扫描速度
    private int SCAN_VELOCITY = 10;
    // 扫描横条的高度
    private int scanLightHeight = 20;
//    Bitmap scanLight;
    // 扫描框直角边框横条的高度
    private int corWidth;

    /**
     * 中间扫描框的宽高
     */
    private int scanViewWidth, scanViewHeight;

    /**
     * 主题颜色, 包括扫描框四个边角框颜色，扫描横条颜色
     */
    private int themeColor;

    public ViewfinderView(Context context, int themeColor, int scanViewWidth, int scanViewHeight) {
        super(context);
        this.themeColor = themeColor;
        this.scanViewWidth = scanViewWidth;
        this.scanViewHeight = scanViewHeight;
        init();
    }

//    public ViewfinderViewNew(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        Resources resources = this.getResources();
//        this.maskColor = resources.getColor(R.color.viewfinder_mask);
//        // 扫描框之外的取景框的背景色
//        this.resultColor = resources.getColor(R.color.result_view);
//        this.resultPointColor = resources.getColor(R.color.possible_result_points);
//        this.statusColor = resources.getColor(R.color.status_text);
//        this.possibleResultPoints = new ArrayList(10);
//        this.lastPossibleResultPoints = null;
//        // 扫描时一直自上而下移动的横条
//        this.scanLight = BitmapFactory.decodeResource(resources, R.drawable.scan_light);
//    }

    private void init() {
        Resources resources = this.getResources();
        this.maskColor = resources.getColor(R.color.viewfinder_mask);
        // 扫描框之外的取景框的背景色
        this.resultColor = resources.getColor(R.color.result_view);
        this.resultPointColor = resources.getColor(R.color.possible_result_points);
        this.statusColor = resources.getColor(R.color.status_text);
        this.possibleResultPoints = new ArrayList(10);
        this.lastPossibleResultPoints = null;
        // 扫描时一直自上而下移动的横条
//        this.scanLight = BitmapFactory.decodeResource(resources, R.drawable.scan_light);
    }


    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
        // 传入扫描框宽高
        this.cameraManager.setScanViewWidth(scanViewWidth);
        this.cameraManager.setScanViewHeight(scanViewHeight);
    }

    @SuppressLint({"DrawAllocation"})
    public void onDraw(Canvas canvas) {
        if (this.cameraManager != null) {
            // 中间扫描框的区域
            Rect frame = this.cameraManager.getFramingRect();
            // 和frame相同的区域
            Rect previewFrame = this.cameraManager.getFramingRectInPreview();
            if (frame != null && previewFrame != null) {
                int width = canvas.getWidth();
                int height = canvas.getHeight();
                this.paint.setColor(this.resultBitmap != null ? this.resultColor : this.maskColor);
                // 画扫描框外外面的稍微暗点颜色的区域（也是取景区域）
                canvas.drawRect(0.0F, 0.0F, (float)width, (float)frame.top, this.paint);
                canvas.drawRect(0.0F, (float)frame.top, (float)frame.left, (float)(frame.bottom + 1), this.paint);
                canvas.drawRect((float)(frame.right + 1), (float)frame.top, (float)width, (float)(frame.bottom + 1), this.paint);
                canvas.drawRect(0.0F, (float)(frame.bottom + 1), (float)width, (float)height, this.paint);

                if (this.resultBitmap != null) {
                    this.paint.setAlpha(160);
                    canvas.drawBitmap(this.resultBitmap, (Rect)null, frame, this.paint);
                } else {
                    // 画扫描框的四个角
                    this.drawFrameBounds(canvas, frame);
                    // 画扫描框中自上而下不断移动的横条，即扫描横条
                    this.drawScanLight(canvas, frame);
                    // 以下部分暂时看不懂为啥不断画小圈，注释掉后暂时没发现有任何影响
//                    float scaleX = (float)frame.width() / (float)previewFrame.width();
//                    float scaleY = (float)frame.height() / (float)previewFrame.height();
//                    List<ResultPoint> currentPossible = this.possibleResultPoints;
//                    List<ResultPoint> currentLast = this.lastPossibleResultPoints;
//                    int frameLeft = frame.left;
//                    int frameTop = frame.top;
//                    if (currentPossible.isEmpty()) {
//                        this.lastPossibleResultPoints = null;
//                    } else {
//                        this.possibleResultPoints = new ArrayList(5);
//                        this.lastPossibleResultPoints = currentPossible;
//                        this.paint.setAlpha(160);
//                        this.paint.setColor(this.resultPointColor);
//                        synchronized(currentPossible) {
//                            Iterator var13 = currentPossible.iterator();
//
//                            while(var13.hasNext()) {
//                                ResultPoint point = (ResultPoint)var13.next();
//                                canvas.drawCircle((float)(frameLeft + (int)(point.getX() * scaleX)), (float)(frameTop + (int)(point.getY() * scaleY)), 6.0F, this.paint);
//                            }
//                        }
//                    }
//
//                    if (currentLast != null) {
//                        this.paint.setAlpha(80);
//                        this.paint.setColor(this.resultPointColor);
//                        synchronized(currentLast) {
//                            float radius = 3.0F;
//                            Iterator var20 = currentLast.iterator();
//
//                            while(var20.hasNext()) {
//                                ResultPoint point = (ResultPoint)var20.next();
//                                canvas.drawCircle((float)(frameLeft + (int)(point.getX() * scaleX)), (float)(frameTop + (int)(point.getY() * scaleY)), radius, this.paint);
//                            }
//                        }
//                    }

                    // 四个坐标参数围成的区域重绘，其他超出的区域不重绘
                    this.postInvalidateDelayed(80L, frame.left - 6, frame.top - 6, frame.right + 6, frame.bottom + 6);
                }

            }
        }
    }

    /**
     * 画扫描框的四个边缘直角框
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {
        // 设置扫描框四个边缘直角边框的颜色
//        this.paint.setColor(-16776961);
        this.paint.setColor(themeColor);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setStrokeWidth(1.0F);
        int width = frame.width();
        // 设置直角边框的长度和宽度，长度是扫描框的 1 /10， 宽度是长度的 1 / 5， 上限是15
        int corLength = (int)((double)width * 0.1D);
        corWidth = (int)((double)corLength * 0.2D);
        corWidth = corWidth > 15 ? 15 : corWidth;
        // 画四个角
        canvas.drawRect((float)(frame.left - corWidth), (float)frame.top, (float)frame.left, (float)(frame.top + corLength), this.paint);
        canvas.drawRect((float)(frame.left - corWidth), (float)(frame.top - corWidth), (float)(frame.left + corLength), (float)frame.top, this.paint);
        canvas.drawRect((float)frame.right, (float)frame.top, (float)(frame.right + corWidth), (float)(frame.top + corLength), this.paint);
        canvas.drawRect((float)(frame.right - corLength), (float)(frame.top - corWidth), (float)(frame.right + corWidth), (float)frame.top, this.paint);
        canvas.drawRect((float)(frame.left - corWidth), (float)(frame.bottom - corLength), (float)frame.left, (float)frame.bottom, this.paint);
        canvas.drawRect((float)(frame.left - corWidth), (float)frame.bottom, (float)(frame.left + corLength), (float)(frame.bottom + corWidth), this.paint);
        canvas.drawRect((float)frame.right, (float)(frame.bottom - corLength), (float)(frame.right + corWidth), (float)frame.bottom, this.paint);
        canvas.drawRect((float)(frame.right - corLength), (float)frame.bottom, (float)(frame.right + corWidth), (float)(frame.bottom + corWidth), this.paint);
    }


    /**
     * 提示：
     * 二维码/条码放入取景框内，即可自动扫描（暂时不调用）
     * @param canvas
     * @param frame
     * @param width
     */
    private void drawStatusText(Canvas canvas, Rect frame, int width) {
        String statusText1 = this.getResources().getString(R.string.viewfinderview_status_text1);
        String statusText2 = this.getResources().getString(R.string.viewfinderview_status_text2);
        byte statusTextSize;
        if (width >= 480 && width <= 600) {
            statusTextSize = 22;
        } else if (width > 600 && width <= 720) {
            statusTextSize = 26;
        } else {
            statusTextSize = 45;
        }

        int statusPaddingTop = 180;
        this.paint.setColor(this.statusColor);
        this.paint.setTextSize((float)statusTextSize);
        int textWidth1 = (int)this.paint.measureText(statusText1);
        canvas.drawText(statusText1, (float)((width - textWidth1) / 2), (float)(frame.top - statusPaddingTop), this.paint);
        int textWidth2 = (int)this.paint.measureText(statusText2);
        canvas.drawText(statusText2, (float)((width - textWidth2) / 2), (float)(frame.top - statusPaddingTop + 60), this.paint);
    }

    /**
     * 画扫描框中自上而下不断移动的横条
     * @param canvas
     * @param frame
     */
    private void drawScanLight(Canvas canvas, Rect frame) {
        // scanLineTop != 0 后一直刷新
        if (this.scanLineTop != 0 && this.scanLineTop + this.SCAN_VELOCITY < frame.bottom - corWidth) {
            this.SCAN_VELOCITY = (frame.bottom - this.scanLineTop) / 12;
            this.SCAN_VELOCITY = (int)(this.SCAN_VELOCITY > 10 ? Math.ceil((double)this.SCAN_VELOCITY) : 10.0D);
            this.scanLineTop += this.SCAN_VELOCITY;
        }
        // 第一次显示位置在最上面, 如果是全屏扫描框，frame.top = 0，将永远不能进入if里面，所以+1
        else {
            this.scanLineTop = frame.top == 0 ? 1 : frame.top;
        }

        Rect scanRect = new Rect(frame.left, this.scanLineTop + 8, frame.right, this.scanLineTop + this.scanLightHeight - 8);
//        canvas.drawBitmap(this.scanLight, (Rect)null, scanRect, this.paint);
        // 颜色渐变
        LinearGradient backGradient = new LinearGradient(frame.left, this.scanLineTop + 8, frame.right, this.scanLineTop + this.scanLightHeight - 8, new int[]{Color.parseColor("#00000000"), themeColor, Color.parseColor("#00000000")}, null, Shader.TileMode.CLAMP);
        Paint paintScanLine = new Paint();
        paintScanLine.setShader(backGradient);
        canvas.drawRect(scanRect, paintScanLine);
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }

        this.invalidate();
    }

    public void drawResultBitmap(Bitmap barcode) {
        this.resultBitmap = barcode;
        this.invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = this.possibleResultPoints;
        synchronized(points) {
            points.add(point);
            int size = points.size();
            if (size > 20) {
                points.subList(0, size - 10).clear();
            }

        }
    }
}
