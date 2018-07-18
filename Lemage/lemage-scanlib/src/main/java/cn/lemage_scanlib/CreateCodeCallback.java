package cn.lemage_scanlib;

import android.graphics.Bitmap;

/**
 * 生成二维码或者条形码回调接口
 * @author zhaoguangyang
 */
public interface CreateCodeCallback {

    void createQRcodeFinishResult(Bitmap bitmap);
}
