package br.com.vovo.travasom;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

/** Guarda as escolhas do usuário (modo do telefone, volumes, PIN, proteção ligada). */
final class Config {
    private Config() {}

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("travasom", Context.MODE_PRIVATE);
    }

    static boolean active(Context c) { return sp(c).getBoolean("active", false); }

    static int ringerMode(Context c) {
        return sp(c).getInt("ringer", AudioManager.RINGER_MODE_NORMAL);
    }

    static int pct(Context c, String key, int def) { return sp(c).getInt(key, def); }

    static String pin(Context c) { return sp(c).getString("pin", ""); }

    static void save(Context c, boolean active, int ringer, int ring, int call, int alarm, String pin) {
        sp(c).edit()
                .putBoolean("active", active)
                .putInt("ringer", ringer)
                .putInt("ring", ring)
                .putInt("call", call)
                .putInt("alarm", alarm)
                .putString("pin", pin)
                .apply();
    }
}
