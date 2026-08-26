package com.timeback.device.contract;

public record CollectionCheckpoint(DataOwnerScope owner, long successfulThroughMillis) {}
