package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import jp.kozu_osaka.android.kozuzen.util.BarrageGuardButton;
import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;

public final class RequestPermissionFragment extends Fragment {

    public static final String REQUEST_PERMISSION_FRAGMENT_TAG = "DeChain_RequestPermissionFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        BarrageGuardButton notificationSettingButton = getActivity().findViewById(R.id.view_requestPermission_notification_setting);
        notificationSettingButton.setOnClickListener(new OnClickNotificationSettingButton());
        if(PermissionsStatus.isAllowedNotification()) {
            markButtonAsDone(notificationSettingButton);
        }
        BarrageGuardButton installPackagesSettingButton = getActivity().findViewById(R.id.view_requestPermission_installPackages_setting);
        installPackagesSettingButton.setOnClickListener(new OnClickInstallPackagesSettingButton());
        if(PermissionsStatus.isAllowedInstallPackage()) {
            markButtonAsDone(installPackagesSettingButton);
        }
        BarrageGuardButton usageSettingButton = getActivity().findViewById(R.id.view_requestPermission_usage_setting);
        usageSettingButton.setOnClickListener(new OnClickUsageSettingButton());
        if(PermissionsStatus.isAllowedAppUsageStats()) {
            markButtonAsDone(usageSettingButton);
        }
        BarrageGuardButton alarmSettingButton = getActivity().findViewById(R.id.view_requestPermission_scheduleAlarm_setting);
        alarmSettingButton.setOnClickListener(new OnClickAlarmSettingButton());
        if(PermissionsStatus.isAllowedScheduleAlarm()) {
            markButtonAsDone(alarmSettingButton);
        }
        BarrageGuardButton dangerPermissionsSettingButton = getActivity().findViewById(R.id.view_requestPermission_allDangerPermissions_setting);
        dangerPermissionsSettingButton.setOnClickListener(new OnClickDangerPermissionsSettingButton());
        if(PermissionsStatus.isAllowedAppUsageStats()) {
            markButtonAsDone(dangerPermissionsSettingButton);
        }
        BarrageGuardButton closeButton = getActivity().findViewById(R.id.view_button_requestPermission_close);
        closeButton.setOnClickListener(new OnClickCloseButton());
        closeButton.setEnabled(!PermissionsStatus.isAnyNotPermitted());
    }

    private void markButtonAsDone(Button button) {
        button.setText(R.string.text_requestPermission_button_alreadySet);
        button.setEnabled(false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_permission, container, false);
    }

    private final class OnClickNotificationSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
            getActivity().startActivity(intent);
        }
    }

    private final class OnClickInstallPackagesSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
            getActivity().startActivity(intent);
        }
    }

    private final class OnClickUsageSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            getActivity().startActivity(intent);
        }
    }

    private final class OnClickAlarmSettingButton implements BarrageGuardButton.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
            getActivity().startActivity(intent);
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
            getActivity().getSupportFragmentManager().beginTransaction().remove(RequestPermissionFragment.this).commit();
        }
    }


}