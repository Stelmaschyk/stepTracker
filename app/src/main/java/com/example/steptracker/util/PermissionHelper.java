package com.example.steptracker.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
    public static boolean hasActivityRecognitionPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACTIVITY_RECOGNITION)
            == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestActivityRecognitionPermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
            new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
            requestCode);
    }
}
