package br.com.vovo.travasom;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

/**
 * Serviço em primeiro plano que vigia mudanças de modo/volume
 * e desfaz na hora qualquer alteração feita fora do app.
 */
public class GuardService extends Service {
    private static final String CHANNEL = "travasom";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable apply = () -> Enforcer.apply(getApplicationContext());

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) { schedule(); }
    };

    private final ContentObserver observer = new ContentObserver(handler) {
        @Override public void onChange(boolean selfChange) { schedule(); }
    };

    static void start(Context c) {
        c.startForegroundService(new Intent(c, GuardService.class));
    }

    static void stop(Context c) {
        c.stopService(new Intent(c, GuardService.class));
    }

    private void schedule() {
        handler.removeCallbacks(apply);
        handler.postDelayed(apply, 150);
    }

    @Override public void onCreate() {
        super.onCreate();

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(
                new NotificationChannel(CHANNEL, "Trava Som", NotificationManager.IMPORTANCE_LOW));
        Notification n = new Notification.Builder(this, CHANNEL)
                .setContentTitle("Trava Som ativo")
                .setContentText("Mantendo o som do telefone fixo")
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, n);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        filter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
        getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, observer);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Config.active(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        schedule();
        return START_STICKY;
    }

    @Override public void onDestroy() {
        handler.removeCallbacks(apply);
        try { unregisterReceiver(receiver); } catch (IllegalArgumentException ignored) {}
        getContentResolver().unregisterContentObserver(observer);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
