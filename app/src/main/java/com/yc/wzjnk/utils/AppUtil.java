package com.yc.wzjnk.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.kk.utils.ToastUtil;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.ui.MainActivity;

/**
 * Created by zhangkai on 2017/10/25.
 */

public class AppUtil {
    public static void gotoWeiXin(Context context) {
        try {
            Intent intent = new Intent();
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui" +
                    ".LauncherUI");
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }

    public static void gotoWeiXin(final Context context, String msg) {
        gotoWeiXin(context, msg, null);
    }

    public static void gotoWeiXin(final Context context, String msg, final Runnable runnable) {
        ToastUtil.toast2(context, msg);
        UIUtil.postDelayed(2000, new Runnable() {
            @Override
            public void run() {
                AppUtil.gotoWeiXin(context);
                if (runnable != null) runnable.run();
            }
        });
    }

    public static void copy(Context context, String data) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", data);
        clipboard.setPrimaryClip(clip);
    }

    public static void openWxShareText(final Activity ctx, String content) {
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "");
            intent.putExtra(Intent.EXTRA_TEXT, content);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"));
            ctx.startActivity(intent);
        } catch (Exception e) {
            AppUtil.gotoWeiXin(ctx, "下载地址已复制, 正在前往微信...");
        }
    }
}
