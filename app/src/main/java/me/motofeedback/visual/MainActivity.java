package me.motofeedback.visual;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import me.motofeedback.Bluetooth.BluetoothComunicator;
import me.motofeedback.GPS;
import me.motofeedback.Helper.TLog;
import me.motofeedback.Motion;
import me.motofeedback.R;
import me.motofeedback.Recievers.CallReceiver;
import me.motofeedback.Recievers.SMSReceiver;
import me.motofeedback.mApplication;
import me.motofeedback.mServices;

public class MainActivity extends AppCompatActivity {

    private DrawGraf mDrawGraf;
    public static Activity mActivity;
    private boolean mIsVisible;
    private static Button mBTbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mActivity = this;
        mBTbutton = (Button) findViewById(R.id.buttonBluetooth);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        mServices.StartServices(this);
        initUI();
        mServices.AddIMotionLogListener(mIMotionLogListener);
        mServices.AddIMotionListener(mIMotionListener);
        mServices.AddILocationChanged(mILocationChanged);
        mServices.AddIChangeState(mIChangeState);
        //CallReceiver.findMethod(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                SettingsActivity.Start(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        final TextView textViewLog = (TextView) findViewById(R.id.textViewLog);
        mDrawGraf = (DrawGraf) findViewById(R.id.DrawGraf);

        textViewLog.setMovementMethod(new ScrollingMovementMethod());

        final ScrollView scrollView = (ScrollView) findViewById(R.id.ScrollView_main);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //clearFocus();
                textViewLog.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        textViewLog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                textViewLog.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        TLog.SetITLogChanged(new TLog.ITLogChanged() {
            @Override
            public void changed(final String str) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewLog.setText(textViewLog.getText() + "\n" + str);
                        //int scroll_amount = textViewLog.getBottom();
                        int scroll_amount = (int) (textViewLog.getLineCount() * textViewLog.getLineHeight()) - (textViewLog.getBottom() - textViewLog.getTop());
                        textViewLog.scrollTo(0, scroll_amount);
                    }
                });
            }

            @Override
            public void changed(String tag, String str) {
                changed(tag + " " + str);
            }
        });
    }

    public static void updateCheckedUI(boolean motion, boolean gps) {
        if (null == mActivity) return;
        ToggleButton buttonMotionListener = (ToggleButton) mActivity.findViewById(R.id.buttonMotionListener);
        buttonMotionListener.setChecked(motion);
        ToggleButton buttonGPS = (ToggleButton) mActivity.findViewById(R.id.buttonGPS);
        buttonGPS.setChecked(gps);

        if (mApplication.getSettings().isClient())
            mServices.SendCheckWatching(true);
    }

    Motion.IMotionLogListener mIMotionLogListener = new Motion.IMotionLogListener() {
        @Override
        public void clearRange() {
            mDrawGraf.clearRange();
        }

        @Override
        public void setRange(int from1, int to1, int fromEp1, int toEp1,
                             int from2, int to2, int fromEp2, int toEp2,
                             int from3, int to3, int fromEp3, int toEp3) {
            mDrawGraf.setRange(
                    from1, to1, fromEp1, toEp1,
                    from2, to2, fromEp2, toEp2,
                    from3, to3, fromEp3, toEp3);
        }

        @Override
        public void changed(int XY, int XZ, int ZY, int dXY, int dXZ, int dZY) {
            mDrawGraf.draw(XY, XZ, ZY, dXY, dXZ, dZY);
        }
    };

    Motion.IMotionListener mIMotionListener = new Motion.IMotionListener() {
        @Override
        public void changeAlarm(boolean enable) {
            mDrawGraf.changeAlarm(enable);
        }

        @Override
        public void startAlarm(int count, int msg) {
            mDrawGraf.alarm(count, msg);
        }

        @Override
        public void difInProcessMotion(int xy, int xz, int zy) {
            mDrawGraf.alarm(xy, xz, zy);
        }
    };

    GPS.ILocationChanged mILocationChanged = new GPS.ILocationChanged() {
        @Override
        public void changedLocation(String location) {
        }

        @Override
        public void changedGPS(boolean on) {
            ToggleButton buttonGPS = (ToggleButton) mActivity.findViewById(R.id.buttonGPS);
            if (buttonGPS.isChecked() != on)
                buttonGPS.setChecked(on);
        }
    };

    static BluetoothComunicator.IChangeState.STATE_BTC mState = BluetoothComunicator.IChangeState.STATE_BTC.NONE;
    static boolean mBTconnect = true;
    static BluetoothComunicator.IChangeState mIChangeState = new BluetoothComunicator.IChangeState() {
        @Override
        public void change(STATE_BTC state) {
            boolean enable = true;
            String text = "Подключить";
            mBTconnect = true;
            switch (state) {
                case NONE:
                    break;
                case LISTEN:
                    text = "(в процессе)\nотключить";
                    mBTconnect = false;
                    break;
                case CONNECT:
                    text = "Отключить";
                    mBTconnect = false;
                    break;
                case START:
                    text = "в процессе";
                    mBTconnect = false;
                    enable = false;
                    break;
                case STOP:
                    text = "Подключить";
                    break;
                case EROR:
                    text = "в процессе";
                    mBTconnect = false;
                    break;
            }
            mBTbutton.setText(text);
            mBTbutton.setEnabled(enable);
            mState = state;
        }
    };

    public void BluetoothClick(View view) {
        mServices.EnableBluetooth(this, mBTconnect, false);
    }

    public void ClearTracing(View view) {
        mDrawGraf.free();
    }

    public void SendSMS(View view) {
        SMSReceiver.sendSMS(this, mApplication.getSettings().getPhoneServer(), "тест сообщения");
    }

    public void ConnectToDevice(View view) {
        //CallReceiver.call(me.motofeedback.mApplication.getSettings().getPhoneClient());
        CallReceiver.callToPhone();
    }

    public void StartMotionListener(View view) {
        if (view instanceof ToggleButton) {
            final ToggleButton motionButton = (ToggleButton) view;
            boolean checked = motionButton.isChecked();
            mServices.ChangeWatching(checked);
        }
    }

    public void ChangeGPS(View view) {
        if (view instanceof ToggleButton) {
            final ToggleButton gpsButton = (ToggleButton) view;
            boolean checked = gpsButton.isChecked();
            mServices.GPSSetEnabled(checked);
        }
    }

    private long timerExit = -1;

    @Override
    public void onBackPressed() {
        if (timerExit == -1 || (System.currentTimeMillis() - timerExit) >= 3000) {
            timerExit = System.currentTimeMillis();
            Toast.makeText(this, R.string.exitText, Toast.LENGTH_LONG).show();
            return;
        }
        if ((System.currentTimeMillis() - timerExit) < 3000) {
            if (me.motofeedback.mApplication.getSettings().isServer())
                me.motofeedback.mServices.StopServices(this);
            finish();
        }

    }

    public boolean isVisible() {
        return mIsVisible;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsVisible = true;
        mApplication.getSettings().updateUI(this);
        mServices.CheckBluetooth();
        MainActivity.updateCheckedUI(mServices.IsWatching(), mServices.GPSisEnabled());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServices.EraceIMotionLogListener(mIMotionLogListener);
        mServices.EraceIMotionListener(mIMotionListener);
        mServices.EraceILocationChanged(mILocationChanged);
        mServices.EraceIChangeState(mIChangeState);
        mIsVisible = false;
        mActivity = null;
        mBTbutton = null;
        mApplication.getSettings().updateUI(null);
    }
}
