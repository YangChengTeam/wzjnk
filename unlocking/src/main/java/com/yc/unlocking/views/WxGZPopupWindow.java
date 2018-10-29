package com.yc.unlocking.views;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.yc.unlocking.R;
import com.yc.unlocking.utils.AppUtil;


/**
 * Created by zhangkai on 2017/7/28.
 */

public class WxGZPopupWindow extends BasePopupWindow {

    private TextView mTitleTextView;
    private TextView mDespTextView;
    private TextView mWxNameTextView;
    private TextView mWxNmae2TextView;

    public WxGZPopupWindow(final Activity context) {
        super(context);

        View contextView = getContentView();

        TextView tvOpenWx = (TextView) contextView.findViewById(R.id.tv_open_wx);

        mTitleTextView = (TextView) contextView.findViewById(R.id.tv_unloing_title);
        mDespTextView = (TextView) contextView.findViewById(R.id.tv_unloing_desc);
        mWxNameTextView = (TextView) contextView.findViewById(R.id.tv_wx_name);
        mWxNmae2TextView = (TextView) contextView.findViewById(R.id.tv_wx_name2);

        tvOpenWx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public WxGZPopupWindow setInfo(String title, String wxname) {
        AppUtil.copy(getContext(), wxname);
        mTitleTextView.setText(title);
        mWxNameTextView.setText(wxname);
        mWxNmae2TextView.setText(wxname);
        mDespTextView.setText(wxname + "已经复制到剪切板\n, 关注公众号，解锁方法");
        return this;
    }


    @Override
    public int getLayoutID() {
        return R.layout.unlocking_ppw_wx_gz;
    }
}
