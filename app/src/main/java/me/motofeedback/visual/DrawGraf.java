package me.motofeedback.visual;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import me.motofeedback.Helper.Range;

/**
 * Created by trbrmrdr on 29/07/16.
 */
public class DrawGraf extends View {

    private final int SIZE_LOG_DRAW = 25;

    public DrawGraf(Context context) {
        super(context);
        Init();
    }

    public DrawGraf(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public DrawGraf(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }


    Paint mBlackPaint = new Paint();
    Paint mWhitePaint = new Paint();
    int width;
    int height;
    int ox;
    int oy;

    Paint mPR = new Paint();
    Paint mPG = new Paint();
    Paint mPB = new Paint();

    Paint mPY = new Paint();

    class TempDraw {
        public double xy;
        public double xz;
        public double zy;

        public double dxy;
        public double dxz;
        public double dzy;

        public TempDraw(double xy, double xz, double zy, double dXY, double dXZ, double dZY) {
            this.xy = xy;
            this.xz = xz;
            this.zy = zy;
            this.dxy = dXY;
            this.dxz = dXZ;
            this.dzy = dZY;
        }
    }

    ArrayList<TempDraw> tempDraw = new ArrayList<>();

    private void Init() {
        mBlackPaint.setStyle(Paint.Style.FILL);
        mBlackPaint.setColor(Color.BLACK);

        mWhitePaint.setStyle(Paint.Style.FILL);
        mWhitePaint.setColor(Color.WHITE);

        mPR.setStyle(Paint.Style.FILL);
        mPR.setColor(Color.RED);

        mPG.setStyle(Paint.Style.FILL);
        mPG.setColor(Color.GREEN);

        mPB.setStyle(Paint.Style.FILL);
        mPB.setColor(Color.BLUE);

        mPY.setStyle(Paint.Style.FILL);
        mPY.setColor(Color.YELLOW);
        mPY.setStrokeWidth(2);
        mPY.setAlpha(120);
    }

    class TSector {
        public int mFrom = 0;
        public int mTo = 0;

        public int mFrom2 = 0;
        public int mTo2 = 0;

        public void set(int from, int to,
                        int fromEp, int toEp) {
            mFrom = -1 * from;
            mTo = -1 * to;

            mFrom2 = -1 * fromEp;
            mTo2 = -1 * toEp;
        }

        public void clear() {
            mFrom = mFrom2 = 0;
            mTo = mTo2 = 0;
        }

        public TSector() {
        }
    }

    TSector tSector1 = new TSector();
    TSector tSector2 = new TSector();
    TSector tSector3 = new TSector();
    boolean isRange = true;

    public void free() {
        clearRange();
        invalidate();
    }

    public void clearRange() {
        isRange = false;
        tSector1.clear();
        tSector2.clear();
        tSector3.clear();
        tempDraw.clear();
        alarmCount = 0;
        alarmMsg = Range.MSG_ALARM_NONE;
    }

    public void setRange(int from1, int to1, int from1Ep, int to1Ep,
                         int from2, int to2, int from2Ep, int to2Ep,
                         int from3, int to3, int from3Ep, int to3Ep) {
        isRange = true;
        /*
        TLog.Log("setRange - " +
                        from1 + "->" + to1 + " : " + from1Ep + "->" + to1Ep + "  \n  " +
                        from2 + "->" + to2 + " : " + from2Ep + "->" + to2Ep + "  \n  " +
                        from3 + "->" + to3 + " : " + from3Ep + "->" + to3Ep
        );
        */
        tSector1.set(from1, to1, from1Ep, to1Ep);
        tSector2.set(from2, to2, from2Ep, to2Ep);
        tSector3.set(from3, to3, from3Ep, to3Ep);
    }

    boolean alarm = false;

    public void changeAlarm(boolean enable) {
        alarm = enable;
    }

    int rxy = 0;
    int rxz = 0;
    int rzy = 0;
    int alarmCount = 0;
    int alarmMsg = Range.MSG_ALARM_NONE;

    public void alarm(int count, int msg) {
        alarmCount = count;
        alarmMsg = msg;
        invalidate();
    }

    public void alarm(int xy, int xz, int zy) {
        rxy = xy;
        rxz = xz;
        rzy = zy;
        invalidate();
    }

    public void draw(int xy, int xz, int zy,
                     int dxy, int dxz, int dzy) {
        if (width == 0 || height == 0) {
            width = getWidth();
            height = getHeight();
            oy = (int) (height * 0.5f);
            ox = (int) (width * 0.5f);
        }
        if (width == 0 || height == 0)
            return;

        if (tempDraw.size() > SIZE_LOG_DRAW)
            tempDraw.remove(0);
        tempDraw.add(new TempDraw(xy, xz, zy, dxy, dxz, dzy));
        invalidate();
    }

    float radius = 180.0f;
    float dr1 = 20.f;

    private Point getPos(double degree, float radius) {
        Point ret = new Point();
        double rad = Math.toRadians(-1 * degree);
        int tx = (int) (Math.cos(rad) * radius);
        int ty = (int) (Math.sin(rad) * radius);
        if (degree == 90 || degree == -90)
            tx = 0;
        if (degree == 0 || degree == 180 || degree == -180)
            ty = 0;
        ret.x = tx;
        ret.y = ty;
        return ret;
    }

    private void drawline(Canvas canvas, double degree, Paint paint, float len) {
        int tx0 = ox;
        int ty0 = oy;
        Point p = getPos(degree, len);
        canvas.drawLine(tx0, ty0, tx0 + p.x, ty0 + p.y, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawGrid(canvas);

        int i = 0;
        TempDraw last = null;
        for (TempDraw it : tempDraw) {
            drawline(canvas, it.xy, mPR, radius);
            drawline(canvas, it.xz, mPG, radius);
            drawline(canvas, it.zy, mPB, radius);

            canvas.drawLine(i, 0, i, (float) it.dxy, mPR);
            canvas.drawLine(i, 0, i, (float) it.dxz, mPG);
            canvas.drawLine(i, 0, i, (float) it.dzy, mPB);
            i++;
            last = it;
        }

        float len = radius + dr1;

        if (null != last) {
            drawline(canvas, last.xy, mPR, len);
            drawline(canvas, last.xz, mPG, len);
            drawline(canvas, last.zy, mPB, len);
        }

        if (isRange) {
            Paint mPr = new Paint(mPR);
            mPr.setStyle(Paint.Style.FILL);
            mPr.setAlpha(80);
            Paint mPg = new Paint(mPG);
            mPg.setStyle(Paint.Style.FILL);
            mPg.setAlpha(80);
            Paint mPb = new Paint(mPB);
            mPb.setStyle(Paint.Style.FILL);
            mPb.setAlpha(80);

            RectF rectF = new RectF(ox - len, oy - len, ox + len, oy + len);

            if (0 != tSector1.mTo2)
                canvas.drawArc(rectF, (float) tSector1.mFrom2, (float) tSector1.mTo2, true, mPr);
            if (0 != tSector2.mTo2)
                canvas.drawArc(rectF, (float) tSector2.mFrom2, (float) tSector2.mTo2, true, mPg);
            if (0 != tSector3.mTo2)
                canvas.drawArc(rectF, (float) tSector3.mFrom2, (float) tSector3.mTo2, true, mPb);

            if (0 != tSector1.mTo)
                canvas.drawArc(rectF, (float) tSector1.mFrom, (float) tSector1.mTo, true, mPr);
            if (0 != tSector2.mTo)
                canvas.drawArc(rectF, (float) tSector2.mFrom, (float) tSector2.mTo, true, mPg);
            if (0 != tSector3.mTo)
                canvas.drawArc(rectF, (float) tSector3.mFrom, (float) tSector3.mTo, true, mPb);
        }


        if (alarm) {
            if (rxy != 0)
                canvas.drawText(String.valueOf("" + rxy), 10, 10, mPR);
            if (rxz != 0)
                canvas.drawText(String.valueOf("" + rxz), 10, 20, mPG);
            if (rzy != 0)
                canvas.drawText(String.valueOf("" + rzy), 10, 30, mPB);
            if (alarmCount > 0 || Range.MSG_ALARM_NONE != alarmMsg) {
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(20.f);

                String msg = "";
                switch (alarmMsg) {
                    case Range.MSG_ALARM_POS:
                        msg = "new pos";
                        break;
                    case Range.MSG_ALARM_VIBR:
                        msg = "vibr change";
                        break;
                    case Range.MSG_ALARM_ALL:
                        msg = "reinit";
                        break;
                }
                canvas.drawText(String.valueOf("countAlarm = " + alarmCount + " " + msg), 10, 40, paint);
            }
        }
        //canvas.drawPaint(mBlackPaint);
    }

    private void drawGrid(Canvas canvas) {
        canvas.drawLine(0, oy, width, oy, mBlackPaint);
        canvas.drawLine(ox, 0, ox, height, mBlackPaint);

        double radius = ox;
        double rad = Math.toRadians(45);
        double x = Math.cos(rad) * radius;
        double y = Math.sin(rad) * radius;
        draw(canvas,
                ox - x, oy - y,
                ox + x, oy + y,
                mBlackPaint);
        draw(canvas,
                ox - x, oy + y,
                ox + x, oy - y,
                mBlackPaint);

        double radius2 = ox * 0.5;
        radius = ox * 0.75;
        boolean step = true;
        for (int i = 0; i <= 90; i += 5) {
            rad = Math.toRadians(i);
            x = i != 90 ? Math.cos(rad) : 0;
            y = i != 0 ? Math.sin(rad) : 0;

            Paint paint = null;
            double r = 0;
            if (step) {
                paint = mBlackPaint;
                r = radius2;
            } else {
                paint = mWhitePaint;
                r = radius;
            }
            x *= r;
            y *= r;

            draw(canvas, ox + x, oy + y, ox - x, oy - y, paint);
            draw(canvas, ox - x, oy + y, ox + x, oy - y, paint);
            step = !step;
        }
    }

    private void draw(Canvas canvas, double x0, double y0, double x1, double y1, Paint paint) {
        canvas.drawLine((float) x0, (float) y0, (float) x1, (float) y1, paint);
    }

}
