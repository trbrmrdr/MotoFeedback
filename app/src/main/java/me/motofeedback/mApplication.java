package me.motofeedback;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * Created by admin on 30.07.2016.
 */
public class mApplication extends Application {


    private static Settings mSettings;
    private static Context mContext;

    public static Settings getSettings() {
        return mSettings;
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        mSettings = new Settings(this);
        mContext = this;
        //mServices.StartServices(this);

        super.onCreate();

        //Thread.setDefaultUncaughtExceptionHandler(mUncaughtExceptionHandler);
    }

    Thread.UncaughtExceptionHandler mUncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();

            //if (getSettings().isClient()) {
            //    mServices.StartServices(getContext());
            //}
        }
    };
}
