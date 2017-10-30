package com.yc.wzjnk.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.kk.pay.I1PayAbs;
import com.kk.pay.IPayAbs;
import com.kk.pay.IPayCallback;
import com.kk.pay.OrderInfo;
import com.kk.pay.OrderParamsInfo;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.utils.ToastUtil;
import com.yc.wzjnk.R;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.domain.InitInfo;
import com.yc.wzjnk.domain.PayWayInfo;
import com.yc.wzjnk.domain.PaywayListInfo;
import com.yc.wzjnk.engin.PaywayListEngin;
import com.yc.wzjnk.helper.PayHelper;
import com.yc.wzjnk.utils.NavgationBarUtils;

import java.util.Random;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


/**
 * Created by zhangkai on 2017/7/28.
 */

public class RewardPopupWindow extends BasePopupWindow {


    private TextView tvRewardMoney;
    private TextView tvRandomPrice;
    private EditText etPrice;

    private float rewardPrice = 6.66f;

    public float getRewardPrice() {
        return rewardPrice;
    }

    private PayHelper payHelper;


    public RewardPopupWindow(Activity context) {
        super(context);
        vipType = Config.REWARD;
        payHelper = new PayHelper(this);

        View contextView = getContentView();

        tvRewardMoney = (TextView) contextView.findViewById(R.id.tv_reward_money);
        tvRandomPrice = (TextView) contextView.findViewById(R.id.tv_random_price);
        etPrice = (EditText) contextView.findViewById(R.id.et_money);

        tvRandomPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float[] prices = new float[]{6.66f, 8.88f, 18.8f, 5.6f, 1.88f, 2.88f, 3.88f, 4.88f, 6.18f, 7.88f};
                Random random = new Random();
                rewardPrice = prices[random.nextInt(prices.length)];
                tvRewardMoney.setText(rewardPrice + "元");
                payHelper.setMoney(rewardPrice + "元");
            }
        });

        etPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        rewardPrice = Integer.parseInt(s.toString());
                        if (rewardPrice > 888) {
                            ToastUtil.toast(mContext, "输入的金额不符合要求");
                            return;
                        }
                        tvRewardMoney.setText(rewardPrice + "元");
                        payHelper.setMoney(rewardPrice + "元");
                    } catch (Exception e) {
                        ToastUtil.toast(mContext, "输入的金额不符合要求");
                        return;
                    }
                }
            }
        });

    }

    @Override
    public int getLayoutID() {
        return R.layout.ppw_reward;
    }


}
