package com.axreng.backend.domain.service;

import com.axreng.backend.domain.model.BadKeywordException;
import com.axreng.backend.domain.model.KeywordNotCrawledException;
import com.axreng.backend.domain.model.dto.*;
import com.axreng.backend.infrastructure.CrawlingRepository;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import spark.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlingService {

    private final String BASE_URL;

    private CrawlingRepository crawlingRepository;

    public CrawlingService(final String baseUrl) {
        this.BASE_URL = baseUrl;
        this.crawlingRepository = new CrawlingRepository();
    }

    public void crawlForKeyword(CrawlingRequest crawlDto, Response resp) {
        if (crawlDto.getKeyword().length() < 4 || crawlDto.getKeyword().length() > 32) {
            throw new BadKeywordException();
        }

        String urlPattern = "(www|http:|https:)+[^\s]+[\\w]";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher();
    }
}
