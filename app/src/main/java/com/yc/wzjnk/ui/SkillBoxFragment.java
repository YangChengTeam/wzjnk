package com.yc.wzjnk.ui;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kk.loading.LoadingDialog;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.utils.TaskUtil;
import com.shizhefei.fragment.LazyFragment;
import com.umeng.analytics.MobclickAgent;
import com.yc.wzjnk.App;
import com.yc.wzjnk.R;
import com.yc.wzjnk.adpater.SkillBoxInfoAdpater;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.domain.GoodInfo;
import com.yc.wzjnk.domain.GoodListInfo;
import com.yc.wzjnk.engin.Good2Engin;
import com.yc.wzjnk.engin.GoodEngin;
import com.yc.wzjnk.helper.ImageHelper;
import com.yc.wzjnk.utils.PreferenceUtil;
import com.yc.wzjnk.utils.UIUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zhangkai on 2017/10/26.
 */

public class SkillBoxFragment extends LazyFragment {

    private GridView gdInfo;
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
        gdInfo = (GridView) findViewById(R.id.gd_info);
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
                    if (MainActivity.getMainActivity().checkPermission(Manifest.permission
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
                    if (MainActivity.getMainActivity().checkPermission(Manifest.permission
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
                    final ResultInfo<GoodListInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<GoodListInfo>>() {
                    }.getType());
                    UIUtil.post(new Runnable() {
                        @Override
                        public void run() {
                            getGoodsInfo(resultInfo);
                        }
                    });
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
                    final ResultInfo<GoodListInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<GoodListInfo>>() {
                    }.getType());
                    UIUtil.post(new Runnable() {
                        @Override
                        public void run() {
                            infoAdpater.dataInfos = resultInfo.data.getGoodInfoList();
                            infoAdpater.notifyDataSetChanged();
                        }
                    });
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
            if (info == null || mainActivity.getVipGoodInfo() == null) {
                Toast.makeText(mainActivity, "技能框信息初始化有误", Toast.LENGTH_LONG).show();
                MainActivity.getMainActivity().notifyDataSetChanged();
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
        ImagePopupWindow imagePopupWindow = new ImagePopupWindow(mainActivity, info.getIcon());
        imagePopupWindow.show(mainActivity.getWindow().getDecorView().getRootView());
    }

}
