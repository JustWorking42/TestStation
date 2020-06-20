package com.example.teststation.test.simulation;

import android.content.Context;
import android.os.Handler;

import com.example.teststation.test.CurrentTest;
import com.example.teststation.test.MomentTest;

import java.util.ArrayList;

public class TestSimulation {
    private final int PERIOD = 30;
    private TestSimulationCallback callback;
    private ArrayList<java.lang.String> result = new ArrayList<>();
    private int indexCurrentTest = 0;
    private Handler handler = new Handler();

    public void startSimulation(Context context, TestSimulationCallback callback, int testIndex) {
        result = CurrentTest.getTestsFromFiles(context, testIndex);
        this.callback = callback;
        handler.postDelayed(timeUpdaterRunnable, PERIOD);
    }

    public void stopSimulation() {
        callback = null;
        indexCurrentTest = 0;
        result.clear();
        handler.removeCallbacks(timeUpdaterRunnable);
    }

    private Runnable timeUpdaterRunnable = new Runnable() {
        public void run() {
            callback.getTestData(result.get(indexCurrentTest));
            if (indexCurrentTest < result.size() - 1) {
                indexCurrentTest++;
                handler.postDelayed(this, PERIOD);
            }
            else {
                stopSimulation();
            }
        }
    };
}
