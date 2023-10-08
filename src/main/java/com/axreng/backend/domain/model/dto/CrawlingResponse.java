package com.axreng.backend.domain.model.dto;

import java.io.Serializable;
import java.util.UUID;

public class CrawlingResponse implements Serializable {

    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
