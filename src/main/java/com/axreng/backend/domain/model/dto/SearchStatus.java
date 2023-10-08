package com.axreng.backend.domain.model.dto;

public enum SearchStatus {
    ACTIVE, FINISHED;

    @Override
    public String toString() {
        return switch (this) {
            case ACTIVE -> "active";
            case FINISHED -> "done";
            default -> null;
        };
    }
}
