package com.yc.wzjnk.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yc.wzjnk.ui.SkillBoxView;

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
        skillBoxView = SkillBoxView.getInstance(getBaseContext());
    }

    public boolean isOpen() {
        if (skillBoxView != null) {
            return skillBoxView.isOpen();
        }
        return false;
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

}
