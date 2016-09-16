package me.motofeedback.Bluetooth;

/**
 * Created by trbrm on 31.08.2016.
 */
public class StateMessages {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_RECIEVE = 2;
    public static final int MESSAGE_SEND = 3;
    /*
    public static final int MESSAGE_TOAST_CONNECTION_LOST = 4;
    public static final int MESSAGE_TOAST_CONNECTION_FAILED = 5;
    public static final String ERR_MSG = "ERR_MSG";
    */

    public static final String MESSAGE_SEPARATOR = "`";

    public final static String MSG_MOTION_CHANGED = "c_";
    public final static String MSG_MOTION_RANGE = "sr_";
    public final static String MSG_MOTION_CLEAR = "cr_";
    public final static String MSG_MOTION_CHANGE_ALARM = "ca_";
    public final static String MSG_MOTION_START_ALARM = "sa_";
    public final static String MSG_MOTION_DIFINPROCESS = "dpm_";
    public final static String MSG_SETTINGS = "ss_";
    public final static String MSG_SETTING = "sg_";

    public final static String MSG_CHANGE_GPS = "cg_";
    public final static String MSG_GET_LOCATION = "gl_";
    public final static String MSG_CHANGE_MOTION = "cm_";

    private final static String SPLIT = "^";

    public static final String setMessageInt(int... var) {
        String ret = "";
        boolean first = true;
        for (int it : var) {
            if (first) {
                ret += it;
                first = false;
                continue;
            }
            ret += SPLIT + it;
        }
        return ret;
    }

    public static final String setMessageObject(Object... var) {
        String ret = "";
        boolean first = true;
        for (Object it : var) {
            if (first) {
                ret += it;
                first = false;
                continue;
            }
            ret += SPLIT + it;
        }
        return ret;
    }

    public static final int[] getMessageInts(final String var) {
        String[] tmp = var.split("\\" + SPLIT);
        if (tmp.length <= 0)
            return null;
        int[] ret = new int[tmp.length];
        int i = 0;
        for (String it : tmp) {
            ret[i] = Integer.parseInt(it);
            i++;
        }
        return ret;
    }

    public static final String[] getMessageStrings(final String var) {
        String[] tmp = var.split("\\" + SPLIT);
        if (tmp.length <= 0)
            return null;
        return tmp;
    }


}
