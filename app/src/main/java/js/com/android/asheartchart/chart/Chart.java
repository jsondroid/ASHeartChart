package js.com.android.asheartchart.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * creat by :  droidjson
 * Date     :  2020/9/25
 * -----------------------------------------------
 * 备注:
 */
public class Chart {

    private WeakReference<View> weakview;

    /**
     * 画笔
     */
    private Paint bgPaint;
    private Paint xyPaint;
    private Paint pathPaint;


    /**
     * 背景参数
     */
    private int bgColor;
    private int solidColor;
    private int dashColor;


    /**
     * X、Y轴的最大最小值
     */
    private int YMaxValue = 300;
    private int YMinValue = 0;
    private int XMaxValue = 200;
    private int XMinValue = 0;


    private PathEffect effect;
    private PathEffect limeeffect;

    private float lineSolidSize = 4f;
    private float lineDashSize = 1f;
    private float lineSize = 4f;

    /**
     * chart的区域
     */
    private RectF chart;
    private RectF bgRectF;
    private RectF yRectF;
    private RectF xRectF;
    private Rect textRect;

    private float gridH;
    private float gridW;

    // 数据一秒钟采集频率，默认100个点一秒钟
    private int Hz = 50;
    // 速度控制每秒钟画几个点（1000/(100*1)=10ms）
    private float heart_speed = 0.3f;

    /**
     * 在chart的宽度下能显示几秒钟的点，
     * 如：chart.width=4000px,showTimeSeconds = 2f;
     * 4000px的宽度显示2秒（2秒中采集到的点数）的点数
     */
    private float showTimeSeconds = 4f;
    //记录x轴坐标显示的秒数
    private int xCount = -1;
    private int[] xCounts;

    private Path path;
    // 根据显示秒数,以及采样频率算出总共需要申请多少个内存的数据
    private int[] showTimeDatas;
    // 待显示的数据队列
    private LinkedBlockingDeque<Integer> dataQueue = new LinkedBlockingDeque<>(400);
    // 定时运行栈
    private HeartTask heartTask = null;
    // 精准定时器
    private Timer timer = new Timer();


    public Chart(View view) {
        weakview = new WeakReference<>(view);
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);

        xyPaint = new Paint();
        xyPaint.setAntiAlias(true);

        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);

        this.solidColor = Color.parseColor("#32cd32");
        this.dashColor = Color.parseColor("#32cd32");
        this.bgColor = Color.BLACK;
        effect = new DashPathEffect(new float[]{0.5f, 4f}, 0.5f);
        limeeffect = new DashPathEffect(new float[]{5, 5f}, 5f);
        path = new Path();
    }

    public void init() {
        // 速度怎么可以小于0
        if (heart_speed < 0) {
            throw new RuntimeException("Attributes heart_speed Can Not < 0 ");
        }
        // 最小值怎么可以大于或等于最大值
        if (YMinValue >= YMaxValue) {
            throw new RuntimeException("Attributes heart_min Can Not >= heart_max ");
        }
        showTimeDatas = new int[(int) (showTimeSeconds * Hz)];
        xCounts = new int[showTimeDatas.length];
    }


    public void onDrawBG(Canvas canvas, float viewWidth, float viewHeigth) {
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);
        bgRectF = new RectF(0, 0, viewWidth, viewHeigth);
        canvas.drawRect(bgRectF, bgPaint);

        float y_width = viewWidth * 0.03f;
        float x_Heigth = viewHeigth * 0.08f;
        yRectF = new RectF(0, viewHeigth * 0.05f, y_width, viewHeigth - x_Heigth);
        xRectF = new RectF(y_width, viewHeigth - x_Heigth, viewWidth, viewHeigth);

        chart = new RectF(yRectF.right, yRectF.top, viewWidth, xRectF.top);
        gridH = chart.height() / YMaxValue;
        gridW = chart.width() / XMaxValue;

    }

    public void onDrawXYGrid(Canvas canvas) {

        for (int i = 0; i < YMaxValue; i++) {
            //行
            if (i % 50 == 0 || i % 25 == 0) {
                xyPaint.setColor(solidColor);
                xyPaint.setPathEffect(limeeffect);
                xyPaint.setStrokeWidth(lineSolidSize);
                canvas.drawLine(chart.left, chart.bottom - (gridH * i), chart.right, chart.bottom - (gridH * i), xyPaint);
            } else if (i % 5 == 0) {
                xyPaint.setColor(dashColor);
                xyPaint.setPathEffect(effect);
                xyPaint.setStrokeWidth(lineDashSize);
                canvas.drawLine(chart.left, chart.bottom - (gridH * i), chart.right, chart.bottom - (gridH * i), xyPaint);
            }
            //列
            if (i < XMaxValue) {
                if (i % 10 == 0) {
                    xyPaint.setColor(solidColor);
                    xyPaint.setPathEffect(limeeffect);
                    xyPaint.setStrokeWidth(lineSolidSize);
                    canvas.drawLine(chart.left + (gridW * i), chart.bottom, chart.left + (gridW * i), chart.top, xyPaint);
                } else if (i % 2 == 0) {
                    xyPaint.setColor(dashColor);
                    xyPaint.setPathEffect(effect);
                    xyPaint.setStrokeWidth(lineDashSize);
                    canvas.drawLine(chart.left + (gridW * i), chart.bottom, chart.left + (gridW * i), chart.top, xyPaint);
                }
            }
            /**放在后面*/
            if (i % 50 == 0) {
                drawXYTextLable(canvas, String.valueOf(i), yRectF.centerX(), yRectF.bottom - (i * gridH), yRectF.width() * 0.4f);
            } else if (i == (YMaxValue - 1)) {
                drawXYTextLable(canvas, String.valueOf(i + 1), yRectF.centerX(), yRectF.bottom - (i * gridH), yRectF.width() * 0.4f);
            }
        }
        xyPaint.setPathEffect(null);
        xyPaint.setStrokeWidth(lineSolidSize);
        canvas.drawLine(chart.left, chart.bottom, chart.right, chart.bottom, xyPaint);
        canvas.drawLine(chart.left, chart.bottom, chart.left, chart.top, xyPaint);
    }


    private void drawXYTextLable(Canvas canvas, String text, float cx, float cy, float textSize) {
        xyPaint.setTextSize(textSize);
        textRect = new Rect();
        xyPaint.getTextBounds(text, 0, text.length(), textRect);
        //基准线计算
        float textBaseY = cy + (Math.abs(xyPaint.ascent()) - xyPaint.descent()) / 2f;
        //x==0
        canvas.drawText(text, cx - textRect.width() / 2f, textBaseY, xyPaint);
    }


    public void onDrawChart(Canvas canvas) {
        int[] showDatas = showTimeDatas;
        // 画心电
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setColor(Color.RED);
        pathPaint.setStrokeWidth(lineSize);
        int firstData = showDatas[0];
        float firstY = calculateY(firstData);

        path.reset();
        path.moveTo(chart.left, firstY);
        for (int i = 0; i < showDatas.length; i++) {
            int value = showDatas[i];
            float x = chart.left + (((float) i / showDatas.length) * chart.width());
            float y = calculateY(value);
            path.lineTo(x, y);

            if (xCounts[i] > 0 && xCounts[i] % Hz == 0) {
                xyPaint.setPathEffect(null);
                xyPaint.setStrokeWidth(lineSolidSize);
                canvas.drawLine(x, chart.bottom, x, chart.bottom - gridH * 5f, xyPaint);
                drawXYTextLable(canvas, String.valueOf((float) (xCounts[i]) / Hz) + "s", x, xRectF.centerY(), xRectF.height() * 0.5f);
            }
        }
        canvas.drawPath(path, pathPaint);
    }


    /**
     * 计算点的y坐标
     * int firstY = calculateY(firstData - Min, Max - Min, viewHeight);
     */
    private float calculateY(float value) {
//        return chart.bottom - (((value / (YMaxValue - YMinValue)) * chart.height()));
        return chart.bottom - (value * gridH);
    }


    /**
     * 添加一组点，自动依据频率来动态显示
     *
     * @param points
     */
    public void offer(int[] points) {
        for (int i = 0; i < points.length; i++) {
            offer(points[i]);
        }
    }

    public synchronized void offer(int point) {
        dataQueue.offer(point);
        if (heartTask == null) {
            publishJob();
        }
    }

    /**
     * 重新部署发点任务
     */
    private synchronized void publishJob() {
        // 根据采集的频率，自动算出每一个点之间暂停的时间
        long yield = (int) (1000 / (Hz * heart_speed));
        if (heartTask != null) {
            heartTask.cancel();
            heartTask = null;
        }
        heartTask = new HeartTask();
        timer.scheduleAtFixedRate(heartTask, 0, yield);
    }

    /**
     * 清空图案
     */
    public synchronized void clear() {
        for (int i = 0; i < showTimeDatas.length; i++)
            showTimeDatas[i] = 0;
        if (weakview.get() != null) {
            weakview.get().postInvalidate();
        }
        xCount = -1;
    }

    /**
     * 释放资源
     */
    public synchronized void recycle() {
        if (heartTask != null) {
            heartTask.cancel();
        }
        weakview.clear();
        timer.cancel();
        xCount = -1;
    }

    /**
     * 发点的任务
     */
    class HeartTask extends TimerTask {
        @Override
        public void run() {
            try {
                Integer value = dataQueue.poll();
                xCount++;
                if (value != null) {
                    for (int i = 0; i < showTimeDatas.length; i++) {
                        if (i + 1 < showTimeDatas.length) {
                            showTimeDatas[i] = showTimeDatas[i + 1];
                            xCounts[i] = xCounts[i + 1];
                        } else {
                            showTimeDatas[i] = value;
                            xCounts[i] = xCount;
                        }
                    }
//                    Log.e("run: ", new Gson().toJson(showTimeDatas));
                    weakview.get().postInvalidate();
                } else {
                    cancel();
                    heartTask = null;
//                    xCount = -1;
                }
            } catch (Exception e) {
                e.printStackTrace();
//                xCount = -1;
            }
        }
    }

    public float getLineSize() {
        return lineSize;
    }

    public void setLineSize(float lineSize) {
        this.lineSize = lineSize;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getSolidColor() {
        return solidColor;
    }

    public void setSolidColor(int solidColor) {
        this.solidColor = solidColor;
    }

    public int getDashColor() {
        return dashColor;
    }

    public void setDashColor(int dashColor) {
        this.dashColor = dashColor;
    }

    public int getYMaxValue() {
        return YMaxValue;
    }

    public void setYMaxValue(int YMaxValue) {
        this.YMaxValue = YMaxValue;
    }

    public int getYMinValue() {
        return YMinValue;
    }

    public void setYMinValue(int YMinValue) {
        this.YMinValue = YMinValue;
    }

    public int getXMaxValue() {
        return XMaxValue;
    }

    public void setXMaxValue(int XMaxValue) {
        this.XMaxValue = XMaxValue;
    }

    public int getXMinValue() {
        return XMinValue;
    }

    public void setXMinValue(int XMinValue) {
        this.XMinValue = XMinValue;
    }

    public float getLineSolidSize() {
        return lineSolidSize;
    }

    public void setLineSolidSize(float lineSolidSize) {
        this.lineSolidSize = lineSolidSize;
    }

    public float getLineDashSize() {
        return lineDashSize;
    }

    public void setLineDashSize(float lineDashSize) {
        this.lineDashSize = lineDashSize;
    }
}
