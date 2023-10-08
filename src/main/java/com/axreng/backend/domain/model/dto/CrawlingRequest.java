package com.axreng.backend.domain.model.dto;

import java.io.Serializable;

public class CrawlingRequest implements Serializable {

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

}
