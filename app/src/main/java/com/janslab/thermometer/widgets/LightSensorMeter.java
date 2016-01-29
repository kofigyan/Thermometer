package com.janslab.thermometer.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;
import android.widget.Toast;

import java.util.List;


/**
 * Created by Kofi Gyan on 11/27/2015.
 */


public class LightSensorMeter extends View implements SensorEventListener {

    private static final String TAG = Thermometer.class.getSimpleName();

    private Handler handler;


    //circle paint
    private Paint mInnerCirclePaint;
    private Paint mOuterCirclePaint;
    private Paint mFirstOuterCirclePaint;

    //arc paint
    private Paint mFirstOuterArcPaint;


    //line paint
    private Paint mInnerLinePaint;
    private Paint mOuterLinePaint;
    private Paint mFirstOuterLinePaint;


    //radii
    private int mOuterRadius;
    private int mInnerRadius;
    private int mFirstOuterRadius;


    //circles and lines values variables
    private float mLastCellWidth;
    private int mStageHeight;
    private float mCellWidth;
    private float mStartCenterY; //center of first cell
    private float mEndCenterY; //center of last cell
    private float mStageCenterX;
    private float xOffset;
    private float yOffset;

    // I   1st Cell     I  2nd Cell       I  3rd Cell  I
    private static final int mNoOfCells = 3; //three cells in all  ie.stageHeight divided into 3 equal cells

    private float incrementalTempValue;
    private boolean isAnimationStarted = false;


    Animator mAnimator;


    public LightSensorMeter(Context context) {
        this(context, null);
    }

    public LightSensorMeter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        handler = new Handler();

        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setColor(Color.rgb(200, 115, 205));
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setStrokeWidth(17f);


        mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterCirclePaint.setColor(Color.WHITE);
        mOuterCirclePaint.setStyle(Paint.Style.FILL);
        mOuterCirclePaint.setStrokeWidth(32f);


        mFirstOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstOuterCirclePaint.setColor(Color.rgb(200, 115, 205));
        mFirstOuterCirclePaint.setStyle(Paint.Style.FILL);
        mFirstOuterCirclePaint.setStrokeWidth(60f);


        mFirstOuterArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstOuterArcPaint.setColor(Color.rgb(200, 115, 205));
        mFirstOuterArcPaint.setStyle(Paint.Style.STROKE);
        mFirstOuterArcPaint.setStrokeWidth(30f);


        mInnerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerLinePaint.setColor(Color.rgb(200, 115, 205));
        mInnerLinePaint.setStyle(Paint.Style.FILL);
        mInnerLinePaint.setStrokeWidth(17f);

        mOuterLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterLinePaint.setColor(Color.WHITE);
        mOuterLinePaint.setStyle(Paint.Style.FILL);


        mFirstOuterLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstOuterLinePaint.setColor(Color.rgb(200, 115, 205));
        mFirstOuterLinePaint.setStyle(Paint.Style.FILL);


    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mStageCenterX = getWidth() / 2;

        mStageHeight = getHeight();

        mCellWidth = mStageHeight / mNoOfCells;

        //center of first cell
        mStartCenterY = mCellWidth / 2;


        //move to 3rd cell
        mLastCellWidth = (mNoOfCells * mCellWidth);

        //center of last(3rd) cell
        mEndCenterY = mLastCellWidth - (mCellWidth / 2);


        // mOuterRadius is 1/4 of mCellWidth
        mOuterRadius = (int) (0.25 * mCellWidth);

        mInnerRadius = (int) (0.656 * mOuterRadius);

        mFirstOuterRadius = (int) (1.344 * mOuterRadius);

        mFirstOuterLinePaint.setStrokeWidth(mFirstOuterRadius);

        mOuterLinePaint.setStrokeWidth(mFirstOuterRadius / 2);

        mFirstOuterArcPaint.setStrokeWidth(mFirstOuterRadius / 4);

        xOffset = mFirstOuterRadius / 4;
        xOffset = xOffset / 2;

        //get the d/f btn firstOuterLine and innerAnimatedline
        yOffset = (mStartCenterY + (float) 0.875 * mOuterRadius) - (mStartCenterY + mInnerRadius);
        yOffset = yOffset / 2;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawFirstOuterCircle(canvas);

        drawOuterCircle(canvas);

        drawInnerCircle(canvas);

        drawFirstOuterLine(canvas);

        drawOuterLine(canvas);

        animateInnerLine(canvas);

        drawFirstOuterCornerArc(canvas);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //take care of paddingTop and paddingBottom
        int paddingY = getPaddingBottom() + getPaddingTop();

        //get height and width
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        height += paddingY;

        setMeasuredDimension(width, height);
    }


    private void drawInnerCircle(Canvas canvas) {
        drawCircle(canvas, mStageCenterX, mEndCenterY, mInnerRadius, mInnerCirclePaint);
    }

    private void drawOuterCircle(Canvas canvas) {
        drawCircle(canvas, mStageCenterX, mEndCenterY, mOuterRadius, mOuterCirclePaint);
    }


    private void drawFirstOuterCircle(Canvas canvas) {
        drawCircle(canvas, mStageCenterX, mEndCenterY, mFirstOuterRadius, mFirstOuterCirclePaint);
    }


    private void drawCircle(Canvas canvas, float cx, float cy, float radius, Paint paint) {
        canvas.drawCircle(cx, cy, radius, paint);
    }


    private void drawOuterLine(Canvas canvas) {
        canvas.drawLine(mStageCenterX, mEndCenterY - (float) (0.875 * mOuterRadius), mStageCenterX, mStartCenterY + (float) (0.875 * mOuterRadius), mOuterLinePaint);
    }


    private void drawFirstOuterLine(Canvas canvas) {
        canvas.drawLine(mStageCenterX, mEndCenterY - (float) (0.875 * mFirstOuterRadius), mStageCenterX, mStartCenterY + (float) (0.875 * mOuterRadius), mFirstOuterLinePaint);
    }


    //simulate temperature measurement for now

    float uppermostDistanceReading;
    float currentDistanceReading;
    float startingDistanceReading; //base distance or distance at which reading starts from ie.reading distance does not start from 0

    static final float DEFAULT_UPPERMOST_TEMPERATURE_READING = 150; //IL for Light or 50 Celcius for temperature
    float currentTemperatureReading;

    float startingUppermostDiff;

    float distanceToMeasureTo;

    private void animateInnerLine(Canvas canvas) {

        if (isAnimationStarted == false) {

            Toast.makeText(getContext(), String.valueOf(temperatureC)+ " LUX", Toast.LENGTH_LONG).show();

            currentTemperatureReading = temperatureC;

            uppermostDistanceReading = mStartCenterY + mInnerRadius;
            startingDistanceReading = mEndCenterY + (float) (0.875 * mInnerRadius);

            // Toast.makeText(getContext(),String.valueOf(startingDistanceReading) + " starting distance reading",Toast.LENGTH_LONG).show();
            // Toast.makeText(getContext(),String.valueOf(uppermostDistanceReading) + " uppermost distance reading" ,Toast.LENGTH_LONG).show();

            startingUppermostDiff = startingDistanceReading - uppermostDistanceReading;

            //currentDistanceReading = (DEFAULT_UPPERMOST_TEMPERATURE_READING /currentTemperatureReading) * ( startingDistanceReading - uppermostDistanceReading ) ;

            currentDistanceReading = (currentTemperatureReading / DEFAULT_UPPERMOST_TEMPERATURE_READING) * (startingUppermostDiff);

            //Toast.makeText(getContext(),String.valueOf(startingUppermostDiff) + " startingUppermostDiff reading",Toast.LENGTH_LONG).show();
            //Toast.makeText(getContext(),String.valueOf(currentDistanceReading) + " currentDistanceReading reading" ,Toast.LENGTH_LONG).show();

            distanceToMeasureTo = startingDistanceReading - currentDistanceReading;


            //  Toast.makeText(getContext(),String.valueOf(startingDistanceReading) + " starting distance reading",Toast.LENGTH_LONG).show();
            //Toast.makeText(getContext(),String.valueOf(distanceToMeasureTo ) + " distanceToMeasureTo reading" ,Toast.LENGTH_LONG).show();


            incrementalTempValue = mEndCenterY + (float) (0.875 * mInnerRadius);

            isAnimationStarted = true;

        } else {

            incrementalTempValue = mEndCenterY + (float) (0.875 * mInnerRadius) - incrementalTempValue;

        }

        // if (incrementalTempValue > mStartCenterY + mInnerRadius) {
        if (incrementalTempValue > distanceToMeasureTo) {

            canvas.drawLine(mStageCenterX, mEndCenterY + (float) (0.875 * mInnerRadius), mStageCenterX, incrementalTempValue, mInnerCirclePaint);

        } else {

            // canvas.drawLine(mStageCenterX, mEndCenterY + (float) (0.875 * mInnerRadius), mStageCenterX, mStartCenterY + mInnerRadius , mInnerCirclePaint);
            canvas.drawLine(mStageCenterX, mEndCenterY + (float) (0.875 * mInnerRadius), mStageCenterX, distanceToMeasureTo, mInnerCirclePaint);

        }

    }


    //simulate temperature measurement for now
    private void measureTemperature() {
        mAnimator = new Animator();
        mAnimator.start();
    }


    private class Animator implements Runnable {
        private Scroller mScroller;
        private final static int ANIM_START_DELAY = 1000;
        private final static int ANIM_DURATION = 4000;
        private boolean mRestartAnimation = false;

        public Animator() {
            mScroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
        }

        public void run() {
            if (mAnimator != this)
                return;

            if (mRestartAnimation) {
                mScroller.startScroll(0, (int) (mStartCenterY - (float) (0.875 * mInnerRadius)), 0, (int) (mEndCenterY + mInnerRadius), ANIM_DURATION);
                mRestartAnimation = false;
            }

            boolean isScrolling = mScroller.computeScrollOffset();
            incrementalTempValue = mScroller.getCurrY();

            if (isScrolling) {
                invalidate();
                post(this);
            } else {
                stop();
            }


        }

        public void start() {
            mRestartAnimation = true;
            postDelayed(this, ANIM_START_DELAY);
        }

        public void stop() {
            removeCallbacks(this);
            mAnimator = null;
        }

    }


    @Override
    protected void onAttachedToWindow() {

        super.onAttachedToWindow();

        attachToSensor();

        measureTemperature();

    }

    @Override
    protected void onDetachedFromWindow() {

        detachFromSensor();


        super.onDetachedFromWindow();


        stopAnimation();

    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        switch (visibility) {
            case View.VISIBLE:

                measureTemperature();

                break;

            default:

                stopAnimation();

                break;
        }
    }


    private void drawFirstOuterCornerArc(Canvas canvas) {

        float y = mStartCenterY - (float) (0.875 * mFirstOuterRadius);

        RectF rectF = new RectF(mStageCenterX - mFirstOuterRadius / 2 + xOffset, y + mFirstOuterRadius, mStageCenterX + mFirstOuterRadius / 2 - xOffset, y + (2 * mFirstOuterRadius) + yOffset);

        canvas.drawArc(rectF, -180, 180, false, mFirstOuterArcPaint);

    }


    private void stopAnimation() {
        if (mAnimator != null)
            mAnimator.stop();
    }


    private SensorManager getSensorManager() {
        return (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
    }

    private void attachToSensor() {

        SensorManager sensorManager = getSensorManager();

        //List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_TEMPERATURE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);

        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
        } else {
            Log.e(TAG, "No temperature sensor found");
            Toast.makeText(getContext(), "Not found", Toast.LENGTH_LONG).show();
        }
    }

    private void detachFromSensor() {
        SensorManager sensorManager = getSensorManager();
        sensorManager.unregisterListener(this);
    }

    int counter;
    float temperatureC;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.values.length > 0) {

            temperatureC = sensorEvent.values[0];
            Log.i(TAG, "*** Temperature: " + temperatureC);

            //float temperatureF = (9.0f / 5.0f) * temperatureC + 32.0f;

            // if(counter < 1)
            //Toast.makeText(getContext(),String.valueOf(temperatureC),Toast.LENGTH_LONG).show();

            counter++;

        } else {
            Log.w(TAG, "Empty sensor event received");
            Toast.makeText(getContext(), "Not found", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}


