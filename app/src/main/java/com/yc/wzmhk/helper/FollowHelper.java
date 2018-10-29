package com.yc.wzmhk.helper;

/**
 * Created by zhangkai on 2018/1/2.
 */

public class FollowHelper {
    private long startTime;
    private long defaultDiff = 20 * 1000;

    public FollowHelper(long diff) {
        this.defaultDiff = diff;
    }

    public FollowHelper() {
    }

    public void start() {
        followIng = true;
        startTime = System.currentTimeMillis();
    }

    public boolean stopWatch() {
        followIng = false;
        long diff = System.currentTimeMillis() - startTime;
        if (diff >= defaultDiff) {
            return true;
        }
        return false;
    }

    public void setDefaultDiff(long defaultDiff) {
        this.defaultDiff = defaultDiff;
    }

    private boolean followIng = false;

    public boolean isFollowIng() {
        return followIng;
    }
}
