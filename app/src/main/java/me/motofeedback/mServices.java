package me.motofeedback;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

import me.motofeedback.Bluetooth.BluetoothComunicator;
import me.motofeedback.Bluetooth.StateMessages;
import me.motofeedback.visual.MainActivity;

/**
 * Created by admin on 30.07.2016.
 */
public class mServices extends Service {
    private static final String PARAM_SERVICE = "param_service";
    private static final int RESSTART_ALL = 0;
    private static final int RESSTART_GPS = 1;
    private static final int RESSTART_MOTION = 2;

    //#####################################
    //Launchers

    public static boolean isStartedServices() {
        synchronized (mStartedServices) {
            if (mStartedServices) return true;
        }
        return false;
    }

    public static void StartServices(final Context context) {
        if (isStartedServices()) return;
        Intent startServiceIntent = new Intent(context, me.motofeedback.mServices.class);
        startServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(startServiceIntent);
    }


    public static void StopServices(final Context context) {
        if (!isStartedServices()) return;
        Intent startServiceIntent = new Intent(context, me.motofeedback.mServices.class);
        context.stopService(startServiceIntent);
    }

    public static void reinitGPS(final Context context) {
        Intent intent = new Intent(context, me.motofeedback.mServices.class).putExtra(PARAM_SERVICE, RESSTART_GPS);
        context.startService(intent);
    }

    public static void reinitMotion(final Context context) {
        Intent intent = new Intent(context, me.motofeedback.mServices.class).putExtra(PARAM_SERVICE, RESSTART_MOTION);
        context.startService(intent);
    }

    public static void reinitAll(final Context context) {
        Intent intent = new Intent(context, me.motofeedback.mServices.class).putExtra(PARAM_SERVICE, RESSTART_ALL);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final int NOTIFICATION_ID = 1001;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Notification notification = new Notification(R.drawable.ic_launcher, "сервис запущен", System.currentTimeMillis());
        if (isStartedServices() && null != intent) {
            int param = intent.getIntExtra(PARAM_SERVICE, -1);
            if (RESSTART_ALL == param) {
                this.initSensor(true);
                return START_STICKY;
            } else if (RESSTART_GPS == param) {
                this.initGPS(true);
                return START_STICKY;
            } else if (RESSTART_MOTION == param) {
                this.initMotion(true);
                return START_STICKY;
            }
        }
        if (me.motofeedback.mApplication.getSettings().isServer())
            return START_NOT_STICKY;
        if (null == intent)
            intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText("сервис запущен")
                .setContentIntent(pendingIntent).build();
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    //#####################################
    //Services

    private static Boolean mStartedServices = false;
    private static BluetoothComunicator mBluetooth;
    private static Motion mMotion;
    private static GPS mGPS;

    @Override
    public void onCreate() {
        synchronized (mStartedServices) {
            mStartedServices = true;
        }
        super.onCreate();
        initSensor(false);
        //#######################
        try {
            mBluetooth = new BluetoothComunicator(this);
            mBluetooth.setICommunicatorState(mICommunicatorState);
            mBluetooth.setIChangeState(mIChangeState);
        } catch (Error e) {
            //ToDo неподдерживаемые устройства
        }
        //#######################
        CheckBluetooth();
        MainActivity.updateCheckedUI(IsWatching(), GPSisEnabled());
    }

    private void initSensor(boolean resstart) {
        initGPS(resstart);
        initMotion(resstart);
    }

    private void initGPS(boolean resstart) {
        if (me.motofeedback.mApplication.getSettings().isClient()) {
            if (null != mGPS) {
                if (resstart)
                    mGPS.onDestroy();
                else
                    return;
            }
            mGPS = new GPS(this);
            mGPS.setILocationChanged(mILocationChanged);
            //mGPS.changeLocationState(false);
            return;
        }
        if (me.motofeedback.mApplication.getSettings().isServer()) {
            if (null == mGPS) return;
            mGPS.onDestroy();
            mGPS = null;
            return;
        }
    }

    private void initMotion(boolean resstart) {
        if (me.motofeedback.mApplication.getSettings().isClient()) {
            if (null != mMotion) {
                if (resstart)
                    mMotion.onDestroy();
                else
                    return;
            }
            mMotion = new Motion(this);
            mMotion.setIMotionLogListener(mIMotionLogListener);
            mMotion.setIMotionListener(mIMotionListener);
            mMotion.startSensor();
            if (me.motofeedback.mApplication.getSettings().isChengedMotion()) {
                ChangeWatching(true);
            }
            return;
        }
        if (me.motofeedback.mApplication.getSettings().isServer()) {
            if (null == mMotion) return;
            mMotion.onDestroy();
            mMotion = null;
            return;
        }
    }

    @Override
    public void onDestroy() {
        synchronized (mStartedServices) {
            mStartedServices = false;
        }
        super.onDestroy();
        if (null != mGPS) mGPS.onDestroy();
        mGPS = null;
        if (null != mMotion) mMotion.onDestroy();
        mMotion = null;
        if (null != mBluetooth) mBluetooth.onDestory();
        mBluetooth = null;

        if (me.motofeedback.mApplication.getSettings().isClient()) {
            StartServices(this);
            return;
        }
        if (me.motofeedback.mApplication.getSettings().isServer()) {
        }
    }

    //#####################################
    //List Interfaces

    private static ArrayList<Motion.IMotionLogListener> listIMotionLogs = new ArrayList<>();
    private static ArrayList<Motion.IMotionListener> listIMotion = new ArrayList<>();
    private static ArrayList<BluetoothComunicator.IChangeState> listIChangeState = new ArrayList<>();

    private Motion.IMotionLogListener mIMotionLogListener = new Motion.IMotionLogListener() {

        @Override
        public void setRange(int from1, int to1, int fromEp1, int toEp1, int from2, int to2, int fromEp2, int toEp2, int from3, int to3, int fromEp3, int toEp3) {
            synchronized (listIMotionLogs) {
                for (Motion.IMotionLogListener it : listIMotionLogs) {
                    it.setRange(from1, to1, fromEp1, toEp1, from2, to2, fromEp2, toEp2, from3, to3, fromEp3, toEp3);
                }
            }
        }

        @Override
        public void clearRange() {
            synchronized (listIMotionLogs) {
                for (Motion.IMotionLogListener it : listIMotionLogs) {
                    it.clearRange();
                }
            }
        }

        @Override
        public void changed(int xy, int xz, int zy, int dxy, int dxz, int dzy) {
            synchronized (listIMotionLogs) {
                for (Motion.IMotionLogListener it : listIMotionLogs) {
                    it.changed(xy, xz, zy, dxy, dxz, dzy);
                }
            }
        }
    };

    public static void AddIMotionLogListener(Motion.IMotionLogListener mIMotionLogListener) {
        synchronized (listIMotionLogs) {
            listIMotionLogs.add(mIMotionLogListener);
        }
    }

    public static void EraceIMotionLogListener(Motion.IMotionLogListener mIMotionLogListener) {
        synchronized (listIMotionLogs) {
            listIMotionLogs.remove(mIMotionLogListener);
        }
    }

    private Motion.IMotionListener mIMotionListener = new Motion.IMotionListener() {
        @Override
        public void changeAlarm(boolean enable) {
            synchronized (listIMotion) {
                for (Motion.IMotionListener it : listIMotion) {
                    it.changeAlarm(enable);
                }
            }
        }

        @Override
        public void startAlarm(int count, int msg) {
            synchronized (listIMotion) {
                for (Motion.IMotionListener it : listIMotion) {
                    //count ==1 msg == "EMPTY" or ""
                    //count >1 msg == "EMPTY" or ""
                    //count ==0 msg != ("EMPTY" or "")
                    it.startAlarm(count, msg);
                }
            }
        }

        @Override
        public void difInProcessMotion(int xy, int xz, int zy) {
            synchronized (listIMotion) {
                for (Motion.IMotionListener it : listIMotion) {
                    it.difInProcessMotion(xy, xz, zy);
                }
            }
        }
    };

    public static void AddIMotionListener(Motion.IMotionListener mIMotionListener) {
        synchronized (listIMotion) {
            listIMotion.add(mIMotionListener);
        }
    }

    public static void EraceIMotionListener(Motion.IMotionListener mIMotionListener) {
        synchronized (listIMotion) {
            listIMotion.remove(mIMotionListener);
        }
    }

    private BluetoothComunicator.IChangeState mIChangeState = new BluetoothComunicator.IChangeState() {
        @Override
        public void change(STATE_BTC state) {
            synchronized (listIChangeState) {
                for (BluetoothComunicator.IChangeState it : listIChangeState) {
                    it.change(state);
                }
            }
        }
    };

    public static void AddIChangeState(BluetoothComunicator.IChangeState mIChangeState) {
        synchronized (listIChangeState) {
            listIChangeState.add(mIChangeState);
        }
    }

    public static void EraceIChangeState(BluetoothComunicator.IChangeState mIChangeState) {
        synchronized (listIChangeState) {
            listIChangeState.remove(mIChangeState);
        }
    }

    //#####################################
    //Action

    public static synchronized void ChangeWatching(final boolean enable) {
        if (me.motofeedback.mApplication.getSettings().isClient()) {
            if (!(enable ^ mServices.IsWatching())) return;
            /*
            if (enable) {
                mMotion.startSensor();
                */
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMotion.changeWatching(enable);
                }
            }, 100);
            /*
            } else {
                mMotion.stopSensor();
            }
            */

            me.motofeedback.mApplication.getSettings().setChangeMotion(enable);
            return;
        }
        if (me.motofeedback.mApplication.getSettings().isServer()) {
            SendData(StateMessages.MSG_CHANGE_MOTION + (enable ? "1" : "0"));
            return;
        }
    }

    public static void SendCheckWatching(boolean isHandle) {
        if (null == mMotion || !mMotion.isMotionListenerWatching())
            return;
        Runnable checkingWatching = new Runnable() {
            @Override
            public void run() {
                mMotion.checkRangeTracing();
            }
        };
        if (isHandle)
            new Handler(Looper.getMainLooper()).postDelayed(checkingWatching, 100);
        else
            checkingWatching.run();

    }

    public static synchronized boolean IsWatching() {
        if (null == mMotion) return false;
        return mMotion.isMotionListenerWatching();
    }

    //#####################################
    //GPS
    private static ArrayList<GPS.ILocationChanged> listILocationChanged = new ArrayList<>();

    GPS.ILocationChanged mILocationChanged = new GPS.ILocationChanged() {
        @Override
        public void changedLocation(String location) {
            synchronized (listILocationChanged) {
                for (GPS.ILocationChanged it : listILocationChanged) {
                    it.changedLocation(location);
                }
            }
        }

        @Override
        public void changedGPS(boolean on) {
            synchronized (listILocationChanged) {
                for (GPS.ILocationChanged it : listILocationChanged) {
                    it.changedGPS(on);
                }
            }
        }
    };

    public static void AddILocationChanged(GPS.ILocationChanged locationChanged) {
        synchronized (listILocationChanged) {
            listILocationChanged.add(locationChanged);
        }
        if (null != mGPS) mGPS.getLocation();
    }

    public static void EraceILocationChanged(GPS.ILocationChanged locationChanged) {
        synchronized (listILocationChanged) {
            listILocationChanged.remove(locationChanged);
        }
    }

    public static synchronized boolean GPSisEnabled() {
        if (null == mGPS) return false;
        return mGPS.isEnabled();
    }

    public static synchronized void GPSSetEnabled(boolean enable) {
        if (me.motofeedback.mApplication.getSettings().isClient()) {
            //if (mServices.GPSisToggle() &&
            if (!(enable ^ mServices.GPSisEnabled())) return;
            if (null != mGPS) mGPS.changeLocationState(enable);
            return;
        }
        if (me.motofeedback.mApplication.getSettings().isServer()) {
            SendData(StateMessages.MSG_CHANGE_GPS + (enable ? 1 : 0));
            return;
        }
    }

    public static boolean GPSisToggle() {
        if (null == mGPS) return false;
        return mGPS.canToggleGPS();
    }

    //#####################################
    //Bluetooth

    private static boolean isStopped = true;

    BluetoothComunicator.ICommunicatorState mICommunicatorState = new BluetoothComunicator.ICommunicatorState() {

        boolean isServer;
        boolean isClient;

        @Override
        public void connect() {
            isStopped = false;
            isClient = me.motofeedback.mApplication.getSettings().isClient();
            isServer = !isClient;
            if (isClient) {
                mServices.this.AddIMotionLogListener(localIMotionLogListener);
                mServices.this.AddIMotionListener(localIMotionListener);
                mServices.this.AddILocationChanged(localILocationChanged);
                sendSetting(true);
            }
            if (isServer) {
                me.motofeedback.mApplication.getSettings().setEnabledUIClientGroup(true);
                //me.motofeedback.mApplication.getSettings().setEnabledUIServerGroup(true);
            }
            me.motofeedback.mApplication.getSettings().setEnabledUIChangeType(false);
            initMotion(false);
            initGPS(false);
        }

        Motion.IMotionListener localIMotionListener = new Motion.IMotionListener() {
            @Override
            public void changeAlarm(boolean enable) {
                if (isStopped) return;
                String tmp = StateMessages.MSG_MOTION_CHANGE_ALARM + StateMessages.setMessageInt(enable ? 1 : 0);
                mServices.this.SendData(tmp);
            }

            @Override
            public void startAlarm(int count, int msg) {
                if (isStopped) return;
                String tmp = StateMessages.MSG_MOTION_START_ALARM +
                        StateMessages.setMessageObject(count, msg);
                mServices.this.SendData(tmp);
            }

            @Override
            public void difInProcessMotion(int xy, int xz, int zy) {
                if (isStopped || (0 == xy + xz + zy)) return;
                String tmp = StateMessages.MSG_MOTION_DIFINPROCESS + StateMessages.setMessageInt(xy, xz, zy);
                mServices.this.SendData(tmp);
            }
        };

        Motion.IMotionLogListener localIMotionLogListener = new Motion.IMotionLogListener() {
            @Override
            public void clearRange() {
                if (isStopped) return;
                mServices.this.SendData(StateMessages.MSG_MOTION_CLEAR);
            }

            @Override
            public void setRange(int from1, int to1, int fromEp1, int toEp1, int from2, int to2, int fromEp2, int toEp2, int from3, int to3, int fromEp3, int toEp3) {
                if (isStopped) return;
                String tmp = StateMessages.MSG_MOTION_RANGE +
                        StateMessages.setMessageInt(
                                from1, to1, fromEp1, toEp1,
                                from2, to2, fromEp2, toEp2,
                                from3, to3, fromEp3, toEp3);
                mServices.this.SendData(tmp);
            }

            //private int filterCount = 5;

            @Override
            public void changed(int xy, int xz, int zy, int dxy, int dxz, int dzy) {
                if (isStopped) return;
                //if (filterCount-- > 0)
                //    return;
                //filterCount = 5;
                String tmp = StateMessages.MSG_MOTION_CHANGED +
                        StateMessages.setMessageInt(
                                xy, xz, zy,
                                dxy, dxz, dzy);
                mServices.this.SendData(tmp);
            }
        };

        GPS.ILocationChanged localILocationChanged = new GPS.ILocationChanged() {
            @Override
            public void changedLocation(String location) {
                if (isStopped) return;

                mServices.this.SendData(StateMessages.MSG_GET_LOCATION +
                        StateMessages.setMessageObject(
                                location,
                                me.motofeedback.mApplication.getSettings().getLastFindedLocation()

                        ));
            }

            @Override
            public void changedGPS(boolean on) {
                if (isStopped) return;
                mServices.this.SendData(StateMessages.MSG_CHANGE_GPS + (on ? 1 : 0));
            }
        };

        @Override
        public void disconnect() {
            if (isStopped) return;
            isStopped = true;
            if (isClient) {
                mServices.this.EraceIMotionLogListener(localIMotionLogListener);
                mServices.this.EraceIMotionListener(localIMotionListener);
                mServices.this.EraceILocationChanged(localILocationChanged);
            }
            if (isServer) {
                me.motofeedback.mApplication.getSettings().setEnabledUIClientGroup(false);
                //me.motofeedback.mApplication.getSettings().setEnabledUIServerGroup(true);
            }
            me.motofeedback.mApplication.getSettings().setEnabledUIChangeType(true);
        }

        private void sendSetting(boolean isHandle) {
            Runnable sending = new Runnable() {
                @Override
                public void run() {
                    String data = StateMessages.MSG_SETTINGS +
                            StateMessages.setMessageObject(
                                    me.motofeedback.mApplication.getSettings().getSettings()
                            );
                    SendData(data);
                    SendCheckWatching(true);
                }
            };

            if (isHandle)
                new Handler(Looper.getMainLooper()).postDelayed(sending, 100);
            else
                sending.run();
        }

        @Override
        public void getsData(String data) {
            if (isClient) {
                if (data.contains(StateMessages.MSG_CHANGE_MOTION)) {
                    String val = data.replace(StateMessages.MSG_CHANGE_MOTION, "");
                    ChangeWatching(0 == "1".compareTo(val));
                } else if (data.contains(StateMessages.MSG_CHANGE_GPS)) {
                    String val = data.replace(StateMessages.MSG_CHANGE_GPS, "");
                    GPSSetEnabled(0 == "1".compareTo(val));
                } else if (data.contains(StateMessages.MSG_SETTING)) {
                    String[] var = StateMessages.getMessageStrings(data.replace(StateMessages.MSG_SETTING, ""));
                    String name = String.valueOf(var[0]);
                    String val = String.valueOf(var[1]);
                    me.motofeedback.mApplication.getSettings().convertToSetting(name, val);
                }
                return;
            }
            if (isServer) {
                if (data.contains(StateMessages.MSG_MOTION_CHANGED)) {
                    int[] vars = StateMessages.getMessageInts(data.replace(StateMessages.MSG_MOTION_CHANGED, ""));
                    //mIMotionLogListener.clearRange();
                    mIMotionLogListener.changed(
                            vars[0], vars[1], vars[2],
                            vars[3], vars[4], vars[5]
                    );
                } else if (data.contains(StateMessages.MSG_MOTION_RANGE)) {
                    int[] vars = StateMessages.getMessageInts(data.replace(StateMessages.MSG_MOTION_RANGE, ""));
                    mIMotionLogListener.setRange(
                            vars[0], vars[1], vars[2], vars[3],
                            vars[4], vars[5], vars[6], vars[7],
                            vars[8], vars[9], vars[10], vars[11]);
                } else if (data.contains(StateMessages.MSG_MOTION_CLEAR)) {
                    mIMotionLogListener.clearRange();
                } else if (data.contains(StateMessages.MSG_MOTION_START_ALARM)) {
                    int[] vars = StateMessages.getMessageInts(data.replace(StateMessages.MSG_MOTION_START_ALARM, ""));
                    mIMotionListener.startAlarm(vars[0], vars[1]);
                } else if (data.contains(StateMessages.MSG_MOTION_DIFINPROCESS)) {
                    int[] vars = StateMessages.getMessageInts(data.replace(StateMessages.MSG_MOTION_DIFINPROCESS, ""));
                    mIMotionListener.difInProcessMotion(vars[0], vars[1], vars[2]);
                } else if (data.contains(StateMessages.MSG_MOTION_CHANGE_ALARM)) {
                    int[] var = StateMessages.getMessageInts(data.replace(StateMessages.MSG_MOTION_CHANGE_ALARM, ""));
                    mIMotionListener.changeAlarm(var[0] == 1);
                } else if (data.contains(StateMessages.MSG_CHANGE_GPS)) {
                    int[] var = StateMessages.getMessageInts(data.replace(StateMessages.MSG_CHANGE_GPS, ""));
                    me.motofeedback.mApplication.getSettings().setChangeGPS(1 == var[0]);
                } else if (data.contains(StateMessages.MSG_GET_LOCATION)) {
                    String[] var = StateMessages.getMessageStrings(data.replace(StateMessages.MSG_GET_LOCATION, ""));

                    me.motofeedback.mApplication.getSettings().setLastLocation(var[0]);
                    me.motofeedback.mApplication.getSettings().setLastFindedLocation(Long.parseLong(var[1]));
                    mILocationChanged.changedLocation(var[0]);

                } else if (data.contains(StateMessages.MSG_SETTINGS)) {
                    String[] var = StateMessages.getMessageStrings(data.replace(StateMessages.MSG_SETTINGS, ""));
                    me.motofeedback.mApplication.getSettings().convertToSetting(var);

                    mILocationChanged.changedLocation(me.motofeedback.mApplication.getSettings().getLastLocation());
                }
            }

        }
    };

    public static synchronized void SendData(final String data) {
        if (null == mBluetooth || isStopped) return;
        mBluetooth.sendData(data);
    }

    public static synchronized void EnableBluetooth(Activity activity, boolean enable) {
        if (null == mBluetooth) return;
        boolean statIfError = me.motofeedback.mApplication.getSettings().isClient();
        if (enable)
            mBluetooth.start(activity, statIfError);
        else
            mBluetooth.stop();
    }

    public static void CheckBluetooth() {
        if (null == mBluetooth) return;
        mBluetooth.check();
    }

    public static boolean isBTConnected() {
        return !isStopped;
    }

    //#####################################
}
