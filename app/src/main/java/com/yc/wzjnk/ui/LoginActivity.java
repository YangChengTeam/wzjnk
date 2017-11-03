package com.yc.wzjnk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.umeng.analytics.game.UMGameAgent;
import com.yc.wzjnk.App;
import com.yc.wzjnk.R;

/**
 * Created by zhangkai on 2017/10/17.
 */

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLgoin = (Button) findViewById(R.id.btn_login);
        btnLgoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            App.playMp3();
            new MaterialDialog.Builder(LoginActivity.this)
                    .title("提示")
                    .content("确认退出" + getResources().getString(R.string.app_name) + "?")
                    .positiveText("确定")
                    .negativeText("取消")
                    .backgroundColor(Color.WHITE)
                    .contentColor(Color.GRAY)
                    .titleColor(Color.BLACK)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (DialogAction.POSITIVE == which) {
                                finish();
                                System.exit(0);
                            }
                        }
                    })
                    .build().show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 集成基本统计分析,初始化 Session
        UMGameAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // //集成基本统计分析, 结束 Session
        UMGameAgent.onPause(this);
    }


}
