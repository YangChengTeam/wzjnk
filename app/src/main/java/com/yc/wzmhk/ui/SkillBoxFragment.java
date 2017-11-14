package com.yc.wzmhk.ui;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kk.loading.LoadingDialog;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.utils.TaskUtil;
import com.shizhefei.fragment.LazyFragment;
import com.umeng.analytics.MobclickAgent;
import com.yc.wzmhk.App;
import com.yc.wzmhk.R;
import com.yc.wzmhk.adpater.SkillBoxInfoAdpater;
import com.yc.wzmhk.domain.Config;
import com.yc.wzmhk.domain.GoodInfo;
import com.yc.wzmhk.domain.GoodListInfo;
import com.yc.wzmhk.engin.Good2Engin;
import com.yc.wzmhk.engin.GoodEngin;
import com.yc.wzmhk.helper.ImageHelper;
import com.yc.wzmhk.utils.AppUtil;
import com.yc.wzmhk.utils.LogUtil;
import com.yc.wzmhk.utils.PreferenceUtil;
import com.yc.wzmhk.utils.UIUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zhangkai on 2017/10/26.
 */

public class SkillBoxFragment extends LazyFragment {

    private SkillBoxInfoAdpater infoAdpater;

    private LoadingDialog loadingDialog;
    private ImageHelper imageHelper;

    private GoodEngin goodEngin;
    private Good2Engin good2Engin;

    private String type = "";

    public void setType(String type) {
        this.type = type;
    }


    public void loadData() {
        if (type.equals("")) {
            getGoodsInfo();
        } else {
            getGoodsInfo(type);
        }
    }

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_skillbox);
        GridView gdInfo = (GridView) findViewById(R.id.gd_info);
        goodEngin = new GoodEngin(getActivity());
        good2Engin = new Good2Engin(getActivity());

        infoAdpater = new SkillBoxInfoAdpater(getActivity(), null);
        infoAdpater.setType(type);
        gdInfo.setAdapter(infoAdpater);
        imageHelper = new ImageHelper(getActivity());
        loadingDialog = new LoadingDialog(getActivity());

        infoAdpater.setOnItemClickListener(new SkillBoxInfoAdpater.OnItemClickListener() {
            @Override
            public void onUse(View view) {
                App.playMp3();
                final GoodInfo info = (GoodInfo) view.getTag();
                if (!info.is_download()) {
                    if (AppUtil.checkPermission(getActivity(), Manifest.permission
                            .WRITE_EXTERNAL_STORAGE, "请允许授予储存卡写入权限，下载更多炫酷技能框")
                            ) {
                        MobclickAgent.onEvent(getActivity(), "king", "下载素材" + info.getTitle());
                        loadingDialog.show("正在下载素材，请稍后...");
                        imageHelper.downloadGifs(info);
                        imageHelper.setRunnable(new Runnable() {
                            @Override
                            public void run() {
                                info.setIs_download(true);
                                loadingDialog.dismiss();
                                use(info);
                            }
                        });
                    }
                } else {
                    use(info);
                }
            }

            @Override
            public void onPreview(View view) {
                App.playMp3();
                final GoodInfo info = (GoodInfo) view.getTag();
                if (!info.is_download()) {
                    if (AppUtil.checkPermission(getActivity(), Manifest.permission
                            .WRITE_EXTERNAL_STORAGE, "请允许授予储存卡写入权限，下载更多炫酷技能框")) {
                        MobclickAgent.onEvent(getActivity(), "king", "下载素材" + info.getTitle());
                        loadingDialog.show("正在下载素材，请稍后...");
                        imageHelper.downloadGifs(info);
                        imageHelper.setRunnable(new Runnable() {
                            @Override
                            public void run() {
                                info.setIs_download(true);
                                loadingDialog.dismiss();
                                preview(info);
                            }
                        });
                    }
                } else {
                    preview(info);
                }
            }
        });

        loadData();
    }

    public void getGoodsInfo(ResultInfo<GoodListInfo> resultInfo) {
        if (resultInfo != null && resultInfo.data != null && resultInfo.data.getGoodInfoList() != null) {
            infoAdpater.dataInfos = resultInfo.data.getGoodInfoList();
            if (infoAdpater.dataInfos.size() > 0) {
                MainActivity.getMainActivity().setVipGoodInfo(infoAdpater.dataInfos.get(infoAdpater.dataInfos
                        .size()
                        - 1));
            }
            infoAdpater.notifyDataSetChanged();
        }
    }

    public void getGoodsInfo() {
        if (goodEngin == null) return;

        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                String data = PreferenceUtil.getImpl(getActivity()).getString(Config.VIP_LIST_URL + type, "");
                if (!data.isEmpty()) {
                    try {
                        final ResultInfo<GoodListInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<GoodListInfo>>() {
                        }.getType());
                        UIUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                getGoodsInfo(resultInfo);
                            }
                        });
                    } catch (Exception e) {
                        LogUtil.msg("getGoodList本地缓存" + e);
                    }
                }
            }
        });

        goodEngin.getGoodList().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ResultInfo<GoodListInfo>>() {
            @Override
            public void call(final ResultInfo<GoodListInfo> resultInfo) {
                if (resultInfo != null && resultInfo.data != null && resultInfo.data.getGoodInfoList() != null) {
                    TaskUtil.getImpl().runTask(new Runnable() {
                        @Override
                        public void run() {
                            PreferenceUtil.getImpl(getActivity()).putString(Config.VIP_LIST_URL + type, JSON.toJSONString
                                    (resultInfo));
                        }
                    });
                    getGoodsInfo(resultInfo);
                }
            }
        });
    }

    public void getGoodsInfo(final String type) {
        if (good2Engin == null) return;

        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                String data = PreferenceUtil.getImpl(getActivity()).getString(Config.VIP_LIST2_URL + type, "");
                if (!data.isEmpty()) {
                    try {
                        final ResultInfo<GoodListInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<GoodListInfo>>() {
                        }.getType());
                        UIUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                infoAdpater.dataInfos = resultInfo.data.getGoodInfoList();
                                infoAdpater.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {
                        LogUtil.msg("getGoodList2本地缓存" + e);
                    }
                }
            }
        });
        good2Engin.getGoodList(type).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ResultInfo<GoodListInfo>>() {
            @Override
            public void call(final ResultInfo<GoodListInfo> resultInfo) {
                if (resultInfo != null && resultInfo.data != null && resultInfo.data.getGoodInfoList() != null) {
                    TaskUtil.getImpl().runTask(new Runnable() {
                        @Override
                        public void run() {
                            PreferenceUtil.getImpl(getActivity()).putString(Config.VIP_LIST2_URL + type, JSON.toJSONString
                                    (resultInfo));
                        }
                    });
                    infoAdpater.dataInfos = resultInfo.data.getGoodInfoList();
                    infoAdpater.notifyDataSetChanged();
                }
            }
        });
    }

    private void use(GoodInfo info) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (info.is_free() || mainActivity.isVip() || mainActivity.isPay(info.getIcon()) || mainActivity.isFree(info.getId())) {
            Toast.makeText(mainActivity, "正在使用" + info.getTitle() + "技能框", Toast.LENGTH_LONG).show();
            infoAdpater.notifyDataSetChanged();
            PreferenceUtil.getImpl(mainActivity).putString(mainActivity.CURRENT_INFO, info.getIcon());
            if (mainActivity.isOpen()) {
                mainActivity.updateSkillBoxView();
            }
        } else {
            if (mainActivity.isOpen()) {
                mainActivity.removeSkillBoxView();
            }
            if (mainActivity.getVipGoodInfo() == null) {
                Toast.makeText(mainActivity, "技能框信息初始化有误", Toast.LENGTH_LONG).show();
                mainActivity.notifyDataSetChanged();
                return;
            }
            PayPopupWindow payPopupWindow = new PayPopupWindow(mainActivity);
            payPopupWindow.setInfo(info, mainActivity.getVipGoodInfo());
            payPopupWindow.show(mainActivity.getWindow().getDecorView().getRootView());
            MobclickAgent.onEvent(getActivity(), "king", "打开支付窗口");
        }
    }

    //更新列表
    public void notifyDataSetChanged() {
        if (infoAdpater != null) {
            infoAdpater.notifyDataSetChanged();
        }
    }

    private void preview(GoodInfo info) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.isOpen()) {
            mainActivity.removeSkillBoxView();
        }
        ImagePopupWindow imagePopupWindow = new ImagePopupWindow(mainActivity, info.getIcon());
        imagePopupWindow.show(mainActivity.getWindow().getDecorView().getRootView());
    }

}
