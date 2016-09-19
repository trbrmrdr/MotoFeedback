package me.motofeedback.Recievers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.ArrayList;

import me.motofeedback.Settings;

/**
 * Created by trbrmrdr on 29/07/16.
 */
public class SMSReceiver extends BroadcastReceiver {


    private void sendSMS(String phoneNumber, String message, Context context, Class<?> cls) {
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, cls), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);
    }

    public static void sendSMS(String message) {
        String phone;
        final Context context = me.motofeedback.mApplication.getContext();
        Settings settings = me.motofeedback.mApplication.getSettings();
        if (settings.isClient())
            phone = settings.getPhoneServer();
        else
            phone = settings.getPhoneClient();
        sendSMS(context, phone, message);
    }

    public static void sendSMS(final Context context, String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

//---когда SMS отправлено---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

//---когда SMS доставлено---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:

                        Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        if (null != bundle) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] msgs = new SmsMessage[pdus.length];
            ArrayList<String> numbers = new ArrayList<>();
            ArrayList<String> messages = new ArrayList<>();
            for (int i = 0; i < msgs.length; i++) { //пробегаемся по всем полученным сообщениям
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], "3gpp");
                } else {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                numbers.add(msgs[i].getOriginatingAddress()); //получаем номер отправителя
                messages.add(msgs[i].getMessageBody());//получаем текст сообщения
            }
            if (messages.size() > 0) {
                //делаем что-то с сообщениями
                messages = messages;
            }
        }
    }


    private static final String CONTENT_SMS = "content://sms/";
    private static long id = 0;

    public static void RegisterReadSMS(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        OutgoingSmsObserver outgoingSmsObserver = new OutgoingSmsObserver(new Handler());
        outgoingSmsObserver.setContext(context);
        contentResolver.registerContentObserver(Uri.parse(CONTENT_SMS), true, outgoingSmsObserver);
    }


    private static class OutgoingSmsObserver extends ContentObserver {
        private Context context;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public OutgoingSmsObserver(Handler handler) {
            super(handler);
        }

        @Override

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Uri uriSMSURI = Uri.parse(CONTENT_SMS);
            Cursor cur = context.getContentResolver().query(uriSMSURI, null, null, null, null);
            cur.moveToNext();
            String protocol = cur.getString(cur.getColumnIndex("protocol"));
            if (protocol == null) {
                long messageId = cur.getLong(cur.getColumnIndex("_id"));
                //проверяем не обрабатывали ли мы это сообщение только-что
                //if (messageId != id)
                {
                    id = messageId;
                    int threadId = cur.getInt(cur.getColumnIndex("thread_id"));
                    Cursor c = context.getContentResolver().query(Uri.parse("content://sms/outbox/" + threadId), null, null, null, null);
                    c.moveToNext();
                    //получаем адрес получателя
                    String address = cur.getString(cur.getColumnIndex("address"));
                    //получаем текст сообщения
                    String body = cur.getString(cur.getColumnIndex("body"));
                    //делаем что-то с сообщением
                    body = body;
                }
            }
        }

        public void setContext(Context context) {
            this.context = context;
        }
    }
}
