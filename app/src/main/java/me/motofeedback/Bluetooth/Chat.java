package me.motofeedback.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.StringTokenizer;

import me.motofeedback.Helper.TLog;
import me.motofeedback.Recievers.BootReciever;

/**
 * Created by trbrm on 31.08.2016.
 */
public class Chat {


    boolean isServer = false;
    private ServerThread mServerThread;
    private ClientThread mClientThread;
    private ConnectedThread mConnectedThread;

    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;

    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;     // прослушивание входящих сообщений
    public static final int STATE_CONNECTING = 2; // исходящее соединение
    public static final int STATE_CONNECTED = 3;  // подкдючение к удалённому устроёству
    public static final int STATE_CONNECTION_LOST = 4;
    public static final int STATE_CONNECTION_FAILED = 5;

    public Chat(Context context, Handler handler) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    private synchronized void setState(int state) {
        mState = state;
        mHandler.obtainMessage(StateMessages.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }


    public synchronized void start(Boolean server) {
        //TLog.Log(this, "Chat start", false);
        isServer = server;
// Cancel any thread attempting to make a connection
        if (mClientThread != null) {
            mClientThread.cancel();
            mClientThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (isServer && mServerThread == null) {
            mServerThread = new ServerThread();
            mServerThread.start();
        }

    }

    public void connect(BluetoothDevice device) {
        if (mState == STATE_CONNECTING) {
            if (mClientThread != null) {
                mClientThread.cancel();
                mClientThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mClientThread = new ClientThread(device);
        mClientThread.start();
        setState(STATE_CONNECTING);
    }

    private void connected(BluetoothSocket socket, BluetoothDevice mmDevice) {
        if (mClientThread != null) {
            mClientThread.cancel();
            mClientThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mServerThread != null) {
            mServerThread.cancel();
            mServerThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        //mmDevice.getName()
        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (STATE_NONE == getState())
            return;

        //TLog.Log(this, "Chat stop\n###############################", false);
        if (mClientThread != null) {
            mClientThread.cancel();
            mClientThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mServerThread != null) {
            mServerThread.cancel();
            mServerThread = null;
        }

        setState(STATE_NONE);
    }


    public void write(String msg) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(msg);
    }

    private void connectionFailed() {
        /*
        Message msg = mHandler.obtainMessage(StateMessages.MESSAGE_TOAST_CONNECTION_FAILED);
        Bundle bundle = new Bundle();
        bundle.putString(StateMessages.ERR_MSG, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        */

        Chat.this.setState(STATE_CONNECTION_FAILED);
        // Start the service over to restart listening mode
        //Chat.this.start();
        Chat.this.stop();
    }

    private void connectionLost() {
        /*
        Message msg = mHandler.obtainMessage(StateMessages.MESSAGE_TOAST_CONNECTION_LOST);
        Bundle bundle = new Bundle();
        bundle.putString(StateMessages.ERR_MSG, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        */

        Chat.this.setState(STATE_CONNECTION_LOST);
        // Start the service over to restart listening mode
        //Chat.this.start();
        Chat.this.stop();
    }

    /* ########################### */

    private class ClientThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ClientThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(me.motofeedback.mApplication.getSettings().MY_UUID);
                //tmp = device.createInsecureRfcommSocketToServiceRecord(me.motofeedback.mApplication.getSettings().MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null == tmp)
                TLog.Log(this, "BluetoothServerSocket == null", false);
            mmSocket = tmp;
        }

        public void run() {
            setName("ListenedThread");
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (Exception e) {
                e.printStackTrace();
                //TLog.Log(this, "ClientThread run " + e.getLocalizedMessage(), false);
                try {
                    mmSocket.close();
                } catch (Exception e2) {
                    //TLog.Log(this, "ClientThread run " + e2.getLocalizedMessage(), false);
                    e2.printStackTrace();
                }
                connectionFailed();
                return;
            }

            synchronized (Chat.this) {
                mClientThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
                //TLog.Log(this, "ClientThread cancel " + e.getLocalizedMessage(), false);
            }
        }
    }

    /* ########################### */

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                //TLog.Log(this, "ConnectedThread " + e.getLocalizedMessage(), false);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        StringBuilder buffers = new StringBuilder();

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (mState == STATE_CONNECTED) {
                try {
                    bytes = mmInStream.read(buffer);
//#########################
                    buffers.append(new String(buffer, 0, bytes));
                    //TLog.Log(buffers.toString());
                    boolean end = false;
                    do {
                        int first = buffers.indexOf(StateMessages.MESSAGE_SEPARATOR);
                        if (first == -1) {
                            buffers.delete(0, buffers.length());
                        } else {
                            int last = buffers.indexOf(StateMessages.MESSAGE_SEPARATOR, first + 1);
                            if (last == -1)
                                end = true;
                            else {
                                String msg = buffers.substring(first + 1, last);
                                //buffers = buffers.substring(last + 1, buffers.length());
                                buffers.delete(first, last + 1);
                                mHandler.obtainMessage(StateMessages.MESSAGE_RECIEVE, msg).sendToTarget();
                            }
                        }
                        if (buffers.length() <= 0)
                            end = true;
                    } while (!end);
                    //#########################
                    //mHandler.obtainMessage(StateMessages.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    connectionLost();
                    // Start the service over to restart listening mode
                    break;
                }
            }
        }

        public void write(String message) {
            try {
                mmOutStream.write((StateMessages.MESSAGE_SEPARATOR + message + StateMessages.MESSAGE_SEPARATOR).getBytes());
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(StateMessages.MESSAGE_SEND, message).sendToTarget();
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
                //TLog.Log(this, "ConnectedThread cancel " + e.getLocalizedMessage(), false);
            }
        }
    }

    /* ########################### */

    private class ServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public ServerThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(me.motofeedback.mApplication.getSettings().NAME, me.motofeedback.mApplication.getSettings().MY_UUID);
                //tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(me.motofeedback.mApplication.getSettings().NAME, me.motofeedback.mApplication.getSettings().MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                //TLog.Log(this, "ServerThread " + e.getLocalizedMessage(), false);
            }
            if (null == tmp)
                TLog.Log(this, "BluetoothServerSocket == null", false);
            mmServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (Exception e) {
                    e.printStackTrace();
                    //TLog.Log(this, "ServerThread run" + e.getLocalizedMessage(), false);
                    break;
                }

                if (socket != null) {
                    synchronized (Chat.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //TLog.Log(this, "ServerThread run" + e.getLocalizedMessage(), false);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
                //TLog.Log(this, "ServerThread cancel" + e.getLocalizedMessage(), false);
            }
        }
    }

    /* ########################### */

}
