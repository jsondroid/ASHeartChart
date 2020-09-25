package js.com.android.asheartchart.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * creat by :  droidjson
 * Date     :  2020/9/24
 * -----------------------------------------------
 * 备注:
 */
public class PenChartView extends View {


    private Chart chart;

    public PenChartView(Context context) {
        this(context, null);
    }

    public PenChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PenChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        chart = new Chart(this);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        chart.setLineSolidSize(dip2px(getContext(), 0.8f));
        chart.setLineDashSize(dip2px(getContext(), 0.3f));
        chart.setLineSize(dip2px(getContext(), 1f));
        chart.init();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        chart.onDrawBG(canvas, getMeasuredWidth(), getMeasuredHeight());
        chart.onDrawXYGrid(canvas);
        chart.onDrawChart(canvas);
    }

    public void offer(int point) {
        chart.offer(point);
    }

    public void offer(int point[]) {
        chart.offer(point);
    }

    /**
     * dp 转 px
     *
     * @param context  上下文
     * @param dipValue dp值
     * @return
     */
    private float dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }


}
