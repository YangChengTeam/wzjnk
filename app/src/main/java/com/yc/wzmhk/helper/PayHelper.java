package com.yc.wzmhk.helper;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kk.pay.I1PayAbs;
import com.kk.pay.IPayAbs;
import com.kk.pay.IPayCallback;
import com.kk.pay.OrderInfo;
import com.kk.pay.OrderParamsInfo;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.utils.TaskUtil;
import com.yc.wzmhk.R;
import com.yc.wzmhk.domain.Config;
import com.yc.wzmhk.domain.GoodInfo;
import com.yc.wzmhk.domain.PayWayInfo;
import com.yc.wzmhk.domain.PaywayListInfo;
import com.yc.wzmhk.engin.PaywayListEngin;
import com.yc.wzmhk.ui.BasePopupWindow;
import com.yc.wzmhk.ui.MainActivity;
import com.yc.wzmhk.ui.RewardPopupWindow;
import com.yc.wzmhk.ui.WxGZPopupWindow;
import com.yc.wzmhk.utils.LogUtil;
import com.yc.wzmhk.utils.PreferenceUtil;
import com.yc.wzmhk.utils.UIUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zhangkai on 2017/10/24.
 */

public class PayHelper {
    private ImageView ivAliPay;
    private ImageView ivWxPay;
    private ImageView ivWxGz;

    private TextView tvMoney;

    private int payType = Config.ALIPAY;

    private IPayAbs iPayAbs;

    private GoodInfo goodInfo;
    private GoodInfo vipGoodInfo;

    private String alipayWay;
    private String wxpayWay;
    private String money = "6.66元";

    private BasePopupWindow mBasePopupWindow;

    public PayHelper(final BasePopupWindow basePopupWindow) {
        this.mBasePopupWindow = basePopupWindow;

        final Activity context = basePopupWindow.getmContext();

        iPayAbs = new I1PayAbs(context);
        PaywayListEngin paywayListEngin = new PaywayListEngin(context);


        View contextView = basePopupWindow.getContentView();
        ivAliPay = (ImageView) contextView.findViewById(R.id.iv_alipay);
        ivWxPay = (ImageView) contextView.findViewById(R.id.iv_wxpay);
        ivWxGz = (ImageView) contextView.findViewById(R.id.iv_wxgz);

        tvMoney = (TextView) contextView.findViewById(R.id.tv_money);
        final TextView tvPay = (TextView) contextView.findViewById(R.id.tv_pay);

        ivAliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPay.setText("立即支付");
                tvMoney.setText(money);
                selectAlPay();
            }
        });

        ivWxPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPay.setText("立即支付");
                tvMoney.setText(money);
                selectWxPay();
            }
        });

        ivWxGz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPay.setText("立即关注");
                tvMoney.setText("0.00元");
                selectWxGz();
            }
        });


        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float price;
                String name;
                String goodId;
                if (basePopupWindow.getVipType() == Config.VIP) {
                    price = Float.parseFloat(vipGoodInfo.getReal_price());
                    name = vipGoodInfo.getAlias();
                    goodId = vipGoodInfo.getId() + "";
                } else if (basePopupWindow.getVipType() == Config.GOODS) {
                    name = goodInfo.getAlias();
                    goodId = goodInfo.getId() + "";
                    price = Float.parseFloat(goodInfo.getReal_price());
                } else if (basePopupWindow.getVipType() == Config.REWARD) {
                    price = ((RewardPopupWindow) basePopupWindow).getRewardPrice();
                    name = "打赏";
                    goodId = Config.REWARD + "";
                } else {
                    Toast.makeText(context, "支付类型有误请重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                OrderParamsInfo orderParamsInfo = new OrderParamsInfo(Config.ORDER_URL, goodId,
                        basePopupWindow.getVipType() + "",
                        price, name);
                if (basePopupWindow.getVipType() == Config.REWARD) {
                    orderParamsInfo.setDsMoney(((RewardPopupWindow) basePopupWindow).getRewardPrice() + "");
                }

                if (payType == Config.ALIPAY) {
                    orderParamsInfo.setPayway_name(alipayWay);
                } else if (payType == Config.WXPAY) {
                    orderParamsInfo.setPayway_name(wxpayWay);
                } else if (payType == Config.WXGZ) {

                    mBasePopupWindow.dismiss();
                    WxGZPopupWindow wxGZPopupWindow = new WxGZPopupWindow(context);
                    wxGZPopupWindow.show(context.getWindow().getDecorView().getRootView());
                    return;
                }
                iPayAbs.pay(orderParamsInfo, callback);


            }
        });

        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {

                String data = PreferenceUtil.getImpl(basePopupWindow.getmContext()).getString(Config.PAY_WAY_LIST_URL, "");
                if (!data.isEmpty()) {
                    try {
                        final ResultInfo<PaywayListInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<PaywayListInfo>>() {
                        }.getType());
                        UIUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                setPayway(resultInfo);
                            }
                        });
                    } catch (Exception e) {
                        LogUtil.msg("getPaywayList本地缓存" + e);
                    }
                }
            }
        });

        paywayListEngin.getPaywayList().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ResultInfo<PaywayListInfo>>() {
            @Override
            public void call(final ResultInfo<PaywayListInfo> resultInfo) {
                if (resultInfo != null && resultInfo.data != null && resultInfo.data.getPayWayInfos() != null) {

                    TaskUtil.getImpl().runTask(new Runnable() {
                        @Override
                        public void run() {
                            PreferenceUtil.getImpl(mBasePopupWindow.getmContext()).putString(Config.PAY_WAY_LIST_URL, JSON
                                    .toJSONString
                                            (resultInfo));
                        }
                    });
                    setPayway(resultInfo);


                } else {
                    Toast.makeText(context, "获取支付方式失败，请重试", Toast.LENGTH_SHORT).show();
                    basePopupWindow.dismiss();
                }
            }
        });
    }

    private boolean hasWxPay = false;
    private boolean hasAliPay = false;

    private void setPayway(ResultInfo<PaywayListInfo> resultInfo) {

        for (PayWayInfo payWayInfo : resultInfo.data.getPayWayInfos()) {
            if (payWayInfo != null && payWayInfo.getName() != null) {
                String payway = payWayInfo.getName();
                String title = payWayInfo.getTitle();
                if ((payway.contains("wxpay") || title.contains("微信")) && !hasWxPay) {
                    hasWxPay = true;
                    wxpayWay = payway;
                } else if ((payway.contains("alipay") || title.contains("支付宝")) && !hasAliPay) {
                    hasAliPay = true;
                    alipayWay = payway;
                }
            }
        }
        if (wxpayWay == null || wxpayWay.isEmpty()) {
            selectAlPay();
            ivWxPay.setClickable(false);
            ivWxPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap
                    .no_wxpay));
        }
        if (alipayWay == null || alipayWay.isEmpty()) {
            selectWxPay();
            ivAliPay.setClickable(false);
            ivAliPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap
                    .no_alpay));
        }
    }


    private void selectWxPay() {
        if (payType == Config.WXPAY) return;

        payType = Config.WXPAY;
        if (hasAliPay) {
            ivAliPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.alipay_select));
        }
        if (hasWxPay) {
            ivWxPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.wxpay_select_hover));
        }
        ivWxGz.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.wxpay_gz_select));

    }

    private void selectWxGz() {
        if (payType == Config.WXGZ) return;

        payType = Config.WXGZ;
        ivWxGz.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.wxpay_gz_select_hover));
        if (hasWxPay) {
            ivWxPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.wxpay_select));
        }
        if (hasAliPay) {
            ivAliPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.alipay_select));
        }
    }

    private void selectAlPay() {
        if (payType == Config.ALIPAY) return;

        payType = Config.ALIPAY;
        if (hasAliPay) {
            ivAliPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.alipay_select_hover));
        }
        if (hasWxPay) {
            ivWxPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.wxpay_select));
        }
        ivWxGz.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.wxpay_gz_select));

    }

    //< 支付回调
    final IPayCallback callback = new IPayCallback() {
        @Override
        public void onSuccess(final OrderInfo orderInfo) {
            final Activity mContext = mBasePopupWindow.getmContext();
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBasePopupWindow.getVipType() == Config.VIP) {
                        PreferenceUtil.getImpl(mContext).putString(MainActivity.VIP, MainActivity.VIP);
                    } else if (mBasePopupWindow.getVipType() == Config.GOODS) {
                        MainActivity.getMainActivity().saveVip(goodInfo.getIcon());
                    }
                    ((MainActivity) mContext).notifyDataSetChanged();
                    Toast.makeText(mContext, orderInfo.getMessage(), Toast.LENGTH_SHORT).show();
                    mBasePopupWindow.dismiss();
                }
            });
        }

        @Override
        public void onFailure(final OrderInfo orderInfo) {
            final Activity mContext = mBasePopupWindow.getmContext();
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, orderInfo.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    public void setInfo(GoodInfo goodInfo, GoodInfo vipGoodInfo) {
        this.goodInfo = goodInfo;
        this.vipGoodInfo = vipGoodInfo;
        tvMoney.setText(vipGoodInfo.getReal_price() + "元");
        money = tvMoney.getText().toString();
    }

    public void setMoney(String price) {
        tvMoney.setText(price);
        money = tvMoney.getText().toString();
    }
}
