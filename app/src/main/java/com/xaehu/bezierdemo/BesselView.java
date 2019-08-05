package com.xaehu.bezierdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author : xaeHu
 * e-mail : hsfemail@qq.com
 * @date : 2019/7/31 10:54
 * desc   :
 */
public class BesselView extends View {

    private final int POINT_START = 0;
    private final int POINT_CENTER = 3;
    private final int POINT_END = 1;
    private final int POINT_CTRL = 2;
    private Paint paint;
    private Paint linePaint;
    private float startX, startY, endX, endY, ctrlX, ctrlY;
    private float r;
    private float touchX,touchY;
    private Path path;
    private int currentPoint = -1;

    public BesselView(Context context) {
        this(context,null);
    }

    public BesselView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        paint = new Paint();
        paint.setAntiAlias(true);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(3);
        linePaint.setStyle(Paint.Style.STROKE);
        startX = 100;
        startY = 200;
        endX = 1000;
        endY = 200;
        ctrlX = 800;
        ctrlY = 500;
        touchX = 300;
        touchY = 500;
        r = 20;
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.reset();
        path.moveTo(startX, startY);
        path.cubicTo(touchX,touchY, ctrlX, ctrlY, endX, endY);
        canvas.drawPath(path,linePaint);
        if(currentPoint != -1){
            paint.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryDark));
            canvas.drawLine(startX, startY,touchX,touchY,paint);
            canvas.drawLine(endX, endY, ctrlX, ctrlY,paint);
            canvas.drawLine(ctrlX, ctrlY,touchX,touchY,paint);
        }
        paint.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        canvas.drawCircle(startX, startY,r,paint);
        canvas.drawCircle(endX, endY,r,paint);
        paint.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));
        canvas.drawCircle(ctrlX, ctrlY,r,paint);
        canvas.drawCircle(touchX,touchY,r,paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();
            if(isClickStart(x,y)){
                currentPoint = POINT_START;
            }else if(isClickEnd(x,y)){
                currentPoint = POINT_END;
            }else if(isClickCenter(x,y)){
                currentPoint = POINT_CENTER;
            }else if(isClickCtrl(x,y)){
                currentPoint = POINT_CTRL;
            }else{
                currentPoint = -1;
                invalidate();
            }
        }
        if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(currentPoint != -1){
                if(currentPoint == POINT_START){
                    startX = event.getX();
                    startY = event.getY();
                }else if(currentPoint == POINT_END){
                    endX = event.getX();
                    endY = event.getY();
                }else if(currentPoint == POINT_CENTER){
                    ctrlX = event.getX();
                    ctrlY = event.getY();
                }else{
                    touchX = event.getX();
                    touchY = event.getY();
                }
                invalidate();
            }
        }
        return true;
    }

    private boolean isClickCenter(float clickX, float clickY) {
        return isClick(ctrlX, ctrlY,r,clickX,clickY);
    }
    private boolean isClickStart(float clickX,float clickY){
        return isClick(startX, startY,r,clickX,clickY);
    }
    private boolean isClickEnd(float clickX,float clickY){
        return isClick(endX, endY,r,clickX,clickY);
    }
    private boolean isClickCtrl(float clickX,float clickY){
        return isClick(touchX,touchY,r,clickX,clickY);
    }
    private boolean isClick(float cx,float cy,float cr,float clickX,float clickY){
        return  clickX < cx + cr
                && clickX > cx - cr
                && clickY < cy + cr
                && clickY > cy - cr;
    }
}
