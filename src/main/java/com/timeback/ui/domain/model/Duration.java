package com.timeback.ui.domain.model;

public class Duration {
    private final long millis;

    public Duration(long millis) {
        this.millis = millis;
    }

    public long getMillis() { return millis; }
    public long getMinutes() { return millis / 60_000; }
    public double getHours() { return millis / 3_600_000.0; }
}
