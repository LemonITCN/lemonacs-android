package cn.lemage_scanlib.decode;

import com.google.zxing.Result;

/**
 * @author zhaoguangyang
 */
public interface DecodeImgCallback {
    void onImageDecodeSuccess(Result var1);

    void onImageDecodeFailed();
}
