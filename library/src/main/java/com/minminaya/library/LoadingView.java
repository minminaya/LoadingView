package com.minminaya.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * <p>Created by LGM on 2018-04-05 16:32</p>
 * <p>Email:minminaya@gmail.com</p>
 */
public class LoadingView extends View {

    private int mLoadViewSize;
    private int mLoadViewColor;
    private Paint mPaint;

    private ValueAnimator mValueAnimator;
    private int mValueAnimatorValue;

    /**
     * 默认的滚动条数量
     */
    private static final int BAR_COUNT = 12;
    /**
     * 滚动条之间的角度
     */
    private static final int DEGREE_PER_BRR = 360 / BAR_COUNT;


    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.LoadingViewStyle);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingView, defStyleAttr, 0);
        mLoadViewSize = dp2px(context, typedArray.getDimensionPixelSize(R.styleable.LoadingView_loading_view_size, 32));
        mLoadViewColor = typedArray.getInt(R.styleable.LoadingView_loading_view_color, Color.WHITE);
        typedArray.recycle();
        initialPaint();
    }

    private void initialPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(mLoadViewColor);
    }

    /**
     * 设置尺寸后要重绘
     */
    public void setSize(int size) {
        mLoadViewSize = size;
        requestLayout();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //缓冲绘制
        int saveIndex = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        drawLoading(canvas);
        canvas.restoreToCount(saveIndex);
    }

    /**
     * 这里很重要的地方是，Canvas变换为倒序，变换代码要从下往上炒作
     */
    private void drawLoading(Canvas canvas) {
        int barWidth = mLoadViewSize / 12;
        int barHight = mLoadViewSize / 6;
        mPaint.setStrokeWidth(barWidth);
        int rotateDegress = mValueAnimatorValue * DEGREE_PER_BRR;

        //把最下面首先画的全部条旋转起来
        //以中点为圆心旋转
        canvas.rotate(rotateDegress, mLoadViewSize / 2, mLoadViewSize / 2);
        //移动本来在左上角（0,0）的画布到中心点
        canvas.translate(mLoadViewSize / 2, mLoadViewSize / 2);

        //以下主要画出全部的Bar条
        for (int i = 0; i < BAR_COUNT; i++) {

            //2.每画一个旋转
            canvas.rotate(DEGREE_PER_BRR);
            mPaint.setAlpha((int) (255f * (i) / BAR_COUNT));


            //1. 为了将bar条画到中心点以上若干距离(mLoadViewSize / 2 - barWidth)处，画布先移动到下面，画了之后再把画布移动回来
            canvas.translate(0, -mLoadViewSize / 2 + barWidth);
            canvas.drawLine(0, 0, 0, barHight, mPaint);
            canvas.translate(0, mLoadViewSize / 2 - barWidth);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mLoadViewSize, mLoadViewSize);
    }

    private int dp2px(Context context, int dp) {
        return (int) ((context.getResources().getDisplayMetrics().density) * dp + 0.5);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //启动属性动画
        startAnimator();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //停止动画引擎
        stopAnimator();
    }

    /**
     * 可见性改变的时候called
     */
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (VISIBLE == visibility) {
            startAnimator();
        } else {
            stopAnimator();
        }
    }

    private void startAnimator() {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofInt(0, BAR_COUNT - 1);
            mValueAnimator.setDuration(500);
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.addUpdateListener(animatorUpdateListener);
        } else if (!mValueAnimator.isStarted()) {
            mValueAnimator.start();
        }
    }

    private void stopAnimator() {
        if (mValueAnimator != null) {
            //移除监听
            mValueAnimator.removeUpdateListener(animatorUpdateListener);
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }


    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mValueAnimatorValue = (int) animation.getAnimatedValue();
            invalidate();//刷新
        }
    };
}
