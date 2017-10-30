package com.yc.wzjnk.helper;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
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
import com.yc.wzjnk.R;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.domain.GoodInfo;
import com.yc.wzjnk.domain.PayWayInfo;
import com.yc.wzjnk.domain.PaywayListInfo;
import com.yc.wzjnk.domain.TypeListInfo;
import com.yc.wzjnk.engin.PaywayListEngin;
import com.yc.wzjnk.ui.BasePopupWindow;
import com.yc.wzjnk.ui.MainActivity;
import com.yc.wzjnk.ui.RewardPopupWindow;
import com.yc.wzjnk.utils.PreferenceUtil;
import com.yc.wzjnk.utils.UIUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zhangkai on 2017/10/24.
 */

public class PayHelper {
    private ImageView ivAliPay;
    private ImageView ivWxPay;

    private TextView tvMoney;
    private TextView tvPay;

    private int payType = Config.ALIPAY;

    private IPayAbs iPayAbs;
    private PaywayListEngin paywayListEngin;

    private GoodInfo goodInfo;
    private GoodInfo vipGoodInfo;

    private String alipayWay;
    private String wxpayWay;

    private BasePopupWindow mBasePopupWindow;

    public PayHelper(final BasePopupWindow basePopupWindow) {
        this.mBasePopupWindow = basePopupWindow;

        final Activity context = basePopupWindow.getmContext();

        iPayAbs = new I1PayAbs(context);
        paywayListEngin = new PaywayListEngin(context);


        View contextView = basePopupWindow.getContentView();
        ivAliPay = (ImageView) contextView.findViewById(R.id.iv_alipay);
        ivWxPay = (ImageView) contextView.findViewById(R.id.iv_wxpay);

        tvMoney = (TextView) contextView.findViewById(R.id.tv_money);
        tvPay = (TextView) contextView.findViewById(R.id.tv_pay);

        ivAliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAlPay();
            }
        });

        ivWxPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectWxPay();
            }
        });


        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float price = 0.f;
                String name = "";
                String goodId = "";
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
                }
                iPayAbs.pay(orderParamsInfo, callback);


            }
        });

        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                String data = PreferenceUtil.getImpl(basePopupWindow.getmContext()).getString(Config.PAY_WAY_LIST_URL, "");
                if (!data.isEmpty()) {
                    final ResultInfo<PaywayListInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<PaywayListInfo>>() {
                    }.getType());
                    UIUtil.post(new Runnable() {
                        @Override
                        public void run() {
                            setPayway(resultInfo);
                        }
                    });
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
                            PreferenceUtil.getImpl(mBasePopupWindow.getmContext()).putString(Config.PAY_WAY_LIST_URL , JSON
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

    private void setPayway(ResultInfo<PaywayListInfo> resultInfo){
        boolean hasWxPay = false;
        boolean hasAliPay = false;
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
        if(payType == Config.WXPAY) return;

        payType = Config.WXPAY;
        ivWxPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.wxpay_select_hover));
        ivAliPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.alipay_select));
    }

    private void selectAlPay() {
        if(payType == Config.ALIPAY) return;

        payType = Config.ALIPAY;
        ivAliPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.alipay_select_hover));
        ivWxPay.setImageDrawable(ContextCompat.getDrawable(mBasePopupWindow.getmContext(), R.mipmap.wxpay_select));
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
                        String goods = PreferenceUtil.getImpl(mContext).getString(MainActivity.GOODS, "");
                        goods += "," + goodInfo.getIcon();
                        PreferenceUtil.getImpl(mContext).putString(MainActivity.GOODS, goods);
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
    }

    public void setMoney(String price) {
        tvMoney.setText(price);
    }
}
