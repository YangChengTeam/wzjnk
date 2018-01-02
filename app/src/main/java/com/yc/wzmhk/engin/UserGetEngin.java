package com.yc.wzmhk.engin;

import android.content.Context;

import com.alibaba.fastjson.TypeReference;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.securityhttp.engin.BaseEngin;
import com.yc.wzmhk.domain.Config;
import com.yc.wzmhk.domain.UserInfoWrapper;

import rx.Observable;

/**
 * Created by zhangkai on 2017/12/1.
 */

public class UserGetEngin extends BaseEngin {
    public UserGetEngin(Context context) {
        super(context);
    }

    @Override
    public String getUrl() {
        return Config.GET_USER_INFO_URL;
    }

    public Observable<ResultInfo<UserInfoWrapper>> getUserInfo() {
        return rxpost(new TypeReference<ResultInfo<UserInfoWrapper>>() {
        }.getType(), null, true, true, true);
    }
}
