package com.lt.lottery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author Scorpio
 * @date 2018/7/22
 */

public class LotteryView extends View {
    /**
     * 扇形分区的颜色
     */
    private int[] colors = new int[]{R.color.red, R.color.gray, R.color.blue, R.color.red, R.color.gray, R.color.blue};
    /**
     * 分区的文字描述
     */
    private String[] prizeName = new String[]{"微信红包", "王者皮肤", "谢谢参与", "1QB", "5QB", "10QB"};
    /**
     * 扇形的角度
     */
    private float mAngel;
    /**
     * 盘块的数量
     */
    private int mItems = 6;
    /**
     * 扇形分区图片
     */
    private int[] imgs = new int[]{R.drawable.danfan, R.drawable.ipad, R.drawable.iphone, R.drawable.meizi
            , R.drawable.f015, R.drawable.f040};
    /**
     * 图片对应的bitmap
     */
    private Bitmap[] mImgBitmap;
    /**
     * 圆盘的大小范围
     */
    private RectF mRange = new RectF();
    /**
     * 圆盘的直径
     */
    private int mRadius;

    /**
     * 扇形分区的画笔
     */
    private Paint mArcPaint;
    /**
     * 绘制文本的画笔
     */
    private Paint mTextPaint;
    /**
     * 圆盘滚动的速度
     */
    private double mSpeed = 50;

    /**
     * 绘制的起始角度
     */
    private double mStartAngle;
    /**
     * 是否点击了结束按钮
     */
    private boolean start = false;
    /**
     * 转盘的中心位置
     */
    private int mCenter;

    /**
     * 圆盘Padding值
     */
    private int mPadding;
    /**
     * 圆盘背景图片
     */
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg2);
    /**
     * 文字的大小
     */
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler();

    public LotteryView(Context context) {
        this(context, null);
    }

    public LotteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LotteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 量测尺寸  我们将画圆盘在一个正方形之中
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取宽高的最小值
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        //以PaddingLeft为准
        mPadding = getPaddingLeft();
        //获取直径
        mRadius = width - mPadding * 2;
        setMeasuredDimension(width, width);
        //初始化圆盘的范围
        mRange = new RectF(mPadding, mPadding, mRadius + mPadding, mRadius + mPadding);
        //中心位置
        mCenter = getMeasuredWidth() / 2;
    }

    /**
     * 初始化
     */
    private void init() {
        //初始化盘块画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        //初文字画笔
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSize);

        //初始化盘块中的图片
        mImgBitmap = new Bitmap[mItems];
        for (int i = 0; i < mImgBitmap.length; i++) {
            mImgBitmap[i] = BitmapFactory.decodeResource(getResources(), imgs[i]);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        //1.绘制背景
        canvas.drawBitmap(mBgBitmap, null, new RectF(mPadding / 2, mPadding / 2
                , getMeasuredWidth() - mPadding / 2, getMeasuredHeight() - mPadding / 2), null);
        //2.绘制盘块
        int tempAngle = (int) mStartAngle;
        float sweepAngle = 360 / mItems;
        //        //旋转绘制的图片
        //        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        //        for (int j = 0; j < imgs.length; j++) {
        //            //获取bitmap
        //            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs[j]);
        //            int width = bitmap.getWidth();
        //            int height = bitmap.getHeight();
        //            Matrix matrix = new Matrix();
        //            //设置缩放值
        //            matrix.postScale(1f, 1f);
        //            //旋转的角度
        //            matrix.postRotate(sweepAngle * j);
        //            //获取旋转后的bitmap
        //            Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        //            //将旋转过的图片保存到列表中
        //            bitmaps.add(rotateBitmap);
        //        }
        for (int i = 0; i < mItems; i++) {
            //1.绘制盘块背景
            mArcPaint.setColor(ContextCompat.getColor(getContext(), colors[i]));
            canvas.drawArc(mRange, tempAngle, sweepAngle, true, mArcPaint);


            //2.绘制盘块的文字
            Path path = new Path();
            path.addArc(mRange, tempAngle, sweepAngle);
            //通过水平偏移量使得文字居中  水平偏移量=弧度/2 - 文字宽度/2
            float textWidth = mTextPaint.measureText(prizeName[i]);
            float hOffset = (float) (mRadius * Math.PI / mItems / 2 - textWidth / 2);
            //垂直偏移量 = 半径/6
            float vOffset = mRadius / 2 / 6;
            canvas.drawTextOnPath(prizeName[i], path, hOffset, vOffset, mTextPaint);

            //3.绘制盘块上面的IMG
            //约束下图片的宽度
            int imgWidth = mRadius / 8;
            //获取弧度
            float angle = (float) Math.toRadians(tempAngle + sweepAngle / 2);
            //将图片移动到圆弧中心位置
            float x = (float) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
            float y = (float) (mCenter + mRadius / 2 / 2 * Math.sin(angle));
            //确认绘制的矩形
            RectF rectF = new RectF(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs[i]);
            canvas.drawBitmap(bitmap, null, rectF, null);
            tempAngle += sweepAngle;
        }
        if (start) {
            mStartAngle += mSpeed;
            //16ms之后刷新界面
            mHandler.postDelayed(new MyRunnable(), 16);
            mSpeed -= 1;
            if (mSpeed < 10) {
                mSpeed -= 0.5;
            }
            if (mSpeed < 3) {
                mSpeed -= 0.1;
            }
            if (mSpeed < 0) {
                mSpeed = 0;
                start = false;
            }
        }
    }

    public void start() {
        start = true;
        mSpeed = 40;
        invalidate();
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            invalidate();
        }
    }

}
