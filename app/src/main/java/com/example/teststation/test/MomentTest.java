package com.example.teststation.test;

public class MomentTest {
    private float time;
    private float voltage;
    private float amperage;

    MomentTest(float time, float voltage, float amperage) {
        this.time = time;
        this.voltage = voltage;
        this.amperage = amperage;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public float getAmperage() {
        return amperage;
    }

    public void setAmperage(float amperage) {
        this.amperage = amperage;
    }
}
