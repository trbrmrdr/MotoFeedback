package me.motofeedback.Recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.motofeedback.mApplication;
import me.motofeedback.mServices;

/**
 * Created by trbrmrdr on 29/07/16.
 */

public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "CallReciever";

    private static final void Log(String str) {
        Log.e(TAG, str);
    }

    private static String getData() {
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd, HH:mm");
        SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm");
        Date resultdate = new Date(System.currentTimeMillis());
        return sdf.format(resultdate);
    }

    public static boolean callToPhone(Context context) {
        //return callToPhone(context, me.motofeedback.mApplication.getSettings().getPhoneServer());
        return callToPhone(context, me.motofeedback.mApplication.getSettings().getPhoneClient());
    }

    public static boolean callToPhone(Context context, final String phone) {
        try {
            //if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);
            Log(getData() + " звонок " + phone);
            //}
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    String phoneNumber = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(android.content.Intent.ACTION_NEW_OUTGOING_CALL)) {
            //получаем исходящий номер
            phoneNumber = intent.getExtras().getString(android.content.Intent.EXTRA_PHONE_NUMBER);
            Log(getData() + " исходящий номер " + phoneNumber);
        } else if (action.equals("android.intent.action.PHONE_STATE")) {
            String phone_state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String msg = "";
            if (phone_state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                //телефон звонит, получаем входящий номер
                phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                msg = " телефон звонит, входящий номер " + phoneNumber;
                //if (me.motofeedback.mApplication.getSettings().isServer())
                //    return;
                //me.motofeedback.mServices.StartServices(context);
                if (me.motofeedback.mApplication.getSettings().isClient()) {
                    endCall(context);
                    String serverPhone = mApplication.getSettings().getPhoneServer();
                    if (0 == phoneNumber.compareTo(serverPhone)) {
                        mServices.EnableBluetooth(null, true);
                    }
                }
            } else if (phone_state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                msg = "телефон в режиме звонка (набор номера / разговор)";
                //телефон находится в режиме звонка (набор номера / разговор)
            } else if (phone_state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                //телефон находиться в ждущем режиме. Это событие наступает по окончанию разговора, когда мы уже знаем номер и факт звонка
                msg = "знаем факт звонка и номер - окончание разговора";
            }
            Log(getData() + " " + phone_state + " " + msg);
        } else {
            Log(getData() + " " + action);
        }
    }

    //#######################################################

    public static boolean endCall(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class c = Class.forName(tm.getClass().getName());
            Method mgetITelephony = c.getDeclaredMethod("getITelephony");
            mgetITelephony.setAccessible(true);
            Object TelephonyService = mgetITelephony.invoke(tm);
            c = Class.forName(TelephonyService.getClass().getName());
            Method endCall = c.getDeclaredMethod("endCall");
            endCall.setAccessible(true);
            endCall.invoke(TelephonyService);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //#######################################################


    private static Object mTelephonyService;
    private static Method mendCall;
    private static Method mcall;

    public static void endCall_() {
        try {
            mendCall.invoke(mTelephonyService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void call(String phone) {
        try {
            mcall.invoke(mTelephonyService, "com.android.phone", phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //void call(String callingPackage, String number);
    }

    public static boolean findMethod(final Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class c = Class.forName(tm.getClass().getName());
            Method mgetITelephony = c.getDeclaredMethod("getITelephony");
            mgetITelephony.setAccessible(true);
            mTelephonyService = mgetITelephony.invoke(tm); // Get the internal ITelephony object
            c = Class.forName(mTelephonyService.getClass().getName()); // Get its class
            mendCall = c.getDeclaredMethod("endCall"); // Get the "endCall()" method
            mendCall.setAccessible(true); // Make it accessible
            //mendCall.invoke(mTelephonyService); // invoke endCall()

            Class s = Class.forName("java.lang.String");
            mcall = c.getDeclaredMethod("call", s, s); // Get the "endCall()" method
            mcall.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
