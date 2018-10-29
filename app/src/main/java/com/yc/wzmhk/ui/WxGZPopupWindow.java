package com.yc.wzmhk.ui;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.yc.wzmhk.R;
import com.yc.wzmhk.domain.Config;
import com.yc.wzmhk.utils.AppUtil;


/**
 * Created by zhangkai on 2017/7/28.
 */

public class WxGZPopupWindow extends BasePopupWindow {
    private TextView tvOpenWx;

    public WxGZPopupWindow(final Activity context) {
        super(context);

        View contextView = getContentView();

        LinearLayout llGoods = (LinearLayout) contextView.findViewById(R.id.ll_goods);
        tvOpenWx = (TextView) contextView.findViewById(R.id.tv_open_wx);

        tvOpenWx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getMainActivity().start();
                if(MainActivity.getMainActivity().isCopyWeiXin()) {
                    AppUtil.copy(context, Config.WEIXIN);
                }
                AppUtil.gotoWeiXin(context, "公众号已复制，正在前往微信");
                MobclickAgent.onEvent(context, "king", "点击打开微信关注");
            }
        });
    }

    @Override
    public int getLayoutID() {
        return R.layout.ppw_wx_gz;
    }
}
