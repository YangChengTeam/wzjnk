package com.yc.wzjnk.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.yc.wzjnk.R;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.ui.MainActivity;
import com.yc.wzjnk.utils.PreferenceUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Created by zhangkai on 2017/10/17.
 */

public class SkillBoxInfoService extends AccessibilityService {


    private boolean isConnect = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String openService = PreferenceUtil.getImpl(this).getString(MainActivity.OPEN_SERVICE, "");
        if (openService.equals("")) {
            return;
        }

        if (event.getPackageName() == null) return;

        String packageName = event.getPackageName().toString();
        if (packageName.equals("com.android.systemui") || packageName.equals("com.yc.wzjnk")) {
            return;
        }

        MainActivity mainActivity = MainActivity.getMainActivity();
        if (mainActivity == null) {
            return;
        }

        if (packageName.equals(Config.GAME_PACKAGE_NAME)) {
            mainActivity.createFloatView();
        } else {
            mainActivity.removeAllView();
        }
    }

    /**
     * 判断当前服务是否正在运行
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isRunning() {
        if (service == null) {
            return false;
        }

        if (service.isConnect) {
            return true;
        }

        AccessibilityServiceInfo info = service.getServiceInfo();

        if (info == null) {
            return false;
        }

        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        assert accessibilityManager != null;
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if (i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        return isConnect;
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断" + getResources().getString(R.string.app_name) + "辅助功能服务", Toast.LENGTH_SHORT).show();
        isConnect = false;
    }

    private static SkillBoxInfoService service = null;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        service = this;
        isConnect = true;
        PreferenceUtil.getImpl(this).putString(MainActivity.OPEN_SERVICE, MainActivity.OPEN_SERVICE);
        Toast.makeText(this, getResources().getString(R.string.app_name) + "辅助功能服务连接上了", Toast.LENGTH_SHORT).show();
    }
}
