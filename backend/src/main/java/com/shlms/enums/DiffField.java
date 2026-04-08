package com.shlms.enums;

public enum DiffField {
    CONTENT("内容"),
    TITLE("标题"),
    STATUS("状态");

    private final String displayName;

    DiffField(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
