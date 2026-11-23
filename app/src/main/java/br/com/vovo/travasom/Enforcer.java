package br.com.vovo.travasom;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

/** Reaplica o modo de toque e os volumes escolhidos, só mexendo no que estiver diferente. */
final class Enforcer {
    private Enforcer() {}

    static void apply(Context c) {
        try {
            AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

            // Não Perturbe também silencia o telefone: desliga se tivermos permissão.
            if (nm.isNotificationPolicyAccessGranted()
                    && nm.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }

            int wantedMode = Config.ringerMode(c);
            if (am.getRingerMode() != wantedMode) {
                am.setRingerMode(wantedMode);
            }

            if (wantedMode == AudioManager.RINGER_MODE_NORMAL) {
                setVolume(am, AudioManager.STREAM_RING, Config.pct(c, "ring", 80));
            }
            setVolume(am, AudioManager.STREAM_VOICE_CALL, Config.pct(c, "call", 80));
            setVolume(am, AudioManager.STREAM_ALARM, Config.pct(c, "alarm", 100));
        } catch (SecurityException e) {
            // Sem permissão de Não Perturbe: o app avisa na tela principal.
        } catch (RuntimeException e) {
            // Alguns fabricantes lançam erros estranhos; tenta de novo na próxima mudança.
        }
    }

    private static void setVolume(AudioManager am, int stream, int pct) {
        int max = am.getStreamMaxVolume(stream);
        int min = Build.VERSION.SDK_INT >= 28 ? am.getStreamMinVolume(stream) : 0;
        int target = Math.max(min, Math.round(max * pct / 100f));
        if (am.getStreamVolume(stream) != target) {
            am.setStreamVolume(stream, target, 0);
        }
    }
}
