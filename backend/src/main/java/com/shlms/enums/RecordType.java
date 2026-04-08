package com.shlms.enums;

public enum RecordType {
    HEALTH("健康记录"),
    LEARNING("学习表现"),
    BEHAVIOR("行为表现"),
    OTHER("其他");

    private final String displayName;

    RecordType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
