package kaf.tv.autoresolution;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.socks.library.KLog;

import java.util.Random;

public class OverlayService extends Service {

    public static final String RES_CHANGED_BROADCAST = "RES_CHANGED_BROADCAST";
    private static final String ACTION_START_OVERLAY = "ACTION_START_OVERLAY";
    private static TextView textView;
    private BootReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        broadcastReceiver = new BootReceiver();
        registerReceiver(broadcastReceiver, screenStateFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_START_OVERLAY:
                    Display.Mode selectedMode = getHighestResMode(this);

                    if (getShowDebug(this))
                        Toast.makeText(this, selectedMode.toString(), Toast.LENGTH_LONG).show();
                    canWriteOverlay(this);
                    addOverlayButton(this, selectedMode);
                    break;

            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public OverlayService() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Display.Mode getHighestResMode(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Display.Mode[] modes = new Display.Mode[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            modes = display.getSupportedModes();
        }
        if (modes == null) {
            return null;
        }

        for (Display.Mode mode : modes) {
            KLog.d(mode);
        }

        Display.Mode max = modes[0];
        for (int i = 1; i < modes.length; i++) {
            Display.Mode mode = modes[i];
            if (mode.getPhysicalHeight() > max.getPhysicalHeight()) {
                max = mode;
                continue;
            }
            if (mode.getPhysicalHeight() == max.getPhysicalHeight())
                if (mode.getRefreshRate() > max.getRefreshRate())
                    max = mode;
        }

        return max;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean canWriteOverlay(Context context) {
        if (!Settings.canDrawOverlays(context)) {
            KLog.e("Apps > Special app access > Display over other apps");
            Toast.makeText(context, "Apps > Special app access > Display over other apps > activate app", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static void addOverlayButton(Context context, Display.Mode selectedMode) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        Display.Mode mode = display.getMode();
        if (mode.equals(selectedMode) || display.getRefreshRate() == 24)
            return;

        if (textView == null)
            textView = new TextView(context);

        textView.setText(selectedMode.toString() + new Random().nextInt(100));
        KLog.i(getShowDebug(context));
        if (!getShowDebug(context))
            textView.setAlpha(0.0f);
        textView.setBackgroundColor(0x55fe4444);


        int LAYOUT_FLAG = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.preferredDisplayModeId = selectedMode.getModeId();

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        if (textView.getParent() != null)
            windowManager.removeView(textView);
        windowManager.addView(textView, params);

        context.sendBroadcast(new Intent().setAction(RES_CHANGED_BROADCAST));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static boolean getAutoSetPref(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("autoon", true);
    }

    public static void setAutoSetPref(Context context, boolean autoOn) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean("autoon", autoOn).apply();
    }

    public static boolean getShowDebug(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("ShowDebug", false);
    }

    public static void setShowDebug(Context context, boolean autoOn) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean("ShowDebug", autoOn).apply();
    }

    public static void actionAddOverlay(Context context) {
        Intent intent = new Intent(context, OverlayService.class);
        intent.setAction(ACTION_START_OVERLAY);
        context.startService(intent);
    }
}
