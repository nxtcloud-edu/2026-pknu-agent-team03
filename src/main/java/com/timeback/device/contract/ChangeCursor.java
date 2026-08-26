package com.timeback.device.contract;

public record ChangeCursor(long sequence) {
    public ChangeCursor() {
        this(0);
    }

    public ChangeCursor {
        if (sequence < 0) {
            throw new IllegalArgumentException("cursor sequence must not be negative");
        }
    }
}
