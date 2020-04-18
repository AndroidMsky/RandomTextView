package com.example.liangmutian.randomtextviewlibrary;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lmt on 16/11/1.
 */

@SuppressLint("AppCompatCustomView")
public class RandomTextView extends TextView {
    //高位快
    public static final int HIGH_FIRST = 0;
    //高位慢
    public static final int LOW_FIRST = 1;
    //速度相同
    public static final int ALL = 2;
    //用户自定义速度
    public static final int USER = 3;
    //   滚动总行数 可设置
    private int maxLine = 10;
    //   当前字符串长度
    private int numLength = 0;
    //   当前text
    private String text;
    //滚动速度数组
    private int[] speedList;
    //总滚动距离数组
    private int[] speedSum;
    //滚动完成判断
    private int[] overLine;
    private Paint mPaint;
    //第一次绘制
    private boolean firstIn = true;
    //滚动中
    private boolean animating = true;
    //text int值列表
    private ArrayList<Character> arrayListText;
    //字体宽度
    private float fontWidth;
    //小数点的宽度.
    private float pointWidth;
    //基准线
    private int baseline;
    private int measuredHeight;
    //point index
    private int pointIndex = -1;

    private boolean pointAnimation = false;


    public RandomTextView(Context context) {
        super(context);
    }

    public RandomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RandomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isPointAnimation() {
        return pointAnimation;
    }

    public void setPointAnimation(boolean pointAnimation) {
        this.pointAnimation = pointAnimation;
    }


    //按系统提供的类型滚动
    public void setSpeeds(int speedsTpye) {
        this.text = getText().toString();

        speedSum = new int[text.length()];
        overLine = new int[text.length()];
        speedList = new int[text.length()];
        switch (speedsTpye) {
            case HIGH_FIRST:
                for (int i = 0; i < text.length(); i++) {
                    speedList[i] = 20 - i;
                }
                break;
            case LOW_FIRST:
                for (int i = 0; i < text.length(); i++) {
                    speedList[i] = 15 + i;
                }
                break;
            case ALL:
                for (int i = 0; i < text.length(); i++) {
                    speedList[i] = 15;
                }

                break;
        }
    }

    //自定义滚动速度数组
    public void setSpeeds(int[] list) {
        this.text = getText().toString();
        speedSum = new int[list.length];
        overLine = new int[list.length];
        speedList = list;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("RandomTextView", "draw");
        if (firstIn) {
            firstIn = false;
            super.onDraw(canvas);
            mPaint = getPaint();
            Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
            measuredHeight = getMeasuredHeight();
            baseline = (measuredHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
            float[] widths = new float[4];
            mPaint.getTextWidths("9999", widths);
            fontWidth = widths[0];
            pointWidth = mPaint.measureText(".");
            invalidate();
        }
        drawNumber(canvas);
    }


    //绘制
    private void drawNumber(Canvas canvas) {
        for (int j = 0; j < numLength; j++) {
            for (int i = 1; i < maxLine; i++) {
                //计算偏移量和绘制判断逻辑
                if (i == maxLine - 1 && i * baseline + speedSum[j] <= baseline) {
                    speedList[j] = 0;
                    overLine[j] = 1;
                    int autoOverLine = 0;
                    for (int k = 0; k < numLength; k++) {
                        autoOverLine += overLine[k];
                    }
                    if (autoOverLine == numLength * 2 - 1) {
                        stopAnimatorLoop();
                        invalidate();
                        animating = false;
                    }

                }
                if (overLine[j] == 0) {
                    Log.e("lmtlmt", setBack(arrayListText.get(j), maxLine - i - 1) + "");
                    if (setBack(arrayListText.get(j), maxLine - i - 1) >= 0 && setBack(arrayListText.get(j), maxLine - i - 1) <= 9) {
                        drawText(canvas, setBack(arrayListText.get(j), maxLine - i - 1) + "", getDrawX(j),
                                i * baseline + speedSum[j], mPaint);


                    } else {
                        int pyl = 0;
                        if (pointAnimation) {
                            pyl = speedSum[j];
                        }
                        drawText(canvas, ".", getDrawX(j), i * baseline + pyl, mPaint);
                    }

                } else {
                    //定位后画一次就好啦
                    if (overLine[j] == 1) {
                        overLine[j]++;
                        drawText(canvas, arrayListText.get(j) + "", getDrawX(j),
                                baseline, mPaint);
                    }
                }
            }
        }
    }

    //设置上方数字0-9递减
    private int setBack(int c, int back) {
        //如果不是0-9的数组直接返回本身的char
        if (c < '0' || c > '9') {
            return c;
        }
        c = c - '0';
        if (back == 0) return c;
        back = back % 10;
        int re = c - back;
        if (re < 0) re = re + 10;
        return re;
    }

    //开始滚动
    public void start() {
        this.text = getText().toString();
        numLength = text.length();
        arrayListText = getList(text);
        animating = true;
        startAnimatorLoop();
    }

    public void setMaxLine(int l) {
        this.maxLine = l;
    }

    private ArrayList<Character> getList(String s) {
        pointIndex = -1;
        ArrayList<Character> arrayList = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            Character c = s.charAt(i);
            arrayList.add(c);
            if (c == '.') pointIndex = i;
        }
        return arrayList;

    }


    public void destroy() {
        animating = false;
        stopAnimatorLoop();
    }

    private final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);

    private void stopAnimatorLoop() {
        valueAnimator.removeAllUpdateListeners();
        valueAnimator.cancel();
    }

    private void startAnimatorLoop() {
        valueAnimator.cancel();
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(animatorUpdateListener);
        valueAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (animating) {
                for (int j = 0; j < numLength; j++) {
                    speedSum[j] -= speedList[j];
                }
                invalidate();
            } else {
                stopAnimatorLoop();
            }
        }
    };

    private void drawText(Canvas mCanvas, String text, float x, float y, Paint p) {
        if (y >= -measuredHeight && y <= 2 * measuredHeight)
            mCanvas.drawText(text + "", x, y, p);
        else return;
    }

    private int getDrawX(int j) {
        if (j <= pointIndex || pointIndex == -1) return (int) (fontWidth * j);
        else return (int) (fontWidth * (j - 1) + pointWidth);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

}
