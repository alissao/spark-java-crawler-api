package com.axreng.backend.infrastructure;

import com.axreng.backend.domain.model.dto.SearchResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlingRepository {

    private final ConcurrentHashMap<String, SearchResponse> searchRepository = new ConcurrentHashMap<>();

    public SearchResponse saveSearch(SearchResponse search) {
        boolean searchAlreadyStored = search.getId() != null;
        String searchId = "";
        if (searchAlreadyStored) {
            searchId = search.getId();
            this.searchRepository.computeIfPresent(searchId, (s, storedSearch) -> {
                storedSearch.setStatus(search.getStatus());
                if (search.getUrls() != null) {
                    storedSearch.getUrls().addAll(search.getUrls());
                }
                return storedSearch;
            });
        } else {
            searchId = generateUniqueId();
            search.setId(searchId);
            this.searchRepository.putIfAbsent(searchId, search);
        }
        return searchRepository.get(searchId);
    }

    public Optional<SearchResponse> findById(String id) {
        return Optional.ofNullable(this.searchRepository.get(id));
    }

    private String generateUniqueId() {
        String id = UUID
                .randomUUID().toString()
                .replace("-","")
                .substring(0,8);
        return searchRepository.get(id) == null ? id : generateUniqueId();
    }

}
