package co.allconnected.libspeedtest.view;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import co.allconnected.libspeedtest.R;
import co.allconnected.libspeedtest.core.OnDetectSpeedListener;
import co.allconnected.libspeedtest.core.SpeedTestManagerImpl;

/**
 * 抽象类，需要实现该类的方法
 * 用于展示测速界面
 *
 * @author michael
 * @time 16/12/1 上午10:07
 */
public abstract class SpeedTestFragment extends Fragment {
    private Context mContext;
    private static final int MSG_WAITING_COUNTDOWN = -1;
    private int count = 0;
    private View mView;

    private ChartView mChartView;
    private TextView mStartSpeedTestBtn;
    private TextView mDownloadSpeedTv;
    private TextView mUploadSpeedTv;

    // Speed Test
    private PanelView mPanelView;
    private TextView mTestProgressDesc;
    private ProgressBar mProgressBar;
    private TextView mAvgSpeedTv;
    private TextView mSpeedResultDescTv;
    private ImageView mSpeedResultDescIv;
    private ImageView mVpnStatusIv;
    private SpeedTestManagerImpl mSpeedTestManager;
    private int mIsTesting = -1;

    private int mWidth;

    private LinearLayout mTestPanelLayout;
    private LinearLayout mResultPanelLayout;
    private TextView mRetestBtn;
    private TextView mAdvancedTestBtn;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WAITING_COUNTDOWN:
                    count++;
                    if (!mIsStartTestProgress && count >= 5) {
                        reset();
                        Toast.makeText(mContext, "Test failed, please check your network!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (mIsStartTestProgress) {
                        count = 0;
                        if (mHandler.hasMessages(MSG_WAITING_COUNTDOWN)) {
                            mHandler.removeMessages(MSG_WAITING_COUNTDOWN);
                        }
                        break;
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_WAITING_COUNTDOWN, 1000);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mView = inflater.inflate(R.layout.fragment_network_discover, container, false);
        mWidth = getResources().getDisplayMetrics().widthPixels;
        initViews();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getResources().getColor(R.color.speedtest_bg_download_speed_connected);
        if (isShowVpnIcon()) {
            mVpnStatusIv.setImageResource(isVpnConnected() ? R.mipmap.ic_vpn_on : R.mipmap.ic_vpn_off);
            mView.findViewById(R.id.view_offline).setVisibility(isVpnConnected() ? View.INVISIBLE : View.VISIBLE);
            mVpnStatusIv.setVisibility(View.VISIBLE);
        } else {
            mVpnStatusIv.setVisibility(View.GONE);
        }
        mPanelView.setIsTesting(true);
        mSpeedTestManager = new SpeedTestManagerImpl();
    }


    @Override
    public void onStop() {
        super.onStop();
        mSpeedTestManager.removeSpeedRunnable();
        mChartView.invalidate();
        mPanelView.invalidate();
    }

    boolean mIsStartTestProgress = false;
    boolean mIsRetest = false;
    private View.OnClickListener mViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btn_retest) {
                mIsRetest = true;
                mTestPanelLayout.setVisibility(View.VISIBLE);
                mResultPanelLayout.setVisibility(View.GONE);
                mStartSpeedTestBtn.performClick();
                mChartView.setSpeedColor(true);
            } else if (id == R.id.btn_advanced_test) {
                mStartSpeedTestBtn.setText(getResources().getString(R.string.text_quick_test));
                mIsTesting = -1;
                showAd();
            } else if (id == R.id.tv_start_speed_test) {
                if (getResources().getString(R.string.text_quick_test).equalsIgnoreCase(((TextView) v).getText().toString())) {
                    count = 0;
                    mErrorCount = 0;
                    mHandler.sendEmptyMessage(MSG_WAITING_COUNTDOWN);
                    mSpeeds.clear();
                    mTestProgressDesc.setText("Testing...");
                    mSpeedTestManager.startTest(mOnDetectSpeedListener);
                    mTestProgressDesc.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mIsTesting = 1;
                    mStartSpeedTestBtn.setText(getResources().getString(android.R.string.cancel));
                    mStartSpeedTestBtn.setBackgroundResource(R.drawable.button_disable);
                    mIsRetest = false;
                } else if (getResources().getString(android.R.string.cancel).equalsIgnoreCase(((TextView) v).getText().toString())) {
                    mIsTesting = -1;
                    reset();
                }
            }
        }
    };

    int mErrorCount = 0;
    ArrayList<Float> mSpeeds = new ArrayList<>();
    private OnDetectSpeedListener mOnDetectSpeedListener = new OnDetectSpeedListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onFinished(final float speed) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPanelView.setSpeed(0);
                    String speedText = mSpeedTestManager.getAvgSpeed();
                    mAvgSpeedTv.setText(speedText);
                    mSpeedResultDescIv.setImageResource(getSpeedResultDescResId(speed));
                    mSpeedResultDescTv.setText(getSpeedResultDescText(speed));
                    mStartSpeedTestBtn.setBackgroundResource(R.drawable.button_ad_install);
                    mTestProgressDesc.setText(mContext.getResources().getString(R.string.text_speed_test));
                    resetProgressBar();
                    mStartSpeedTestBtn.setText(getResources().getString(R.string.text_quick_test));
                    mIsTesting = -1;
                    mIsStartTestProgress = false;
                    Animation outAnim = AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_left_out);
                    mTestPanelLayout.startAnimation(outAnim);
                    mTestPanelLayout.setVisibility(View.GONE);
                    Animation inAnim = AnimationUtils.loadAnimation(mContext, R.anim.anim_slide_right_in);
                    mResultPanelLayout.startAnimation(inAnim);
                    mResultPanelLayout.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onProgress(final float percent, final float speed) {
            mSpeeds.add(speed);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPanelView.setSpeed(speed / 1024);
                    mProgressBar.setProgress((int) (100 * percent));
                    if (percent != 0) {
                        mIsStartTestProgress = true;
                    }
                }
            });
        }

        @Override
        public void onError(final String url) {
            mErrorCount += 1;
            if (mErrorCount >= 6) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        reset();
//                        if (VpnAgent.getInstance().isConnected()) {
//                            showErrorDialog();
//                        } else {
//                            Toast.makeText(mContext, "Test failed, please check your network!", Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
            }
        }

        @Override
        public void getDownLoadSpeed(long downSpeed) {

            mDownloadSpeedTv.setText(mSpeedTestManager.getTrafficString(downSpeed) + "/s");
            mChartView.setDownloadSpeed(downSpeed / 1024);


        }

        @Override
        public void getUpLoadSpeed(long upSpeed) {
            mUploadSpeedTv.setText(mSpeedTestManager.getTrafficString(upSpeed) + "/s");
            mChartView.setUploadSpeed(upSpeed / 1024);

        }
    };


    private void reset() {
        count = 0;
        if (mHandler.hasMessages(MSG_WAITING_COUNTDOWN)) {
            mHandler.removeMessages(MSG_WAITING_COUNTDOWN);
        }
        mStartSpeedTestBtn.setText(getResources().getString(R.string.text_quick_test));
        mStartSpeedTestBtn.setBackgroundResource(R.drawable.button_ad_install);
        mTestProgressDesc.setText("Speed Test");
        mPanelView.setSpeed(0);
        resetProgressBar();
        mSpeedTestManager.cancelTest();
        mIsTesting = -1;
        mIsStartTestProgress = false;
    }

    private void resetProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setProgress(0);
    }

    private void initViews() {
        mDownloadSpeedTv = (TextView) mView.findViewById(R.id.tv_download_speed);
        mUploadSpeedTv = (TextView) mView.findViewById(R.id.tv_upload_speed);
        mChartView = (ChartView) mView.findViewById(R.id.view_speed_chart);
        mChartView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mWidth / 2));

        // Speed Test
        mPanelView = (PanelView) mView.findViewById(R.id.view_speed_panel);
        mTestProgressDesc = (TextView) mView.findViewById(R.id.tv_test_progress_desc);
        mAvgSpeedTv = (TextView) mView.findViewById(R.id.tv_avg_speed_new);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.progressbar_test);
        mStartSpeedTestBtn = (TextView) mView.findViewById(R.id.tv_start_speed_test);
        mSpeedResultDescIv = (ImageView) mView.findViewById(R.id.iv_speed_result_desc_new);
        mVpnStatusIv = (ImageView) mView.findViewById(R.id.iv_vpn_status);
        mStartSpeedTestBtn.setOnClickListener(mViewClickListener);
        mSpeedResultDescTv = (TextView) mView.findViewById(R.id.tv_speed_result_desc_new);

        if (mIsTesting == 1) {
            mTestProgressDesc.setText("Testing...");
            mStartSpeedTestBtn.setText(getResources().getString(android.R.string.cancel));
            mStartSpeedTestBtn.setBackgroundResource(R.drawable.button_disable);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mTestProgressDesc.setText("Speed Test");
            mStartSpeedTestBtn.setText(getResources().getString(R.string.text_quick_test));
            mStartSpeedTestBtn.setBackgroundResource(R.drawable.button_ad_install);
            mProgressBar.setVisibility(View.GONE);
        }

        mTestPanelLayout = (LinearLayout) mView.findViewById(R.id.layout_test_panel);
        mResultPanelLayout = (LinearLayout) mView.findViewById(R.id.layout_result_panel);
        mRetestBtn = (TextView) mView.findViewById(R.id.btn_retest);
        mRetestBtn.setOnClickListener(mViewClickListener);
        mAdvancedTestBtn = (TextView) mView.findViewById(R.id.btn_advanced_test);
        if (!isShowAd()) {
            mAdvancedTestBtn.setVisibility(View.GONE);
        } else {
            mAdvancedTestBtn.setVisibility(View.VISIBLE);
        }
        mAdvancedTestBtn.setOnClickListener(mViewClickListener);
    }

    private String getSpeedResultDescText(float speed) {
        if (speed > 500 * 1024) {
            return "Turbo";
        } else if (speed > 300 * 1024) {
            return "Fast";
        } else if (speed > 100 * 1024) {
            return "Normal";
        }
        return "Slow";
    }

    private int getSpeedResultDescResId(float speed) {
        if (speed > 500 * 1024) {
            return R.mipmap.ic_flight_green_24dp;
        } else if (speed > 300 * 1014) {
            return R.mipmap.ic_directions_car_green_24dp;
        } else if (speed > 100 * 1024) {
            return R.mipmap.ic_directions_bike_green_24dp;
        }
        return R.mipmap.ic_directions_walk_green_24dp;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public abstract boolean isShowVpnIcon();//是否显示vpnicon

    public abstract boolean isVpnConnected();//vpn是否连接上

    public abstract boolean isShowAd();//是否显示广告

    public abstract void showAd();//显示广告


}
