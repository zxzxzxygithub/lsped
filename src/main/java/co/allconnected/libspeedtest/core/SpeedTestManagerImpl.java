package co.allconnected.libspeedtest.core;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.System.currentTimeMillis;

/**
 * Created by michael on 16/11/28.
 */

public class SpeedTestManagerImpl implements SpeedTestManager {

    private static final int mWifiCount = 20;

    private static final int INTERVAL = 500;

    private String sourceUrl1 = "http://speedtest.fremont.linode.com/100MB-fremont.bin";
    private String sourceUrl2 = "http://down.netspeedtestmaster.com/100mb.dat";
    private String sourceUrl3 = "http://cachefly.cachefly.net/100mb.test";
    private String sourceUrl4 = "http://" + "%s" + "/20mb.dat";

    private String speedStrategy = STRATEGY_MIXED;

    private static final String STRATEGY_DEFAULT = "default";
    private static final String STRATEGY_ONLY_SELF = "onlyself";
    private static final String STRATEGY_ONLY_SOURCE3 = "onlysource3";
    private static final String STRATEGY_MIXED = "mixed";

    private final long KB = 1024;
    private final long MB = 1024 * KB;
    private final long GB = 1024 * MB;
    private final long TB = 1024 * GB;

    private List<DownloadTask> mDownloadTaskList = new ArrayList<>();
    private List<Float> mSpeedList = new ArrayList<>();

    private OnDetectSpeedListener mListener;
    private Timer mTimer;
    private long mStartDownloadBytes;
    private long mStartTestTime;
    private int mCount;
    private long mRxBytes;
    private long mTxBytes;
    private boolean mRemoveCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper());


    private Runnable mGetSpeedsRunnable = new Runnable() {
        @Override
        public void run() {
            long totalRxBytes = TrafficStats.getTotalRxBytes();
            long downloadSpeed = totalRxBytes - mRxBytes;
            mListener.getDownLoadSpeed(downloadSpeed);
            long totalTxBytes = TrafficStats.getTotalTxBytes();
            long uploadSpeed = totalTxBytes - mTxBytes;
            mListener.getUpLoadSpeed(uploadSpeed);
            mRxBytes = TrafficStats.getTotalRxBytes();
            mTxBytes = TrafficStats.getTotalTxBytes();
            if (!mRemoveCallback) {
                mHandler.postDelayed(mGetSpeedsRunnable, 1000);
            }
        }
    };


    @Override
    public void startTest(OnDetectSpeedListener listener, String ip) {

        if (mRxBytes == 0) {
            mRxBytes = TrafficStats.getTotalRxBytes();
        }
        if (mTxBytes == 0) {
            mTxBytes = TrafficStats.getTotalTxBytes();
        }

        mListener = listener;
        mTimer = new Timer();
        for (int i = 0; i < 2; i++) {
            useSpeedTestStrategy(listener,ip);
        }
        mStartDownloadBytes = TrafficStats.getTotalRxBytes();
        mRemoveCallback = false;
        mHandler.postDelayed(mGetSpeedsRunnable, 1000);
        startTimer();
        mListener.onStart();

    }

    public void removeSpeedRunnable() {

        mHandler.removeCallbacks(mGetSpeedsRunnable);
    }

    @Override
    public void cancelTest() {

        stateReset();
        reset();
    }


    @Override
    public String getAvgSpeed() {
        return getTrafficString((long) getProcessedAvg(mSpeedList)) + "/s";
    }


    /**
     * 给流量数据加单位
     *
     * @author michael
     * @time 16/11/28 下午4:20
     */
    public String getTrafficString(long bytes) {
        if (bytes >= 10 * TB)
            return bytes / TB + "TB";
        else if (bytes > TB)
            return format(bytes / (float) TB) + "TB";
        else if (bytes >= 10 * GB)
            return bytes / GB + "GB";
        else if (bytes > GB)
            return format(bytes / (float) GB) + "GB";
        else if (bytes >= 10 * MB)
            return bytes / MB + "MB";
        else if (bytes > MB)
            return format(bytes / (float) MB) + "MB";
        else if (bytes >= 10 * KB)
            return bytes / KB + "KB";
        else
            return format(bytes / (float) KB) + "KB";
    }

    private float format(float value) {
        value = (float) (Math.round(value * 100)) / 100;
        return value;
    }


    private float getProcessedAvg(List<Float> list) {
        float sum = 0;
        for (Float data : list) {
            sum += data;
        }
        float avg = sum / (float) list.size();
        List<Float> newList = new ArrayList<>();
        for (Float data : list) {
            if (Math.abs(data - avg) / avg < 0.7) {
                newList.add(data);
            }
        }
        if (newList.size() == 0) {
            //抖动过大,返回第一次计算平均值
            return avg;
        }
        sum = 0;
        for (Float data : newList) {
            sum += data;
        }
        avg = sum / newList.size();
        return avg;
    }

    private class ObtainSpeedTask extends TimerTask {

        @Override
        public void run() {
            mCount++;
            long nowWifiDownloadBytes = TrafficStats.getTotalRxBytes();
            long currentTimeMillis = currentTimeMillis();
            long downloadInterval = currentTimeMillis - mStartTestTime;
            if (downloadInterval == 0) {
                downloadInterval = INTERVAL;
            }
            float speed = (nowWifiDownloadBytes - mStartDownloadBytes) * 1000 / downloadInterval;
            mStartDownloadBytes = nowWifiDownloadBytes;
            mStartTestTime = currentTimeMillis();
            mSpeedList.add(speed);
            if (mListener != null) {
                mListener.onProgress((float) mCount / (float) mWifiCount, speed);
            }
            if (mCount == mWifiCount) {
                cancelTest();
                float processedAvg = getProcessedAvg(mSpeedList);
                if (mListener != null) {
                    mListener.onFinished(processedAvg);
                }
            }
        }
    }

    public void startTimer() {
        int delay = 500;
        mTimer.schedule(new ObtainSpeedTask(), delay, INTERVAL);
    }


    private void shutDownTask() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = null;
        TaskManager.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mDownloadTaskList.size(); i++) {
                    mDownloadTaskList.get(i).disconnect();
                }
            }
        });

    }

    /**
     * 清理工作
     */
    private void stateReset() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        shutDownTask();
    }

    private void reset() {
        mStartDownloadBytes = 0;
        mCount = 0;
        mRxBytes = 0;
        mRxBytes = 0;
        mHandler.removeCallbacks(mGetSpeedsRunnable);
        mRemoveCallback = true;
    }

    private Map<String, String> formatTrafficForMap(int byteTraffic) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("#0");
        Map<String, String> map = new HashMap<>();

        if (byteTraffic <= 0) {
            map.put("data", "0");
            map.put("unit", "kB/s");
            return map;
        }

        map.put("data", df.format(byteTraffic / 1000.0));
        map.put("unit", "kB/s");

        return map;
    }

    private void useSpeedTestStrategy(OnDetectSpeedListener listener, String ip) {

        if (!TextUtils.isEmpty(ip)) {
            sourceUrl4 = String.format(sourceUrl4, ip);
            if (STRATEGY_ONLY_SELF.equals(speedStrategy)) {
                sourceUrl1 = sourceUrl2 = sourceUrl3 = sourceUrl4;
            } else if (STRATEGY_MIXED.equals(speedStrategy)) {
                DownloadTask downloadTask = new DownloadTask(sourceUrl4, listener);
                mDownloadTaskList.add(downloadTask);
                TaskManager.getInstance().submit(downloadTask);
            }
        }

        DownloadTask downloadTask = new DownloadTask(sourceUrl1, listener);
        DownloadTask downloadTask2 = new DownloadTask(sourceUrl2, listener);
        DownloadTask downloadTask3 = new DownloadTask(sourceUrl3, listener);

        mDownloadTaskList.add(downloadTask);
        mDownloadTaskList.add(downloadTask2);
        mDownloadTaskList.add(downloadTask3);
        TaskManager.getInstance().submit(downloadTask);
        TaskManager.getInstance().submit(downloadTask2);
        TaskManager.getInstance().submit(downloadTask3);
    }


}
