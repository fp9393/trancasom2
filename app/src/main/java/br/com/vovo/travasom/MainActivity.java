package br.com.vovo.travasom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int ID_BASE = 1000; // id do radio = ID_BASE + modo do toque

    private Button dndButton;
    private RadioGroup modeGroup;
    private SeekBar ringBar, callBar, alarmBar;
    private Switch activeSwitch;
    private EditText pinField;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }
        String pin = Config.pin(this);
        if (pin.isEmpty()) {
            buildUi();
        } else {
            askPin(pin);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        updateDndButton();
    }

    private void askPin(final String pin) {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle("Digite o PIN")
                .setView(et)
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> {
                    if (et.getText().toString().equals(pin)) buildUi(); else finish();
                })
                .setNegativeButton("Sair", (d, w) -> finish())
                .show();
    }

    private void buildUi() {
        int pad = dp(16);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        root.addView(label("Trava Som", 26, true));

        dndButton = new Button(this);
        dndButton.setText("1) Permitir acesso a Não Perturbe (necessário)");
        dndButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)));
        root.addView(dndButton);

        root.addView(label("Modo do telefone", 18, true));
        modeGroup = new RadioGroup(this);
        addMode("Normal (toca)", AudioManager.RINGER_MODE_NORMAL);
        addMode("Vibrar", AudioManager.RINGER_MODE_VIBRATE);
        addMode("Silencioso", AudioManager.RINGER_MODE_SILENT);
        modeGroup.check(ID_BASE + Config.ringerMode(this));
        root.addView(modeGroup);

        root.addView(label("Volumes", 18, true));
        ringBar = addSeek(root, "Toque", Config.pct(this, "ring", 80));
        callBar = addSeek(root, "Ligação", Config.pct(this, "call", 80));
        alarmBar = addSeek(root, "Alarme", Config.pct(this, "alarm", 100));

        root.addView(label("PIN para abrir este app (opcional, só números)", 16, false));
        pinField = new EditText(this);
        pinField.setInputType(InputType.TYPE_CLASS_NUMBER);
        pinField.setText(Config.pin(this));
        root.addView(pinField);

        activeSwitch = new Switch(this);
        activeSwitch.setText("Proteção ativa");
        activeSwitch.setTextSize(18);
        activeSwitch.setChecked(Config.active(this));
        root.addView(activeSwitch);

        Button save = new Button(this);
        save.setText("Salvar e aplicar");
        save.setOnClickListener(v -> save());
        root.addView(save);

        setContentView(scroll);
        updateDndButton();
    }

    private void addMode(String text, int mode) {
        RadioButton rb = new RadioButton(this);
        rb.setText(text);
        rb.setTextSize(18);
        rb.setId(ID_BASE + mode);
        modeGroup.addView(rb);
    }

    private SeekBar addSeek(LinearLayout root, final String name, int pct) {
        final TextView tv = label(name + ": " + pct + "%", 16, false);
        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress(pct);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                tv.setText(name + ": " + p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        root.addView(tv);
        root.addView(sb);
        return sb;
    }

    private void save() {
        int mode = modeGroup.getCheckedRadioButtonId() - ID_BASE;
        boolean on = activeSwitch.isChecked();
        Config.save(this, on, mode,
                ringBar.getProgress(), callBar.getProgress(), alarmBar.getProgress(),
                pinField.getText().toString().trim());
        if (on) GuardService.start(this); else GuardService.stop(this);
        Enforcer.apply(this);
        Toast.makeText(this, on ? "Salvo. Proteção ativa." : "Salvo. Proteção desligada.",
                Toast.LENGTH_SHORT).show();
    }

    private void updateDndButton() {
        if (dndButton == null) return;
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        dndButton.setVisibility(nm.isNotificationPolicyAccessGranted() ? View.GONE : View.VISIBLE);
    }

    private TextView label(String text, int sp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setPadding(0, dp(12), 0, dp(4));
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
