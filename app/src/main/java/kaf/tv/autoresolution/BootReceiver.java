package kaf.tv.autoresolution;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Display;

import androidx.annotation.RequiresApi;

import com.socks.library.KLog;

public class BootReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null)
            return;
        KLog.e(intent.getAction());

        if (intent.getAction().equals(OverlayService.RES_CHANGED_BROADCAST))
            return;

        if (OverlayService.getAutoSetPref(context) && OverlayService.canWriteOverlay(context)) {
            Display.Mode selectedMode = OverlayService.getHighestResMode(context);
            OverlayService.addOverlayButton(context, selectedMode);
        }
    }
}
