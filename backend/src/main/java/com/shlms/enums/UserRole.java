package com.shlms.enums;

public enum UserRole {
    ADMIN("管理员"),
    TEACHER("老师"),
    PARENT("家长");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
