package me.motofeedback.Recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by admin on 30.07.2016.
 */
public class BootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (me.motofeedback.mApplication.getSettings().isServer())
            return;
        me.motofeedback.mServices.StartServices(context);
    }
}
