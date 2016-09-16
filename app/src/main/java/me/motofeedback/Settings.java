package me.motofeedback;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import me.motofeedback.Bluetooth.StateMessages;
import me.motofeedback.Helper.BaseSettings;
import me.motofeedback.visual.MainActivity;

/**
 * Created by admin on 30.07.2016.
 */
public class Settings extends BaseSettings {

    // minimum distance to update location in meters
    private static final String MIN_DISTANCE_CHANGE_TO_UPDATES_NAME = "MIN_DISTANCE_CHANGE_TO_UPDATES_NAME";
    private static final long MIN_DISTANCE_CHANGE_TO_UPDATES = 5; // 5m

    // minimum time between updates in milliseconds
    private static final String MIN_TIME_ELAPSED_BETWEEN_UPDATES_NAME = "MIN_TIME_ELAPSED_BETWEEN_UPDATES";
    private static final long MIN_TIME_ELAPSED_BETWEEN_UPDATES = 1000 * 60 * 1; // 1 minute

    private static final String LAST_LOCATION_NAME = "LAST_LOCATION_NAME";
    private static final String LAST_FINDED_LOCATION_NAME = "LAST_FINDED_LOCATION_NAME";
    private static final String DELAY_FINDED_LOCATION_NAME = "DELAY_FINDED_LOCATION_NAME";
    private static final long DELAY_FINDED_LOCATION = 1000 * 60 * 60 * 10; // 10ч


    private static final String PHONE_SERVER_NAME = "PHONE_SERVER";
    private static final String PHONE_SERVER = "+79123456789";
    private static final String PHONE_CLIENT_NAME = "PHONE_CLIENT";
    private static final String PHONE_CLIENT = "+79123456789";

    private static final String SERVER_NAME_NAME = "SERVER_NAME";
    private static final String SERVER_NAME = "ServerMotoFeedbak";

    private static final String CLIENT_NAME_NAME = "CLIENT_NAME";
    private static final String CLIENT_NAME = "ClientMotoFeedbak";

    private static final String BLUETOOTH_TYPE_NAME = "BLUETOOTH_TYPE";
    private static final boolean BLUETOOTH_TYPE = true;

    private static final String DOUBLE_TARICING_ALARM_NAME = "DOUBLE_TARICING_FROM_ALARM";
    private static final boolean DOUBLE_TARICING_ALARM = true;

    private static final String ZERO_DEGREE_XY_NAME = "ZERO_DEGREE_XY";
    private static final int ZERO_DEGREE_XY = 5;
    private static final String ZERO_DEGREE_XZ_NAME = "ZERO_DEGREE_XZ";
    private static final int ZERO_DEGREE_XZ = 2;
    private static final String ZERO_DEGREE_ZY_NAME = "ZERO_DEGREE_ZY";
    private static final int ZERO_DEGREE_ZY = 2;

    private static final String DELAY_IDLE_NAME = "DELAY_IDLE";
    private static final long DELAY_IDLE = 1000 * 30;

    private static final String DELAY_DIMENSION_RANGE_NAME = "DELAY_DIMENSION_RANGE";
    private static final long DELAY_DIMENSION_RANGE = 1000 * 3; // 3 sec

    private static final String DELAY_SECOND_DIMENSION_RANGE_NAME = "DELAY_SECOND_DIMENSION_RANGE";
    private static final long DELAY_SECOND_DIMENSION_RANGE = 1000 * 2;

    private static final String DELAY_RESET_DIMENSIONS_NAME = "DELAY_RESET_DIMENSIONS";
    private static final long DELAY_RESET_DIMENSIONS = 1000 * 2;
    public static final UUID MY_UUID = UUID.fromString("0f14d1ab-7605-4a62-a9e4-5ed26688389b");
    public static final String NAME = "me.motofeedback";

    private static final String SAMPLING_PERIOD_US_NAME = "SAMPLING_PERIOD_US";
    private static final int SAMPLING_PERIOD_US = SensorManager.SENSOR_DELAY_FASTEST;

    private static final String CHANGE_GPS_NAME = "CHANGE_GPS";
    private static final String CHANGE_MOTION_NAME = "CHANGE_MOTION";

    public Settings(Context context) {
        super(context);
        init();
    }

    private boolean setSetting(int id, String settingName, final int val) {
        boolean ret = setSetting(settingName, val);
        if (isServer())
            mServices.SendData(StateMessages.MSG_SETTING + StateMessages.setMessageObject(settingName, val));
        setValInView(id);
        return ret;
    }

    private boolean setSetting(int id, String settingName, final boolean val) {
        boolean ret = setSetting(settingName, val);
        if (isServer())
            mServices.SendData(StateMessages.MSG_SETTING + StateMessages.setMessageObject(settingName, val));
        setValInView(id);
        return ret;
    }

    private boolean setSetting(int id, String settingName, final long val) {
        boolean ret = setSetting(settingName, val);
        if (isServer())
            mServices.SendData(StateMessages.MSG_SETTING + StateMessages.setMessageObject(settingName, val));
        setValInView(id);
        return ret;
    }

    private boolean setSetting(int id, String settingName, final String val) {
        boolean ret = setSetting(settingName, val);
        if (isServer())
            mServices.SendData(StateMessages.MSG_SETTING + StateMessages.setMessageObject(settingName, val));
        setValInView(id);
        return ret;
    }

    /* ###################################### */

    private int idPhoneClient;
    private int idPhoneServer;
    private int idMDCToUpdates;
    private int idMTEBUpdates;
    private int idChangeTypeSensor;
    private int idDoubleCheckGroup;
    private int idDTAlarm;
    private int idZXY;
    private int idZXZ;
    private int idZZY;
    private int idDIdle;
    private int idDelayDR;
    private int idDelaySDR;
    private int idDelayRDR;
    private int idServerName;
    private int idClientName;
    private int idLFLocation;
    private int idDFLocation;

    public String getPhoneClient() {
        return getSetting(PHONE_CLIENT_NAME, PHONE_CLIENT);
    }

    public boolean setPhoneClient(final String val) {
        return setSetting(idPhoneClient, PHONE_CLIENT_NAME, val);
    }

    public String getPhoneServer() {
        return getSetting(PHONE_SERVER_NAME, PHONE_SERVER);
    }

    public boolean setPhoneServer(final String val) {
        return setSetting(idPhoneServer, PHONE_SERVER_NAME, val);
    }

    public long getMinDistanceChangeToUpdates() {
        return getSetting(MIN_DISTANCE_CHANGE_TO_UPDATES_NAME, MIN_DISTANCE_CHANGE_TO_UPDATES);
    }

    public boolean setMinDistanceChangeToUpdates(final long val) {
        long prev = getMinDistanceChangeToUpdates();
        if (prev == val) return true;
        boolean ret = setSetting(idMDCToUpdates, MIN_DISTANCE_CHANGE_TO_UPDATES_NAME, val);
        if (isClient())
            mServices.reinitGPS(mContext);
        return ret;
    }

    public long getMinTimeElapsedBetweenUpdates() {
        return getSetting(MIN_TIME_ELAPSED_BETWEEN_UPDATES_NAME, MIN_TIME_ELAPSED_BETWEEN_UPDATES);
    }

    public boolean setMinTimeElapsedBetweenUpdates(final long val) {
        long prev = getMinTimeElapsedBetweenUpdates();
        if (prev == val) return true;
        boolean ret = setSetting(idMTEBUpdates, MIN_TIME_ELAPSED_BETWEEN_UPDATES_NAME, val);
        if (isClient())
            mServices.reinitGPS(mContext);
        return ret;
    }

    public int getSamplingPeriodUs() {
        return getSetting(SAMPLING_PERIOD_US_NAME, SAMPLING_PERIOD_US);
    }

    //SensorManager.SENSOR_DELAY_UI;//SENSOR_DELAY_FASTEST;
    public boolean setSamplingPeriodUs(final int val) {
        long prev = getSamplingPeriodUs();
        if (prev == val) return true;
        boolean ret = setSetting(idChangeTypeSensor, SAMPLING_PERIOD_US_NAME, val);
        if (isClient())
            mServices.reinitMotion(mContext);
        return ret;
    }

    public boolean isDTAlaram() {
        return getSetting(DOUBLE_TARICING_ALARM_NAME, DOUBLE_TARICING_ALARM);
    }

    public boolean setDTAlaram(boolean val) {
        boolean ret = setSetting(idDTAlarm, DOUBLE_TARICING_ALARM_NAME, val);
        if (null != mActivity) {
            View view = mActivity.findViewById(idDoubleCheckGroup);
            if (null != view) {
                LinearLayout doubleCheckGroup = (LinearLayout) view;
                doubleCheckGroup.setVisibility(val ? View.VISIBLE : View.GONE);
            }
        }
        return ret;
    }

    public int getZeroXY() {
        return getSetting(ZERO_DEGREE_XY_NAME, ZERO_DEGREE_XY);
    }

    public int getZeroXZ() {
        return getSetting(ZERO_DEGREE_XZ_NAME, ZERO_DEGREE_XZ);
    }

    public int getZeroZY() {
        return getSetting(ZERO_DEGREE_ZY_NAME, ZERO_DEGREE_ZY);
    }

    public boolean setZeroXY(final int val) {
        boolean ret = setSetting(idZXY, ZERO_DEGREE_XY_NAME, val);
        return ret;
    }

    public boolean setZeroXZ(final int val) {
        return setSetting(idZXZ, ZERO_DEGREE_XZ_NAME, val);
    }

    public boolean setZeroZY(final int val) {
        boolean ret = setSetting(idZZY, ZERO_DEGREE_ZY_NAME, val);
        return ret;
    }

    public boolean setDIdle(final long val) {
        boolean ret = setSetting(idDIdle, DELAY_IDLE_NAME, val);
        return ret;
    }

    public long getDIdle() {
        return getSetting(DELAY_IDLE_NAME, DELAY_IDLE);
    }

    public long getDelayDimensionRange() {
        return getSetting(DELAY_DIMENSION_RANGE_NAME, DELAY_DIMENSION_RANGE);
    }

    public long getDelaySecondDimensionRange() {
        return getSetting(DELAY_SECOND_DIMENSION_RANGE_NAME, DELAY_SECOND_DIMENSION_RANGE);
    }

    public long getDelayResetDimensionRange() {
        return getSetting(DELAY_RESET_DIMENSIONS_NAME, DELAY_RESET_DIMENSIONS);
    }

    public boolean setDelayDimensionRange(final long val) {
        boolean ret = setSetting(idDelayDR, DELAY_DIMENSION_RANGE_NAME, val);
        return ret;
    }

    public boolean setDelaySecondDimensionRange(final long val) {
        boolean ret = setSetting(idDelaySDR, DELAY_SECOND_DIMENSION_RANGE_NAME, val);
        return ret;
    }

    public boolean setDelayResetDimensionRange(final long val) {
        boolean ret = setSetting(idDelayRDR, DELAY_RESET_DIMENSIONS_NAME, val);
        return ret;
    }

    public boolean setServerName(final String val) {
        boolean ret = setSetting(idServerName, SERVER_NAME_NAME, val);
        return ret;
    }

    public String getServerName() {
        return getSetting(SERVER_NAME_NAME, SERVER_NAME);
    }

    public boolean setClientName(String val) {
        boolean ret = setSetting(idClientName, CLIENT_NAME_NAME, val);
        return ret;
    }

    public String getClientName() {
        return getSetting(CLIENT_NAME_NAME, CLIENT_NAME);
    }


    public String getLastLocation() {
        return getSetting(LAST_LOCATION_NAME, "NONE");
    }

    public boolean setLastLocation(final String val) {// редактируется только у клиента
        boolean ret = setSetting(LAST_LOCATION_NAME, val);
        setValInView(idtextGPS);
        return ret;
    }

    public long getLastFindedLocation() {
        return getSetting(LAST_FINDED_LOCATION_NAME, (long) 0);
    }

    public boolean setLastFindedLocation(final long val) {//редактируется только у клиента
        boolean ret = setSetting(LAST_FINDED_LOCATION_NAME, val);
        setValInView(idLFLocation);
        return ret;
    }

    public long getDelayFindedLocation() {
        return getSetting(DELAY_FINDED_LOCATION_NAME, DELAY_FINDED_LOCATION);
    }

    public boolean setDelayFindedLocation(final long val) {
        long prev = getDelayFindedLocation();
        if (prev == val) return true;
        boolean ret = setSetting(idDFLocation, DELAY_FINDED_LOCATION_NAME, val);
        if (isClient())
            mServices.reinitGPS(mContext);
        return ret;
    }
    /* ####################### */

    /*
    *   true Client
    *   false Server
    */
    private boolean setBType(Boolean val) {
        if (isClient())
            mServices.SendData(StateMessages.MSG_SETTING + StateMessages.setMessageObject(BLUETOOTH_TYPE_NAME, val));
        return setSetting(BLUETOOTH_TYPE_NAME, val);
    }

    public boolean isClient() {
        return getSetting(BLUETOOTH_TYPE_NAME, BLUETOOTH_TYPE);
    }

    public boolean isServer() {
        return !isClient();
    }

    /* ############ */

    public Object[] getSettings() {
        Object[] ret = {
                getPhoneClient(),                   //0
                getPhoneServer(),                   //1
                getMinDistanceChangeToUpdates(),    //2
                getMinTimeElapsedBetweenUpdates(),  //3
                getSamplingPeriodUs(),              //4
                isDTAlaram() ? 1 : 0,               //5
                getZeroXY(),                        //6
                getZeroXZ(),                        //7
                getZeroZY(),                        //8
                getDIdle(),                         //9
                getDelayDimensionRange(),           //10
                getDelaySecondDimensionRange(),     //11
                getDelayResetDimensionRange(),      //12
                getServerName(),                    //13
                getClientName(),                    //14
                getLastFindedLocation(),            //15
                getDelayFindedLocation(),           //16
                isChengedMotion() ? 1 : 0,          //17
                isChengedGPS() ? 1 : 0,             //18
                getLastLocation()};                 //19
        return ret;
    }

    private boolean isTrue(String val) {
        return 0 == "1".compareTo(val);
    }

    public void convertToSetting(final String[] var) {//client->server
        setPhoneClient(var[0]);
        setPhoneServer(var[1]);
        setMinDistanceChangeToUpdates(Long.valueOf(var[2]));
        setMinTimeElapsedBetweenUpdates(Long.valueOf(var[3]));
        setSamplingPeriodUs(Integer.valueOf(var[4]));
        setDTAlaram(isTrue(var[5]));
        setZeroXY(Integer.valueOf(var[6]));
        setZeroXZ(Integer.valueOf(var[7]));
        setZeroZY(Integer.valueOf(var[8]));
        setDIdle(Long.valueOf(var[9]));
        setDelayDimensionRange(Long.valueOf(var[10]));
        setDelaySecondDimensionRange(Long.valueOf(var[11]));
        setDelayResetDimensionRange(Long.valueOf(var[12]));
        setServerName(var[13]);
        setClientName(var[14]);
        setLastFindedLocation(Long.valueOf(var[15]));
        setDelayFindedLocation(Long.valueOf(var[16]));

        setChangeMotion(isTrue(var[17]));
        setChangeGPS(isTrue(var[18]));

        setLastLocation(var[19]);

    }

    public void convertToSetting(final String name, final String val) {//server -> client
        if (0 == MIN_DISTANCE_CHANGE_TO_UPDATES_NAME.compareTo(name)) {
            setMinDistanceChangeToUpdates(Long.parseLong(val));
        } else if (0 == MIN_TIME_ELAPSED_BETWEEN_UPDATES_NAME.compareTo(name)) {
            setMinTimeElapsedBetweenUpdates(Long.parseLong(val));
        } else if (0 == PHONE_SERVER_NAME.compareTo(name)) {
            setPhoneServer(val);
        } else if (0 == PHONE_CLIENT_NAME.compareTo(name)) {
            setPhoneClient(val);
        } else if (0 == SERVER_NAME_NAME.compareTo(name)) {
            setServerName(val);
        } else if (0 == CLIENT_NAME_NAME.compareTo(name)) {
            setClientName(val);
        } else if (0 == BLUETOOTH_TYPE_NAME.compareTo(name)) {
            setBType(Boolean.parseBoolean(val));
        } else if (0 == DOUBLE_TARICING_ALARM_NAME.compareTo(name)) {
            setDTAlaram(Boolean.valueOf(val));
        } else if (0 == ZERO_DEGREE_XY_NAME.compareTo(name)) {
            setZeroXY(Integer.parseInt(val));
        } else if (0 == ZERO_DEGREE_XZ_NAME.compareTo(name)) {
            setZeroXZ(Integer.parseInt(val));
        } else if (0 == ZERO_DEGREE_ZY_NAME.compareTo(name)) {
            setZeroZY(Integer.parseInt(val));
        } else if (0 == DELAY_IDLE_NAME.compareTo(name)) {
            setDIdle(Long.parseLong(val));
        } else if (0 == DELAY_DIMENSION_RANGE_NAME.compareTo(name)) {
            setDelayDimensionRange(Long.parseLong(val));
        } else if (0 == DELAY_SECOND_DIMENSION_RANGE_NAME.compareTo(name)) {
            setDelaySecondDimensionRange(Long.parseLong(val));
        } else if (0 == DELAY_RESET_DIMENSIONS_NAME.compareTo(name)) {
            setDelayResetDimensionRange(Long.parseLong(val));
        } else if (0 == SAMPLING_PERIOD_US_NAME.compareTo(name)) {
            setSamplingPeriodUs(Integer.parseInt(val));
        } else if (0 == LAST_FINDED_LOCATION_NAME.compareTo(name)) {
            //setLastFindedLocation(Long.parseLong(val));
        } else if (0 == DELAY_FINDED_LOCATION_NAME.compareTo(name)) {
            setDelayFindedLocation(Long.parseLong(val));
        }
    }

    /* ############ */

    abstract class IChanger {
        boolean isInit = false;

        abstract void change(final String val);

        abstract String getVal();
    }

    private boolean mDisableWriteSettings = false;
    private ArrayList<Pair<Integer, IChanger>> edits = new ArrayList<>();
    private int idBType;
    private int idButtonGPS;
    private int idtextGPS;
    private int idButtonMotionListener;
    private Activity mActivity;


    public boolean isChengedGPS() {
        return getSetting(CHANGE_GPS_NAME, false);
    }

    public boolean isChengedMotion() {
        return getSetting(CHANGE_MOTION_NAME, false);
    }

    public void setChangeGPS(boolean checked) {
        //if (isClient()) return;
        setChecked(idButtonGPS, checked);
        setSetting(CHANGE_GPS_NAME, checked);
    }

    public void setChangeMotion(boolean checked) {
        //if (isClient()) return;
        setChecked(idButtonMotionListener, checked);
        setSetting(CHANGE_MOTION_NAME, checked);
    }

    private void setChecked(int id, boolean checked) {
        if (null == mActivity) return;
        View view = mActivity.findViewById(id);
        if (null == view) return;
        ToggleButton toggleButton = (ToggleButton) view;
        toggleButton.setChecked(checked);
    }

    public void setEnabledUIChangeType(boolean enable) {
        setEnabledView(idBType, enable);
    }

    public void setEnabledUIServerGroup(boolean enable) {
        setEnabledView(idPhoneServer, enable);
        setEnabledView(idPhoneClient, enable);
        setEnabledView(idServerName, enable);
    }

    public void setEnabledUIClientGroup(boolean enable) {
        setEnabledView(idDFLocation, enable);
        setEnabledView(idButtonGPS, enable);
        setEnabledView(idButtonMotionListener, enable);
        setEnabledView(idChangeTypeSensor, enable);
        setEnabledView(idZXY, enable);
        setEnabledView(idZXZ, enable);
        setEnabledView(idZZY, enable);
        setEnabledView(idDIdle, enable);
        setEnabledView(idDTAlarm, enable);
        setEnabledView(idDelayDR, enable);
        setEnabledView(idDelaySDR, enable);
        setEnabledView(idDelayRDR, enable);
        setEnabledView(idMDCToUpdates, enable);
        setEnabledView(idMTEBUpdates, enable);
        setEnabledView(idClientName, enable);
    }

    private boolean setEnabledView(int id, boolean enable) {
        if (null == mActivity) return false;
        View view = mActivity.findViewById(id);
        if (null == view)
            return false;
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            editText.setEnabled(enable);
        } else if (view instanceof ToggleButton) {
            ToggleButton toggleButton = (ToggleButton) view;
            toggleButton.setEnabled(enable);
        } else if (view instanceof RadioGroup) {
            RadioGroup radioGroup = (RadioGroup) view;
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(enable);
            }
        }
        return true;
    }

    public void init() {
        this.idtextGPS = R.id.textGPS;
        this.idButtonGPS = R.id.buttonGPS;
        this.idButtonMotionListener = R.id.buttonMotionListener;
        this.idBType = R.id.toggleBType;
        this.idChangeTypeSensor = R.id.ChangeTypeSensor;
        this.idZXY = R.id.editTextXY;
        this.idZXZ = R.id.editTextXZ;
        this.idZZY = R.id.editTextZY;
        this.idDIdle = R.id.editTextDIdle;
        this.idDTAlarm = R.id.toggleDTAlarm;
        this.idDoubleCheckGroup = R.id.DoubleCheckGroup;
        this.idDelayDR = R.id.editTextDR;
        this.idDelaySDR = R.id.editTextSDR;
        this.idDelayRDR = R.id.editTextRDR;
        this.idMDCToUpdates = R.id.editTextMDCToUpdates;
        this.idMTEBUpdates = R.id.editTextMTEBUpdates;
        this.idPhoneServer = R.id.editTextPhoneServer;
        this.idPhoneClient = R.id.editTextPhoneClient;
        this.idServerName = R.id.editTextServerName;
        this.idClientName = R.id.editTextClientName;

        this.idLFLocation = R.id.editTextLFLocation;
        this.idDFLocation = R.id.editTextDFLocation;

        edits.add(new Pair<Integer, IChanger>(idtextGPS, new IChanger() {

            @Override
            public void change(String val) {
            }

            @Override
            public String getVal() {
                return getLastLocation();
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idLFLocation, new IChanger() {

            @Override
            public void change(String val) {
            }

            @Override
            public String getVal() {
                long yourmilliseconds = getLastFindedLocation();
                if (0 == yourmilliseconds)
                    return "NONE";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd, HH:mm");
                Date resultdate = new Date(yourmilliseconds);
                return sdf.format(resultdate);
                //return String.valueOf(getDelayFindedLocation());
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idDFLocation, new IChanger() {

            @Override
            public void change(String val) {
                if (mDisableWriteSettings) return;
                try {
                    float tmp = ToFloat(val) * 3600000.f;
                    setDelayFindedLocation((long) tmp);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                float tmp = (float) getDelayFindedLocation() / 3600000.f;
                return String.valueOf(tmp);
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idPhoneServer, new IChanger() {

            @Override
            public void change(String val) {
                try {
                    setPhoneServer(val);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return getPhoneServer();
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idPhoneClient, new IChanger() {

            @Override
            public void change(String val) {
                try {
                    setPhoneClient(val);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return getPhoneClient();
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idMTEBUpdates, new IChanger() {

            @Override
            public void change(String val) {
                try {
                    float tmp = ToFloat(val) * 1000.f;
                    setMinTimeElapsedBetweenUpdates((long) tmp);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                float tmp = (float) getMinTimeElapsedBetweenUpdates() / 1000.f;
                return String.valueOf(tmp);
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idMDCToUpdates, new IChanger() {

            @Override
            public void change(String val) {
                try {
                    setMinDistanceChangeToUpdates(Long.parseLong(val));
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return String.valueOf(getMinDistanceChangeToUpdates());
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idChangeTypeSensor, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    setSamplingPeriodUs(Integer.parseInt(val));
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return String.valueOf(getSamplingPeriodUs());
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idBType, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    boolean client = Boolean.parseBoolean(val);
                    setBType(client);
                    if (client) {
                        setEnabledUIClientGroup(true);
                    } else {
                        setEnabledUIClientGroup(mServices.isBTConnected());
                        setEnabledUIServerGroup(true);
                    }
                    if (null != mActivity && mActivity instanceof MainActivity && ((MainActivity) mActivity).isVisible())
                        mServices.reinitAll(mContext);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return String.valueOf(isClient());
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idServerName, new IChanger() {

            @Override
            public void change(String val) {
                try {
                    setServerName(val);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return getServerName();
            }
        }));
        edits.add(new Pair<Integer, IChanger>(idClientName, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    setClientName(val);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return getClientName();
            }
        }));


        edits.add(new Pair<Integer, IChanger>(idDTAlarm, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    setDTAlaram(Boolean.parseBoolean(val));
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return String.valueOf(isDTAlaram());
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idDIdle, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    float tmp = ToFloat(val) * 1000.f;
                    setDIdle((int) tmp);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                float tmp = (float) getDIdle() / 1000.f;
                return String.valueOf(tmp);
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idZXY, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    setZeroXY(Integer.parseInt(val));
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return String.valueOf(getZeroXY());
            }
        }));
        edits.add(new Pair<Integer, IChanger>(idZXZ, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    setZeroXZ(Integer.parseInt(val));
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return String.valueOf(getZeroXZ());
            }
        }));
        edits.add(new Pair<Integer, IChanger>(idZZY, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    setZeroZY(Integer.parseInt(val));
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                return String.valueOf(getZeroZY());
            }
        }));


        edits.add(new Pair<Integer, IChanger>(idDelayDR, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    float tmp = ToFloat(val) * 1000.f;
                    setDelayDimensionRange((long) tmp);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                float tmp = (float) getDelayDimensionRange() / 1000.f;
                return String.valueOf(tmp);
            }
        }));
        edits.add(new Pair<Integer, IChanger>(idDelaySDR, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    float tmp = ToFloat(val) * 1000.f;
                    setDelaySecondDimensionRange((long) tmp);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                float tmp = (float) getDelaySecondDimensionRange() / 1000.f;
                return String.valueOf(tmp);
            }
        }));

        edits.add(new Pair<Integer, IChanger>(idDelayRDR, new IChanger() {
            @Override
            public void change(String val) {
                try {
                    float tmp = ToFloat(val) * 1000.f;
                    setDelayResetDimensionRange((long) tmp);
                } catch (Exception e) {
                }
            }

            @Override
            public String getVal() {
                float tmp = (float) getDelayResetDimensionRange() / 1000.f;
                return String.valueOf(tmp);
            }
        }));
    }

    private static float ToFloat(String val) {
        return Float.parseFloat(val.replace(",", "."));
    }


    public void updateUI(Activity activity) {
        this.mActivity = activity;
        if (null == this.mActivity) return;

        setChangeGPS(isChengedGPS());
        setChangeMotion(isChengedMotion());

        for (Pair<Integer, IChanger> it : edits) {
            if (null == it || it.first == 0) continue;
            View view = mActivity.findViewById(it.first);
            if (null == view) continue;
            final IChanger mIChanger = it.second;

            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                if (false == it.second.isInit) {
                    //it.second.isInit = true;
                    TextWatcher textWatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (mDisableWriteSettings) return;
                            mIChanger.change(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    };
                    editText.addTextChangedListener(textWatcher);
                }
                editText.setText(mIChanger.getVal());
            } else if (view instanceof ToggleButton) {
                final ToggleButton toggleButton = (ToggleButton) view;
                toggleButton.setChecked(mIChanger.getVal().compareTo("true") == 0);
                if (false == it.second.isInit) {
                    //it.second.isInit = true;
                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v instanceof ToggleButton) {
                                ToggleButton tbutton = (ToggleButton) v;
                                if (mDisableWriteSettings) return;
                                mIChanger.change(String.valueOf(tbutton.isChecked()));
                            }

                        }
                    };
                    onClickListener.onClick(toggleButton);
                    toggleButton.setOnClickListener(onClickListener);
                }
            } else if (view instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) view;
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                    if (radioButton.getText().toString().compareTo(mIChanger.getVal()) == 0) {
                        radioGroup.check(radioButton.getId());
                        break;
                    }
                }
                if (false == it.second.isInit) {
                    //it.second.isInit = true;
                    RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            if (-1 == checkedId) return;
                            if (mDisableWriteSettings) return;
                            RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
                            if (null != radioButton)
                                mIChanger.change(radioButton.getText().toString());
                        }
                    };
                    radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
                }
            } else if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (false == it.second.isInit) {
                    //it.second.isInit = true;
                    TextWatcher textWatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (mDisableWriteSettings) return;
                            mIChanger.change(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    };
                    textView.addTextChangedListener(textWatcher);
                }
                textView.setText(mIChanger.getVal());
            }

        }

    }

    private void setValInView(int id) {
        if (null == mActivity) return;
        Pair<Integer, IChanger> it = null;
        for (Pair<Integer, IChanger> finded : edits) {
            if (finded.first == id) {
                it = finded;
                break;
            }
        }

        mDisableWriteSettings = true;
        View view = mActivity.findViewById(it.first);
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            int pos = editText.getSelectionStart();
            editText.setText(it.second.getVal());
            if (-1 != pos) editText.setSelection(pos);
        } else if (view instanceof ToggleButton) {
            final ToggleButton toggleButton = (ToggleButton) view;
            toggleButton.setChecked(it.second.getVal().compareTo("true") == 0);
        } else if (view instanceof RadioGroup) {
            RadioGroup radioGroup = (RadioGroup) view;
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                if (radioButton.getText().toString().compareTo(it.second.getVal()) == 0) {
                    radioGroup.check(radioButton.getId());
                    break;
                }
            }
        }
        mDisableWriteSettings = false;
    }


}
