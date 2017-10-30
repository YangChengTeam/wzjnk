package com.yc.wzjnk.adpater;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yc.wzjnk.R;
import com.yc.wzjnk.domain.Config;
import com.yc.wzjnk.domain.GoodInfo;
import com.yc.wzjnk.helper.ImageHelper;
import com.yc.wzjnk.ui.MainActivity;
import com.yc.wzjnk.utils.PreferenceUtil;

import java.util.List;


/**
 * Created by zhangkai on 2017/10/17.
 */

public class SkillBoxInfoAdpater extends BaseAdapter {

    public List<GoodInfo> dataInfos;
    private Context mContext;
    private LayoutInflater inflater;
    private ImageHelper imageUtil;
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public SkillBoxInfoAdpater(Context context, List<GoodInfo> dataInfos) {
        this.mContext = context;
        this.dataInfos = dataInfos;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageUtil = new ImageHelper(mContext);
    }

    @Override
    public int getCount() {
        if (type.equals("")) {
            return dataInfos != null ? dataInfos.size() - 1 : 0;
        } else {
            return dataInfos != null ? dataInfos.size() : 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return dataInfos != null ? dataInfos.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_info, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.title);
            viewHolder.ivFree = (ImageView) convertView.findViewById(R.id.free);
            viewHolder.btnPreview = (Button) convertView.findViewById(R.id.btn_preview);
            viewHolder.btnUse = (Button) convertView.findViewById(R.id.btn_use);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final GoodInfo info = dataInfos.get(position);
        viewHolder.tvTitle.setText(info.getTitle());
        viewHolder.btnUse.setTag(info);
        viewHolder.btnPreview.setTag(info);

        viewHolder.btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onUse(v);
            }
        });

        viewHolder.btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onPreview(v);
            }
        });

        MainActivity mainActivity = (MainActivity) mContext;
        if (info.is_free() || mainActivity.isPay(info.getIcon()) || mainActivity.isVip() || mainActivity.isFree(info
                .getId())) {
            viewHolder.ivFree.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.free));
        } else {
            viewHolder.ivFree.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.charge));
        }

        String currentInfo = PreferenceUtil.getImpl(mContext).getString(MainActivity.CURRENT_INFO, Config.DEFAULT_ICON);
        if (currentInfo.equals(info.getIcon())) {
            viewHolder.btnUse.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn6_selector));
            viewHolder.btnUse.setText("使用中");
        } else if (currentInfo.equals(Config.DEFAULT_ICON) && position == 0) {
            viewHolder.btnUse.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn6_selector));
            viewHolder.btnUse.setText("使用中");
        } else {
            viewHolder.btnUse.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn5_selector));
            viewHolder.btnUse.setText("使用");
        }

        imageUtil.showImage(mContext, viewHolder.ivIcon, info.getIcon(), 10, info);
        return convertView;
    }

    class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        ImageView ivFree;
        Button btnUse;
        Button btnPreview;
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onUse(View view);

        void onPreview(View view);
    }
}
