package com.yc.wzmhk.engin;

import android.content.Context;

import com.alibaba.fastjson.TypeReference;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.securityhttp.engin.BaseEngin;
import com.yc.wzmhk.domain.Config;
import com.yc.wzmhk.domain.UserInfoWrapper;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;

/**
 * Created by zhangkai on 2017/12/1.
 */

public class UserUpdateEngin extends BaseEngin {
    public UserUpdateEngin(Context context) {
        super(context);
    }

    @Override
    public String getUrl() {
        return Config.UPADTE_USER_INFO_URL;
    }

    public Observable<ResultInfo<UserInfoWrapper>> updateUserInfo(String phone) {
        Map<String, String> params = new HashMap<>();
        params.put("mobile", phone);

        return rxpost(new TypeReference<ResultInfo<UserInfoWrapper>>() {
        }.getType(), params, true, true, true);
    }
}
