package cn.lemage_scanlib.camera;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.util.Log;

/**
 * @author zhaoguangyang
 */
public final class OpenCameraInterface {

    private static final String TAG = OpenCameraInterface.class.getName();

    private OpenCameraInterface() {
    }

    @SuppressLint({"NewApi"})
    public static Camera open(int cameraId) {
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            Log.w(TAG, "No cameras!");
            return null;
        } else {
            boolean explicitRequest = cameraId >= 0;
            if (!explicitRequest) {
                int index;
                for(index = 0; index < numCameras; ++index) {
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(index, cameraInfo);
                    if (cameraInfo.facing == 0) {
                        break;
                    }
                }

                cameraId = index;
            }

            Camera camera;
            if (cameraId < numCameras) {
                Log.i(TAG, "Opening camera #" + cameraId);
                camera = Camera.open(cameraId);
            } else if (explicitRequest) {
                Log.w(TAG, "Requested camera does not exist: " + cameraId);
                camera = null;
            } else {
                Log.i(TAG, "No camera facing back; returning camera #0");
                camera = Camera.open(0);
            }

            return camera;
        }
    }

    public static Camera open() {
        return open(-1);
    }
}
