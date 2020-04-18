package com.example.teststation.test;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CurrentTest {
    public static ArrayList<MomentTest> results = new ArrayList<>();
    public static ArrayList<java.lang.String> strResults = new ArrayList<>();

    public static void appendMomentTest(java.lang.String moment) {
        MomentTest momentTest = getMomentFromString(moment);
        results.add(momentTest);
    }

    private static MomentTest getMomentFromString(java.lang.String moment) {
        java.lang.String[] data = moment.split(",");
        float time = Float.parseFloat(data[0]);
        float vol = Float.parseFloat(data[1]);
        float amp = Float.parseFloat(data[2]);
        return new MomentTest(time, vol, amp);
    }

    public static java.lang.String convertTestsToJson(ArrayList current) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        java.lang.String currentTestJson = gson.toJson(current);
        return currentTestJson;
    }

    public static ArrayList<java.lang.String> getTestsFromFiles(Context context, int fileNumber) {
        ArrayList<java.lang.String> simulation = new ArrayList<>();
        java.lang.String currentFile;

        switch (fileNumber) {
            case 0:
                currentFile = "sample_input/cyclic.txt";
                break;
            case 1:
                currentFile = "sample_input/linear_sweep.txt";
                break;
            case 2:
                currentFile = "sample_input/sinusoid.txt";
                break;
            default:
                currentFile = "sample_input/constant_voltage.txt";
                break;
        }

        try {
            InputStreamReader input = new InputStreamReader(context.getAssets().open(currentFile));
            BufferedReader reader = new BufferedReader(input);
            java.lang.String line = reader.readLine();
            strResults.add(line);
            simulation.add(line + "\n");
            while (line != null){
                simulation.add(line + "\n");
                strResults.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return simulation;
    }
}

