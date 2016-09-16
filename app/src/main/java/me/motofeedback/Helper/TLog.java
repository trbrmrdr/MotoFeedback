package me.motofeedback.Helper;

import android.util.Log;

/**
 * Created by admin on 02.08.2016.
 */
public class TLog {
    private static final String TAG = "TLog";

    public static final void Log(final String str) {
        Log.e(TAG, str);
    }

    //########################################### static

    public interface ITLogChanged {
        void changed(final String str);

        void changed(final String tag, final String str);
    }

    private static ITLogChanged mITLogChanged = null;

    public static void SetITLogChanged(ITLogChanged iLogChanged) {
        mITLogChanged = iLogChanged;
    }

    public static final void Log(Object clazz, final String str, boolean printClassName) {
        String tag = clazz.getClass().getName();
        Log.e(tag, str);
        if (null != mITLogChanged) {
            synchronized (mITLogChanged) {
                if (printClassName)
                    mITLogChanged.changed(tag, str);
                else
                    mITLogChanged.changed(str);
            }
        }
    }

    public static final void Log(Object clazz, final String str) {
        Log(clazz, str, true);
    }


}
