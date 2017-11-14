package com.yc.wzmhk.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yc.wzmhk.R;
import com.yc.wzmhk.ui.MainActivity;
import com.yc.wzmhk.ui.SkillBoxView;
import com.yc.wzmhk.utils.PreferenceUtil;
import com.yc.wzmhk.utils.TaskUtil;
import com.yc.wzmhk.utils.UIUtil;

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

        whiteLife();
        grayLife();
    }

    private void whiteLife() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this.getApplicationContext())
                .setContentText("点击可打开" + getString(R.string.app_name))
                .setContentTitle(getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icon_round)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(this.hashCode(), notification);
    }

    private final static int GRAY_SERVICE_ID = 1001;

    private void grayLife() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }
    }


    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class GrayInnerService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


    private static Runnable runnable;
    private boolean isProtail = false;

    public void checkCircle() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isDestroy()) {
                    if (skillBoxView != null) {
                        skillBoxView.showOff();
                    }
                    return;
                }

                String isOpen = PreferenceUtil.getImpl(getApplicationContext()).getString(MainActivity.OPEN_SERVICE, "");
                if (!SkillBoxInfoService.isRunning() && !isOpen.equals("")) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (isOpen()) {
                            addSkillBoxView();
                        }
                        if (isProtail) {
                            if (skillBoxView != null) {
                                skillBoxView.hide();
                            }
                        }
                        isProtail = false;
                    } else {
                        if (!isProtail && isOpen()) {
                            if (skillBoxView != null) {
                                skillBoxView.showOn();
                            }
                        } else {
                            if (skillBoxView != null) {
                                skillBoxView.showOff();
                            }
                        }
                        isProtail = true;
                        removeSkillBoxView();
                    }
                }
                UIUtil.postDelayed(2000, this);
            }
        };
        UIUtil.postDelayed(2000, runnable);
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

    public void removeSkillBoxViewWithState() {
        if (skillBoxView != null) {
            skillBoxView.removeSkillBoxViewWithState();
        }
    }

    public void removeSkillBoxView() {
        if (skillBoxView != null) {
            skillBoxView.removeSkillBoxView();
        }
    }

    public void addSkillBoxView() {
        if (skillBoxView != null) {
            skillBoxView.addSkillBoxView();
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
        stopForeground(true);
        destroy = true;
        MainActivity mainActivity = MainActivity.getMainActivity();
        if (mainActivity != null) {
            mainActivity.startService();
        }
    }
}
