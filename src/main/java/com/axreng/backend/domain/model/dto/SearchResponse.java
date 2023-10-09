package com.axreng.backend.domain.model.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class SearchResponse implements Serializable {

    private String id;

    private SearchStatus status;

    private Set<String> urls;

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

    public Set<String> getUrls() {
        return urls;
    }

    public void setUrls(Set<String> urls) {
        this.urls = urls;
    }
}
