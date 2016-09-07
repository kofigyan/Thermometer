package com.janslab.thermometer.widgets;

import android.content.Context;
import android.content.res.TypedArray;
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

import com.janslab.thermometer.R;

import java.util.List;

/**
 * Created by Kofi Gyan on 11/27/2015.
 */


public class Thermometer extends View implements SensorEventListener {

    //thermometer circles paints
    private Paint mInnerCirclePaint;
    private Paint mOuterCirclePaint;
    private Paint mFirstOuterCirclePaint;

    //thermometer arc paint
    private Paint mFirstOuterArcPaint;


    //thermometer lines paints
    private Paint mInnerLinePaint;
    private Paint mOuterLinePaint;
    private Paint mFirstOuterLinePaint;


    //thermometer radii
    private int mOuterRadius;
    private int mInnerRadius;
    private int mFirstOuterRadius;

    //thermometer colors
    private int mThermometerColor = Color.rgb(200, 115, 205);

    //thermometer circles and lines variables
    private float mLastCellWidth;
    private int mStageHeight;
    private float mCellWidth;
    private float mStartCenterY; //center of first cell
    private float mEndCenterY; //center of last cell
    private float mStageCenterX;
    private float mXOffset;
    private float mYOffset;

    // I   1st Cell     I  2nd Cell       I  3rd Cell  I
    private static final int NUMBER_OF_CELLS = 3; //three cells in all  ie.stageHeight divided into 3 equal cells

    //thermometer animation variables
    private float mIncrementalTempValue;
    private boolean mIsAnimating;
    private Animator mAnimator;

    private Handler handler;


    //temperature measured
    private float mTemperatureC;
    //temperature range -30 <= temperature <= 50
    private static final float DEFAULT_UPPERMOST_TEMPERATURE_READING = 50;
    private static final float TEMPERATURE_OFFSET = 30; //takes care of distance btn -30 degree celcius and 0 degree celcius

    private float mMaxDistance; //distance to measure to => difference between startingDistanceReading and currentDistanceReading


    public Thermometer(Context context) {
        this(context, null);
    }

    public Thermometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Thermometer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Thermometer, defStyle, 0);

            mThermometerColor = a.getColor(R.styleable.Thermometer_therm_color, mThermometerColor);

            a.recycle();
        }

        init();
    }

    private void init() {

        handler = new Handler();

        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setColor(mThermometerColor);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setStrokeWidth(17f);


        mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterCirclePaint.setColor(Color.WHITE);
        mOuterCirclePaint.setStyle(Paint.Style.FILL);
        mOuterCirclePaint.setStrokeWidth(32f);


        mFirstOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstOuterCirclePaint.setColor(mThermometerColor);
        mFirstOuterCirclePaint.setStyle(Paint.Style.FILL);
        mFirstOuterCirclePaint.setStrokeWidth(60f);


        mFirstOuterArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstOuterArcPaint.setColor(mThermometerColor);
        mFirstOuterArcPaint.setStyle(Paint.Style.STROKE);
        mFirstOuterArcPaint.setStrokeWidth(30f);


        mInnerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerLinePaint.setColor(mThermometerColor);
        mInnerLinePaint.setStyle(Paint.Style.FILL);
        mInnerLinePaint.setStrokeWidth(17f);


        mOuterLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterLinePaint.setColor(Color.WHITE);
        mOuterLinePaint.setStyle(Paint.Style.FILL);


        mFirstOuterLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstOuterLinePaint.setColor(mThermometerColor);
        mFirstOuterLinePaint.setStyle(Paint.Style.FILL);


    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mStageCenterX = getWidth() / 2;

        mStageHeight = getHeight();

        mCellWidth = mStageHeight / NUMBER_OF_CELLS;

        //center of first cell
        mStartCenterY = mCellWidth / 2;


        //move to 3rd cell
        mLastCellWidth = (NUMBER_OF_CELLS * mCellWidth);

        //center of last(3rd) cell
        mEndCenterY = mLastCellWidth - (mCellWidth / 2);


        // mOuterRadius is 1/4 of mCellWidth
        mOuterRadius = (int) (0.25 * mCellWidth);

        mInnerRadius = (int) (0.656 * mOuterRadius);

        mFirstOuterRadius = (int) (1.344 * mOuterRadius);

        mFirstOuterLinePaint.setStrokeWidth(mFirstOuterRadius);

        mOuterLinePaint.setStrokeWidth(mFirstOuterRadius / 2);

        mFirstOuterArcPaint.setStrokeWidth(mFirstOuterRadius / 4);

        mXOffset = mFirstOuterRadius / 4;
        mXOffset = mXOffset / 2;

        //get the difference btn firstOuterLine and innerAnimatedline
        mYOffset = (mStartCenterY + (float) 0.875 * mOuterRadius) - (mStartCenterY + mInnerRadius);
        mYOffset = mYOffset / 2;

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

        //takes care of paddingTop and paddingBottom
        int paddingY = getPaddingBottom() + getPaddingTop();

        //get height and width
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        height += paddingY;

        setMeasuredDimension(width, height);
    }


    private void drawInnerCircle(Canvas canvas) {
        drawCircle(canvas, mInnerRadius, mInnerCirclePaint);
    }

    private void drawOuterCircle(Canvas canvas) {
        drawCircle(canvas, mOuterRadius, mOuterCirclePaint);
    }


    private void drawFirstOuterCircle(Canvas canvas) {
        drawCircle(canvas, mFirstOuterRadius, mFirstOuterCirclePaint);
    }


    private void drawCircle(Canvas canvas, float radius, Paint paint) {
        canvas.drawCircle(mStageCenterX, mEndCenterY, radius, paint);
    }


    private void drawOuterLine(Canvas canvas) {

        float startY = mEndCenterY - (float) (0.875 * mOuterRadius);
        float stopY = mStartCenterY + (float) (0.875 * mOuterRadius);

        drawLine(canvas, startY, stopY, mOuterLinePaint);

    }


    private void drawFirstOuterLine(Canvas canvas) {

        float startY = mEndCenterY - (float) (0.875 * mFirstOuterRadius);
        float stopY = mStartCenterY + (float) (0.875 * mOuterRadius);

        drawLine(canvas, startY, stopY, mFirstOuterLinePaint);
    }


    private void drawLine(Canvas canvas, float startY, float stopY, Paint paint) {
        canvas.drawLine(mStageCenterX, startY, mStageCenterX, stopY, paint);
    }


    private void drawFirstOuterCornerArc(Canvas canvas) {

        float y = mStartCenterY - (float) (0.875 * mFirstOuterRadius);

        RectF rectF = new RectF(mStageCenterX - mFirstOuterRadius / 2 + mXOffset, y + mFirstOuterRadius, mStageCenterX + mFirstOuterRadius / 2 - mXOffset, y + (2 * mFirstOuterRadius) + mYOffset);

        canvas.drawArc(rectF, -180, 180, false, mFirstOuterArcPaint);

    }


    private void animateInnerLine(Canvas canvas) {

        if (mAnimator == null)
            measureTemperature();

        if (!mIsAnimating) {

            //this local variables help to get the distance to measure to
            float uppermostDistanceReading;
            float currentDistanceReading; //  distance equivalence of temperature reading
            float startingDistanceReading; //base distance or distance at which reading starts from ie.reading distance does not start from 0
            float startingUppermostDiff; //difference between startingDistanceReading and uppermostDistanceReading

            float maxTemperature; //maximum temperature NB. makes provision for TEMPERATURE_OFFSET
            float currentTemperatureReading; // current temperature reading NB. makes provision for TEMPERATURE_OFFSET


            currentTemperatureReading = mTemperatureC + TEMPERATURE_OFFSET;
            maxTemperature = DEFAULT_UPPERMOST_TEMPERATURE_READING + TEMPERATURE_OFFSET;

            uppermostDistanceReading = mStartCenterY + mInnerRadius;
            startingDistanceReading = mEndCenterY + (float) (0.875 * mInnerRadius);

            startingUppermostDiff = startingDistanceReading - uppermostDistanceReading;

            //convert temperature reading to its distance equivalence
            currentDistanceReading = (currentTemperatureReading / maxTemperature) * (startingUppermostDiff);

            mMaxDistance = startingDistanceReading - currentDistanceReading;

            mIncrementalTempValue = mEndCenterY + (float) (0.875 * mInnerRadius);

            mIsAnimating = true;


        } else {

            mIncrementalTempValue = mEndCenterY + (float) (0.875 * mInnerRadius) - mIncrementalTempValue;

        }

        if (mIncrementalTempValue > mMaxDistance) {

            float startY = mEndCenterY + (float) (0.875 * mInnerRadius);
            drawLine(canvas, startY, mIncrementalTempValue, mInnerCirclePaint);

        } else {

            float startY = mEndCenterY + (float) (0.875 * mInnerRadius);
            drawLine(canvas, startY, mMaxDistance, mInnerCirclePaint);

            mIsAnimating = false;
            stopMeasurement();
        }

    }


    public void setThermometerColor(int thermometerColor) {
        this.mThermometerColor = thermometerColor;

        mInnerCirclePaint.setColor(mThermometerColor);

        mFirstOuterCirclePaint.setColor(mThermometerColor);

        mFirstOuterArcPaint.setColor(mThermometerColor);

        mInnerLinePaint.setColor(mThermometerColor);

        mFirstOuterLinePaint.setColor(mThermometerColor);

        invalidate();
    }


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
                int startY = (int) (mStartCenterY - (float) (0.875 * mInnerRadius));
                int dy = (int) (mEndCenterY + mInnerRadius);
                mScroller.startScroll(0, startY, 0, dy, ANIM_DURATION);
                mRestartAnimation = false;
            }

            boolean isScrolling = mScroller.computeScrollOffset();
            mIncrementalTempValue = mScroller.getCurrY();

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


    private void stopMeasurement() {
        if (mAnimator != null)
            mAnimator.stop();
    }


    private SensorManager getSensorManager() {
        return (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
    }

    private void attachToSensor() {

        SensorManager sensorManager = getSensorManager();

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
        } else {
            Toast.makeText(getContext(), "No Temperature Sensor Found", Toast.LENGTH_LONG).show();
        }
    }

    private void detachFromSensor() {
        SensorManager sensorManager = getSensorManager();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.values.length > 0) {

            mTemperatureC = sensorEvent.values[0];

        } else {
            Toast.makeText(getContext(), "Sensor Not Found", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

        stopMeasurement();

        super.onDetachedFromWindow();

    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        switch (visibility) {
            case View.VISIBLE:

                measureTemperature();

                break;

            default:

                stopMeasurement();

                break;
        }
    }


}
