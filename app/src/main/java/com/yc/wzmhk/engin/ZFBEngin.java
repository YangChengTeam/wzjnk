package com.yc.wzmhk.engin;

import android.content.Context;

import com.alibaba.fastjson.TypeReference;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.securityhttp.engin.BaseEngin;
import com.yc.wzmhk.domain.UserInfoWrapper;
import com.yc.wzmhk.domain.ZFBCode;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;

/**
 * Created by zhangkai on 2017/12/16.
 */

public class ZFBEngin extends BaseEngin {
    public ZFBEngin(Context context) {
        super(context);
    }

    @Override
    public String getUrl() {
        return "http://u.wk990.com/api/index/zfb_code?app_name=jnk";
    }

    public Observable<ResultInfo<ZFBCode>> getCode() {
        return rxpost(new TypeReference<ResultInfo<ZFBCode>>() {
        }.getType(), null, true, true, false);
    }
}
