package com.yc.wzjnk;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Build;

import com.kk.securityhttp.domain.GoagalInfo;
import com.kk.securityhttp.net.contains.HttpConfig;
import com.kk.utils.FileUtil;
import com.kk.utils.TaskUtil;
import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.game.UMGameAgent;
import com.yc.wzjnk.domain.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangkai on 2017/10/17.
 */

public class App extends Application {
    private static MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();

        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                //腾迅自动更新
                Bugly.init(getApplicationContext(), "4ab6b413fc", false);
                try {
                    AssetManager assetManager = getAssets();
                    AssetFileDescriptor afd = assetManager.openFd("sound.mp3");
                    player = new MediaPlayer();
                    player.setDataSource(afd.getFileDescriptor(),
                            afd.getStartOffset(), afd.getLength());
                    player.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //友盟统计
                UMGameAgent.setDebugMode(Config.DEBUG);
                UMGameAgent.init(App.this);
                UMGameAgent.setPlayerLevel(1);
                MobclickAgent.setScenarioType(App.this, MobclickAgent.EScenarioType.E_UM_NORMAL);
                //全局信息初始化
                GoagalInfo.get().init(getApplicationContext());

                //设置文件唯一性 防止手机相互拷贝
                FileUtil.setUuid(GoagalInfo.get().uuid);

                //设置http默认参数
                String agent_id = "1";
                Map<String, String> params = new HashMap<>();
                if (GoagalInfo.get().channelInfo != null && GoagalInfo.get().channelInfo.agent_id != null) {
                    params.put("from_id", GoagalInfo.get().channelInfo.from_id + "");
                    params.put("author", GoagalInfo.get().channelInfo.author + "");
                    agent_id = GoagalInfo.get().channelInfo.agent_id;
                }
                params.put("agent_id", agent_id);
                params.put("ts", System.currentTimeMillis() + "");
                params.put("imeil", GoagalInfo.get().uuid);
                String sv = getSV();
                params.put("sv", sv);
                params.put("device_type", "2");
                if (GoagalInfo.get().packageInfo != null) {
                    params.put("app_version", GoagalInfo.get().packageInfo.versionName + "");
                }
                HttpConfig.setDefaultParams(params);

                //动态设置渠道信息
                String appId_agentId = "王者技能框-渠道id" + agent_id;
                MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(getApplicationContext(),
                        "59e8116107fe650a04000070", appId_agentId));
            }
        });
    }

    public static String getSV() {
        return android.os.Build.MODEL.contains(android.os.Build.BRAND) ? android.os.Build.MODEL + " " + android
                .os.Build.VERSION.RELEASE : Build.BRAND + " " + android
                .os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE;
    }

    public static boolean isBugBrand() {
        boolean flag = false;
        String sv = getSV();
        String[] brands = new String[]{"oneplus", "gionee", "nx563j", "yu", "zte", "m8wl", "moto", "hisense", "redmi"};
        for (String brand : brands) {
            if (sv.toLowerCase().contains(brand)) {
                flag = true;
            }
        }
        return flag;
    }

    public static void playMp3() {
        if (player != null) {
            player.start();
        }
    }
}
