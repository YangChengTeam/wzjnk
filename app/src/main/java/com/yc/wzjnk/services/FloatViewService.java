package com.yc.wzjnk.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yc.wzjnk.ui.MainActivity;
import com.yc.wzjnk.ui.SkillBoxView;
import com.yc.wzjnk.utils.PreferenceUtil;
import com.yc.wzjnk.utils.TaskUtil;
import com.yc.wzjnk.utils.UIUtil;

/**
 * Created by zhangkai on 2017/10/23.
 */

public class FloatViewService extends Service {
    private SkillBoxView skillBoxView;

    public FloatViewService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new FloatViewServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        destroy = false;
        skillBoxView = SkillBoxView.getInstance(getApplicationContext());
        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                checkCircle();
            }
        });

    }

    public void checkCircle() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isDestroy()) return;

                MainActivity mainActivity = MainActivity.getMainActivity();

                if (mainActivity == null) return;

                String isOpen = PreferenceUtil.getImpl(getApplicationContext()).getString(MainActivity.OPEN_SERVICE, "");
                if (!SkillBoxInfoService.isRunning() && !isOpen.equals("")) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mainActivity.createFloatView();
                    } else {
                        mainActivity.removeAllView();
                    }
                }
                UIUtil.postDelayed(1000, this);
            }
        };
        UIUtil.postDelayed(1000, runnable);
    }

    public boolean isOpen() {
        return skillBoxView != null && skillBoxView.isOpen();
    }

    public void removeAllView() {
        if (skillBoxView != null) {
            skillBoxView.removeAllView();
        }
    }

    public void createFloatView() {
        if (skillBoxView != null) {
            skillBoxView.createFloatView();
        }
    }

    public void removeSkillBoxView() {
        if (skillBoxView != null) {
            skillBoxView.removeSkillBoxViewWithState();
        }
    }

    public void updateSkillBoxView() {
        if (skillBoxView != null) {
            skillBoxView.updateSkillBoxView();
        }
    }

    public class FloatViewServiceBinder extends Binder {
        public FloatViewService getService() {
            return FloatViewService.this;
        }
    }

    private boolean destroy;

    public boolean isDestroy() {
        return destroy;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy = true;
        MainActivity mainActivity = MainActivity.getMainActivity();
        if (mainActivity != null) {
            mainActivity.startService();
        }
    }
}
