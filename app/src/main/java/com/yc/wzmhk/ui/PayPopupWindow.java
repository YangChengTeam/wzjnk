package com.yc.wzmhk.ui;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yc.wzmhk.R;
import com.yc.wzmhk.domain.Config;
import com.yc.wzmhk.domain.GoodInfo;
import com.yc.wzmhk.helper.PayHelper;


/**
 * Created by zhangkai on 2017/7/28.
 */

public class PayPopupWindow extends BasePopupWindow {
    private TextView tvGoodsPrice;
    private TextView tvGoodsTitle;

    private TextView tvVipPrice;


    private ImageView ivGoodsSelect;
    private ImageView ivVipSelect;

    private PayHelper payHelper;

    public PayPopupWindow(Activity context) {
        super(context);
        payHelper = new PayHelper(this);

        View contextView = getContentView();

        LinearLayout llGoods = (LinearLayout) contextView.findViewById(R.id.ll_goods);
        tvGoodsPrice = (TextView) contextView.findViewById(R.id.tv_goods_price);
        tvGoodsTitle = (TextView) contextView.findViewById(R.id.tv_goods_title);

        LinearLayout llVip = (LinearLayout) contextView.findViewById(R.id.ll_vip);
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
        MainActivity.getMainActivity().setCurrentGoodInfo(goodInfo);
        payHelper.setInfo(goodInfo, vipGoodInfo);
        tvGoodsPrice.setText(goodInfo.getReal_price() + "元");
        tvVipPrice.setText(vipGoodInfo.getReal_price() + "元");
        tvGoodsTitle.setText("购买" + goodInfo.getTitle() + "技能框");
    }
}
