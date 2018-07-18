package cn.lemage_scanlib.view;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

/**
 * @author zhaoguangyang
 */
public final class ViewfinderResultPointCallback implements ResultPointCallback {

    private final ViewfinderView viewfinderView;

    public ViewfinderResultPointCallback(ViewfinderView viewfinderView) {
        this.viewfinderView = viewfinderView;
    }

    public void foundPossibleResultPoint(ResultPoint point) {
        this.viewfinderView.addPossibleResultPoint(point);
    }
}
