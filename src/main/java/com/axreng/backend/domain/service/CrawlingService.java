package com.axreng.backend.domain.service;

import com.axreng.backend.domain.model.BadKeywordException;
import com.axreng.backend.domain.model.KeywordNotCrawledException;
import com.axreng.backend.domain.model.Search;
import com.axreng.backend.domain.model.dto.*;
import com.axreng.backend.infrastructure.CrawlingRepository;
import com.google.gson.Gson;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import spark.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

public class CrawlingService implements CrawlingHost {

    private final String BASE_URL;

    private Logger log = Log.getLogger(CrawlingService.class);

    private URL baseUrl;

    private Queue<Search> crawlingQueue;

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

    public void enqueueKeyword(CrawlingRequest crawlDto, Response resp) {
        if (crawlDto.getKeyword().length() < 4 || crawlDto.getKeyword().length() > 32) {
            throw new BadKeywordException();
        }

        SearchResponse newResponse = new SearchResponse();
        newResponse.setStatus(SearchStatus.ACTIVE);
        newResponse.setUrls(new ArrayList<>());

        newResponse = crawlingRepository.saveSearch(newResponse);

        Search search = new Search(newResponse.getId(), crawlDto.getKeyword());

        var enqueuedWithSuccess = crawlingQueue.add(search);

        if (enqueuedWithSuccess) {
            resp.status(200);
            resp.body(new Gson().toJson(new CrawlingResponse(search.getSearchId())).toString());
        } else {
            resp.status(500);
            resp.body(
                    new Gson()
                            .toJson(
                                    new ErrorResponse(
                                            "Error putting search on the queue. " +
                                                    "Try again in a few seconds."
                                    )
                            )
            );
        }
    }

    protected void crawlFromQueue() {
        if (crawlingQueue.size() > 0) {
            var crawlerSearch = crawlingQueue.remove();

            log.info("CrawlerSearch: ID = " + crawlerSearch.getSearchId());
            log.info("CrawlerSearch: Keyword = " + crawlerSearch.getKeyword());

            Pattern anchorPattern = getAnchorPattern();
            //Matcher matcher = anchorPattern.matcher();
        }
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

    protected int getQueueSize() {
        return this.crawlingQueue.size();
    }

    public SearchResponse findSearchById(final String id) {
        var result = this.crawlingRepository.findById(id);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new KeywordNotCrawledException();
        }
    }

    @Override
    public void accept(CrawlingVisitor visitor) {
        visitor.visit(this);
    }
}
