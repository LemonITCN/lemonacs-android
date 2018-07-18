package cn.lemonit.lemage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.lemage_scanlib.CreateCodeCallback;
import cn.lemage_scanlib.Lemage;
import cn.lemage_scanlib.ScanResultCallback;
import cn.lemage_scanlib.ScreenUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scanButton, createTwoCode, createStripeCode, scanLocalPhoto;
    private TextView textview;
    private ImageView imageview;
    /**
     * 生成的二维码或者条形码图片，可以长按后识别回字符串（测试用）
     */
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(this);
        createTwoCode = findViewById(R.id.createTwoCode);
        createTwoCode.setOnClickListener(this);
        createStripeCode = findViewById(R.id.createStripeCode);
        createStripeCode.setOnClickListener(this);
        scanLocalPhoto = findViewById(R.id.scanLocalPhoto);
        scanLocalPhoto.setOnClickListener(this);


        textview = findViewById(R.id.textview);

        imageview = findViewById(R.id.imageview);
        imageview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(mBitmap != null) {
                    Lemage.scanLocalPhoto(MainActivity.this, mBitmap,  new ScanResultCallback() {
                        @Override
                        public void scanFinishResult(String resultStr) {
                            textview.setText("");
                            textview.setText(resultStr);
                        }
                    });
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 扫描
            case R.id.scanButton:
                Lemage.startScan(this, true, true, true, false, true,  Color.BLUE, ScreenUtil.getScreenWidth(this) / 5 * 3, ScreenUtil.getScreenWidth(this) / 5 * 3, new ScanResultCallback() {
                    @Override
                    public void scanFinishResult(String resultStr) {
                        textview.setText("");
                        textview.setText(resultStr);
                    }
                });
                break;
            // 生成二维码
            case R.id.createTwoCode:
                Lemage.createQRCode(this, "神气小风", 300, 400, new CreateCodeCallback() {
                    @Override
                    public void createQRcodeFinishResult(Bitmap bitmap) {
                        if(bitmap != null) {
                            imageview.setImageBitmap(bitmap);
                            mBitmap = bitmap;
                        }
                    }
                });
                break;
            // 生成条形码
            case R.id.createStripeCode:
                String str = "aaaaff333421a_@`'";
                Lemage.createStripeCode(this, str, 400, 400, true, new CreateCodeCallback() {
                    @Override
                    public void createQRcodeFinishResult(Bitmap bitmap) {
                        if(bitmap != null) {
                            imageview.setImageBitmap(bitmap);
                            mBitmap = bitmap;

                        }
                    }
                });
                break;
        }
    }
}
