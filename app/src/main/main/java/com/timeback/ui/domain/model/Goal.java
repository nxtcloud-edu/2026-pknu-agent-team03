package com.timeback.ui.domain.model;

public class Goal {
    private final Identifier id;
    private final String name;
    private final Duration targetDuration;

    public Goal(Identifier id, String name, Duration targetDuration) {
        this.id = id;
        this.name = name;
        this.targetDuration = targetDuration;
    }

    public Identifier getId() { return id; }
    public String getName() { return name; }
    public Duration getTargetDuration() { return targetDuration; }
}
