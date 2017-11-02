package com.yc.wzjnk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kk.loading.LoadingDialog;
import com.kk.pay.IPayImpl;
import com.kk.pay.NavgationBarUtils;
import com.kk.utils.ToastUtil;
import com.yc.wzjnk.R;


/**
 * Created by zhangkai on 2017/7/28.
 */

public class WebPopupWindow extends BasePopupWindow {

    private LoadingDialog loadingDialog;

    public WebPopupWindow(Activity context, final String url) {
        super(context);
        loadingDialog = new LoadingDialog(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View contextView = inflater.inflate(com.kk.pay.R.layout.ppw_web, null);

        WebView webView = (WebView) contextView.findViewById(com.kk.pay.R.id.wv_pay);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 4.4.4; zh-CN; HTC D820u Build/KTU84P) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 UCBrowser/10.1.0.527 U3/0.8.0 Mobile Safari/534.30");
        webSettings.setLoadsImagesAutomatically(false);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setNeedInitialFocus(false);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);

        if ((!Build.MANUFACTURER.toLowerCase().contains("xiaomi")) && (Build.MANUFACTURER.toLowerCase().contains("huawei"))) {

        }
        if ((Build.VERSION.SDK_INT >= 11) && (Build.MANUFACTURER.toLowerCase().contains("lenovo")))
            webView.setLayerType(1, null);

        loadingDialog.show("正在打开微信公众号...");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // 如下方案可在非微信内部WebView的H5页面中调出微信支付
                super.onPageStarted(view, url, favicon);
                openWxpay(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 如下方案可在非微信内部WebView的H5页面中调出微信支付
                if (openWxpay(url)) {
                }  else {
                    view.loadUrl(url);
                }
                return true;
            }
        });


        webView.loadUrl(url);
    }


    private boolean openWxpay(String url) {
        if (url.startsWith("weixin://")) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                MainActivity.getMainActivity().fixOpenwx();
            }
            dismiss();
            return true;
        }
        return false;
    }


    @Override
    public int getLayoutID() {
        return R.layout.ppw_web;
    }

    @Override
    public void dismiss() {
        loadingDialog.dismiss();
        super.dismiss();
    }

}
