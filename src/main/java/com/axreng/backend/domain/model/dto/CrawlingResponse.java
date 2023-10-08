package com.axreng.backend.domain.model.dto;

import java.io.Serializable;

public class CrawlingResponse implements Serializable {

    private String id;

    public CrawlingResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
