package com.axreng.backend.domain.service;

import com.axreng.backend.domain.model.BadKeywordException;
import com.axreng.backend.domain.model.dto.*;
import com.axreng.backend.infrastructure.CrawlingRepository;
import spark.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

public class CrawlingService implements CrawlingHost {

    private final String BASE_URL;

    private URL baseUrl;

    private Queue<CrawlingRequest> crawlingQueue;

    private final CrawlingRepository crawlingRepository;

    public CrawlingService(final String baseUrl) throws MalformedURLException {
        this.BASE_URL = baseUrl;
        this.crawlingQueue = new LinkedList<>();
        this.crawlingRepository = new CrawlingRepository();
        checksUrl();
    }

    private void checksUrl() throws MalformedURLException {
        this.baseUrl = new URL(BASE_URL);
    }

    public void crawlForKeyword(CrawlingRequest crawlDto, Response resp) {
        if (crawlDto.getKeyword().length() < 4 || crawlDto.getKeyword().length() > 32) {
            throw new BadKeywordException();
        }

        Pattern anchorPattern = getAnchorPattern();
        //Matcher matcher = anchorPattern.matcher();
    }

    private Pattern getAnchorPattern() {
        StringBuilder anchorPattern = new StringBuilder("<a\\s+[^>]*?href=\"(?:http://)?")
                .append(getRegexReadyBaseUrl()) //Model for the regex: "www\.google\.com\/"
                .append("(.\\/|\\??).*?\".*?>.*?</a>");
        return Pattern.compile(anchorPattern.toString());
    }

    private String getRegexReadyBaseUrl() {
        String regexReadyUrl = BASE_URL
                .replace("http://", "")
                .replace("https://", "")
                .replace(".", "\\.");

        if (regexReadyUrl.endsWith("/")) {
            regexReadyUrl = regexReadyUrl.substring(0, regexReadyUrl.length() - 1);
        }

        return regexReadyUrl
                .replace("/", "\\/");
    }

    public int getQueueSize() {
        return this.crawlingQueue.size();
    }

    @Override
    public void accept(CrawlingVisitor visitor) {
        visitor.visit(this);
    }
}
