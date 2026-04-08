package com.shlms.enums;

public enum ReportStatus {
    DRAFT("草稿"),
    PENDING_AUDIT("待审核"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    DISTRIBUTED("已分发");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
