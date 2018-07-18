package cn.lemage_scanlib.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.IOException;

import cn.lemage_scanlib.android.CaptureActivityHandler;

/**
 * @author zhaoguangyang
 */
public class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();
    private static CameraManager cameraManager;
    private final Context context;
    private final CameraConfigurationManager configManager;
    private Camera camera;
    private AutoFocusManager autoFocusManager;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private int requestedCameraId = -1;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;
    private final PreviewCallback previewCallback;

    /**
     * 中间扫描框的宽高
     */
    private int scanViewWidth, scanViewHeight;

    public CameraManager(Context context) {
        this.context = context;
        this.configManager = new CameraConfigurationManager(context);
        this.previewCallback = new PreviewCallback(this.configManager);
    }

    public static void init(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context);
        }

    }

    public synchronized void openDriver(SurfaceHolder holder) throws IOException {
        Camera theCamera = this.camera;
        if (theCamera == null) {
            if (this.requestedCameraId >= 0) {
                theCamera = OpenCameraInterface.open(this.requestedCameraId);
            } else {
                theCamera = OpenCameraInterface.open();
            }

            if (theCamera == null) {
                throw new IOException();
            }

            this.camera = theCamera;
        }

        theCamera.setPreviewDisplay(holder);
        if (!this.initialized) {
            this.initialized = true;
            this.configManager.initFromCameraParameters(theCamera);
            if (this.requestedFramingRectWidth > 0 && this.requestedFramingRectHeight > 0) {
                this.setManualFramingRect(this.requestedFramingRectWidth, this.requestedFramingRectHeight);
                this.requestedFramingRectWidth = 0;
                this.requestedFramingRectHeight = 0;
            }
        }

        Camera.Parameters parameters = theCamera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten();

        try {
            this.configManager.setDesiredCameraParameters(theCamera);
        } catch (RuntimeException var8) {
            Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
            Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
            if (parametersFlattened != null) {
                parameters = theCamera.getParameters();
                parameters.unflatten(parametersFlattened);

                try {
                    theCamera.setParameters(parameters);
                    this.configManager.setDesiredCameraParameters(theCamera);
                } catch (RuntimeException var7) {
                    Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }

    }

    public synchronized boolean isOpen() {
        return this.camera != null;
    }

    public synchronized void closeDriver() {
        if (this.camera != null) {
            this.camera.release();
            this.camera = null;
            this.framingRect = null;
            this.framingRectInPreview = null;
        }

    }

    public void switchFlashLight(CaptureActivityHandler handler) {
        Camera.Parameters parameters = this.camera.getParameters();
        Message msg = new Message();
        String flashMode = parameters.getFlashMode();
        if (flashMode.equals("torch")) {
            parameters.setFlashMode("off");
            msg.what = 9;
        } else {
            parameters.setFlashMode("torch");
            msg.what = 8;
            Log.d(TAG, "switchFlashLight: 123");
        }

        this.camera.setParameters(parameters);
        handler.sendMessage(msg);
    }

    public synchronized void startPreview() {
        Camera theCamera = this.camera;
        if (theCamera != null && !this.previewing) {
            theCamera.startPreview();
            this.previewing = true;
            this.autoFocusManager = new AutoFocusManager(this.camera);
        }

    }

    public synchronized void stopPreview() {
        if (this.autoFocusManager != null) {
            this.autoFocusManager.stop();
            this.autoFocusManager = null;
        }

        if (this.camera != null && this.previewing) {
            this.camera.stopPreview();
            this.previewCallback.setHandler((Handler)null, 0);
            this.previewing = false;
        }

    }

    public synchronized void requestPreviewFrame(Handler handler, int message) {
        Camera theCamera = this.camera;
        if (theCamera != null && this.previewing) {
            this.previewCallback.setHandler(handler, message);
            theCamera.setOneShotPreviewCallback(this.previewCallback);
        }

    }

    // 设置中间扫描框的区域
    public synchronized Rect getFramingRect() {
        if (this.framingRect == null) {
            if (this.camera == null) {
                return null;
            }
            // 屏幕右下角的点
            Point screenResolution = this.configManager.getScreenResolution();
            if (screenResolution == null) {
                return null;
            }

            // 屏幕长宽
            int screenWidth = screenResolution.x;
            int screenHeight = screenResolution.y;
//            // 中间扫描框的宽度
//            int width = (int)((double)screenResolutionX * 0.6D);
//            // 左侧边缘
//            int leftOffset = (screenResolution.x - width) / 2;
//            // 上部边缘
//            int topOffset = (screenResolution.y - width) / 5;
//            this.framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + width);
            // 如果传入的扫描框宽度<=0或者>屏幕宽度，都设置为默认的屏幕宽度的 3 / 5
            scanViewWidth = scanViewWidth <= 0 || scanViewWidth > screenWidth ? (int)((double)screenWidth * 0.6D) : scanViewWidth;
            // 传入的高度的值<=0获取>屏幕高度，都设置为和宽度相等
            scanViewHeight = scanViewHeight <= 0 || scanViewHeight > screenHeight ? scanViewWidth : scanViewHeight;
//            scanViewHeight = scanViewWidth - scanViewHeight > 0 ? scanViewHeight : scanViewWidth;
            int leftOffset = (screenResolution.x - scanViewWidth) / 2;
            int topOffset = (screenResolution.y - scanViewHeight) / 4;
            this.framingRect = new Rect(leftOffset, topOffset, leftOffset + scanViewWidth, topOffset + scanViewHeight);
        }

        return this.framingRect;
    }


    public synchronized Rect getFramingRectInPreview() {
        if (this.framingRectInPreview == null) {
            Rect framingRect = this.getFramingRect();
            if (framingRect == null) {
                return null;
            }

            Rect rect = new Rect(framingRect);
            Point cameraResolution = this.configManager.getCameraResolution();   // (800, 480)
            Point screenResolution = this.configManager.getScreenResolution();   // (480, 800)
            if (cameraResolution == null || screenResolution == null) {
                return null;
            }

            rect.left = rect.left * cameraResolution.y / screenResolution.x;
            rect.right = rect.right * cameraResolution.y / screenResolution.x;
            rect.top = rect.top * cameraResolution.x / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
            this.framingRectInPreview = rect;
        }

        return this.framingRectInPreview;
    }

    public synchronized void setManualCameraId(int cameraId) {
        this.requestedCameraId = cameraId;
    }

    public synchronized void setManualFramingRect(int width, int height) {
        if (this.initialized) {
            Point screenResolution = this.configManager.getScreenResolution();
            if (width > screenResolution.x) {
                width = screenResolution.x;
            }

            if (height > screenResolution.y) {
                height = screenResolution.y;
            }

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            this.framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            Log.d(TAG, "Calculated manual framing rect: " + this.framingRect);
            this.framingRectInPreview = null;
        } else {
            this.requestedFramingRectWidth = width;
            this.requestedFramingRectHeight = height;
        }

    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = this.getFramingRectInPreview();
        return rect == null ? null : new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
    }

    public static CameraManager get() {
        return cameraManager;
    }

    public void setScanViewWidth(int scanViewWidth) {
        this.scanViewWidth = scanViewWidth;
    }

    public void setScanViewHeight(int scanViewHeight) {
        this.scanViewHeight = scanViewHeight;
    }
}
