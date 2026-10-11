package de.blinkt.openvpn.activities;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.*;
import java.io.InputStreamReader;
import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.*;

public class HiraActivity extends Activity implements VpnStatus.StateListener {
    private EditText username, password;
    private TextView status;
    private Button connect;
    private boolean active;
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView label(String s, int size) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.WHITE);
        t.setPadding(0,dp(12),0,dp(12)); return t;
    }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().setStatusBarColor(Color.rgb(10,18,32));
        getWindow().setNavigationBarColor(Color.rgb(10,18,32));
        LinearLayout root = new LinearLayout(this); root.setOrientation(1);
        root.setPadding(dp(28),dp(52),dp(28),dp(28)); root.setBackgroundColor(Color.rgb(10,18,32));
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(root); setContentView(scroll);
        root.addView(label("HIRA VPN",36)); root.addView(label("اتصال امن به شبکه هیرا",17));
        TextView server = label("Hira Server  •  OpenVPN\n2.hiragroup.tech : 1194 / TCP",16);
        server.setPadding(dp(18),dp(18),dp(18),dp(18)); server.setBackground(bg(Color.rgb(25,38,57),18)); root.addView(server);
        root.addView(label("نام کاربری",15)); username = new EditText(this);
        username.setSingleLine(true); username.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        username.setTextColor(Color.WHITE); username.setHintTextColor(Color.GRAY); username.setHint("Username"); root.addView(username);
        root.addView(label("رمز عبور",15)); password = new EditText(this); password.setSingleLine(true);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setTextColor(Color.WHITE); password.setHintTextColor(Color.GRAY); password.setHint("Password"); root.addView(password);
        status = label("آماده اتصال",16); root.addView(status);
        connect = new Button(this); connect.setText("اتصال"); connect.setTextColor(Color.WHITE);
        connect.setBackground(bg(Color.rgb(0,153,130),16)); root.addView(connect);
        connect.setOnClickListener(v -> { if(active) startActivity(new Intent(this,DisconnectVPN.class)); else connectVPN(); });
        Button logs = new Button(this); logs.setText("گزارش اتصال"); root.addView(logs);
        logs.setOnClickListener(v -> startActivity(new Intent(this,LogWindow.class)));
        root.addView(label("Powered by OpenVPN for Android\nGPLv2 — Arne Schwabe and contributors",12));
        if(android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},7);
    }
    private void connectVPN() {
        if(username.getText().toString().trim().isEmpty() || password.getText().length()==0) {
            status.setText("نام کاربری و رمز عبور را وارد کن"); return;
        }
        try(InputStreamReader r = new InputStreamReader(getAssets().open("hira.ovpn"),java.nio.charset.StandardCharsets.UTF_8)) {
            ConfigParser parser = new ConfigParser(); parser.parseConfig(r);
            VpnProfile p = parser.convertProfile(); p.mName="Hira VPN";
            p.mUsername=username.getText().toString().trim(); p.mPassword=password.getText().toString();
            p.mUserEditable=false;
            ProfileManager.setTemporaryProfile(this,p);
            Intent i = new Intent(this,LaunchVPN.class); i.putExtra(LaunchVPN.EXTRA_KEY,p.getUUIDString());
            i.putExtra(LaunchVPN.EXTRA_HIDELOG,true); startActivity(i);
            password.setText("");
        } catch(Exception e) { status.setText("خطا در بارگذاری تنظیمات سرور"); }
    }
    @Override protected void onResume() { super.onResume(); VpnStatus.addStateListener(this); }
    @Override protected void onPause() { VpnStatus.removeStateListener(this); super.onPause(); }
    @Override public void setConnectedVPN(String uuid) {}
    @Override public void updateState(String state,String message,int resource,ConnectionStatus level,Intent intent) {
        runOnUiThread(() -> {
            active = level == ConnectionStatus.LEVEL_CONNECTED || level == ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET || level == ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED;
            String text = "وضعیت: " + state;
            if(level == ConnectionStatus.LEVEL_CONNECTED) text="متصل به هیرا";
            else if(level == ConnectionStatus.LEVEL_AUTH_FAILED) text="نام کاربری یا رمز عبور نادرست است";
            else if(level == ConnectionStatus.LEVEL_NOTCONNECTED) text="قطع اتصال";
            status.setText(text); connect.setText(active ? "قطع اتصال" : "اتصال");
        });
    }
}
