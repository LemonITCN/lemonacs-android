package cn.lemage_scanlib.android;

import android.app.Activity;
import android.content.DialogInterface;

/**
 * @author zhaoguangyang
 */
public final class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

    private final Activity activityToFinish;

    public FinishListener(Activity activityToFinish) {
        this.activityToFinish = activityToFinish;
    }

    public void onCancel(DialogInterface dialogInterface) {
        this.run();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.run();
    }

    private void run() {
        this.activityToFinish.finish();
    }
}
