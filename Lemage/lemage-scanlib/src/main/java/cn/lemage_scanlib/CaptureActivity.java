package cn.lemage_scanlib;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.io.IOException;

import cn.lemage_scanlib.android.BeepManager;
import cn.lemage_scanlib.android.CaptureActivityHandler;
import cn.lemage_scanlib.android.FinishListener;
import cn.lemage_scanlib.android.InactivityTimer;
import cn.lemage_scanlib.bean.ZxingConfig;
import cn.lemage_scanlib.camera.CameraManager;
import cn.lemage_scanlib.decode.DecodeImgCallback;
import cn.lemage_scanlib.decode.DecodeImgThread;
import cn.lemage_scanlib.decode.ImageUtil;
import cn.lemage_scanlib.view.BackView;
import cn.lemage_scanlib.view.ViewfinderView;

/**
 * @author zhaoguangyang
 */
public class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private ZxingConfig config;
    private SurfaceView previewView;
    private ViewfinderView viewfinderView;
    /**
     * 顶部条返回键
     */
//    private ImageView backIv;
    private BackView backIv;

    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;
    /**
     * 根布局
     */
    private RelativeLayout rootLayout;
    /**
     * 整个控件层父布局
     */
    private LinearLayout viewsLayout;
    /**
     * 顶部条
     */
    private RelativeLayout topLayout;
    /**
     * 顶部条标题
     */
    private TextView titleText;
    /**
     * 底部条
     */
    private LinearLayout bottomLayout;
    /**
     * 底部条打开闪光灯父控件
     */
    private LinearLayout flashLightLayout;
    /**
     * 打开闪光灯图片
     */
    private ImageView flashLightIv;
    /**
     * 打开闪光灯文字
     */
    private TextView flashLightTv;
    /**
     * 底部条相册
     */
    private LinearLayout albumLayout;
    /**
     * 底部条相册图片
     */
    private ImageView albumIv;
    /**
     * 底部条相册文字
     */
    private TextView albumTv;
    /**
     * 扫描框宽度高度
     */
    private int scanWidth, scanHeight;
    /**
     * 主题颜色
     */
    private int themeColor;
    /**
     * 扫描界面是否需要底部条（打开闪光灯和相册）
     */
    private boolean isShowbottomLayout;
    /**
     * 是否显示打开闪光灯
     */
    private boolean isShowFlashLight;
    /**
     * 是否显示打开相册
     */
    private boolean isShowAlbum;

    public ViewfinderView getViewfinderView() {
        return this.viewfinderView;
    }

    public Handler getHandler() {
        return this.handler;
    }

    public CameraManager getCameraManager() {
        return this.cameraManager;
    }

    public void drawViewfinder() {
        this.viewfinderView.drawViewfinder();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(128);
        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(-16777216);
        }
        getData();
        initViewNew();
        addView();
        setContentView(rootLayout);
    }

    private void getData() {
        try {
            this.config = (ZxingConfig)this.getIntent().getExtras().get("zxingConfig");
        } catch (Exception var4) {
            Log.i("config", var4.toString());
        }
        if (this.config == null) {
            this.config = new ZxingConfig();
        }
        this.hasSurface = false;
        this.inactivityTimer = new InactivityTimer(this);
        this.beepManager = new BeepManager(this);
        this.beepManager.setPlayBeep(this.config.isPlayBeep());
        this.beepManager.setVibrate(this.config.isShake());

        themeColor = config.getThemeColor();
        scanWidth = config.getScanWidth();
        scanHeight = config.getScanHeight();
        isShowbottomLayout = config.isShowbottomLayout();
        isShowFlashLight = config.isShowFlashLight();
        isShowAlbum = config.isShowAlbum();
    }

    private void initViewNew() {
        
        // 根布局
        rootLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParamsRoot = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(layoutParamsRoot);
        // 整个摄像区域
        previewView = new SurfaceView(this);
        RelativeLayout.LayoutParams layoutParamsPreview = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        previewView.setLayoutParams(layoutParamsPreview);
        // 整个控件层父布局
        viewsLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParamsViews = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        viewsLayout.setOrientation(LinearLayout.VERTICAL);
        viewsLayout.setLayoutParams(layoutParamsViews);

        // 顶部条
        topLayout = new RelativeLayout(this);
        LinearLayout.LayoutParams layoutParamsTop = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtil.dp2px(this, 48));
        topLayout.setGravity(Gravity.TOP);
        topLayout.setLayoutParams(layoutParamsTop);
        topLayout.setBackgroundColor(Color.parseColor("#99000000"));
        // 顶部条返回按钮
        backIv = new BackView(this);
        RelativeLayout.LayoutParams layoutParamsBack = new RelativeLayout.LayoutParams(ScreenUtil.getScreenWidth(this) / 6, RelativeLayout.LayoutParams.MATCH_PARENT);
        backIv.setLayoutParams(layoutParamsBack);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CaptureActivity.this.finish();
            }
        });
        // 顶部条标题
        titleText = new TextView(this);
        RelativeLayout.LayoutParams layoutParamsTitle = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,  RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsTitle.addRule(RelativeLayout.CENTER_IN_PARENT);
        titleText.setLayoutParams(layoutParamsTitle);
        titleText.setTextSize(20);
        titleText.setText("扫一扫");
        titleText.setTextColor(Color.parseColor("#ffffffff"));
        // 扫描框
        viewfinderView = new ViewfinderView(this, themeColor, scanWidth, scanHeight);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        viewfinderView.setLayoutParams(layoutParams);
        // 底部条
        bottomLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams1Bottom = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtil.dp2px(this, 50));
        bottomLayout.setLayoutParams(layoutParams1Bottom);
        bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
        bottomLayout.setBackgroundColor(Color.parseColor("#99000000"));
        layoutParams1Bottom.gravity = Gravity.BOTTOM;
        // 底部条打开闪光灯
        flashLightLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams1Light = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        flashLightLayout.setLayoutParams(layoutParams1Light);
        flashLightLayout.setOrientation(LinearLayout.VERTICAL);
        flashLightLayout.setGravity(Gravity.CENTER);
        flashLightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraManager.switchFlashLight(handler);
            }
        });
        // 闪光灯图片
//        flashLightIv = new ImageView(this);
//        LinearLayout.LayoutParams layoutParams1LightImg = new LinearLayout.LayoutParams(ScreenUtil.dp2px(this, 36), ScreenUtil.dp2px(this, 36));
//        flashLightIv.setLayoutParams(layoutParams1LightImg);
//        flashLightIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
        // 闪光灯文字
        flashLightTv = new TextView(this);
        LinearLayout.LayoutParams layoutParams1LightText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1LightText.setMargins(0, ScreenUtil.dp2px(this, 5), 0, 0);
        flashLightTv.setGravity(Gravity.CENTER);
        flashLightTv.setTextColor(getResources().getColor(R.color.result_text));
        flashLightTv.setText("打开闪光灯");
        flashLightTv.setTextSize(20);
        flashLightTv.setLayoutParams(layoutParams1LightText);
        // 底部条相册
        albumLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams1Album = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        albumLayout.setOrientation(LinearLayout.VERTICAL);
        albumLayout.setLayoutParams(layoutParams1Album);
        albumLayout.setGravity(Gravity.CENTER);
        albumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.PICK");
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });
        // 底部条相册图片
//        albumIv = new ImageView(this);
//        LinearLayout.LayoutParams layoutParams1AlbumImg = new LinearLayout.LayoutParams(ScreenUtil.dp2px(this, 36), ScreenUtil.dp2px(this, 36));
//        albumIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo));
//        albumIv.setLayoutParams(layoutParams1AlbumImg);
        // 底部条相册文字
        albumTv = new TextView(this);
        LinearLayout.LayoutParams layoutParams1AlbumText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1AlbumText.setMargins(0, ScreenUtil.dp2px(this, 5), 0, 0);
        albumTv.setGravity(Gravity.CENTER);
        albumTv.setTextColor(getResources().getColor(R.color.result_text));
        albumTv.setText("相册");
        albumTv.setTextSize(20);
        albumTv.setLayoutParams(layoutParams1AlbumText);
    }

    private void addView() {
        // 以下是控件层逐个添加

        // 添加顶部条
        viewsLayout.addView(topLayout);

        topLayout.addView(backIv);
        topLayout.addView(titleText);
        // 扫描框
        viewsLayout.addView(viewfinderView);
        // 底部条
        if(isShowbottomLayout) {
            viewsLayout.addView(bottomLayout);
        }
        // 闪光灯
        if(isShowFlashLight) {
            bottomLayout.addView(flashLightLayout);
            flashLightLayout.addView(flashLightTv);
        }
        // 相册
        if(isShowAlbum) {
            bottomLayout.addView(albumLayout);
            albumLayout.addView(albumTv);
        }

        // 添加图像层
        rootLayout.addView(previewView);
        // 添加控件层
        rootLayout.addView(viewsLayout);
    }


    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                FeatureInfo[] var2 = features;
                int var3 = features.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    FeatureInfo f = var2[var4];
                    if (f != null && "android.hardware.camera.flash".equals(f.name)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void switchFlashImg(int flashState) {
        if (flashState == 8) {
//            this.flashLightIv.setImageResource(R.drawable.ic_open);
            this.flashLightTv.setText("关闭闪光灯");
        } else {
//            this.flashLightIv.setImageResource(R.drawable.ic_close);
            this.flashLightTv.setText("打开闪光灯");
        }

    }

    public void handleDecode(Result rawResult) {
        this.inactivityTimer.onActivity();
        this.beepManager.playBeepSoundAndVibrate();
//        Intent intent = this.getIntent();
//        intent.putExtra("codedContent", rawResult.getText());
//        this.setResult(-1, intent);
        if(scanResultCallback != null) {
            scanResultCallback.scanFinishResult(rawResult.getText());
        }
        this.finish();
    }

    private void switchVisibility(View view, boolean b) {
        if (b) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }

    }

    protected void onResume() {
        super.onResume();
        this.cameraManager = new CameraManager(this.getApplication());
        this.viewfinderView.setCameraManager(this.cameraManager);
        this.handler = null;
        this.surfaceHolder = this.previewView.getHolder();
        if (this.hasSurface) {
            this.initCamera(this.surfaceHolder);
        } else {
            this.surfaceHolder.addCallback(this);
        }

        this.beepManager.updatePrefs();
        this.inactivityTimer.onResume();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        } else if (!this.cameraManager.isOpen()) {
            try {
                this.cameraManager.openDriver(surfaceHolder);
                if (this.handler == null) {
                    this.handler = new CaptureActivityHandler(this, this.cameraManager);
                }
            } catch (IOException var3) {
                Log.w(TAG, var3);
                this.displayFrameworkBugMessageAndExit();
            } catch (RuntimeException var4) {
                Log.w(TAG, "Unexpected error initializing camera", var4);
                this.displayFrameworkBugMessageAndExit();
            }

        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("扫一扫");
        builder.setMessage(this.getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    protected void onPause() {
        if (this.handler != null) {
            this.handler.quitSynchronously();
            this.handler = null;
        }

        this.inactivityTimer.onPause();
        this.beepManager.close();
        this.cameraManager.closeDriver();
        if (!this.hasSurface) {
            this.surfaceHolder.removeCallback(this);
        }

        super.onPause();
    }

    protected void onDestroy() {
        this.inactivityTimer.shutdown();
        super.onDestroy();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (!this.hasSurface) {
            this.hasSurface = true;
            this.initCamera(holder);
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.hasSurface = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

//    public void onClick(View view) {
//        int id = view.getId();
//        if (id == R.id.flashLightLayout) {
//            this.cameraManager.switchFlashLight(this.handler);
//        } else if (id == R.id.albumLayout) {
//            Intent intent = new Intent();
//            intent.setAction("android.intent.action.PICK");
//            intent.setType("image/*");
//            this.startActivityForResult(intent, 10);
//        } else if (id == R.id.backIv) {
//            this.finish();
//        }
//
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == -1) {
            String path = ImageUtil.getImageAbsolutePath(this, data.getData());
            (new DecodeImgThread(path, new DecodeImgCallback() {
                public void onImageDecodeSuccess(Result result) {
                    CaptureActivity.this.handleDecode(result);
                }

                public void onImageDecodeFailed() {
                    Toast.makeText(CaptureActivity.this, "抱歉，解析失败,换个图片试试.", Toast.LENGTH_SHORT).show();
                }
            })).run();
        }

    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    /**
     * 扫描回调
     */
    private static ScanResultCallback scanResultCallback;

    public static void setScanResultCallback(ScanResultCallback mScanResultCallback) {
        scanResultCallback = mScanResultCallback;
    }
}
