package js.com.android.asheartchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.Random;

import js.com.android.asheartchart.chart.HeartView2;
import js.com.android.asheartchart.chart.PenChartView;

public class MainActivity extends AppCompatActivity {
    HeartView2 headview2;
    PenChartView headview;
    ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        headview2 = findViewById(R.id.headview2);
        headview = findViewById(R.id.headview);
        image = findViewById(R.id.image);

        headview2.setMax(300);
        headview2.setMin(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
//                    int[] numbers = new int[10];
//                    for (int i = 0; i < numbers.length; i++) {
                    int n = new Random().nextInt(300);
//                        numbers[i] = n;
//                    }
                    headview2.offer(n);
                    headview.offer(n);
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }).start();
    }
}
