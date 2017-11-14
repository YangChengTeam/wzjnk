package com.yc.wzmhk.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.kk.utils.PathUtil;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.yc.wzmhk.domain.Config;
import com.yc.wzmhk.domain.GoodInfo;
import com.yc.wzmhk.ui.MainActivity;
import com.yc.wzmhk.utils.LogUtil;
import com.yc.wzmhk.utils.PreferenceUtil;
import com.yc.wzmhk.utils.ScreenUtil;
import com.yc.wzmhk.utils.TaskUtil;
import com.yc.wzmhk.utils.UIUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by zhangkai on 2017/10/23.
 */

public class ImageHelper {
    private String dir = "";

    public ImageHelper(Context context) {
        dir = PathUtil.createDir(context, "/images");
    }

    public String getIconIntName(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        int lastIndexs = url.lastIndexOf('/');
        int lastIndexe = url.lastIndexOf('.');
        if (lastIndexs != -1 && lastIndexe != -1) {
            return url.substring(lastIndexs + 1, lastIndexe);
        }
        return url;
    }

    public String getUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        int lastIndexs = url.lastIndexOf('/');
        if (lastIndexs != -1) {
            return url.substring(0, lastIndexs);
        }
        return url;
    }

    public void recyleBimaps() {
//        for (Bitmap bmp : pics) {
//            if (!bmp.isRecycled()) {
//                bmp.recycle();
//            }
//        }
        pics.clear();
    }

    public void showImage(final Context context, final ImageView imageView, final String iconName, final int
            sampleSize, final GoodInfo goodInfo) {
        showImage(context, imageView, iconName, sampleSize, false, goodInfo);
    }

    public void showImage(final Context context, final ImageView imageView) {
        String iconName = PreferenceUtil.getImpl(context).getString(MainActivity.CURRENT_INFO, Config.DEFAULT_ICON);
        showImage(context, imageView, iconName, 1, true, null);
    }

    public void showImage(final Context context, final ImageView imageView, final String iconName) {
        showImage(context, imageView, iconName, 1, true, null);
    }

    public void showImage(final Context context, final ImageView imageView, final String iconName, final int
            sampleSize, final boolean isplay, final GoodInfo goodInfo
    ) {
        idx = 0;
        this.icoName = iconName;
        TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                if (sampleSize > 1) {
                    show(context, imageView, goodInfo);
                    return;
                }

                if (!action(context, imageView, iconName, sampleSize, goodInfo)) {
                    UIUtil.post(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.with(context).load(iconName).into(imageView);
                        }
                    });
                }

                if (isplay) {
                    playGif(context, imageView);
                }
            }
        });
    }

    private boolean action(final Context context, final ImageView imageView, final String iconName, int sampleSize,
                           final GoodInfo goodInfo) {
        final Bitmap bmp = getBitmap(context, iconName, sampleSize, "");
        if (bmp != null) {
            UIUtil.post(new Runnable() {
                @Override
                public void run() {
                    if (imageView != null) {
                        imageView.setImageBitmap(bmp);
                    }
                }
            });
            pics.add(bmp);
            if (goodInfo != null) {
                goodInfo.setIs_download(true);
            }
            return true;
        }
        return false;
    }

    private String getShortName(String iconName, String step) {
        String intName = "26";
        if (!iconName.equals(Config.DEFAULT_ICON)) {
            intName = getIconIntName(iconName);
        }
        return intName + step + ".png";
    }

    private void show(final Context context, final ImageView imageView, final GoodInfo goodInfo) {
        final String iconName = goodInfo.getIcon();
        final String name = getShortName(iconName, "");
        UIUtil.post(new Runnable() {
            @Override
            public void run() {
                Picasso picasso = Picasso.with(context);
//                picasso.setIndicatorsEnabled(true);
                File file = new File(dir + "/" + name);
                if (file.exists()) {
                    picasso.load(file).config(Bitmap.Config.RGB_565).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).resize(ScreenUtil.dip2px(context, 115), ScreenUtil.dip2px
                            (context, 64)).into(imageView);
                    goodInfo.setIs_download(true);
                } else {
                    picasso.load(iconName).config(Bitmap.Config.RGB_565).memoryPolicy(MemoryPolicy.NO_CACHE,
                            MemoryPolicy.NO_STORE).resize(ScreenUtil.dip2px(context, 115), ScreenUtil.dip2px
                            (context, 64)).into(imageView);
                }
            }

        });
    }

    private Bitmap getBitmap(Context context, String iconName, int sampleSize, String step) {
        Bitmap bmp = null;
        String name = getShortName(iconName, step);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        File file = new File(dir + "/" + name);
        if (file.exists()) {
            bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
        return bmp;
    }

    private void writeImageToSDCard(ByteArrayOutputStream out, String name) {
        String tmpName = name;
        File file = new File(tmpName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            out.writeTo(fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.msg(tmpName + "->" + e.getMessage(), LogUtil.W);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.msg(tmpName + "->" + e.getMessage(), LogUtil.W);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Bitmap> pics = new ArrayList<>();
    private int n = 15;  //max pics
    private int idx = 0;
    private String icoName = "/26.png";

    private void playGif(final Context context, final ImageView imageView) {
        com.kk.utils.TaskUtil.getImpl().runTask(new Runnable() {
            @Override
            public void run() {
                final List<Integer> times = getTimes(icoName);
                for (int i = 2; i <= n; i++) {
                    Bitmap bmp = getBitmap(context, icoName, 1, "_" + i);
                    if (bmp != null) {
                        pics.add(bmp);
                    }
                }
                idx = 1;

                int size = pics.size();
                final int delay = 1000 / size;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (pics == null || pics.size() <= 1) return;

                        if (imageView == null) {
                            return;
                        }

                        if (idx >= pics.size()) {
                            idx = 0;
                        }


                        if (pics.get(idx) == null || pics.get(idx).isRecycled()) {
                            return;
                        }

                        int tdelay = delay;
                        if (idx < times.size()) {
                            tdelay = times.get(idx);
                        }
                        imageView.setImageBitmap(pics.get(idx++));
                        UIUtil.postDelayed(tdelay, this);
                    }
                };
                UIUtil.postDelayed(delay, runnable);
            }
        });
    }


    public void downloadGifs(GoodInfo goodInfo) {
        idx = 1;
        String iconName = goodInfo.getIcon();
        final String intName = getIconIntName(iconName);
        String url = getUrl(iconName);

        String fileName = dir + "/" + intName + ".time";
        File file = new File(fileName);
        if (!file.exists()) {
            download(url + "/" + intName + ".time", fileName);
        }

        fileName = dir + "/" + intName + ".png";
        file = new File(fileName);

        if (!file.exists()) {
            download(url + "/" + intName + ".png", fileName);
        } else {
            idx++;
        }
        for (int i = 2; i <= n; i++) {
            fileName = dir + "/" + intName + "_" + i + ".png";
            file = new File(fileName);
            if (!file.exists()) {
                download(url + "/" + intName + "_" + i + ".png", fileName);
            } else {
                idx++;
            }
        }
    }

    ///< 从文件获取字符串
    public String readInfo(File file) {
        if (file.exists()) {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                }
                br.close();
                return text.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<Integer> getTimes(String iconName) {
        List<Integer> integers = new ArrayList<>();
        String intName = "26";
        if (!iconName.equals(Config.DEFAULT_ICON)) {
            intName = getIconIntName(iconName);
        }
        String url = getUrl(iconName);
        String fileName = dir + "/" + intName + ".time";
        File file = new File(fileName);
        if (!file.exists()) {
            download(url + "/" + intName + ".time", fileName);
        }
        String timeStr = readInfo(file);
        if (timeStr == null || timeStr.isEmpty()) {
            return integers;
        }

        String[] times = timeStr.split(":");
        for (String time : times) {
            int mtime = 0;
            try {
                mtime = Integer.parseInt(time);
            } catch (Exception e) {
            }
            if (mtime != 0) {
                integers.add(mtime);
            }
        }
        return integers;
    }

    private Runnable runnable;

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    private void download(@NonNull final String imageUrl, final String path) {
        download(imageUrl, path, null);
    }

    @SuppressLint("StaticFieldLeak")
    private void download(@NonNull final String imageUrl, final String path, final Runnable skillRunnable) {
        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                HttpURLConnection connection = null;
                InputStream is = null;
                ByteArrayOutputStream out = null;
                try {
                    connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                    connection.connect();
                    final int length = connection.getContentLength();
                    if (length <= 0) {
                        this.cancel(true);
                    }
                    is = new BufferedInputStream(connection.getInputStream());
                    out = new ByteArrayOutputStream();
                    byte bytes[] = new byte[8192];
                    int count;
                    while ((count = is.read(bytes)) != -1) {
                        out.write(bytes, 0, count);
                    }
                    writeImageToSDCard(out, path);
                } catch (Throwable e) {
                    if (!this.isCancelled()) {
                        this.cancel(true);
                    }
                } finally {
                    idx++;
                    try {
                        if (connection != null)
                            connection.disconnect();
                        if (out != null) {
                            out.flush();
                            out.close();
                        }
                        if (is != null)
                            is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (idx >= n) {
                    if (runnable != null) {
                        UIUtil.post(runnable);
                        runnable = null;
                        idx = 1;
                    }
                }

                if (skillRunnable != null) {
                    skillRunnable.run();
                }
                return null;
            }
        }.executeOnExecutor(Executors.newFixedThreadPool(n));
    }
}
