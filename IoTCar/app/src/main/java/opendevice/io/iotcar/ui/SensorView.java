package opendevice.io.iotcar.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.github.anastr.speedviewlib.DeluxeSpeedView;
import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.anastr.speedviewlib.base.Speedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.Timer;
import java.util.TimerTask;

import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.ext.obd.OBDSensorPID;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class SensorView extends LinearLayout {

    private OBDSensorPID sensor;
    private long lastValue = 0;
    private Speedometer speedView;
    private LineChart chart;
    private Handler mainThread;
    private Timer timer;

    public SensorView(Context context, OBDSensorPID sensor) {
        super(context);
        this.sensor = sensor;
        init();
        mainThread = new Handler(Looper.getMainLooper());
    }

    /**
     * Create realistic real-time chart effect
     * @param enable
     */
    public void enableAnimation(boolean enable){
        if(enable){
            if(timer == null) timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mainThread.post(updateChartUI); //  Update in UI Thread
                }
            }, 0, 80l);
        }else{
            if(timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }

    //  Update in UI Thread
    private Runnable updateChartUI = new Runnable() {
        public void run() {
            updateChart(lastValue);
        }
    };

    private void init() {

        int maxValue = 100;
        if (OBDSensorPID.SPEED == sensor) {
            maxValue = 250;
        }
        if (OBDSensorPID.ENGINE_RPM == sensor) {
            maxValue = 5000;
        }

        setOrientation(LinearLayout.VERTICAL);


        // Title
        TextView tv1 = new TextView(getContext());
        tv1.setText(sensor.getDescription());
        tv1.setTextSize(20);
        tv1.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams.bottomMargin = 6;
        addView(tv1, layoutParams);

        // LineChart
        // ====================

        chart = new LineChart(getContext());
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
//        mChart.setBackgroundColor(Color.LTGRAY);

        // Setup empty dataset
        LineData data = new LineData();
        data.setValueTextColor(Color.RED);
        data.addDataSet(createDataSet());
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);

        // modify the legend ...
//        l.setForm(Legend.LegendForm.LINE);
//        l.setTypeface(mTfLight);
//        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
//        xl.setTypeface(mTfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
//        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(maxValue);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(true);

        layoutParams = new LayoutParams(MATCH_PARENT, 200);
        layoutParams.gravity = Gravity.CENTER;
        addView(chart, layoutParams);

        // Gauge
        // ====================

        speedView = new PointerSpeedometer(getContext());
        speedView.setWithTremble(false);
//            speedView.setIndicator(Indicator.Indicators.LineIndicator);
        speedView.setTextSize(40);
//            ((PointerSpeedometer)speedView).setSpeedometerColor(Color.BLUE);
//            ((PointerSpeedometer)speedView).setPointerColor(Color.GREEN);
//            ((PointerSpeedometer)speedView).setBackgroundCircleColor(Color.WHITE);
//            ((PointerSpeedometer)speedView).setMarkColor(Color.CYAN);
//            ((PointerSpeedometer)speedView).setIndicatorColor(Color.RED);
//            ((PointerSpeedometer)speedView).setCenterCircleColor(Color.RED);
//            ((PointerSpeedometer)speedView).setSpeedTextColor(Color.BLACK);
//            ((PointerSpeedometer)speedView).setUnitTextColor(Color.BLACK);
//            ((PointerSpeedometer)speedView).setTextColor(Color.DKGRAY);
//            ((PointerSpeedometer)speedView).setUnitTextColor(Color.DKGRAY);
//            ImageIndicator imageIndicator = new ImageIndicator(getContext(), R.drawable.image_indicator1);
//            speedView.setIndicator(imageIndicator);



        if (OBDSensorPID.SPEED == sensor) {
            speedView.setUnit("km/h");
        }
        if (OBDSensorPID.ENGINE_RPM == sensor) {
            speedView.setUnit("rpm");
        }

        speedView.setMaxSpeed(maxValue);

        layoutParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.topMargin = 6;
        layoutParams.gravity = Gravity.CENTER;
        addView(speedView, layoutParams);

    }


    private LineDataSet createDataSet() {

        LineDataSet set = new LineDataSet(null, "Legend");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setDrawFilled(true);

        set.addEntry(new Entry(0, 0));
        return set;
    }

    public void updateData(Device device) {
        speedView.speedTo(device.getValue(), 500);
        lastValue = device.getValue();
        updateChart(device.getValue());
    }

    private void updateChart(long value) {
        LineData data = chart.getData();
        ILineDataSet set = data.getDataSetByIndex(0);
        data.addEntry(new Entry(set.getEntryCount(), value), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        chart.notifyDataSetChanged();

        // limit the number of visible entries
        chart.setVisibleXRangeMaximum(80);

        chart.moveViewToX(data.getEntryCount());
    }


}