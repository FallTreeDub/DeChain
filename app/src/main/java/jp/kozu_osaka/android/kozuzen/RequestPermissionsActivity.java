package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.kozu_osaka.android.kozuzen.security.Secrets;
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

        TextView link = findViewById(R.id.view_requestPermission_usage_policyLink);
        link.setText(Secrets.PRIVACY_POLICY_URL);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Button notificationSettingButton = findViewById(R.id.view_requestPermission_notification_setting);
        notificationSettingButton.setOnClickListener(new OnClickNotificationSettingButton());
        if(PermissionsStatus.isAllowedNotification()) {
            markButtonAsDone(notificationSettingButton);
        }
        Button installPackagesSettingButton = findViewById(R.id.view_requestPermission_installPackages_setting);
        installPackagesSettingButton.setOnClickListener(new OnClickInstallPackagesSettingButton());
        if(PermissionsStatus.isAllowedInstallPackage()) {
            markButtonAsDone(installPackagesSettingButton);
        }
        Button usageSettingButton = findViewById(R.id.view_requestPermission_usage_setting);
        usageSettingButton.setOnClickListener(new OnClickUsageSettingButton());
        if(PermissionsStatus.isAllowedAppUsageStats()) {
            markButtonAsDone(usageSettingButton);
        }
        Button alarmSettingButton = findViewById(R.id.view_requestPermission_scheduleAlarm_setting);
        alarmSettingButton.setOnClickListener(new OnClickAlarmSettingButton());
        if(PermissionsStatus.isAllowedScheduleAlarm()) {
            markButtonAsDone(alarmSettingButton);
        }
        Button dangerPermissionsSettingButton = findViewById(R.id.view_requestPermission_allDangerPermissions_setting);
        dangerPermissionsSettingButton.setOnClickListener(new OnClickDangerPermissionsSettingButton());
        if(PermissionsStatus.isAllowedAppUsageStats()) {
            markButtonAsDone(dangerPermissionsSettingButton);
        }
        Button closeButton = findViewById(R.id.view_button_requestPermission_close);
        closeButton.setOnClickListener(new OnClickCloseButton());
        closeButton.setEnabled(!PermissionsStatus.isAnyNotPermitted());
    }

    private void markButtonAsDone(Button button) {
        button.setText(R.string.text_requestPermission_button_alreadySet);
        button.setEnabled(false);
    }

    private final class OnClickNotificationSettingButton implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
    }

    private final class OnClickInstallPackagesSettingButton implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private final class OnClickUsageSettingButton implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    private final class OnClickAlarmSettingButton implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private final class OnClickDangerPermissionsSettingButton implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        }
    }

    private final class OnClickCloseButton implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent loginIntent = new Intent(RequestPermissionsActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }
}