package me.motofeedback;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import me.motofeedback.Helper.Range;

/**
 * Created by admin on 30.07.2016.
 */
public class Motion {

    private Context mContext;

    public interface IMotionListener {
        void changeAlarm(boolean enable);

        void startAlarm(int count, int msg);

        void difInProcessMotion(int xy, int xz, int zy);
    }

    IMotionListener mIMotionListener;

    public void setIMotionListener(IMotionListener iMotionListener) {
        mIMotionListener = iMotionListener;
    }


    private SensorManager mSensorManager;
    private Sensor mTypeAccelerometer;
    private Sensor mTypeMagneticField;

    public Motion(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mTypeAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mTypeMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private Boolean mMotionListenerEnabled = false;
    private boolean mMotionListenerProcess = false;
    private boolean autoRangeStart;
    private int countAlarm;
    private int msgAlarm = -1;
    private long prevCountAlarm;
    private boolean autoRange2Start;
    private long startMillis;
    private long delay = -1;

    private boolean startIdle;
    private long startIdleMillis;
    private long delayIdle = -1;
    //__
    private boolean afterAutoRange2 = false;

    public boolean isMotionListenerWatching() {
        return mMotionListenerEnabled;
    }

    public void changeWatching(boolean enable) {
        synchronized (mMotionListenerEnabled) {
            mMotionListenerProcess = false;
            startMillis = -1;
            countAlarm = 0;
            prevCountAlarm = 0;
            delay = me.motofeedback.mApplication.getSettings().getDelayDimensionRange();
            autoRange2Start = false;

            startIdle = false;
            startIdleMillis = -1;

            autoRangeStart = enable;
            mMotionListenerEnabled = enable;
            //__
            afterAutoRange2 = false;
            msgAlarm = Range.MSG_ALARM_NONE;
            //if (enable)
            clearRangeTracing();
        }
        if (null != mIMotionListener) {
            synchronized (mIMotionListener) {
                if (!enable)
                    mIMotionListener.changeAlarm(false);
            }
        }
    }

    private void processMotionListener(double XY, double XZ, double ZY, boolean check) {
        synchronized (mMotionListenerEnabled) {
            if (mMotionListenerEnabled) {

                if (autoRange2Start) {
                    mMotionListenerProcess = false;
                    if (startMillis == -1) {
                        startMillis = System.currentTimeMillis();
                        delay = me.motofeedback.mApplication.getSettings().getDelayResetDimensionRange();
                    } else if ((System.currentTimeMillis() - startMillis) >= delay) {
                        delay = me.motofeedback.mApplication.getSettings().getDelaySecondDimensionRange();
                        autoRangeStart = true;
                        autoRange2Start = false;
                        startMillis = -1;
                        //__
                        saveRangeTracing();
                        afterAutoRange2 = true;
                    }
                }

                if (autoRangeStart) {
                    if (startMillis == -1) {
                        startMillis = System.currentTimeMillis();
                        startRangeTracing();
                    } else if ((System.currentTimeMillis() - startMillis) >= delay) {
                        //mIMotionListener.startAlarm(false);
                        autoRangeStart = false;
                        mMotionListenerProcess = true;
                        stopRangeTracing();
                        synchronized (mIMotionListener) {
                            if (null != mIMotionListener) mIMotionListener.changeAlarm(true);
                        }
                    }
                }

                if (mMotionListenerProcess) {
                    if (afterAutoRange2) {
                        afterAutoRange2 = false;
                        msgAlarm = difOldRangeTracing();
                        if (msgAlarm != Range.MSG_ALARM_NONE)
                            check = true;
                    }

                    int rxy = rangeXY.getDif(XY);
                    if (rxy != 0) {
                        //TLog.Log("XY - [" + rangeXY.fromDegreeEp + ":" + rangeXY.toDegreeEp + "] " + rxy);
                    }

                    int rxz = rangeXZ.getDif(XZ);
                    if (rxz != 0) {
                        //TLog.Log("XZ - [" + rangeXZ.fromDegreeEp + ":" + rangeXZ.toDegreeEp + "] " + rxz);
                    }

                    int rzy = rangeZY.getDif(ZY);
                    if (rzy != 0) {
                        //TLog.Log("ZY - [" + rangeZY.fromDegreeEp + ":" + rangeZY.toDegreeEp + "] " + rzy);
                    }

                    mIMotionListener.difInProcessMotion(rxy, rxz, rzy);

                    if (rxy != 0 || rxz != 0 || rzy != 0)
                        countAlarm++;

                    if (startIdle && countAlarm == 1 && me.motofeedback.mApplication.getSettings().isDTAlaram()) {
                        if (startIdleMillis == -1) {
                            startIdleMillis = System.currentTimeMillis();
                            delayIdle = me.motofeedback.mApplication.getSettings().getDIdle();
                        } else if ((System.currentTimeMillis() - startIdleMillis) >= delayIdle) {
                            startIdleMillis = -1;
                            countAlarm = 0;
                            prevCountAlarm = 0;
                        }
                    }

                    if (prevCountAlarm != countAlarm) {
                        prevCountAlarm = countAlarm;
                        if (null != mIMotionListener)
                            synchronized (mIMotionListener) {
                                mIMotionListener.startAlarm(countAlarm, Range.MSG_ALARM_NONE);
                            }
                        if (countAlarm == 1 && me.motofeedback.mApplication.getSettings().isDTAlaram()) {
                            autoRange2Start = true;
                            startIdle = true;
                            startMillis = -1;
                        }
                    }
                    if (check) {
                        if (null != mIMotionListener)
                            synchronized (mIMotionListener) {
                                mIMotionListener.startAlarm(countAlarm, msgAlarm);
                            }
                    }
                }
            }
        }
    }

    public void startSensor() {
        startSensor(me.motofeedback.mApplication.getSettings().getSamplingPeriodUs());
    }

    private void startSensor(int samplingPeriodUs) {
        if (null != mTypeAccelerometer)
            mSensorManager.registerListener(mSensorEventListener, mTypeAccelerometer, samplingPeriodUs);
        if (null != mTypeMagneticField)
            mSensorManager.registerListener(mSensorEventListener, mTypeMagneticField, samplingPeriodUs);
    }

    public void stopSensor() {
        changeWatching(false);
        mSensorManager.unregisterListener(mSensorEventListener);
        if (null != mIMotionLogListener)
            synchronized (mIMotionLogListener) {
                mIMotionLogListener.clearRange();
            }
    }

    public interface IMotionLogListener {
        void clearRange();

        void setRange(int from1, int to1, int fromEp1, int toEp1,
                      int from2, int to2, int fromEp2, int toEp2,
                      int from3, int to3, int fromEp3, int toEp3);

        void changed(int xy, int xz, int zy, int dxy, int dxz, int dzy);
    }

    public IMotionLogListener mIMotionLogListener;

    public void setIMotionLogListener(IMotionLogListener iMotionLogListener) {
        mIMotionLogListener = iMotionLogListener;
    }

    SensorEventListener mSensorEventListener = new SensorEventListener() {

        float[] gravity = new float[3];
        float[] geomagnetic = new float[3];
        float[] orientation_matrics = new float[3];
        float[] rotation_matrics = new float[16];

        @Override
        public void onSensorChanged(SensorEvent event) {

            final int type = event.sensor.getType();
            switch (type) {
                case Sensor.TYPE_ACCELEROMETER:
                    gravity = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    geomagnetic = event.values.clone();
                    break;
            }

            int XY = (int) gravity[0];
            int XZ = (int) gravity[1];
            int ZY = (int) gravity[2];

            if (null != mTypeMagneticField && geomagnetic[0] != 0 && geomagnetic[0] != geomagnetic[1] && geomagnetic[1] != geomagnetic[2]) {
                SensorManager.getRotationMatrix(rotation_matrics, null, gravity, geomagnetic);
                SensorManager.getOrientation(rotation_matrics, orientation_matrics);
                XY = (int) Math.round(Math.toDegrees(orientation_matrics[0]));
                XZ = (int) Math.round(Math.toDegrees(orientation_matrics[1]));
                ZY = (int) Math.round(Math.toDegrees(orientation_matrics[2]));
            }

            double dXY = 0;
            double dXZ = 0;
            double dZY = 0;
            boolean isChangedRange = false;
            boolean isClearRange = false;
            boolean isCheck = false;
            synchronized (mTracing) {
                switch (mTracing) {
                    case TRACING_ON:
                        isClearRange = true;
                        mTracing = TRACING_PROCESS;
                        break;
                    case TRACING_OFF:
                        isChangedRange = true;
                        mTracing = TRACING_DISABLE;
                        break;
                    case TRACING_CLEAR:
                        isClearRange = true;
                        mTracing = TRACING_DISABLE;
                        break;
                    case TRACING_CHECK:
                        mTracing = mPrevTracing;
                        mPrevTracing = TRACING_NONE;
                        isCheck = true;
                        break;
                }

                if (mTracing == TRACING_PROCESS) {
                }

                dXY = rangeXY.processRange(XY, mTracing == TRACING_PROCESS);
                dXZ = rangeXZ.processRange(XZ, mTracing == TRACING_PROCESS);
                dZY = rangeZY.processRange(ZY, mTracing == TRACING_PROCESS);
            }

            if (null != mIMotionLogListener)
                synchronized (mIMotionLogListener) {
                    if (isChangedRange || isCheck)
                        mIMotionLogListener.setRange(
                                rangeXY.fromDegree, rangeXY.toDegree, rangeXY.fromDegreeEp, rangeXY.toDegreeEp,
                                rangeXZ.fromDegree, rangeXZ.toDegree, rangeXZ.fromDegreeEp, rangeXZ.toDegreeEp,
                                rangeZY.fromDegree, rangeZY.toDegree, rangeZY.fromDegreeEp, rangeZY.toDegreeEp);
                    if (isClearRange)
                        mIMotionLogListener.clearRange();

                    mIMotionLogListener.changed(XY, XZ, ZY, (int) dXY, (int) dXZ, (int) dZY);
                }
            processMotionListener(XY, XZ, ZY, isCheck);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    //####################### Tracing

    private static final int TRACING_NONE = -1;
    private static final int TRACING_DISABLE = 0;
    private static final int TRACING_ON = 1;
    private static final int TRACING_PROCESS = 2;
    private static final int TRACING_OFF = 3;
    private static final int TRACING_CLEAR = 4;
    private static final int TRACING_CHECK = 5;
    private Integer mTracing = TRACING_DISABLE;
    private Integer mPrevTracing = TRACING_NONE;

    Range rangeXY = new Range();
    Range rangeXZ = new Range();
    Range rangeZY = new Range();

    Range.TRange rangeXY_old = new Range.TRange();
    Range.TRange rangeXZ_old = new Range.TRange();
    Range.TRange rangeZY_old = new Range.TRange();

    private int difOldRangeTracing() {
        int retXY = rangeXY.getDif(rangeXY_old);
        int retXZ = rangeXZ.getDif(rangeXZ_old);
        int retZY = rangeZY.getDif(rangeZY_old);
        int ret = retXY + retXZ + retZY;
        int tmp = retXY != 0 ? 1 : 0;
        tmp += retXZ != 0 ? 1 : 0;
        tmp += retZY != 0 ? 1 : 0;
        ret = ret > 0 ? ret / tmp : ret;
        ret = ret < -1 ? -1 : ret;
        return ret;
    }

    private void saveRangeTracing() {
        rangeXY_old.setRange(rangeXY);
        rangeXZ_old.setRange(rangeXZ);
        rangeZY_old.setRange(rangeZY);
    }

    public void startRangeTracing() {
        synchronized (mTracing) {
            rangeXY.setZero(me.motofeedback.mApplication.getSettings().getZeroXY());
            rangeXZ.setZero(me.motofeedback.mApplication.getSettings().getZeroXZ());
            rangeZY.setZero(me.motofeedback.mApplication.getSettings().getZeroZY());

            rangeXY.clearRange();
            rangeXZ.clearRange();
            rangeZY.clearRange();
            mTracing = TRACING_ON;
        }
    }

    public void stopRangeTracing() {
        synchronized (mTracing) {
            rangeXY.stopRange();
            rangeXZ.stopRange();
            rangeZY.stopRange();
            mTracing = TRACING_OFF;
        }
    }

    public void clearRangeTracing() {
        synchronized (mTracing) {

            rangeXY.clearRange();
            rangeXZ.clearRange();
            rangeZY.clearRange();

            mTracing = TRACING_CLEAR;
        }
    }

    public void checkRangeTracing() {
        synchronized (mTracing) {
            mPrevTracing = mTracing;
            mTracing = TRACING_CHECK;
        }
    }

    //#############################################

    public void onDestroy() {
        stopSensor();
        if (null != mIMotionListener) {
            synchronized (mIMotionListener) {
                mIMotionListener = null;
            }
        }

        if (null != mIMotionLogListener) {
            synchronized (mIMotionLogListener) {
                mIMotionLogListener = null;
            }
        }
        mIMotionLogListener = null;
    }
}
