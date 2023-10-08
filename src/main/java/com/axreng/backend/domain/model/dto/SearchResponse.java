package com.axreng.backend.domain.model.dto;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class SearchResponse implements Serializable {

    private String id;

    private SearchStatus status;

    private List<URL> urls;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SearchStatus getStatus() {
        return status;
    }

    public void setStatus(SearchStatus status) {
        this.status = status;
    }

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
    }
}
