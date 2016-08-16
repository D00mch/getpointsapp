package com.livermor.getpointsapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

public class Scene {
    private static String TAG = Scene.class.getSimpleName();

    private static final int STEP = 70;
    private static final float SCALE = 2;

    private RelativeLayout mRelativeLayout;
    private ImageView mUpLeft, mUpRight, mDownLeft, mDownRight;
    private ImageView mLoop;

    private float mDensity;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mHandLength;
    private int mLoopLength;

    ImageView[] mHands;
    private int currentSniper = R.drawable.sniper_2;

    public Scene(Context context, RelativeLayout relativeLayout) {
        mRelativeLayout = relativeLayout;
        mDensity = context.getResources().getDisplayMetrics().density;
        calcMainDists(context);

        buildLoop(context);
        buildHands(context);
    }

    public void changeSniper(Context context) {
        if (currentSniper == R.drawable.sniper_2) currentSniper = R.drawable.sniper;
        else currentSniper = R.drawable.sniper_2;

        for (ImageView hand : mHands) {
            Glide.with(context).load(currentSniper).into(hand);
        }
    }

    public void printCoords() {

        float x = (mUpLeft.getX() + mHandLength / 2) / mScreenWidth;
        float y = (mUpLeft.getY() + mHandLength / 2) / mRelativeLayout.getHeight();

        float width = (mUpRight.getX() - mUpLeft.getX()) / mScreenWidth;
        float height = (mDownLeft.getY() - mUpLeft.getY()) / mRelativeLayout.getHeight();

        String ending = ",\n";
        StringBuilder sb = new StringBuilder();
        sb.append("\"x\": ").append(x).append(ending);
        sb.append("\"y\": ").append(y).append(ending);
        sb.append("\"width\": ").append(width).append(ending);
        sb.append("\"height\": ").append(height).append(ending);

        Log.i(TAG, "printCoords: \n" + sb.toString());
    }


    //—————————————————————————————————————————————————————————————————————— inner helpers

    private void buildLoop(Context content) {
        mLoop = new ImageView(content);
        mLoop.setX(200);
        mLoop.setY(400);
        mLoop.setScaleType(ImageView.ScaleType.FIT_XY);
        mRelativeLayout.addView(mLoop, new RelativeLayout.LayoutParams(mLoopLength, mLoopLength));
    }

    private void buildHands(Context context) {
        mUpLeft = buildHand(STEP, STEP, context);
        mUpRight = buildHand(STEP * 2, STEP, context);
        mDownLeft = buildHand(STEP, STEP * 2, context);
        mDownRight = buildHand(STEP * 2, STEP * 2, context);

        ImageView[] hands = {mUpLeft, mUpRight, mDownLeft, mDownRight};
        mHands = hands;

        for (ImageView hand : hands) mRelativeLayout.addView(hand);


        setTouchListener(mUpLeft, mDownLeft, mUpRight);
        setTouchListener(mUpRight, mDownRight, mUpLeft);
        setTouchListener(mDownLeft, mUpLeft, mDownRight);
        setTouchListener(mDownRight, mUpRight, mDownLeft);
    }

    private void setTouchListener(View mainView, final View xDependView, final View yDependView) {


        mainView.setOnTouchListener(new View.OnTouchListener() {

            private float xCoOrdinate, yCoOrdinate;

            @Override public boolean onTouch(View view, MotionEvent event) {
                view.getParent().requestDisallowInterceptTouchEvent(true);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        xCoOrdinate = view.getX() - event.getRawX();
                        yCoOrdinate = view.getY() - event.getRawY();
                        mLoop.setVisibility(View.VISIBLE);
                    }
                    break;

                    case MotionEvent.ACTION_MOVE: {
                        float xToMove = event.getRawX() + xCoOrdinate;
                        float yToMove = event.getRawY() + yCoOrdinate;

                        view.animate().x(xToMove).y(yToMove).setDuration(0).start();
                        xDependView.animate().x(xToMove).setDuration(0).start();
                        yDependView.animate().y(yToMove).setDuration(0).start();

                        drawLoop(xToMove, yToMove);
                        moveLoop(xToMove, yToMove);
                    }
                    break;

                    case MotionEvent.ACTION_UP: {
                        mLoop.setVisibility(View.GONE);
                    }
                }
                return true;
            }
        });
    }


    private ImageView buildHand(int x, int y, Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setX(x * mDensity);
        imageView.setY(y * mDensity);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context).load(currentSniper).into(imageView);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mHandLength, mHandLength);
        imageView.setLayoutParams(lp);
        return imageView;
    }

    private void moveLoop(float x, float y) {
        x = x - mLoopLength / 2;
        y = y + mLoopLength / 2;

        if (x < 0) x = 0;
        if (y < 0) y = 0;

        if (x + mLoopLength > mScreenWidth) x = mScreenWidth - mLoopLength;
        if (y + mLoopLength > mScreenHeight) y = y - mLoopLength * 1.5f;

        mLoop.animate().x(x).y(y).setDuration(0).start();
    }

    //—————————————————————————————————————————————————————————————————————— bitmap

    public void drawLoop(float rawX, float rawY) {

        int x = (int) rawX;
        int y = (int) rawY;

        if (x <= 0) x = 0;
        if (y <= 0) y = 0;

        if (x >= mScreenWidth - mLoopLength / SCALE) x = (int) (mScreenWidth - mLoopLength / SCALE);
        if (y >= mRelativeLayout.getHeight() - mLoopLength / SCALE)
            y = (int) (mRelativeLayout.getHeight() - mLoopLength / SCALE);

        mLoop.setImageBitmap(getBitmapFromPosition(x, y));
    }

    public Bitmap getBitmapFromPosition(int x, int y) {
        Bitmap cache = getCacheBitmap();
        Matrix matrix = new Matrix();
        matrix.postScale(SCALE, SCALE);
        return Bitmap.createBitmap(cache, x, y, mLoopLength / 2, mLoopLength / 2, matrix, false);
    }

    public Bitmap getCacheBitmap() {

        Bitmap bmp = Bitmap.createBitmap(mRelativeLayout.getWidth(),
                mRelativeLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Drawable bgDrawable = mRelativeLayout.getBackground();
        if (bgDrawable != null) bgDrawable.draw(canvas);
        else canvas.drawColor(Color.WHITE);
        mRelativeLayout.draw(canvas);

        return bmp;
    }

    //—————————————————————————————————————————————————————————————————————— calculation
    private void calcMainDists(Context context) {
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels
                - getStatusBarHeight(context);
        mHandLength = (int) (mScreenWidth * 0.1f);
        mLoopLength = mHandLength * 4;
    }

    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
