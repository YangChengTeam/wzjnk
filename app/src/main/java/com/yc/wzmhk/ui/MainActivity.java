package com.yc.wzmhk.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kk.loading.LoadingDialog;
import com.kk.pay.IPayImpl;
import com.kk.securityhttp.domain.GoagalInfo;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.securityhttp.net.contains.HttpConfig;
import com.kk.utils.TaskUtil;
import com.kk.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.game.UMGameAgent;
import com.yc.wzmhk.App;
import com.yc.wzmhk.R;
import com.yc.wzmhk.domain.Config;
import com.yc.wzmhk.domain.ContactInfo;
import com.yc.wzmhk.domain.GoodInfo;
import com.yc.wzmhk.domain.InitInfo;
import com.yc.wzmhk.domain.LoginDataInfo;
import com.yc.wzmhk.domain.StatusInfo;
import com.yc.wzmhk.domain.UserInfoWrapper;
import com.yc.wzmhk.domain.VipInfo;
import com.yc.wzmhk.domain.ZFBCode;
import com.yc.wzmhk.engin.LoginEngin;
import com.yc.wzmhk.engin.UserGetEngin;
import com.yc.wzmhk.engin.UserUpdateEngin;
import com.yc.wzmhk.engin.ZFBEngin;
import com.yc.wzmhk.helper.FollowHelper;
import com.yc.wzmhk.helper.SkillBoxListHelper;
import com.yc.wzmhk.services.FloatViewService;
import com.yc.wzmhk.services.SkillBoxInfoService;
import com.yc.wzmhk.utils.AppUtil;
import com.yc.wzmhk.utils.LogUtil;
import com.yc.wzmhk.utils.PreferenceUtil;
import com.yc.wzmhk.utils.SettingsCompat;
import com.yc.wzmhk.utils.UIUtil;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private TextView tvUser;

    private Button btnOpen;
    private ImageView ivRefresh;
    private TextView tvTitle;

    private LoadingDialog loadingDialog;
    private LoginEngin loginEngin;

    private GoodInfo vipGoodInfo;

    public static final String CURRENT_INFO = "currentInfo";
    public static final String OPEN_SERVICE = "openService";
    public static final String VIP = "vip";
    public static final String GOODS = "goods";

    private List<VipInfo> vipInfoList;
    private ContactInfo contactInfo;
    private ImageView ivPhone;
    SkillBoxListHelper skillBoxListHelper;
    private FollowHelper followHelper;

    private int is_close_gzh;
    private boolean isCopyWeiXin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginEngin = new LoginEngin(this);
        loadingDialog = new LoadingDialog(this);
        skillBoxListHelper = new SkillBoxListHelper(this);
        followHelper = new FollowHelper();

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvUser = (TextView) findViewById(R.id.tv_user);

        btnOpen = (Button) findViewById(R.id.btn_open);
        Button btnReward = (Button) findViewById(R.id.btn_reward);
        Button btnUsage = (Button) findViewById(R.id.btn_usage);
        Button btnWeixin = findViewById(R.id.btn_weixin);

        ivPhone = findViewById(R.id.iv_phone);
        ImageView ivShare = (ImageView) findViewById(R.id.iv_share);
        ImageView ivQQ = (ImageView) findViewById(R.id.iv_qq);
        ImageView ivWeiXin = (ImageView) findViewById(R.id.iv_weixin);

        ivRefresh = (ImageView) findViewById(R.id.iv_refresh);


        if (PreferenceUtil.getImpl(this).getBoolean("bindPhone", false)) {
            ivPhone.setVisibility(View.GONE);
        }
        //绑定手机
        ivPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindPhone();
            }
        });

        //分享
        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                AppUtil.openWxShareText(MainActivity.this, Config.INDEX_URL);
                MobclickAgent.onEvent(MainActivity.this, "king", "分享");
            }
        });

        btnWeixin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.playMp3();
                fixOpenwx();
                MobclickAgent.onEvent(MainActivity.this, "king", "打开微信");
            }
        });

        ivWeiXin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.playMp3();
                fixOpenwx();
                MobclickAgent.onEvent(MainActivity.this, "king", "打开微信");
            }
        });


        //QQ
        ivQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vipInfoList == null || vipInfoList.size() == 0) {
                    String html = "由于咨询用户过多，目前本QQ仅供付费用户咨询，付费会员直接点击QQ图标即可联系我。<br/><br/>普通用户可以关注【技能框大师】微信公众号了解使用问题哦。";
                    new MaterialDialog.Builder(MainActivity.this)
                            .title("QQ客服说明")
                            .content(Html.fromHtml(html))
                            .positiveText("确定")
                            .backgroundColor(Color.WHITE)
                            .contentColor(Color.GRAY)
                            .canceledOnTouchOutside(false)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    fixOpenwx();
                                }
                            })
                            .titleColor(Color.BLACK)
                            .build().show();
                    return;
                }
                App.playMp3();
                if (AppUtil.checkQQInstalled(MainActivity.this)) {
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
                String html = "<font color='red'>**特别提醒**<br/>长按顶部【" + getResources()
                        .getString(R.string.app_name) + "】，体验隐藏功能</font><br/>1.点击开启技能框<br" +
                        "/>2.授予悬浮窗权限<br" +
                        "/>3.若悬浮球出现一会儿消失情况，请开启通知<br/>";
                html += "<br/>分享给好友:<a href='king://download/weixin?data=" + Config.INDEX_URL + "'>" + Config.INDEX_URL +
                        "</a>";
                html += "<br/>关注公众号:<a href='king://public/weixin?data=" + Config.WEIXIN + "'>" + Config.WEIXIN +
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
                refresh();
            }
        });


        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.playMp3();
                openSkillBoxPermission();
            }
        });

        startService();

        setTitle();

        mainActivity = this;
        getLoginInfo();
        skillBoxListHelper.getTypeInfo();

        AppUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "请允许授予储存卡写入权限，下载更多炫酷技能框");

        bindPhone();

        new ZFBEngin(this).getCode().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ResultInfo<ZFBCode>>() {
            @Override
            public void call(ResultInfo<ZFBCode> stringResultInfo) {
                if (!TextUtils.isEmpty(stringResultInfo.data.getZfb_code())) {
                    AppUtil.copy(MainActivity.this, stringResultInfo.data.getZfb_code());
                    isCopyWeiXin = false;
                }
            }
        });
    }

    public void startService() {
        try {
            Intent intent = new Intent(this, FloatViewService.class);
            startService(intent);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
        }
    }

    private void setTitle() {
        if (GoagalInfo.get().packageInfo != null) {
            tvTitle.setText(getString(R.string.app_name) + "v" + GoagalInfo.get().packageInfo.versionName);
            tvTitle.setLongClickable(true);
            tvTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openAccessibility();
                    return true;
                }
            });
        }
    }

    public void saveVip(String vip) {
        String goods = PreferenceUtil.getImpl(this).getString(MainActivity.GOODS, "");
        goods += "," + vip;
        PreferenceUtil.getImpl(this).putString(MainActivity.GOODS, goods);
    }


    private void refresh() {
        if (!tvTitle.getText().toString().contains("v")) {
            App.initGoagal(getApplicationContext());
            setTitle();
        }
        loadingDialog.show("正在同步数据");
        skillBoxListHelper.refreshData();
        getLoginInfo();
    }

    public static MainActivity mainActivity;

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public GoodInfo getVipGoodInfo() {
        return vipGoodInfo;
    }

    public void setVipGoodInfo(GoodInfo vipGoodInfo) {
        this.vipGoodInfo = vipGoodInfo;
    }

    private GoodInfo currentGoodInfo;

    public GoodInfo getCurrentGoodInfo() {
        return currentGoodInfo;
    }

    public void setCurrentGoodInfo(GoodInfo vipGoodInfo) {
        this.currentGoodInfo = vipGoodInfo;
    }

    public void fixOpenwx() {
        if (is_close_gzh == 0) {
            WebPopupWindow webPopupWindow = new WebPopupWindow(MainActivity.getMainActivity(), Config.WEIXIN_JUMP_URL);
            webPopupWindow.show(MainActivity.getMainActivity().getWindow().getDecorView().getRootView());
            return;
        }

        if (isCopyWeiXin) {
            AppUtil.copy(MainActivity.this, Config.WEIXIN);
        }
        String html = "现在起关注【技能框大师】微信公众号，并转发朋友圈即可免费解锁【动态技能框】！<br/><br/>" + "点击确定复制公众号并跳转微信，手动搜索粘贴公众号账号关注！";
        new MaterialDialog.Builder(MainActivity.this)
                .title("关注微信公众号")
                .content(Html.fromHtml(html))
                .positiveText("确定")
                .backgroundColor(Color.WHITE)
                .contentColor(Color.GRAY)
                .canceledOnTouchOutside(false)
                .titleColor(Color.BLACK)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        AppUtil.gotoWeiXin(MainActivity.this, "公众号已复制，正在前往微信...");
                    }
                })
                .build().show();
    }

    public void getLoginInfo() {
        ivRefresh.setClickable(false);
        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                String data = PreferenceUtil.getImpl(MainActivity.this).getString(Config.INIT_URL, "");
                if (!data.isEmpty()) {
                    try {
                        final ResultInfo<LoginDataInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<LoginDataInfo>>() {
                        }.getType());
                        UIUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                getLoginInfo(resultInfo);
                            }
                        });
                    } catch (Exception e) {
                        LogUtil.msg("getLoginInfo本地缓存" + e);
                    }
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
            is_close_gzh = resultInfo.data.getIs_close_gzh();
            vipInfoList = resultInfo.data.getVipInfoList();
            contactInfo = resultInfo.data.getContactInfo();
            StatusInfo statusInfo = resultInfo.data.getStatusInfo();

            if (tvTitle.getText().toString().contains("v")) {
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
            } else {
                tvUser.setText("获取用户信息失败， 点击重新拉取用户信息");
                tvUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refresh();
                    }
                });
            }
            skillBoxListHelper.notifyDataSetChanged();
        }
    }

    //更新列表
    public void notifyDataSetChanged() {
        if (skillBoxListHelper != null) {
            skillBoxListHelper.notifyDataSetChanged();
        }
    }

    //网络同步
    public boolean isFree(int id) {
        if (vipInfoList == null) return false;
        for (VipInfo vipInfo : vipInfoList) {
            if (vipInfo.getType() == Config.VIP_ID) {
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
        return !vip.equals("");
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

    private void openSkillBoxPermission() {
        try {
            if (!canDrawOverlays()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES
                        .M) {
                    createFloatView(); //某些机型主动调一次 开启悬浮窗权限才有效
                    removeAllView();
                }
                SettingsCompat.manageDrawOverlays(this);
                hasSetting = true;
            } else {
                enableSkillBox();
            }
        } catch (Exception e) {
            enableSkillBox();
        }
    }

    private boolean checkSkillBoxPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT <= Build.VERSION_CODES
                .N) {
            return true;
        }
        try {
            if (!canDrawOverlays()) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    private void bindPhone() {
        PreferenceUtil.getImpl(MainActivity.this).putBoolean
                ("bindPhone", true);
//        new UserGetEngin(this).getUserInfo().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ResultInfo<UserInfoWrapper>>() {
//            @Override
//            public void call(ResultInfo<UserInfoWrapper> userInfoWrapperResultInfo) {
//                if(userInfoWrapperResultInfo.data == null || userInfoWrapperResultInfo.data.getUserInfo() == null){
//                    showDialog();
//                    return;
//                }
//                if(userInfoWrapperResultInfo.data != null && userInfoWrapperResultInfo.data.getUserInfo() != null
//                        && TextUtils.isEmpty(userInfoWrapperResultInfo.data.getUserInfo().getMobile())) {
//                    showDialog();
//                } else {
//                    PreferenceUtil.getImpl(MainActivity.this).putBoolean
//                            ("bindPhone",
//                                    true);
//                    ivPhone.setVisibility(View.GONE);
//                }
//            }
//        });
    }

    private void showDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                .title("绑定手机获取免费技能框")
                .titleColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .inputRange(11, 11, Color.GRAY)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .canceledOnTouchOutside(false)
                .input("请输入手机号", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.length() > 11) {
                            input = input.subSequence(0, 10);
                            dialog.getInputEditText().setText(input);
                        }
                        // Do something
                    }
                }).positiveText("提交").onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (DialogAction.POSITIVE == which) {
                            String phone = dialog.getInputEditText().getText().toString();
                            loadingDialog.show("正在绑定手机号");
                            new UserUpdateEngin(MainActivity.this).updateUserInfo(phone).observeOn
                                    (AndroidSchedulers.mainThread()).subscribe
                                    (new
                                             Action1<ResultInfo<UserInfoWrapper>>() {
                                                 @Override
                                                 public void call(ResultInfo<UserInfoWrapper> userInfoWrapperResultInfo) {
                                                     loadingDialog.dismiss();
                                                     if (userInfoWrapperResultInfo.code == HttpConfig.STATUS_OK) {
                                                         ToastUtil.toast2(MainActivity.this, "绑定成功");
                                                         PreferenceUtil.getImpl(MainActivity.this).putBoolean
                                                                 ("bindPhone",
                                                                         true);
                                                         ivPhone.setVisibility(View.GONE);
                                                         skillBoxListHelper.notifyDataSetChanged();

                                                     } else {
                                                         ToastUtil.toast2(MainActivity.this,
                                                                 userInfoWrapperResultInfo.message);
                                                     }
                                                 }
                                             });
                        }
                    }
                }).build();
        dialog.getInputEditText().setTextColor(Color.BLACK);
        dialog.getInputEditText().setHintTextColor(Color.GRAY);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
    }


    private void enableSkillBox() {
        String isOpen = PreferenceUtil.getImpl(this).getString(OPEN_SERVICE, "");
        if (isOpen.equals("")) {
            PreferenceUtil.getImpl(this).putString(OPEN_SERVICE, OPEN_SERVICE);
            btnOpen.setBackground(ContextCompat.getDrawable(this, R.drawable.btn23_selector));
            createFloatView();
        } else {
            PreferenceUtil.getImpl(this).putString(OPEN_SERVICE, "");
            btnOpen.setBackground(ContextCompat.getDrawable(this, R.drawable.btn2_selector));
            removeAllView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UMGameAgent.onResume(this);
        if (canDrawOverlays() && hasSetting) {
            PreferenceUtil.getImpl(this).putString(OPEN_SERVICE, OPEN_SERVICE);
            btnOpen.setBackground(ContextCompat.getDrawable(this, R.drawable.btn23_selector));
            hasSetting = false;
            createFloatView();
        }

        if (IPayImpl.uiPayCallback != null && IPayImpl.uOrderInfo != null && IPayImpl.isGen()) {
            IPayImpl.checkOrder(IPayImpl.uOrderInfo, IPayImpl.uiPayCallback, "http://u.wk990.com/api/index/orders_query?app_id=4");
        }


        //推测关注成功
        if (followHelper.isFollowIng() && followHelper.stopWatch()) {
            LogUtil.msg("推测关注成功");
            if (currentGoodInfo != null) {
                saveVip(currentGoodInfo.getIcon());
            }
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            App.playMp3();
            String html = "<font color=red>退出软件</font>将无法在游戏中使用技能框，<font color=red>请慎重确定！</font>";
            new MaterialDialog.Builder(MainActivity.this)
                    .title("提示")
                    .content(Html.fromHtml(html))
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
                    .title("体验版")
                    .content("点击[❤❤" + getResources().getString(R.string.app_name) + "❤❤]开启辅助功能, 解决黑屏问题！")
                    .positiveText("确定")
                    .backgroundColor(Color.WHITE)
                    .contentColor(Color.GRAY)
                    .titleColor(Color.BLACK)
                    .canceledOnTouchOutside(false)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    LogUtil.msg("openAccessibility异常" + e);
                                }
                            }
                        }
                    })
                    .build().show();
        } else {
            ToastUtil.toast2(this, getResources().getString(R.string.app_name) + "辅助功能已开启");
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
                btnOpen.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn2_selector));
                removeAllView();
            } else {
                btnOpen.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn23_selector));
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

    public void start() {
        followHelper.start();
    }

    public boolean isOpen() {
        return mFloatViewService != null && mFloatViewService.isOpen();
    }

    public void removeAllView() {
        if (checkSkillBoxPermission() && mFloatViewService != null) {
            mFloatViewService.removeAllView();
        }
    }

    public void addSkillBoxView() {
        if (checkSkillBoxPermission() && mFloatViewService != null) {
            mFloatViewService.addSkillBoxView();
        }
    }

    public void createFloatView() {
        if (checkSkillBoxPermission() && mFloatViewService != null) {
            mFloatViewService.createFloatView();
        }
    }

    public void removeSkillBoxView() {
        if (checkSkillBoxPermission() && mFloatViewService != null) {
            mFloatViewService.removeSkillBoxView();
        }
    }

    public void updateSkillBoxView() {
        if (checkSkillBoxPermission() && mFloatViewService != null) {
            mFloatViewService.updateSkillBoxView();
        }
    }
}
