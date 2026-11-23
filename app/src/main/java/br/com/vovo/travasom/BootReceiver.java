package br.com.vovo.travasom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Religa a proteção quando o telefone reinicia ou o app é atualizado. */
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Config.active(context)) {
            GuardService.start(context);
        }
    }
}
