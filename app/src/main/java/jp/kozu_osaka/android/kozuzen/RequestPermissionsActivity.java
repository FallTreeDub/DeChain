package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.kozu_osaka.android.kozuzen.util.BarrageGuardButton;
import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;

public final class RequestPermissionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_permissions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_requestPermission_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        BarrageGuardButton notificationSettingButton = findViewById(R.id.view_requestPermission_notification_setting);
        notificationSettingButton.setOnClickListener(new OnClickNotificationSettingButton());
        if(PermissionsStatus.isAllowedNotification()) {
            markButtonAsDone(notificationSettingButton);
        }
        BarrageGuardButton installPackagesSettingButton = findViewById(R.id.view_requestPermission_installPackages_setting);
        installPackagesSettingButton.setOnClickListener(new OnClickInstallPackagesSettingButton());
        if(PermissionsStatus.isAllowedInstallPackage()) {
            markButtonAsDone(installPackagesSettingButton);
        }
        BarrageGuardButton usageSettingButton = findViewById(R.id.view_requestPermission_usage_setting);
        usageSettingButton.setOnClickListener(new OnClickUsageSettingButton());
        if(PermissionsStatus.isAllowedAppUsageStats()) {
            markButtonAsDone(usageSettingButton);
        }
        BarrageGuardButton alarmSettingButton = findViewById(R.id.view_requestPermission_scheduleAlarm_setting);
        alarmSettingButton.setOnClickListener(new OnClickAlarmSettingButton());
        if(PermissionsStatus.isAllowedScheduleAlarm()) {
            markButtonAsDone(alarmSettingButton);
        }
        BarrageGuardButton dangerPermissionsSettingButton = findViewById(R.id.view_requestPermission_allDangerPermissions_setting);
        dangerPermissionsSettingButton.setOnClickListener(new OnClickDangerPermissionsSettingButton());
        if(PermissionsStatus.isAllowedAppUsageStats()) {
            markButtonAsDone(dangerPermissionsSettingButton);
        }
        BarrageGuardButton closeButton = findViewById(R.id.view_button_requestPermission_close);
        closeButton.setOnClickListener(new OnClickCloseButton());
        closeButton.setEnabled(!PermissionsStatus.isAnyNotPermitted());
    }

    private void markButtonAsDone(Button button) {
        button.setText(R.string.text_requestPermission_button_alreadySet);
        button.setEnabled(false);
    }

    private final class OnClickNotificationSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
    }

    private final class OnClickInstallPackagesSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private final class OnClickUsageSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    private final class OnClickAlarmSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private final class OnClickDangerPermissionsSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            //
        }
    }

    private final class OnClickCloseButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent loginIntent = new Intent(RequestPermissionsActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }
}