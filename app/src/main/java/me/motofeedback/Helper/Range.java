package me.motofeedback.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by admin on 02.08.2016.
 */
public class Range {

    public static class TRange {
        public int fromDegreeEp = 0;
        public int toDegreeEp = 0;

        public TRange() {
        }

        public void setRange(Range range) {
            fromDegreeEp = range.fromDegreeEp;
            toDegreeEp = range.toDegreeEp;
        }
    }

    public int fromDegree;
    public int toDegree;

    public int fromDegreeEp;
    public int toDegreeEp;

    private final int MAX_ANGLE_DIF_DISABLE = 180;
    public boolean difIsOver = false;
    public boolean difIsEnabled = false;

    private final int MAX_ANGLE = 360;
    private int mZero = 0;

    public void setZero(int zero) {
        mZero = zero;
    }

    ArrayList<Integer> array = new ArrayList<>();

    public void clearRange() {
        difIsOver = false;
        difIsEnabled = false;
        fromDegree = toDegree = 0;
        array.clear();
    }


    public Range() {
        /*
        if (true) return;
        clearRange();
        String test = "96 97 98 99 100 101 102 103 104 105 106 107 108 109 110 111 112 113 114 115 116 117 118 120 121 122 124 126 127 129 130 132 133 135 141 144 145 148 152 157 171 177 180 193 198 199 202 207 239 240 241 242 244 245 246 248 249 250 251 252 253 254 255 256 257 258 260";
        //268 269 270 271 272 273 274 275 276 277 278 279 280 281 282 283 284 285 286 287 288 289 291 292 293 294 295 296 297 298 300 301 302 303
        for (String it : test.split(" "))
            processRange(Integer.parseInt(it), true);
        stopRange();
        */
    }

    private void determineRange(int degrees) {
        if (!array.contains(degrees))
            array.add(degrees);
    }

    private double fixAngle(double degree) {
        double tmp = degree - MAX_ANGLE * (int) (degree / MAX_ANGLE);
        return tmp < 0 ? MAX_ANGLE + tmp : tmp;
    }

    private double minDegrees(double minDeg, double maxDeg) {
        double ret1 = maxDeg - minDeg;
        double ret2 = MAX_ANGLE - maxDeg + minDeg;
        return Math.min(ret1, ret2);
    }


    private double rMaxAngle(double angle) {
        return angle - MAX_ANGLE * (int) (angle / MAX_ANGLE);
    }

    //contain if != 0
    private double isContain(double angle, double from, double to) {
        double ret = 0;
        double dif1 = angle - from;
        double dif2 = 0;
        if (dif1 < 0)
            dif2 = (MAX_ANGLE - (from + to)) + angle;
        else
            dif2 = angle - (from + to);
        if (dif2 > 0)
            if (Math.abs(dif1) < Math.abs(dif2))
                ret = dif1;
            else
                ret = dif2;
        /*
        double dif = angle - from;
        if (to > 0 && (dif < 0 || dif > to))
            ret = dif - to;
        else if (to < 0 && (dif > 0 || dif < to))
            ret = to - dif;
            */

        /*
        if (ret != 0) {
            double t1 = rMaxAngle(from - ret);
            double t2 = rMaxAngle(from + to - ret);
            if (Math.abs(t1) > Math.abs(t2))
                return t2;
            else
                return t1;
        }
        */
        return ret;
    }

    public void stopRange() {
        Collections.sort(array, new Comparator<Integer>() {
            public int compare(Integer l, Integer h) {
                return l > h ? 1 : l < h ? -1 : 0;
            }
        });
        int size = array.size();

        if (size > 1) {
            int maxI = -1;
            int maxDif = -1;
            for (int i = 0; i < size; i++) {
                int prev;
                int curr = array.get(i);
                if (i == 0) {
                    prev = array.get(size - 1);
                } else {
                    prev = array.get(i - 1);
                }
                if (prev > curr)
                    prev = prev - MAX_ANGLE;
                int dif = curr - prev;

                if (dif > maxDif) {
                    maxDif = dif;
                    maxI = i;
                }
            }
            while (maxI > 0) {
                array.add(array.remove(0));
                maxI--;
            }
        }
/*
        if (size >= 2) {
            int min = array.get(0);
            int max = array.get(size - 1);
            fromDegree = min;
            int toLeft = max - min;
            int cToLeft = 0;
            int toRight = -1 * (MAX_ANGLE - max + min);
            int cToRight = 0;

            if (size > 2) {
                for (int it : array) {
                    if (isContain(it, fromDegree, toLeft) >= 0)
                        cToLeft++;
                    else
                        cToRight++;
                }
                if (cToLeft != 0 && cToRight != 0)
                    cToLeft = cToLeft;
                if (cToLeft > 0)
                    toDegree = toLeft;
                else
                    toDegree = toRight;
            } else {
                if (Math.abs(toLeft) < Math.abs(toRight))
                    toDegree = toLeft;
                else
                    toDegree = toRight;
            }
        } else
        */
        if (size >= 2) {

            int first = array.get(0);
            int last = array.get(size - 1);
            if (first >= last) {
                fromDegree = last;
                toDegree = first;
            } else {
                fromDegree = first;
                toDegree = last;
            }
            int left = toDegree - fromDegree;
            int right = MAX_ANGLE - toDegree + fromDegree;
            if (left <= right) {
                toDegree = left;
            } else {
                toDegree = right;
                fromDegree = fromDegree - right;
                if (fromDegree < 0)
                    fromDegree = MAX_ANGLE + fromDegree;
            }
        } else if (size == 1) {
            fromDegree = array.get(0);
            toDegree = 0;
        }


        int sign = toDegree >= 0 ? +1 : -1;
        fromDegreeEp = (int) fixAngle(fromDegree - sign * mZero);
        toDegreeEp = toDegree + sign * 2 * mZero;
        difIsOver = Math.abs(toDegreeEp) >= MAX_ANGLE_DIF_DISABLE;
        difIsEnabled = true;
        /*
        if (fromDegreeEp + toDegreeEp > MAX_ANGLE) {
            fromDegreeEp = fromDegreeEp + toDegreeEp - MAX_ANGLE;
            toDegreeEp = -toDegreeEp;
        }*/

        String tmpS = "";
        for (int it : array)
            tmpS += " " + it;
        //TLog.Log("tmp1 = [" + tmpS + "]");

        //TLog.Log(this, " {" + fromDegree + ":" + toDegree + "}");
        //TLog.Log("Z{" + fromDegreeEp + ":" + toDegreeEp + "}");
    }

    public int getDif(double degree) {
        int ret = 0;
        if (difIsOver)
            return 0;
        double fixDegree = fixAngle(degree);
        //ret = isContain(fixDegree, fromDegree, toDegree);
        ret = (int) isContain(fixDegree, fromDegreeEp, toDegreeEp);
        return ret;
    }

    public static final int MSG_ALARM_NONE = -1;
    public static final int MSG_ALARM_POS = 0;
    public static final int MSG_ALARM_VIBR = 1;
    public static final int MSG_ALARM_ALL = 2;

    public int getDif(TRange range) {
        int ret = MSG_ALARM_NONE;
        String tmp = "";
        int dif = Math.abs(toDegreeEp - range.toDegreeEp);
        if (dif >= 2 * mZero) {
            tmp = " erDif to = " + dif + " ";
            ret = MSG_ALARM_POS;
        }
        dif = Math.abs(fromDegreeEp - range.fromDegreeEp);
        //if (0 == tmp.length() &&
        if (dif >= mZero) {
            tmp = " erDif from = " + dif + " ";
            if (ret == MSG_ALARM_POS)
                ret = MSG_ALARM_ALL;
            else
                ret = MSG_ALARM_VIBR;
        }
        TLog.Log(range.fromDegreeEp + " -> " + range.toDegreeEp + "  ~  " + fromDegreeEp + " ->" + toDegreeEp + "  zero==" + mZero);
        if (0 != tmp.length())
            TLog.Log(tmp);
        return ret;
    }

    double prevDegree = 0;

    public double processRange(double degree, boolean determine) {
        int fixDegree = (int) fixAngle(degree);
        if (determine)
            determineRange(fixDegree);
        double ret = Math.abs(prevDegree - degree);
        prevDegree = degree;
        return ret;
    }
}

