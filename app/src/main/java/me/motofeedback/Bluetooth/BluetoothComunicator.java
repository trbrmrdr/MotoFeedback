package me.motofeedback.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;

import me.motofeedback.Helper.TLog;

/**
 * Created by trbrm on 25.08.2016.
 */
public class BluetoothComunicator {

    private final long LISTEN_DELAY = 1000 * 10;

    public interface ICommunicatorState {
        void connect();

        void disconnect();

        void getsData(final String data);
    }

    public interface IChangeState {
        enum STATE_BTC {
            NONE,
            LISTEN,
            CONNECT,
            START,
            STOP,
            EROR
        }

        void change(STATE_BTC state);
    }

    private IChangeState mIChangeState;
    private IChangeState.STATE_BTC lastState = IChangeState.STATE_BTC.NONE;

    public void setIChangeState(IChangeState iChangeState) {
        this.mIChangeState = iChangeState;
    }

    private void sendChangeState(IChangeState.STATE_BTC state) {
        lastState = state;
        if (null != mIChangeState) {
            synchronized (mIChangeState) {
                mIChangeState.change(state);
            }
        }
    }

    private ICommunicatorState mICommunicatorState;

    public void setICommunicatorState(@NonNull ICommunicatorState iCommunicatorState) {
        mICommunicatorState = iCommunicatorState;
    }

    private int countNotSend = 3;

    public void sendData(final String data) {
        if (Chat.STATE_CONNECTED != mChat.getState() && countNotSend > 0) {
            countNotSend = countNotSend < 0 ? 0 : countNotSend - 1;
            TLog.Log(this, "not Connected, not send", false);
            return;
        }
        mChat.write(data);
    }

    private Chat mChat;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private boolean mEnabledStart;
    private boolean mStartIfError;
    private boolean mNeededStart;

    private Activity mActivity = null;//only in Server
    private String mNameServer;


    public BluetoothComunicator(final Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == mBluetoothAdapter)
            throw new ExceptionInInitializerError("not finded Bluetooth module");
        mBluetoothAdapter.setName(me.motofeedback.mApplication.getSettings().getClientName());
        mChat = new Chat(mContext, mHandler);
    }

    private final Handler mHandler = new Handler() {

        Integer lastChatState = Chat.STATE_NONE;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StateMessages.MESSAGE_STATE_CHANGE:
                    String nameState = "none";
                    boolean isStop = false;
                    synchronized (lastChatState) {
                        lastChatState = msg.arg1;
                    }
                    switch (msg.arg1) {
                        case Chat.STATE_NONE:
                            break;
                        case Chat.STATE_LISTEN:
                            if (me.motofeedback.mApplication.getSettings().isClient()) {
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        synchronized (lastChatState) {
                                            if (lastChatState == Chat.STATE_LISTEN) {
                                                stop();
                                            }
                                        }
                                    }
                                }, LISTEN_DELAY);
                            }
                            nameState = "STATE_LISTEN";
                            break;
                        case Chat.STATE_CONNECTING:
                            nameState = "STATE_CONNECT ING";
                            break;
                        case Chat.STATE_CONNECTED:
                            nameState = "STATE_CONNECTED";
                            countNotSend = 3;
                            sendChangeState(IChangeState.STATE_BTC.CONNECT);
                            if (null != mICommunicatorState)
                                mICommunicatorState.connect();
                            break;
                        case Chat.STATE_CONNECTION_LOST:
                            nameState = "STATE_CONNECTION_LOST";
                            isStop = true;
                            break;
                        case Chat.STATE_CONNECTION_FAILED:
                            nameState = "STATE_CONNECTION_FAILED";
                            isStop = true;
                            break;
                    }
                    TLog.Log(this, nameState, false);
                    if (isStop)
                        stopAfterError();
                    break;
                case StateMessages.MESSAGE_SEND:
                    //String mw = (String) msg.obj;
                    //TLog.Log(this, "message send: " + mw, false);
                    break;
                case StateMessages.MESSAGE_RECIEVE:
                    //byte[] buffer = (byte[]) msg.obj;
                    //String mr = new String(buffer, 0, msg.arg1);
                    String data = (String) msg.obj;
                    //TLog.Log(this, "message recieve: " + data, false);
                    if (null != mICommunicatorState)
                        mICommunicatorState.getsData(data);
                    break;
            }
        }
    };


    public void start(final Activity activity, boolean startIfError) {
        if (mEnabledStart) return;
        TLog.Log(this, "bt start", false);
        mEnabledStart = true;
        mActivity = activity;
        mStartIfError = startIfError;
        start();
    }

    private void start() {
        sendChangeState(IChangeState.STATE_BTC.START);
        changeState(true, new Runnable() {
            @Override
            public void run() {
                if (me.motofeedback.mApplication.getSettings().isClient()) {
                    mNameServer = me.motofeedback.mApplication.getSettings().getServerName();
                    mBluetoothAdapter.setName(me.motofeedback.mApplication.getSettings().getClientName());
                    changeStateFindedDevices(true);
                    sendChangeState(IChangeState.STATE_BTC.LISTEN);
                    mChat.start(false);
                } else {
                    mBluetoothAdapter.setName(me.motofeedback.mApplication.getSettings().getServerName());
                    changeStateDiscoverable(mActivity, new Runnable() {
                        @Override
                        public void run() {
                            sendChangeState(IChangeState.STATE_BTC.LISTEN);
                            mChat.start(true);
                        }
                    });
                }
            }
        });
    }

    private void stopAfterError() {
        sendChangeState(IChangeState.STATE_BTC.EROR);
        if (mStartIfError)
            mNeededStart = true;
        stop();
    }

    public void stop() {
        if (!mEnabledStart) return;
        TLog.Log(this, "bt stop", false);

        mEnabledStart = false;
        mStartIfError = false;
        mChat.stop();

        changeStateFindedDevices(false);

        changeState(false, new Runnable() {
                    @Override
                    public void run() {
                        sendChangeState(IChangeState.STATE_BTC.STOP);
                        if (null != mICommunicatorState)
                            mICommunicatorState.disconnect();
                        if (mNeededStart) {
                            mStartIfError = true;
                            mNeededStart = false;
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    start(mActivity, mStartIfError);
                                }
                            }, 100);
                        }
                    }
                }
        );
    }

    public void check() {
        sendChangeState(lastState);
    }

    /* ################################################## */

    private static ArrayList<Pair<Integer, Runnable>> mListChangingStateCalbback = new ArrayList<>();

    public static void StateChanging(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                TLog.Log("StateChanging STATE_OFF");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                TLog.Log("StateChanging STATE_TURNING_OFF");
                break;
            case BluetoothAdapter.STATE_ON:
                TLog.Log("StateChanging STATE_ON");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                TLog.Log("StateChanging STATE_TURNING_ON");
                break;
        }
        Pair<Integer, Runnable> changingStateCalbback = null;
        for (Pair<Integer, Runnable> it : mListChangingStateCalbback) {
            if (it.first == state) {
                changingStateCalbback = it;
                break;
            }
        }
        if (null != changingStateCalbback && changingStateCalbback.first == state) {
            TLog.Log("calling ");
            mListChangingStateCalbback.remove(changingStateCalbback);
            changingStateCalbback.second.run();
        }
    }

    private void changeState(boolean enable, Runnable callback) {
        if (null != callback && !(enable ^ mBluetoothAdapter.isEnabled())) {
            callback.run();
            return;
        }
        if (enable) {
            if (null != callback)
                mListChangingStateCalbback.add(new Pair<>(BluetoothAdapter.STATE_ON, callback));
            mBluetoothAdapter.enable();
        } else {
            if (null != callback)
                mListChangingStateCalbback.add(new Pair<>(BluetoothAdapter.STATE_OFF, callback));
            mBluetoothAdapter.disable();
        }

    }

    /* ################################################## */

    private boolean isRegisteredReciver = false;

    private void changeStateFindedDevices(boolean enable) {
        if (enable) {
            if (!isRegisteredReciver) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                //filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                mContext.registerReceiver(mFindedReceiver, filter);
                isRegisteredReciver = true;
            }
            if (!mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.startDiscovery();
        } else {
            if (mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
            if (isRegisteredReciver) {
                mContext.unregisterReceiver(mFindedReceiver);
                isRegisteredReciver = false;
            }
        }
    }

    private final BroadcastReceiver mFindedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (null == device || null == device.getName())
                    return;
                TLog.Log("finded device = " + device.getName());
                if (device.getName().compareTo(mNameServer) == 0) {
                    changeStateFindedDevices(false);
                    mChat.connect(device);
                }
            }/* else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                TLog.Log(this, "ended finded", false);
                stop(null);
            }
            */
        }
    };

    private static Runnable mDisoverableCallback;

    public static void ScanModeChanging(int state) {
        switch (state) {
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:// устройство доступно для поиска
                TLog.Log("scan mode changing to SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                if (null != mDisoverableCallback)
                    mDisoverableCallback.run();
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE: //  устройство не доступно для поиска но способно принимать соединения
                TLog.Log("scan mode changing to SCAN_MODE_CONNECTABLE");
                break;
            case BluetoothAdapter.SCAN_MODE_NONE: // не доступно для поиска и не может принимать соединения
                TLog.Log("scan mode changing to SCAN_MODE_NONE");
                break;
        }
    }

    public void changeStateDiscoverable(Activity activity, final Runnable callback) {
        if (mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            if (null != callback)
                callback.run();
            return;
        }
        mDisoverableCallback = callback;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);//second max - 3600 ; infinity - 0
                discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(discoverableIntent);
            }
        });

    }

    public void onDestory() {
        stop();
    }

}
