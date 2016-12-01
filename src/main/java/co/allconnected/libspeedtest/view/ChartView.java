package co.allconnected.libspeedtest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import co.allconnected.libspeedtest.R;

/**
 * 线形图
 *
 * @author michael
 * @time 16/12/1 上午10:08
 */
public class ChartView extends View {

    private int mWidth;
    private int mHeight;
    Paint mPaint;

    private float mMaxSpeedScale = 2000;
    private float mXScaleSize = 20;
    private float mXScaleMaxCount;

    private float mYAxisLineNumber = 5;
    private float mXOffset = 0;
    private float mStartXAxis;
    private static ArrayList<Float> mDownloadSpeedList = new ArrayList<>();
    private static ArrayList<Float> mUploadSpeedList = new ArrayList<>();
    private int mDownLoadColor = getResources().getColor(R.color.speedtest_bg_download_speed_connected);
    private int mUploadColor = getResources().getColor(R.color.speedtest_bg_upload_speed_connected);

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // For Margin
        Rect bounds = new Rect();
        String text = "1000KB/s";
        mPaint.getTextBounds(text, 0, text.length(), bounds);
        mStartXAxis = bounds.width() * 2.5F;
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        }
        // X scale max count
        mXScaleMaxCount = (mWidth - mStartXAxis) / mXScaleSize;
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAxes(canvas);
        drawLineReverse(canvas);
    }

    private void drawAxes(Canvas canvas) {
        // scale
        float YAxisUnit = mHeight / mYAxisLineNumber;
        float sizeUnit = mMaxSpeedScale / mYAxisLineNumber;
        for (int i = 0; i < mYAxisLineNumber; i++) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(1);
            mPaint.setColor(Color.LTGRAY);
            canvas.drawLine(0, YAxisUnit * i, mWidth, YAxisUnit * i, mPaint);
            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(26);
            Rect bounds = new Rect();
            String text = getSpeedText(Math.round(sizeUnit * (mYAxisLineNumber - i) * 100 / 100)) + "/s";
            mStartXAxis = bounds.width();
            mPaint.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, bounds.height(), YAxisUnit * i + bounds.height(), mPaint);
        }
        // X Axis
        mPaint.setStrokeWidth(4);
        mPaint.setColor(Color.LTGRAY);
        canvas.drawLine(mXOffset, mHeight, mXOffset + mWidth, mHeight, mPaint);
    }

    private void drawLineReverse(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        Path path;
        float heightScale;

        // Download
        int downloadSpeedCount = mDownloadSpeedList.size();
        if (downloadSpeedCount != 0) {
            path = new Path();
            mPaint.setColor(mDownLoadColor);
            path.moveTo(mWidth, mHeight);
            for (int i = downloadSpeedCount - 1; i >= 0; i--) {
                heightScale = mDownloadSpeedList.get(i) / mMaxSpeedScale;
                path.lineTo(mWidth - mXScaleSize * (downloadSpeedCount - i - 1), (mHeight - 8) * (1 - heightScale));
                mPaint.setColor(mDownLoadColor);
            }
            path.lineTo(mWidth - (downloadSpeedCount - 1) * mXScaleSize, mHeight);
            canvas.drawPath(path, mPaint);
        }
        // Download
        int uploadSpeedCount = mUploadSpeedList.size();
        if (uploadSpeedCount != 0) {
            path = new Path();
            mPaint.setColor(mDownLoadColor);
            path.moveTo(mWidth, mHeight);
            for (int i = uploadSpeedCount - 1; i >= 0; i--) {
                heightScale = mUploadSpeedList.get(i) / mMaxSpeedScale;
                path.lineTo(mWidth - mXScaleSize * (uploadSpeedCount - i - 1), (mHeight - 8) * (1 - heightScale));
                mPaint.setColor(mUploadColor);
            }
            path.lineTo(mWidth - (uploadSpeedCount - 1) * mXScaleSize, mHeight);
            canvas.drawPath(path, mPaint);
        }
    }

    public void setDownloadSpeed(float downloadSpeed) {
        if (mDownloadSpeedList.size() > mXScaleMaxCount) {
            mDownloadSpeedList.remove(0);
        }
        mDownloadSpeedList.add(downloadSpeed);
        findMaxSpeed(mDownloadSpeedList);
        invalidate();
    }

    public void setUploadSpeed(float uploadSpeed) {
        if (mUploadSpeedList.size() > mXScaleMaxCount) {
            mUploadSpeedList.remove(0);
        }
        mUploadSpeedList.add(uploadSpeed);
        invalidate();
    }

    public void setSpeedColor(boolean isConnected) {
        if (isConnected) {
            mDownLoadColor = getResources().getColor(R.color.speedtest_bg_download_speed_connected);
            mUploadColor = getResources().getColor(R.color.speedtest_bg_upload_speed_connected);
        } else {
            mDownLoadColor = getResources().getColor(R.color.speedtest_bg_download_speed_unconnected);
            mUploadColor = getResources().getColor(R.color.speedtest_bg_upload_speed_unconnected);
        }
    }

    private float findMaxSpeed(ArrayList<Float> speedList) {
        if (speedList.size() == 0) {
            return 0;
        }
        float maxSpeed = 0;
        for (Float speed : speedList) {
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
        }

        mMaxSpeedScale = getMaxSpeed(maxSpeed);
        return maxSpeed;
    }

    private float getMaxSpeed(float maxSpeed) {
        float speed;
        if (maxSpeed > 15000) {
            speed = 20000;
        } else if (maxSpeed > 10000) {
            speed = 15000;
        } else if (maxSpeed > 5000) {
            speed = 10000;
        } else if (maxSpeed > 2500) {
            speed = 5000;
        } else if (maxSpeed > 1000) {
            speed = 2500;
        } else if (maxSpeed > 500) {
            speed = 1000;
        } else if (maxSpeed > 200) {
            speed = 500;
        } else if (maxSpeed > 100) {
            speed = 200;
        } else if (maxSpeed > 50) {
            speed = 100;
        } else {
            speed = 50;
        }
        return speed;
    }

    private String getSpeedText(float speed) {
        if (mMaxSpeedScale > 2500) {
            return (int) speed / 1000 + "MB";
        }
        if (speed >= 1000) {
            return speed / 1000 + "MB";
        } else {
            return (int) speed + "KB";
        }
    }
}
