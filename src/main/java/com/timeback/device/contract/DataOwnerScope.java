package com.timeback.device.contract;

public record DataOwnerScope(String value) {
    public DataOwnerScope {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("owner scope must not be blank");
        }
    }
}
