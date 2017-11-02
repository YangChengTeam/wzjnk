package com.yc.wzjnk.ui;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kk.utils.ToastUtil;
import com.yc.wzjnk.R;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.helper.PayHelper;

import java.util.Random;


/**
 * Created by zhangkai on 2017/7/28.
 */

public class RewardPopupWindow extends BasePopupWindow {


    private TextView tvRewardMoney;
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
        TextView tvRandomPrice = (TextView) contextView.findViewById(R.id.tv_random_price);
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
                            etPrice.setText(s.subSequence(0, s.length() - 1));
                            ToastUtil.toast2(mContext, "输入的金额不符合要求");
                            return;
                        }
                        tvRewardMoney.setText(rewardPrice + "元");
                        payHelper.setMoney(rewardPrice + "元");
                    } catch (Exception e) {
                        ToastUtil.toast(mContext, "输入的金额不符合要求");
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
