package com.example.liangmutian.randomtextview;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.example.liangmutian.randomtextviewlibrary.RandomTextView;

public class MainActivity extends Activity {

    private RandomTextView mRandomTextView;
    private int[] speeds = new int[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRandomTextView = (RandomTextView) findViewById(R.id.rtv);
        speeds[0] = 10;
        speeds[1] = 9;
        speeds[2] = 8;
        speeds[3] = 7;
        speeds[4] = 6;
        speeds[5] = 5;
        mRandomTextView.setSpeeds(speeds);
        mRandomTextView.start();
    }

    public void start(View v) {
        mRandomTextView.setText("876543");
        mRandomTextView.setSpeeds(RandomTextView.ALL);
        mRandomTextView.start();

    }

    public void start2(View v) {
        mRandomTextView.setText("912111");
        speeds[0] = 7;
        speeds[1] = 6;
        speeds[2] = 12;
        speeds[3] = 8;
        speeds[4] = 18;
        speeds[5] = 10;
        mRandomTextView.setMaxLine(20);
        mRandomTextView.setSpeeds(speeds);
        mRandomTextView.start();

    }

    public void start3(View v) {
        mRandomTextView.setText("9078111123");
        mRandomTextView.setSpeeds(RandomTextView.HIGH_FIRST);
        mRandomTextView.start();

    }

    public void start4(View v) {
        mRandomTextView.setText("1231328.8");
        mRandomTextView.setPointAnimation(true);
        mRandomTextView.setSpeeds(RandomTextView.LOW_FIRST);
        mRandomTextView.start();

    }

    public void start5(View v) {
        mRandomTextView.setText("1231328.8");
        mRandomTextView.setPointAnimation(false);
        mRandomTextView.setSpeeds(RandomTextView.LOW_FIRST);
        mRandomTextView.start();

    }


}
