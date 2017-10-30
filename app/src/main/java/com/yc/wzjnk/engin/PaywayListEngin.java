package com.yc.wzjnk.engin;

import android.content.Context;

import com.alibaba.fastjson.TypeReference;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.securityhttp.engin.BaseEngin;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.domain.PaywayListInfo;

import rx.Observable;

/**
 * Created by zhangkai on 2017/4/1.
 */

public class PaywayListEngin extends BaseEngin<ResultInfo<PaywayListInfo>> {
    public PaywayListEngin(Context context) {
        super(context);
    }

    @Override
    public String getUrl() {
        return Config.PAY_WAY_LIST_URL;
    }

    public Observable<ResultInfo<PaywayListInfo>> getPaywayList() {
        return rxpost(new TypeReference<ResultInfo<PaywayListInfo>>() {
        }.getType(), null, true, true, true);
    }
}
