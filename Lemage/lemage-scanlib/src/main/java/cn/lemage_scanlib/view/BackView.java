package cn.lemage_scanlib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.View;

/**
 * 返回键按钮
 * @author zhaoguangyang
 */
public class BackView extends View {

    private final String TAG = "BackView";

    private Context mContext;
    private Paint mPaint;
    private Path mPath;

    // 箭头左侧顶点距离控件Left的距离
    private int leftLength;
    // 箭头中间横杆的长度
    private int centerLineLength;

    private Point pointA, pointB, pointC, pointD;

    public BackView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(4);

        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 画箭头
        mPath.moveTo(pointA.x, pointA.y);
        mPath.lineTo(pointB.x, pointB.y);
        mPath.lineTo(pointC.x, pointC.y);
        canvas.drawPath(mPath, mPaint);
        // 画箭头横线
        mPath.moveTo(pointB.x, pointB.y);
        mPath.lineTo(pointD.x, pointD.y);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int width = measureWidth(minimumWidth, widthMeasureSpec);
        int height = measureHeight(minimumHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);

        leftLength = width / 6;
        centerLineLength = width / 5 * 3;

        // 箭头上侧的顶点
        pointA = new Point(leftLength + centerLineLength / 3, height / 3);
        // 箭头的顶点
        pointB = new Point(leftLength, height / 2);
        // 箭头下侧的顶点
        pointC = new Point(leftLength + centerLineLength / 3, height / 3 * 2);
        // 箭头横向右侧的顶点
        pointD = new Point(leftLength + centerLineLength / 3 * 2, height / 2);
    }

    private int measureWidth(int defaultWidth, int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);


        switch (specMode) {
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                defaultWidth = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultWidth = Math.max(defaultWidth, specSize);
        }
        return defaultWidth;
    }


    private int measureHeight(int defaultHeight, int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultHeight = (int) (-mPaint.ascent() + mPaint.descent()) + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                defaultHeight = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultHeight = Math.max(defaultHeight, specSize);
                break;
        }
        return defaultHeight;
    }
}
