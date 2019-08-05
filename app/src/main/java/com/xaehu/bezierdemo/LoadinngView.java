package com.xaehu.bezierdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * @author : xaeHu
 * e-mail : hsfemail@qq.com
 * @date : 2019/7/30 14:48
 * desc  :
 */
public class LoadinngView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private final int STATE_DOWN = 0;
    private final int STATE_UP = 1;
    private final int STATE_FREE = 2;
    private int loadingState = STATE_DOWN;
    private SurfaceHolder holder;
    private boolean isRun;
    private Paint ballPaint;
    private Paint linePaint;
    private Path path;

    private ValueAnimator downAnimator;
    private ValueAnimator upAnimator;
    private ValueAnimator freeAnimator;
    private AnimatorSet animatorSet;

    //线宽
    private float lineWidth;
    //小球偏离水平线距离
    private float distance;
    private float distance_temp;
    private float freeDownDistance;
    //线粗
    private float lineStroke;
    //半径
    private float r;

    public LoadinngView(Context context) {
        this(context, null);
    }

    public LoadinngView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        holder = this.getHolder();
        holder.addCallback(this);
        this.setFocusable(true);
        path = new Path();
        ballPaint = new Paint();
        ballPaint.setAntiAlias(true);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        lineStroke = 3;
        linePaint.setStrokeWidth(lineStroke);
        linePaint.setStyle(Paint.Style.STROKE);
        lineWidth = 300;
        distance = 80;
        r = 20;

        initControl();
    }

    private void initControl() {
        downAnimator = ValueAnimator.ofFloat(0, 1);
        downAnimator.setDuration(500);
        //减速
        downAnimator.setInterpolator(new DecelerateInterpolator());
        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                distance_temp = distance * (float) animation.getAnimatedValue();
            }
        });
        downAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                loadingState = STATE_DOWN;
            }
        });
        upAnimator = ValueAnimator.ofFloat(0, 1);
        upAnimator.setDuration(500);
        //加速
        upAnimator.setInterpolator(new ShockInterpolator());
        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                distance_temp = distance - distance * (float) animation.getAnimatedValue();
                if (distance_temp <= 0 && !freeAnimator.isStarted()) {
                    freeAnimator.start();
                }
            }
        });
        upAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                loadingState = STATE_UP;

            }
        });
        freeAnimator = ValueAnimator.ofFloat(0, (float) (2 * Math.sqrt(distance / 5)));
        freeAnimator.setDuration(600);
        freeAnimator.setInterpolator(new LinearInterpolator());
        freeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                freeDownDistance = (float) (10 * Math.sqrt(distance / 5) * t - 5 * t * t);
            }
        });
        freeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                loadingState = STATE_FREE;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet.start();
            }
        });
        animatorSet = new AnimatorSet();
        animatorSet.play(downAnimator).before(upAnimator);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRun = true;
        drawLoadingView();
        new Thread(this).start();
        animatorSet.start();
    }

    private void drawLoadingView() {
        Canvas canvas = holder.lockCanvas();
        try {
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                //贝塞尔曲线
                ballPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                path.reset();
                path.moveTo((float) (getWidth() / 2) - lineWidth / 2, (float) (getHeight() / 2));
                path.rQuadTo(lineWidth / 2, distance_temp * 2 + lineStroke, lineWidth, 0);
                //中间小球
                if (loadingState == STATE_FREE) {
                    canvas.drawCircle((float) (getWidth() / 2), (float) (getHeight() / 2) - freeDownDistance - r, r, ballPaint);
                }else{
                    canvas.drawCircle((float) (getWidth() / 2), (float) (getHeight() / 2) + distance_temp - r, r, ballPaint);
                }
                canvas.drawPath(path, linePaint);
                //两边小球
                ballPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                canvas.drawCircle((float) (getWidth() / 2) - lineWidth / 2, (float) (getHeight() / 2), r, ballPaint);
                canvas.drawCircle((float) (getWidth() / 2) + lineWidth / 2, (float) (getHeight() / 2), r, ballPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRun = false;
        if (animatorSet.isStarted()) {
            animatorSet.end();
            animatorSet.cancel();
        }
        if (freeAnimator.isStarted()) {
            freeAnimator.end();
            freeAnimator.cancel();
        }
    }

    @Override
    public void run() {
        while (isRun) {
            drawLoadingView();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class ShockInterpolator implements Interpolator {

        @Override
        public float getInterpolation(float input) {
            return (float) (1 - Math.exp(-3 * input) * Math.cos(10 * input));
        }
    }
}
