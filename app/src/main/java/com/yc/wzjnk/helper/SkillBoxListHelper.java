package com.yc.wzjnk.helper;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kk.securityhttp.domain.ResultInfo;
import com.kk.securityhttp.net.contains.HttpConfig;
import com.kk.utils.TaskUtil;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;
import com.yc.wzjnk.R;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.domain.TypeInfo;
import com.yc.wzjnk.domain.TypeListInfo;
import com.yc.wzjnk.engin.TypeEngin;
import com.yc.wzjnk.ui.MainActivity;
import com.yc.wzjnk.ui.SkillBoxFragment;
import com.yc.wzjnk.utils.PreferenceUtil;
import com.yc.wzjnk.utils.UIUtil;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zhangkai on 2017/10/31.
 */

public class SkillBoxListHelper {
    private Activity mConetxt;
    private TypeEngin typeEngin;
    private ScrollIndicatorView scrollIndicatorView;
    private ViewPager mViewPager;
    private List<SkillBoxFragment> skillBoxFragments;


    public SkillBoxListHelper(Activity conetxt) {
        this.mConetxt = conetxt;
        typeEngin = new TypeEngin(conetxt);

        scrollIndicatorView = (ScrollIndicatorView) conetxt.findViewById(R.id.fiv_indicator);
        mViewPager = (ViewPager) conetxt.findViewById(R.id.viewpager);

        scrollIndicatorView.setScrollBar(new ColorBar(conetxt, Color.TRANSPARENT, 1));
        float unSelectSize = 15;
        float selectSize = 15;
        int selectColor = Color.WHITE;
        int unSelectColor = Color.parseColor("#8c9caf");
        scrollIndicatorView.setOnTransitionListener(new OnTransitionTextListener().setColor(selectColor, unSelectColor).setSize(selectSize, unSelectSize));
        scrollIndicatorView.setOnIndicatorItemClickListener(new Indicator.OnIndicatorItemClickListener() {
            @Override
            public boolean onItemClick(View clickItemView, int position) {
                mViewPager.setCurrentItem(position);
                return false;
            }
        });
        scrollIndicatorView.setCurrentItem(0, true);
    }


    public void getTypeInfo(final ResultInfo<TypeListInfo> resultInfo) {
        if (resultInfo != null && resultInfo.code == HttpConfig.STATUS_OK && resultInfo
                .data != null && resultInfo.data.getList() != null) {
            if (skillBoxFragments != null && skillBoxFragments.size() == resultInfo.data.getList().size() + 1) {
                notifyDataSetChanged();
            } else {
                initFragments(resultInfo.data.getList());
            }
        }
    }

    public void getTypeInfo() {
        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                String data = PreferenceUtil.getImpl(mConetxt).getString(Config.TYPE_LIST_URL, "");
                if (!data.isEmpty()) {
                    final ResultInfo<TypeListInfo> resultInfo = JSON.parseObject(data, new TypeReference<ResultInfo<TypeListInfo>>() {
                    }.getType());
                    UIUtil.post(new Runnable() {
                        @Override
                        public void run() {
                            getTypeInfo(resultInfo);
                        }
                    });
                }
            }
        });
        typeEngin.getTypeList().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ResultInfo<TypeListInfo>>() {
            @Override
            public void call(final ResultInfo<TypeListInfo> resultInfo) {
                if (resultInfo != null && resultInfo.code == HttpConfig.STATUS_OK && resultInfo
                        .data != null && resultInfo.data.getList() != null) {
                    TaskUtil.getImpl().runTask(new Runnable() {
                        @Override
                        public void run() {
                            PreferenceUtil.getImpl(mConetxt).putString(Config.TYPE_LIST_URL, JSON.toJSONString
                                    (resultInfo));
                        }
                    });
                    getTypeInfo(resultInfo);
                }
            }
        });
    }

    //更新列表
    public void notifyDataSetChanged() {
        if (skillBoxFragments == null) return;
        for (SkillBoxFragment skillBoxFragment : skillBoxFragments) {
            skillBoxFragment.notifyDataSetChanged();
        }
    }

    public void refreshData() {
        if (skillBoxFragments == null) return;
        for (SkillBoxFragment skillBoxFragment : skillBoxFragments) {
            skillBoxFragment.loadData();
        }
    }

    private void initFragments(List<TypeInfo> typeInfos) {
        String[] titles = new String[1 + typeInfos.size()];
        String[] types = new String[1 + typeInfos.size()];
        titles[0] = "全部";
        types[0] = "";
        for (int i = 0; i < typeInfos.size(); i++) {
            titles[i + 1] = typeInfos.get(i).getName();
            types[i + 1] = typeInfos.get(i).getId();
        }
        scrollIndicatorView.setAdapter(new MyAdapter(mConetxt, titles));
        MyFragmentAdapter mFragmentAdapter = new MyFragmentAdapter(((FragmentActivity) mConetxt)
                .getSupportFragmentManager(),
                types);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                scrollIndicatorView.setCurrentItem(i, true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    class MyAdapter extends Indicator.IndicatorAdapter {
        private Activity mContext;
        private String[] mTitles;

        public MyAdapter(Activity context, String[] titles) {
            super();
            this.mContext = context;
            this.mTitles = titles;
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.view_tab, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(mTitles[position]);
            return convertView;
        }
    }

    class MyFragmentAdapter extends FragmentStatePagerAdapter {
        private int count;
        private String[] types;

        public MyFragmentAdapter(FragmentManager fm, String[] types) {
            super(fm);
            this.types = types;
            count = types.length;
            createFragments();
        }

        private void createFragments() {
            skillBoxFragments = new ArrayList<>();
            for (String type : types) {
                SkillBoxFragment skillBoxFragment = new SkillBoxFragment();
                skillBoxFragment.setType(type);
                skillBoxFragments.add(skillBoxFragment);
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (skillBoxFragments == null || position >= skillBoxFragments.size() || skillBoxFragments.get(position) ==
                    null) {
                createFragments();
            }
            return skillBoxFragments.get(position);
        }

        @Override
        public int getCount() {
            return count;
        }
    }
}
