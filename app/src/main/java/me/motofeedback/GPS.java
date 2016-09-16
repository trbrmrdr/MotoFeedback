package me.motofeedback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import me.motofeedback.Helper.TLog;

/**
 * Created by admin on 30.07.2016.
 */
public class GPS {

    public interface ILocationChanged {
        void changedLocation(String location);

        void changedGPS(boolean on);
    }

    private ILocationChanged mILocationChanged;

    public void setILocationChanged(ILocationChanged iLocationChanged) {
        mILocationChanged = iLocationChanged;
    }

    private final Context mContext;

    private Boolean isGPSEnabled = false;
    private Boolean gpsIsNeedEnabled = false;
    private Location mLocation;

    private String mLastLocation = "EMPTY";
    private long lastFindedLocation;
    private long delayFindedLocation;

    private final long GPSListenerDelay = 10000L;

    private long maxMinTimeElapsedBetweenUpdates;
    private long currMinTimeElapsedBetweenUpdates;
    private long maxMinDistanceChangeToUpdates;
    private long currMinDistanceChangeToUpdates;

    private class GPSListener extends Thread {
        public GPSListener() {
        }

        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(GPSListenerDelay);
                } catch (InterruptedException e) {
                }
                boolean isOn = false;
                boolean isNeedFinded = false;
                synchronized (isGPSEnabled) {
                    isOn = isGPSEnabled;
                }
                synchronized (gpsIsNeedEnabled) {
                    isNeedFinded = gpsIsNeedEnabled;
                }

                boolean perm1 = !isOn && isNeedFinded;//включение из вне
                if (perm1) {
                    new Handler(mContext.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    changeGPSState(true);
                                }
                            });
                }
                //#################
                //включение внутри ...постоянное обновление
                if (!perm1 && !isNeedFinded) {
                    long currTime = System.currentTimeMillis();
                    boolean needUpdateGPS = currTime - lastFindedLocation > delayFindedLocation;
                    Runnable runnable = null;
                    if (needUpdateGPS && !isOn) {
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                changeLocationState(true);
                            }
                        };
                    } else if (!needUpdateGPS && isOn) {
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                changeLocationState(false);
                            }
                        };
                    }
                    if (null != runnable)
                        new Handler(mContext.getMainLooper()).post(runnable);
                }
            }
            while (!isInterrupted());

        }


        public void cancel() {
            interrupt();
        }

    }

    private GPSListener mGPSListener;

    private void changeGPSListener(boolean enable) {
        if (enable) {
            if (null != mGPSListener)
                mGPSListener.cancel();
            mGPSListener = new GPSListener();
            mGPSListener.start();
        } else {
            if (null != mGPSListener) {
                mGPSListener.cancel();
                mGPSListener = null;
            }
        }

    }

    protected LocationManager locationManager;

    public GPS(Context context) {
        mContext = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLastLocation = me.motofeedback.mApplication.getSettings().getLastLocation();
        lastFindedLocation = me.motofeedback.mApplication.getSettings().getLastFindedLocation();
        delayFindedLocation = me.motofeedback.mApplication.getSettings().getDelayFindedLocation();
        changeGPSListener(true);
    }

    public boolean isEnabled() {
        //return  GPSTurnIsOn() || isGPSEnabled;
        synchronized (gpsIsNeedEnabled) {
            return gpsIsNeedEnabled;
        }
    }

    private long getScaled(long max, int scale) {
        long ret = max == 1 ? 2 : max;
        ret = (long) Math.pow(ret, scale);
        return ret;
    }

    public void changeLocationState(boolean enable) {
        synchronized (gpsIsNeedEnabled) {
            if (gpsIsNeedEnabled == enable)
                return;
            gpsIsNeedEnabled = enable;
        }
        me.motofeedback.mApplication.getSettings().setChangeGPS(enable);
        changeGPSState(enable);
        if (enable) {
            maxMinTimeElapsedBetweenUpdates = mApplication.getSettings().getMinTimeElapsedBetweenUpdates();
            currMinTimeElapsedBetweenUpdates = getScaled(maxMinTimeElapsedBetweenUpdates, 6);
            maxMinDistanceChangeToUpdates = mApplication.getSettings().getMinDistanceChangeToUpdates();
            currMinDistanceChangeToUpdates = getScaled(maxMinDistanceChangeToUpdates, 5);
            updateLocationState();
        } else {
            locationManager.removeUpdates(mLocationListener);
        }
        if (null != mILocationChanged)
            synchronized (mILocationChanged) {
                mILocationChanged.changedGPS(enable);
            }
    }

    private boolean updateLocationState() {
        boolean isEnd = true;
        if (currMinTimeElapsedBetweenUpdates > maxMinTimeElapsedBetweenUpdates) {
            currMinTimeElapsedBetweenUpdates /= 2;
            currMinTimeElapsedBetweenUpdates =
                    currMinTimeElapsedBetweenUpdates <= maxMinTimeElapsedBetweenUpdates ?
                            maxMinTimeElapsedBetweenUpdates :
                            currMinTimeElapsedBetweenUpdates;
            isEnd = false;
        }

        if (currMinDistanceChangeToUpdates > maxMinDistanceChangeToUpdates) {
            currMinDistanceChangeToUpdates /= 2;
            currMinDistanceChangeToUpdates =
                    currMinDistanceChangeToUpdates <= maxMinDistanceChangeToUpdates ?
                            maxMinDistanceChangeToUpdates :
                            currMinDistanceChangeToUpdates;
            isEnd = false;
        }

        locationManager.removeUpdates(mLocationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, currMinTimeElapsedBetweenUpdates, currMinDistanceChangeToUpdates, mLocationListener);
        getLocation();
        return isEnd;
    }

    public void getLocation() {
        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateLocation();
    }

    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;
            updateLocation();
            if (updateLocationState()) {
                lastFindedLocation = System.currentTimeMillis();
                me.motofeedback.mApplication.getSettings().setLastFindedLocation(lastFindedLocation);
                delayFindedLocation = me.motofeedback.mApplication.getSettings().getDelayFindedLocation();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            TLog.Log("provider = " + provider + "  status = " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                synchronized (isGPSEnabled) {
                    isGPSEnabled = true;
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                synchronized (isGPSEnabled) {
                    isGPSEnabled = false;
                }
            }
        }

    };

    private void updateLocation() {
        if (null != mILocationChanged) return;
        if (null != mLocation) {
            synchronized (mLastLocation) {
                mLastLocation = //"provider = " + mLocation.getProvider() + "\n" +
                        "speed = " + mLocation.getSpeed() + "\n" +
                                "longitude = " + mLocation.getLongitude() + "\n" +
                                "latitude = " + mLocation.getLatitude() + "\n" +
                                "currMinTimeElapsedBetweenUpdates = " + currMinTimeElapsedBetweenUpdates + "\n" +
                                "currMinDistanceChangeToUpdates = " + currMinDistanceChangeToUpdates;
                me.motofeedback.mApplication.getSettings().setLastLocation(mLastLocation);
            }
        }
        mILocationChanged.changedLocation(mLastLocation);
    }

    private boolean changeGPSState(boolean enable) {
        boolean method1 = canToggleGPS();
        if (method1) {
            try {
                if (enable) {
                    turnGPSOn();
                } else
                    turnGPSOff();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void onDestroy() {
        changeLocationState(false);
    }

    //##########################################

    private void turnGPSOn() {
        if (!GPSTurnIsOn()) {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            mContext.sendBroadcast(poke);
        }
    }

    private void turnGPSOff() {
        if (GPSTurnIsOn()) {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            mContext.sendBroadcast(poke);
        }
    }

    private boolean GPSTurnIsOn() {
        String provider = android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        return provider.contains("gps");
    }

    public boolean canToggleGPS() {
        PackageManager pacman = mContext.getPackageManager();
        PackageInfo pacInfo = null;

        try {
            pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            return false; //package not found
        }

        if (pacInfo != null) {
            for (ActivityInfo actInfo : pacInfo.receivers) {
                //test if recevier is exported. if so, we can toggle GPS.
                if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported) {
                    return true;
                }
            }
        }

        return false; //default
    }

    //########################################

    String beforeEnable;

    private void turnGpsOn(Context context) {
        beforeEnable = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        String newSet = String.format("%s,%s", beforeEnable, LocationManager.GPS_PROVIDER);
        try {
            android.provider.Settings.Secure.putString(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
        } catch (Exception e) {
        }
    }

    private void turnGpsOff(Context context) {
        if (null == beforeEnable) {
            String str = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (null == str) {
                str = "";
            } else {
                String[] list = str.split(",");
                str = "";
                int j = 0;
                for (int i = 0; i < list.length; i++) {
                    if (!list[i].equals(LocationManager.GPS_PROVIDER)) {
                        if (j > 0) {
                            str += ",";
                        }
                        str += list[i];
                        j++;
                    }
                }
                beforeEnable = str;
            }
        }
        try {
            android.provider.Settings.Secure.putString(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED, beforeEnable);
        } catch (Exception e) {
        }
    }

    //########################################

    private void setEnableGPS(boolean enable) {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", enable);
        mContext.sendBroadcast(intent);
    }
}

