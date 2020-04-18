package com.example.liangmutian.randomtextview;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.example.liangmutian.randomtextviewlibrary.RandomTextView;

public class MainActivity extends Activity {

    private RandomTextView mRandomTextView;
    private int[] velocityArray = new int[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRandomTextView = (RandomTextView) findViewById(R.id.rtv);
        velocityArray[0] = 10;
        velocityArray[1] = 9;
        velocityArray[2] = 8;
        velocityArray[3] = 7;
        velocityArray[4] = 6;
        velocityArray[5] = 5;
        mRandomTextView.setPianyilian(velocityArray);
        mRandomTextView.start();
    }

    public void start(View v) {
        mRandomTextView.setText("876543");
        mRandomTextView.setPianyilian(RandomTextView.ALL);
        mRandomTextView.start();

    }

    public void start2(View v) {
        mRandomTextView.setText("912111");
        velocityArray[0] = 7;
        velocityArray[1] = 6;
        velocityArray[2] = 12;
        velocityArray[3] = 8;
        velocityArray[4] = 18;
        velocityArray[5] = 10;
        mRandomTextView.setMaxLine(20);
        mRandomTextView.setPianyilian(velocityArray);
        mRandomTextView.start();

    }

//    start
    public void start3(View v) {
        mRandomTextView.setText("9078111123");
        mRandomTextView.setPianyilian(RandomTextView.FIRSTF_LAST);
        mRandomTextView.start();

    }

    public void start4(View v) {
        mRandomTextView.setText("12313288");
        mRandomTextView.setPianyilian(RandomTextView.FIRSTF_FIRST);
        mRandomTextView.start();

    }


}
