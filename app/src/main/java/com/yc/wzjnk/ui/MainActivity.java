package com.yc.wzjnk.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kk.loading.LoadingDialog;
import com.kk.securityhttp.domain.GoagalInfo;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.securityhttp.net.contains.HttpConfig;
import com.kk.utils.TaskUtil;
import com.kk.utils.ToastUtil;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.game.UMGameAgent;
import com.yc.wzjnk.App;
import com.yc.wzjnk.R;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.domain.ContactInfo;
import com.yc.wzjnk.domain.GoodInfo;
import com.yc.wzjnk.domain.LoginDataInfo;
import com.yc.wzjnk.domain.StatusInfo;
import com.yc.wzjnk.domain.TypeInfo;
import com.yc.wzjnk.domain.TypeListInfo;
import com.yc.wzjnk.domain.VipInfo;
import com.yc.wzjnk.engin.LoginEngin;
import com.yc.wzjnk.engin.TypeEngin;
import com.yc.wzjnk.services.FloatViewService;
import com.yc.wzjnk.services.SkillBoxInfoService;
import com.yc.wzjnk.utils.AppUtil;
import com.yc.wzjnk.utils.PreferenceUtil;
import com.yc.wzjnk.utils.SettingsCompat;
import com.yc.wzjnk.utils.UIUtil;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    private Button btnOpen;
    private Button btnReward;
    private Button btnUsage;

    private ImageView ivShare;
    private ImageView ivQQ;
    private ImageView ivWeiXin;
    private ImageView ivRefresh;

    private LoadingDialog loadingDialog;
    private LoginEngin loginEngin;

    private GoodInfo vipGoodInfo;

    private TextView tvTitle;
    private TextView tvUser;

    public static final String CURRENT_INFO = "currentInfo";
    public static final String OPEN_SERVICE = "openService";
    public static final String VIP = "vip";
    public static final String GOODS = "goods";

    private List<VipInfo> vipInfoList;
    private ContactInfo contactInfo;
    private StatusInfo statusInfo;

    private TypeEngin typeEngin;
    private ScrollIndicatorView scrollIndicatorView;
    private ViewPager mViewPager;
    private List<SkillBoxFragment> skillBoxFragments;

    public GoodInfo getVipGoodInfo() {
        return vipGoodInfo;
    }

    public void setVipGoodInfo(GoodInfo vipGoodInfo) {
        this.vipGoodInfo = vipGoodInfo;
    }

    private static final long MIN_CLICK_INTERVAL = 600;
    private int mSecretNumber = 0;
    private long mLastClickTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        loginEngin = new LoginEngin(this);
        loadingDialog = new LoadingDialog(this);
        typeEngin = new TypeEngin(this);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvUser = (TextView) findViewById(R.id.tv_user);

        btnOpen = (Button) findViewById(R.id.btn_open);
        btnReward = (Button) findViewById(R.id.btn_reward);
        btnUsage = (Button) findViewById(R.id.btn_usage);

        ivShare = (ImageView) findViewById(R.id.iv_share);
        ivQQ = (ImageView) findViewById(R.id.iv_qq);
        ivWeiXin = (ImageView) findViewById(R.id.iv_weixin);
        ivRefresh = (ImageView) findViewById(R.id.iv_refresh);

        if (GoagalInfo.get().packageInfo != null) {
            tvTitle.setText(getString(R.string.app_name) + "v" + GoagalInfo.get().packageInfo.versionName);
            tvTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long currentClickTime = SystemClock.uptimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (++mSecretNumber == 5 && elapsedTime < MIN_CLICK_INTERVAL) {
                        openAccessibility();
                        mSecretNumber = 0;
                    }
                }
            });
        }


        getLoginInfo();

        //分享
        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                AppUtil.openWxShareText(MainActivity.this, Config.INDEX_URL);
                MobclickAgent.onEvent(MainActivity.this, "king", "分享");
            }
        });


        //微信
        ivWeiXin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                WebPopupWindow webPopupWindow = new WebPopupWindow(MainActivity.this, Config.WEIXIN_JUMP_URL);
                webPopupWindow.show(getWindow().getDecorView().getRootView());
                MobclickAgent.onEvent(MainActivity.this, "king", "打开微信");
            }
        });

        //QQ
        ivQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                if (checkQQInstalled(MainActivity.this)) {
                    if (contactInfo != null && contactInfo.getQq() != null) {
                        Config.QQ = contactInfo.getQq();
                    }
                    if (vipInfoList != null && vipInfoList.size() > 0) {
                        Config.QQ = Config.VIP_QQ;
                    }
                    AppUtil.gotoQQ(MainActivity.this, Config.QQ);
                } else {
                    Toast.makeText(MainActivity.this, "手机QQ未安装或该版本不支持", Toast.LENGTH_SHORT).show();
                }
                MobclickAgent.onEvent(MainActivity.this, "king", "打开QQ");
            }
        });

        //打赏
        btnReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                removeSkillBoxView();
                RewardPopupWindow rewardPopupWindow = new RewardPopupWindow(MainActivity.this);
                rewardPopupWindow.show(getWindow().getDecorView().getRootView());
                MobclickAgent.onEvent(MainActivity.this, "king", "打开打赏窗口");
            }
        });

        //使用教程
        btnUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                if (contactInfo != null && contactInfo.getQq() != null) {
                    Config.QQ = contactInfo.getQq();
                }
                if (vipInfoList != null && vipInfoList.size() > 0) {
                    Config.QQ = Config.VIP_QQ;
                }
                String html = "<font color='red'>**注意**<br/>最佳用户体验，请先打开游戏后，再开启技能框</font><br/>1.点击开启技能框<br" +
                        "/>2.授予悬浮窗权限<br" +
                        "/>与qq客服进行沟通";
                html += "<a href='king://qq/chat?data=" + Config.QQ + "'>" + Config.QQ + "</a>";
                html += "<br/>分享给好友:<a href='king://download/weixin?data=" + Config.INDEX_URL + "'>" + Config.INDEX_URL +
                        "</a>";
                html += "<br/>打开微信公众号:<a href='king://public/weixin?data=" + Config.WEIXIN + "'>" + Config.WEIXIN +
                        "</a>";
                new MaterialDialog.Builder(MainActivity.this)
                        .title("使用说明")
                        .content(Html.fromHtml(html))
                        .positiveText("确定")
                        .canceledOnTouchOutside(false)
                        .backgroundColor(Color.WHITE)
                        .contentColor(Color.GRAY)
                        .titleColor(Color.BLACK)
                        .build().show();
                MobclickAgent.onEvent(MainActivity.this, "king", "使用方法");
            }
        });

        ivRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show("正在同步数据");
                refreshData();
                getLoginInfo();
            }
        });


        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                open();
            }
        });

        try {
            Intent intent = new Intent(this, FloatViewService.class);
            startService(intent);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
        }

        mainActivity = this;

        scrollIndicatorView = (ScrollIndicatorView) findViewById(R.id.fiv_indicator);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        scrollIndicatorView.setScrollBar(new ColorBar(this, Color.TRANSPARENT, 1));
        float unSelectSize = 15;
        float selectSize = 15;
        int selectColor = Color.WHITE;
        int unSelectColor = Color.parseColor("#8c9caf");
        scrollIndicatorView.setOnTransitionListener(new OnTransitionTextListener().setColor(selectColor, unSelectColor).setSize(selectSize, unSelectSize));
        scrollIndicatorView.setOnIndicatorItemClickListener(new Indicator.OnIndicatorItemClickListener() {
            @Override
            public boolean onItemClick(View clickItemView, int position) {
                mViewPager.setCurrentItem(position);
                return false;
            }
        });
        scrollIndicatorView.setCurrentItem(0, true);

        getTypeInfo();
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, "请允许授予储存卡写入权限，下载更多炫酷技能框");
    }

    public boolean checkPermission(String permission, String msg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                requestMultiplePermissions(permission);
                return false;
            }
            return true;
        }
        return true;
    }

    private static final int REQUEST_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions(String permission) {
        String[] permissions = {permission};
        requestPermissions(permissions, REQUEST_CODE);
    }

    public void fixOpenwx() {
        AppUtil.copy(MainActivity.this, Config.WEIXIN);
        String html = "更多精彩内容，尽在王者技能框 <br/>微信公众号：<a href=king://public/weixin?data='" + Config
                .WEIXIN + "'>" + Config
                .WEIXIN +
                "</a>";
        new MaterialDialog.Builder(MainActivity.this)
                .title("加入微信公众号")
                .content(Html.fromHtml(html))
                .positiveText("确定")
                .backgroundColor(Color.WHITE)
                .contentColor(Color.GRAY)
                .canceledOnTouchOutside(false)
                .titleColor(Color.BLACK)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        AppUtil.gotoWeiXin(MainActivity.this, "下载地址已复制, 正在前往微信...");
                    }
                })
                .build().show();
    }

    public static MainActivity mainActivity;

    public static MainActivity getMainActivity() {
        return mainActivity;
    }


    public void getTypeInfo(final ResultInfo<TypeListInfo> resultInfo) {
        if (resultInfo != null && resultInfo.code == HttpConfig.STATUS_OK && resultInfo
                .data != null && resultInfo.data.getList() != null) {
            if (skillBoxFragments != null && skillBoxFragments.size() == resultInfo.data.getList().size() + 1) {
                notifyDataSetChanged();
            } else {
                initFragments(resultInfo.data.getList());
            }
        }
    }

    public void getTypeInfo() {
        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                String data = PreferenceUtil.getImpl(MainActivity.this).getString(Config.TYPE_LIST_URL, "");
                if (!data.isEmpty()) {
                    final ResultInfo<TypeListInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<TypeListInfo>>() {
                    }.getType());
                    UIUtil.post(new Runnable() {
                        @Override
                        public void run() {
                            getTypeInfo(resultInfo);
                        }
                    });
                }
            }
        });
        typeEngin.getTypeList().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ResultInfo<TypeListInfo>>() {
            @Override
            public void call(final ResultInfo<TypeListInfo> resultInfo) {
                if (resultInfo != null && resultInfo.code == HttpConfig.STATUS_OK && resultInfo
                        .data != null && resultInfo.data.getList() != null) {
                    TaskUtil.getImpl().runTask(new Runnable() {
                        @Override
                        public void run() {
                            PreferenceUtil.getImpl(MainActivity.this).putString(Config.TYPE_LIST_URL, JSON.toJSONString
                                    (resultInfo));
                        }
                    });
                    getTypeInfo(resultInfo);
                }
            }
        });
    }

    public void getLoginInfo() {
        ivRefresh.setClickable(false);
        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                String data = PreferenceUtil.getImpl(MainActivity.this).getString(Config.INIT_URL, "");
                if (!data.isEmpty()) {
                    final ResultInfo<LoginDataInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<LoginDataInfo>>() {
                    }.getType());
                    UIUtil.post(new Runnable() {
                        @Override
                        public void run() {
                            getLoginInfo(resultInfo);
                        }
                    });
                }
            }
        });
        loginEngin.rxGetInfo().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResultInfo<LoginDataInfo>>() {
                    @Override
                    public void call(final ResultInfo<LoginDataInfo> resultInfo) {
                        ivRefresh.setClickable(true);
                        loadingDialog.dismiss();

                        if (resultInfo != null && resultInfo.code == HttpConfig.STATUS_OK) {
                            TaskUtil.getImpl().runTask(new Runnable() {
                                @Override
                                public void run() {
                                    PreferenceUtil.getImpl(MainActivity.this).putString(Config.INIT_URL, JSON.toJSONString
                                            (resultInfo));
                                }
                            });
                            getLoginInfo(resultInfo);
                        }
                    }
                });
    }

    private void getLoginInfo(final ResultInfo<LoginDataInfo> resultInfo) {
        if (resultInfo.data != null) {
            vipInfoList = resultInfo.data.getVipInfoList();
            contactInfo = resultInfo.data.getContactInfo();
            statusInfo = resultInfo.data.getStatusInfo();
            String txt = "";
            if (vipInfoList != null && vipInfoList.size() > 0) {
                vipInfoList = resultInfo.data.getVipInfoList();
                txt += "尊敬的会员您好，";
            }
            if (statusInfo != null) {
                txt += "用户id: " + statusInfo.getUid();
                tvUser.setText(txt);
                tvUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppUtil.copy(MainActivity.this, GoagalInfo.get().uuid);
                        ToastUtil.toast2(MainActivity.this, "用户id复制成功");
                    }
                });
            }
            notifyDataSetChanged();
        }
    }

    //更新列表
    public void notifyDataSetChanged() {
        if (skillBoxFragments == null) return;
        for (SkillBoxFragment skillBoxFragment : skillBoxFragments) {
            skillBoxFragment.notifyDataSetChanged();
        }
    }

    public void refreshData() {
        if (skillBoxFragments == null) return;
        for (SkillBoxFragment skillBoxFragment : skillBoxFragments) {
            skillBoxFragment.loadData();
        }
    }

    private void initFragments(List<TypeInfo> typeInfos) {
        String[] titles = new String[1 + typeInfos.size()];
        String[] types = new String[1 + typeInfos.size()];
        titles[0] = "全部";
        types[0] = "";
        for (int i = 0; i < typeInfos.size(); i++) {
            titles[i + 1] = typeInfos.get(i).getName();
            types[i + 1] = typeInfos.get(i).getId();
        }
        scrollIndicatorView.setAdapter(new MyAdapter(this, titles));
        MyFragmentAdapter mFragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager(),
                types);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                scrollIndicatorView.setCurrentItem(i, true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    class MyAdapter extends Indicator.IndicatorAdapter {
        private Activity mContext;
        private String[] mTitles;

        public MyAdapter(Activity context, String[] titles) {
            super();
            this.mContext = context;
            this.mTitles = titles;
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.view_tab, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(mTitles[position]);
            return convertView;
        }
    }

    class MyFragmentAdapter extends FragmentStatePagerAdapter {
        private int count;

        public MyFragmentAdapter(FragmentManager fm, String[] types) {
            super(fm);
            skillBoxFragments = new ArrayList<>();
            for (int i = 0; i < types.length; i++) {
                SkillBoxFragment skillBoxFragment = new SkillBoxFragment();
                skillBoxFragment.setType(types[i]);
                skillBoxFragments.add(skillBoxFragment);
            }
            count = types.length;
        }

        @Override
        public Fragment getItem(int position) {
            return skillBoxFragments.get(position);
        }

        @Override
        public int getCount() {
            return count;
        }
    }

    //网络同步
    public boolean isFree(int id) {
        if (vipInfoList == null) return false;
        for (VipInfo vipInfo : vipInfoList) {
            if (vipInfo.getType() == 51) {
                return true;
            }
            if (vipInfo.getType() == id) {
                return true;
            }
        }
        return false;
    }


    //商品是否已经购买
    public boolean isPay(String iconName) {
        boolean ispay = false;
        String goodsStr = PreferenceUtil.getImpl(MainActivity.this).getString(GOODS, "");
        String[] goods = goodsStr.split(",");
        for (String good : goods) {
            if (good.equals(iconName)) {
                ispay = true;
                break;
            }
        }
        return ispay;
    }

    //是否是vip
    public boolean isVip() {
        String vip = PreferenceUtil.getImpl(MainActivity.this).getString(VIP, "");
        if (vip.equals("")) {
            return false;
        }
        return true;
    }

    private boolean hasSetting = false;

    private boolean canDrawOverlays() {
        boolean flag;
        try {
            flag = SettingsCompat.canDrawOverlays(this);
        } catch (Exception e) {
            flag = true;
        }
        return flag;
    }

    /**
     * 开启权限
     */
    private void open() {
        try {
            if (!canDrawOverlays()) {
                createFloatView(); //某些机型主动调一次 开启悬浮窗权限才有效
                removeAllView();
                SettingsCompat.manageDrawOverlays(this);
                hasSetting = true;
            } else {
                openSkillBox();
            }
        } catch (Exception e) {
            openSkillBox();
        }
    }

    private void openSkillBox() {
        String isOpen = PreferenceUtil.getImpl(this).getString(OPEN_SERVICE, "");
        if (isOpen.equals("")) {
            PreferenceUtil.getImpl(this).putString(OPEN_SERVICE, OPEN_SERVICE);
            btnOpen.setText("已启用");
            createFloatView();
        } else {
            PreferenceUtil.getImpl(this).putString(OPEN_SERVICE, "");
            btnOpen.setText("开启技能框");
            removeAllView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UMGameAgent.onResume(this);
        if (canDrawOverlays() && hasSetting) {
            PreferenceUtil.getImpl(this).putString(OPEN_SERVICE, OPEN_SERVICE);
            btnOpen.setText("已启用");
            hasSetting = false;
            createFloatView();
        }
    }

    public boolean checkQQInstalled(Context context) {
        Uri uri = Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        return componentName != null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            App.playMp3();
            new MaterialDialog.Builder(MainActivity.this)
                    .title("提示")
                    .content("确认退出王者技能框大师?")
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

    /**
     * 跳转到系统设置页面开启辅助功能
     */
    private void openAccessibility() {
        if (!SkillBoxInfoService.isRunning()) {
            new MaterialDialog.Builder(this)
                    .title("提示")
                    .content("点击[❤❤王者荣耀技能框❤❤]开启服务, 悬浮球只会出现在王者应用上，并有效解决黑屏问题！")
                    .positiveText("确定")
                    .backgroundColor(Color.WHITE)
                    .contentColor(Color.GRAY)
                    .titleColor(Color.BLACK)
                    .canceledOnTouchOutside(false)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                startActivity(intent);
                            }
                        }
                    })
                    .build().show();
        }
    }

    private FloatViewService mFloatViewService;

    /**
     * 连接到Service
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mFloatViewService = ((FloatViewService.FloatViewServiceBinder) iBinder).getService();
            String isOpen = PreferenceUtil.getImpl(MainActivity.this).getString(OPEN_SERVICE, "");
            if (isOpen.equals("")) {
                btnOpen.setText("开启技能框");
                removeAllView();
            } else {
                btnOpen.setText("已启用");
                createFloatView();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mFloatViewService = null;
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        // //集成基本统计分析, 结束 Session
        UMGameAgent.onPause(this);
        removeSkillBoxView();
    }


    public boolean isOpen() {
        if (mFloatViewService != null) {
            return mFloatViewService.isOpen();
        }
        return false;
    }

    public void removeAllView() {
        if (mFloatViewService != null) {
            mFloatViewService.removeAllView();
        }
    }

    public void createFloatView() {
        if (mFloatViewService != null) {
            mFloatViewService.createFloatView();
        }
    }

    public void removeSkillBoxView() {
        if (mFloatViewService != null) {
            mFloatViewService.removeSkillBoxView();
        }
    }

    public void addSkillBoxView() {
        if (mFloatViewService != null) {
            mFloatViewService.addSkillBoxView();
        }
    }

    public void updateSkillBoxView() {
        if (mFloatViewService != null) {
            mFloatViewService.updateSkillBoxView();
        }
    }
}
