package com.axreng.backend.domain.model;

public class Search {

    private final String searchId;

    private final String keyword;

    public Search(String searchId, String keyword) {
        this.searchId = searchId;
        this.keyword = keyword;
    }

    public String getSearchId() {
        return searchId;
    }

    public String getKeyword() {
        return keyword;
    }
}
