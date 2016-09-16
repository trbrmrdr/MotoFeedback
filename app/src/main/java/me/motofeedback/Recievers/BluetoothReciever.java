package me.motofeedback.Recievers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Created by admin on 27.08.2016.
 */
public class BluetoothReciever extends BootReciever {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            me.motofeedback.Bluetooth.BluetoothComunicator.StateChanging(state);
        }

        if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
            //EXTRA_PREVIOUS_SCAN_MODE
            me.motofeedback.Bluetooth.BluetoothComunicator.ScanModeChanging(state);
        }
    }

}

