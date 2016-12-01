package co.allconnected.libspeedtest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import co.allconnected.libspeedtest.R;

/**
 * 速度表盘
 *
 * @author michael
 * @time 16/12/1 上午10:08
 */
public class PanelView extends View {

    Paint mPaint;
    int mWidth;
    int mHeight;
    int mCenterX;
    int mCenterY;
    float mDensity;

    float mSize;
    float mDegreeOffset = 60;
    float mMaxSize = 2000;
    float mPercent = mDegreeOffset / mMaxSize;

    float mStartDegree = 150;
    float mSweepDegree = 240;
    private boolean mIsTesting = false;

    public PanelView(Context context) {
        super(context);
    }

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDensity = getResources().getDisplayMetrics().density;
    }

    public PanelView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (!mIsTesting) {
            initPanel(canvas);
            return;
        }
        float strokeWidth = 8 * mDensity / 3;
        float halfStrokeWidth = strokeWidth / 2;
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);

        mPercent = mSize / mMaxSize;

        // Draw panel with color
        if (mPercent != 0) {
            mPaint.setColor(getResources().getColor(R.color.speedtest_btn_green_normal));
            canvas.drawArc(new RectF(halfStrokeWidth, halfStrokeWidth, mWidth - halfStrokeWidth, mHeight - halfStrokeWidth), mStartDegree, mSweepDegree * mPercent, false, mPaint);
        }

        // Draw panel with gray
        mPaint.setColor(Color.LTGRAY);
        canvas.drawArc(new RectF(halfStrokeWidth, halfStrokeWidth, mWidth - halfStrokeWidth, mHeight - halfStrokeWidth), mStartDegree + mSweepDegree * mPercent, mSweepDegree * (1 - mPercent), false, mPaint);

        // Draw pointer
        canvas.save();
        mPaint.setStrokeWidth(halfStrokeWidth);
        mPaint.setColor(getResources().getColor(R.color.speedtest_colorPrimary));
        canvas.rotate(mDegreeOffset + mSweepDegree * mPercent, mCenterX, mCenterY);
        canvas.drawLine(mCenterX, mCenterY, mCenterX, 2 * (mCenterY - strokeWidth), mPaint);
        canvas.restore();

        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterX, mCenterY, strokeWidth, mPaint);
    }

    public void setSpeed(float size) {
        this.mSize = size > 2000 ? 2000 : size;
        invalidate();
    }

    public void setIsTesting(boolean isTesting) {
        this.mIsTesting = isTesting;
    }

    private void initPanel(Canvas canvas) {
        mPaint.setColor(Color.LTGRAY);
        int strokeWidth = 8;
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(strokeWidth, strokeWidth, mWidth - strokeWidth, mHeight - strokeWidth), mStartDegree, mSweepDegree, false, mPaint);

        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.BLUE);
        canvas.drawLine(mCenterX, mCenterY, mCenterX, 68 - mCenterY, mPaint);
    }
}
