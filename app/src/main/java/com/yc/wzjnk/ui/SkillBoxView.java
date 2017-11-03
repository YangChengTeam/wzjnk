package com.yc.wzjnk.ui;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yc.wzjnk.R;
import com.yc.wzjnk.helper.ImageHelper;
import com.yc.wzjnk.utils.LogUtil;
import com.yc.wzjnk.utils.ScreenUtil;
import com.yc.wzjnk.utils.UIUtil;

/**
 * Created by zhangkai on 2017/10/18.
 */

public class SkillBoxView {
    protected final String TAG = "SkillBoxView";

    private static SkillBoxView instance = null;

    private WindowManager.LayoutParams originParams;
    private WindowManager mWindowManager;

    private Context mContext;
    private LayoutInflater mInflater;

    private RelativeLayout mFloatLayout;
    private ImageView mFloatView;
    private int mFloatViewWidth;
    private boolean isClick = true;

    private ImageView imageSkillBoxView;
    private boolean isOpen = false;
    private ImageHelper imageUtil;


    public boolean isOpen() {
        return isOpen;
    }

    private int screenHeight;
    private int screenWidth;

    private int mSecondes = 0;
    private boolean animating = false;

    private boolean l = false, r = false, u = true, b = false;

    private SkillBoxView(Context context) {
        this.mContext = context;
        screenHeight = ScreenUtil.getHeight(context);
        screenWidth = ScreenUtil.getWidth(context);
        mFloatViewWidth = ScreenUtil.dip2px(context, 50);
        imageUtil = new ImageHelper(mContext);
        mInflater = LayoutInflater.from(context);
    }

    public synchronized static SkillBoxView getInstance(Context context) {
        if (instance == null) {
            instance = new SkillBoxView(context);
        }
        return instance;
    }

    private void resetMark() {
        l = false;
        r = false;
        u = false;
        b = false;
    }

    private void setParamsType(WindowManager.LayoutParams params) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void createFloatView() {
        if (mFloatView != null) return;

        if (mWindowManager == null) {
            if (originParams == null) {
                originParams = new WindowManager.LayoutParams();
                originParams.format = PixelFormat.RGBA_8888;
                originParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                originParams.gravity = Gravity.LEFT | Gravity.TOP;

                originParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                originParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                originParams.x = screenWidth / 2 - mFloatViewWidth / 2;
                originParams.y = 0;

                setParamsType(originParams);
            }

            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }

        mFloatLayout = (RelativeLayout) mInflater
                .inflate(R.layout.view_float, null);
        mWindowManager.addView(mFloatLayout, originParams);
        mFloatView = mFloatLayout.findViewById(R.id.iv_float);
        showOff();
        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                int x = (int) event.getRawX() - mFloatViewWidth / 2;
                int y = (int) event.getRawY() - mFloatViewWidth / 2;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mSecondes = 0;
                        animating = true;
                        if (!isClick) {
                            isClick = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isClick) {
                            int tempy = y;
                            int tempx = x;

                            float scaleX, scaleY;
                            resetMark();
                            if (tempy + mFloatViewWidth / 2 <= screenHeight / 2) {
                                scaleY = tempy / (float) screenHeight;
                                u = true;
                            } else {
                                scaleY = (screenHeight - tempy) / (float) screenHeight;
                                b = true;
                            }

                            if (tempx + mFloatViewWidth / 2 < screenWidth / 2) {
                                scaleX = tempx / (float) screenWidth;
                                l = true;
                            } else {
                                scaleX = (screenWidth - tempx) / (float) screenWidth;
                                r = true;
                            }

                            if (scaleX < scaleY) {
                                if (l) {
                                    resetMark();
                                    l = true;
                                    tempx = 0;
                                } else if (r) {
                                    tempx = screenWidth - mFloatViewWidth / 2;
                                    resetMark();
                                    r = true;
                                }

                            } else {
                                if (u) {
                                    tempy = 0;
                                    resetMark();
                                    u = true;
                                } else if (b) {
                                    tempy = screenHeight - mFloatViewWidth / 2;
                                    resetMark();
                                    b = true;
                                }
                            }

                            animate(x, tempx, y, tempy);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isOpen) {
                            showOn();
                        } else {
                            showOff();
                        }
                        mSecondes = 0;
                        animating = true;
                        if (!isClick || (Math.abs(originParams.x - x) > ScreenUtil.dip2px(mContext, 30)
                                || Math.abs(originParams.y - y) > ScreenUtil.dip2px(mContext, 30))) {
                            isClick = false;
                            originParams.x = x;
                            originParams.y = y;
                            mWindowManager.updateViewLayout(mFloatLayout, originParams);
                        }
                        break;
                }
                return false;
            }
        });
        mFloatView.setOnClickListener(onclick);
        hide();
    }

    public void hide() {
        mSecondes = 0;
        animating = false;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mFloatView == null) {
                    mSecondes = 0;
                    return;
                }

                if (animating) {
                    mSecondes = 0;
                    return;
                }

                if (++mSecondes >= 3) {
                    showHide();
                    return;
                }
                UIUtil.postDelayed(1000, this);
            }
        };

        if (mFloatView != null) {
            UIUtil.postDelayed(1000, runnable);
        }
    }

    public void showHide() {
        if (l) {
            mFloatView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.float_left));
        } else if (u) {
            mFloatView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.float_up));
        } else if (b) {
            mFloatView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.float_down));
        } else if (r) {
            mFloatView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.float_right));
        }

    }

    private void animate(int x1, int x2, int y1, int y2) {
        PropertyValuesHolder mPropertyValuesX = PropertyValuesHolder.ofInt("x", x1, x2);
        PropertyValuesHolder mPropertyValuesY = PropertyValuesHolder.ofInt("y", y1, y2);
        ValueAnimator mAnimator = ValueAnimator.ofPropertyValuesHolder(mPropertyValuesX,
                mPropertyValuesY);
        mAnimator.setInterpolator(new BounceInterpolator());//使用线性插值器
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    int x = Integer.parseInt(animation.getAnimatedValue("x") + "");
                    int y = Integer.parseInt(animation.getAnimatedValue("y") + "");
                    originParams.x = x;
                    originParams.y = y;
                    mWindowManager.updateViewLayout(mFloatLayout, originParams);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animating = true;
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                hide();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.setDuration(1000);
        mAnimator.setTarget(mWindowManager);
        mAnimator.start();
    }

    private View.OnClickListener onclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFloatView != null && v.getId() == mFloatView.getId()) {
                if (isClick) {
                    isOpen = !isOpen;
                    if (isOpen) {
                        addSkillBoxView();
                        showOn();
                    } else {
                        removeSkillBoxView();
                        showOff();
                    }
                    hide();
                }
            }
        }
    };

    public void removeAllView() {
        if (mFloatView == null) return;
        try {
            mWindowManager.removeView(mFloatLayout);
            mFloatView = null;
            removeSkillBoxViewWithState();
        } catch (Exception e) {
            LogUtil.msg("removeAllView异常" + e);
        }
    }

    public void removeSkillBoxViewWithState() {
        isOpen = false;
        removeSkillBoxView();
    }


    public void removeSkillBoxView() {
        try {
            mWindowManager.removeView(imageSkillBoxView);
            imageSkillBoxView = null;
            imageUtil.recyleBimaps();
        } catch (Exception e) {
        }
    }

    public void showOff() {
        if (mFloatView == null) return;
        isOpen = false;
        mFloatView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.float_off));
    }

    public void showOn() {
        if (mFloatView == null) return;
        isOpen = true;
        mFloatView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.float_on));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addSkillBoxView() {
        if (imageSkillBoxView != null) return;
        if (mWindowManager == null) return;

        isOpen = true;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager
                                .LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
        setParamsType(params);
        try {
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
            ;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                params.systemUiVisibility = params.systemUiVisibility | View
                        .SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
        } catch (Exception e) {
        }
        imageSkillBoxView = new ImageView(mContext);
        imageUtil.showImage(mContext, imageSkillBoxView);
        mWindowManager.addView(imageSkillBoxView, params);
    }

    public void updateSkillBoxView() {
        imageUtil.recyleBimaps();
        imageUtil.showImage(mContext, imageSkillBoxView);
    }
}
