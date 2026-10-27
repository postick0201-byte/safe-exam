package kr.co.safetyexam.app;

import android.app.Activity;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.function.Consumer;

/**
 * Blocks screenshots / screen recording while an exam is active and reports attempts to JS.
 *
 * - FLAG_SECURE: screenshots fail, recordings and casts show black, recents thumbnail is hidden.
 * - Android 14+ (API 34): ScreenCaptureCallback reports a screenshot that was taken anyway.
 * - Android 15+ (API 35): screen recording callback reports when recording starts or is active.
 */
@CapacitorPlugin(name = "ExamGuard")
public class ExamGuardPlugin extends Plugin {
    private Activity.ScreenCaptureCallback captureCallback;
    private Consumer<Integer> recordingCallback;
    private boolean enabled = false;

    @PluginMethod
    public void enable(PluginCall call) {
        Activity activity = getActivity();
        activity.runOnUiThread(() -> {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            registerCallbacks(activity);
            enabled = true;
            JSObject ret = new JSObject();
            ret.put("secure", true);
            ret.put("screenshotDetection", Build.VERSION.SDK_INT >= 34);
            ret.put("recordingDetection", Build.VERSION.SDK_INT >= 35);
            call.resolve(ret);
        });
    }

    @PluginMethod
    public void disable(PluginCall call) {
        Activity activity = getActivity();
        activity.runOnUiThread(() -> {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            unregisterCallbacks(activity);
            enabled = false;
            call.resolve();
        });
    }

    private void registerCallbacks(Activity activity) {
        if (enabled) return;
        if (Build.VERSION.SDK_INT >= 34) {
            captureCallback = () -> notifyListeners("screenshot", new JSObject());
            activity.registerScreenCaptureCallback(activity.getMainExecutor(), captureCallback);
        }
        if (Build.VERSION.SDK_INT >= 35) {
            recordingCallback = state -> {
                if (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE) {
                    notifyListeners("recording", new JSObject());
                }
            };
            WindowManager wm = activity.getSystemService(WindowManager.class);
            int initial = wm.addScreenRecordingCallback(activity.getMainExecutor(), recordingCallback);
            if (initial == WindowManager.SCREEN_RECORDING_STATE_VISIBLE) {
                notifyListeners("recording", new JSObject());
            }
        }
    }

    private void unregisterCallbacks(Activity activity) {
        if (!enabled) return;
        if (Build.VERSION.SDK_INT >= 34 && captureCallback != null) {
            activity.unregisterScreenCaptureCallback(captureCallback);
            captureCallback = null;
        }
        if (Build.VERSION.SDK_INT >= 35 && recordingCallback != null) {
            activity.getSystemService(WindowManager.class).removeScreenRecordingCallback(recordingCallback);
            recordingCallback = null;
        }
    }

    @Override
    protected void handleOnDestroy() {
        Activity activity = getActivity();
        if (activity != null) unregisterCallbacks(activity);
    }
}
