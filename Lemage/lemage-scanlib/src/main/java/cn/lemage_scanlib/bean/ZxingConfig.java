package cn.lemage_scanlib.bean;

import java.io.Serializable;

/**
 * @author zhaoguangyang
 */
public class ZxingConfig implements Serializable {

    /**
     * 是否需要声音
     */
    private boolean isPlayBeep = true;
    /**
     * 是否需要震动
     */
    private boolean isShake = false;
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

    /**
     * 主题颜色
     */
    private int themeColor;
    /**
     * 扫描框的宽度和高度
     */
    private int scanWidth, scanHeight;

    public void setPlayBeep(boolean playBeep) {
        isPlayBeep = playBeep;
    }

    public void setShake(boolean shake) {
        isShake = shake;
    }

    public void setShowbottomLayout(boolean showbottomLayout) {
        isShowbottomLayout = showbottomLayout;
    }

    public void setShowFlashLight(boolean showFlashLight) {
        isShowFlashLight = showFlashLight;
    }

    public void setShowAlbum(boolean showAlbum) {
        isShowAlbum = showAlbum;
    }

    public void setThemeColor(int themeColor) {
        this.themeColor = themeColor;
    }

    public void setScanWidth(int scanWidth) {
        this.scanWidth = scanWidth;
    }

    public void setScanHeight(int scanHeight) {
        this.scanHeight = scanHeight;
    }

    public boolean isPlayBeep() {
        return isPlayBeep;
    }

    public boolean isShake() {
        return isShake;
    }

    public boolean isShowbottomLayout() {
        return isShowbottomLayout;
    }

    public boolean isShowFlashLight() {
        return isShowFlashLight;
    }

    public boolean isShowAlbum() {
        return isShowAlbum;
    }

    public int getThemeColor() {
        return themeColor;
    }

    public int getScanWidth() {
        return scanWidth;
    }

    public int getScanHeight() {
        return scanHeight;
    }

    //    public ZxingConfig() {
//    }
//
//    public boolean isPlayBeep() {
//        return this.isPlayBeep;
//    }
//
//    public void setPlayBeep(boolean playBeep) {
//        this.isPlayBeep = playBeep;
//    }
//
//    public boolean isShake() {
//        return this.isShake;
//    }
//
//    public void setShake(boolean shake) {
//        this.isShake = shake;
//    }
//
//    public boolean isShowbottomLayout() {
//        return this.isShowbottomLayout;
//    }
//
//    public void setShowbottomLayout(boolean showbottomLayout) {
//        this.isShowbottomLayout = showbottomLayout;
//    }
//
//    public boolean isShowFlashLight() {
//        return this.isShowFlashLight;
//    }
//
//    public void setShowFlashLight(boolean showFlashLight) {
//        this.isShowFlashLight = showFlashLight;
//    }
//
//    public boolean isShowAlbum() {
//        return this.isShowAlbum;
//    }
//
//    public void setShowAlbum(boolean showAlbum) {
//        this.isShowAlbum = showAlbum;
//    }
}
