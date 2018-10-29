package com.yc.unlocking.domin;

import java.util.List;

/**
 * Created by zhangkai on 2018/1/3.
 */

public class ResultInfo {
    private int code;
    private String msg;
    private List<UnLockingInfo> lockingInfoList;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<UnLockingInfo> getLockingInfoList() {
        return lockingInfoList;
    }

    public void setLockingInfoList(List<UnLockingInfo> lockingInfoList) {
        this.lockingInfoList = lockingInfoList;
    }
}
