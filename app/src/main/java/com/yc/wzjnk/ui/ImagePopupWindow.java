package com.yc.wzjnk.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.yc.wzjnk.R;
import com.yc.wzjnk.helper.ImageHelper;
import com.yc.wzjnk.utils.NavgationBarUtils;


/**
 * Created by zhangkai on 2017/7/28.
 */

public class ImagePopupWindow extends BasePopupWindow {
    ImageHelper imageUtil;

    public ImagePopupWindow(Activity context, final String iconName) {
        super(context);
        View contextView = getContentView();

        imageUtil = new ImageHelper(mContext);

        ImageView previewImage = (ImageView) contextView.findViewById(R.id.iv_preview);
        previewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        imageUtil.showImage(mContext, previewImage, iconName);

    }

    @Override
    public void dismiss() {
        super.dismiss();
        imageUtil.recyleBimaps();
    }

    @Override
    public int getLayoutID() {
        return R.layout.ppw_image;
    }
}
