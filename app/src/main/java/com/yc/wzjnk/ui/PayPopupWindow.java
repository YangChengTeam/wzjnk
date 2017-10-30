package com.yc.wzjnk.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.kk.pay.I1PayAbs;
import com.kk.pay.IPayAbs;
import com.kk.pay.IPayCallback;
import com.kk.pay.OrderInfo;
import com.kk.pay.OrderParamsInfo;
import com.kk.securityhttp.domain.ResultInfo;
import com.yc.wzjnk.R;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.domain.GoodInfo;
import com.yc.wzjnk.domain.InitInfo;
import com.yc.wzjnk.domain.PayWayInfo;
import com.yc.wzjnk.domain.PaywayListInfo;
import com.yc.wzjnk.engin.PaywayListEngin;
import com.yc.wzjnk.helper.PayHelper;
import com.yc.wzjnk.utils.NavgationBarUtils;
import com.yc.wzjnk.utils.PreferenceUtil;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


/**
 * Created by zhangkai on 2017/7/28.
 */

public class PayPopupWindow extends BasePopupWindow {
    private LinearLayout llGoods;
    private TextView tvGoodsPrice;
    private TextView tvGoodsTitle;

    private LinearLayout llVip;
    private TextView tvVipPrice;


    private ImageView ivGoodsSelect;
    private ImageView ivVipSelect;


    private PayHelper payHelper;

    public PayPopupWindow(Activity context) {
        super(context);
        payHelper = new PayHelper(this);

        View contextView = getContentView();

        llGoods = (LinearLayout) contextView.findViewById(R.id.ll_goods);
        tvGoodsPrice = (TextView) contextView.findViewById(R.id.tv_goods_price);
        tvGoodsTitle = (TextView) contextView.findViewById(R.id.tv_goods_title);

        llVip = (LinearLayout) contextView.findViewById(R.id.ll_vip);
        tvVipPrice = (TextView) contextView.findViewById(R.id.tv_vip_price);

        ivGoodsSelect = (ImageView) contextView.findViewById(R.id.iv_good_select);
        ivVipSelect = (ImageView) contextView.findViewById(R.id.iv_vip_select);

        llVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vipType = Config.VIP;
                ivVipSelect.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.vip_select_hover));
                ivGoodsSelect.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.vip_select));
                payHelper.setMoney(tvVipPrice.getText().toString());

            }
        });

        llGoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vipType = Config.GOODS;
                ivGoodsSelect.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.vip_select_hover));
                ivVipSelect.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.vip_select));
                payHelper.setMoney(tvGoodsPrice.getText().toString());
            }
        });

    }

    @Override
    public int getLayoutID() {
        return R.layout.ppw_pay;
    }


    public void setInfo(GoodInfo goodInfo, GoodInfo vipGoodInfo) {
        payHelper.setInfo(goodInfo, vipGoodInfo);
        tvGoodsPrice.setText(goodInfo.getReal_price() + "元");
        tvVipPrice.setText(vipGoodInfo.getReal_price() + "元");
        tvGoodsTitle.setText("购买" + goodInfo.getTitle() + "技能框");
    }
}
