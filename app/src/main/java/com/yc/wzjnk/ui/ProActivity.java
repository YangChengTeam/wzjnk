package com.yc.wzjnk.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.utils.AppUtil;

/**
 * Created by zhangkai on 2017/10/25.
 */

public class ProActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = this.getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                // king://xxxx/xx?data=xxxx
                String host = uri.getHost();
                switch (host) {
                    case "public":
                        WebPopupWindow webPopupWindow = new WebPopupWindow(MainActivity.getMainActivity(), Config.WEIXIN_JUMP_URL);
                        webPopupWindow.show(MainActivity.getMainActivity().getWindow().getDecorView().getRootView());
                        break;
                    case "download":
                        AppUtil.openWxShareText(MainActivity.getMainActivity(), uri.getQueryParameter("data"));
                        break;
                    case "qq":
                        AppUtil.gotoQQ(MainActivity.getMainActivity(), Config.QQ);
                        break;
                }
                finish();
            }
        }
    }
}
