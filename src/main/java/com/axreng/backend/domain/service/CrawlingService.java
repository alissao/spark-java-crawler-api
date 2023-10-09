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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlingService implements CrawlingHost {

    private final String BASE_URL;

    private Logger log = Log.getLogger(CrawlingService.class);

    private URL baseUrl;

    private Pattern dotHtmlPattern;

    private Queue<Search> crawlingQueue;

    private final CrawlingRepository crawlingRepository;

    public CrawlingService(final String baseUrl) throws MalformedURLException {
        this.BASE_URL = baseUrl;
        this.crawlingQueue = new LinkedList<>();
        this.crawlingRepository = new CrawlingRepository();
        this.dotHtmlPattern = Pattern
                .compile("<a\\s+[^>]*href=\"(.*?\\.html)\"[^>]*>([^<]*)</a>");
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
        newResponse.setUrls(new HashSet<>());

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

            final Queue<URL> urlsWithSameBase = new LinkedList<>();
            urlsWithSameBase.add(baseUrl); //starting point

            final Set<URL> visitedUrls = new HashSet<>();

            log.info("Initiating Thread based crawling for ID = " + crawlerSearch.getSearchId());

            final Pattern anchorPattern = getAnchorPattern();

            while (!urlsWithSameBase.isEmpty()) {
                crawlOnNextUrl(urlsWithSameBase, anchorPattern, visitedUrls, crawlerSearch);
            }

            SearchResponse result = crawlingRepository.findById(crawlerSearch.getSearchId()).get();
            result.setStatus(SearchStatus.FINISHED);
            crawlingRepository.saveSearch(result);

        }
    }

    private void crawlOnNextUrl(Queue<URL> urlsWithSameBase, final Pattern anchorPattern, Set<URL> visitedUrls, Search crawlerSearch) {
        final URL urlToCrawl = urlsWithSameBase.remove();
        if (!visitedUrls.contains(urlToCrawl)) {
            StringBuilder rawHTMLsb = new StringBuilder();

            var searchResult = crawlingRepository.findById(crawlerSearch.getSearchId()).get();
            final Set<String> resultList = searchResult.getUrls();

            try {
                log.info("Getting HTML from URL " + urlToCrawl);
                BufferedReader in = new BufferedReader(new InputStreamReader(urlToCrawl.openStream()));
                String inputLine = in.readLine();

                while(inputLine  != null){
                    rawHTMLsb.append(inputLine);

                    inputLine = in.readLine();
                }
                in.close();

                visitedUrls.add(urlToCrawl);

                String rawHtml = rawHTMLsb.toString();

                log.info("Searching for Keyword = " + crawlerSearch.getKeyword() + ". In URL: " + urlToCrawl);
                Pattern keywordPattern = Pattern.compile("(?i)" + crawlerSearch.getKeyword());
                Matcher keywordMatcher = keywordPattern.matcher(rawHtml);
                if (keywordMatcher.find()) {
                    log.info("Found keyword in URL: " + urlToCrawl + "Saving URL.");
                    resultList.add(urlToCrawl.toString());
                    crawlingRepository.saveSearch(searchResult);
                }

                Matcher matcher = anchorPattern.matcher(rawHtml);

                processAllRegexMatches(matcher, urlsWithSameBase, visitedUrls);

                matcher = dotHtmlPattern.matcher(rawHtml);

                processAllRegexMatches(matcher, urlsWithSameBase, visitedUrls);

            } catch(Exception ex) {
                log.warn("Error searching URL: " + urlToCrawl.toString());
            } finally {
                log.info("Finished crawling on URL: " + urlToCrawl);
            }
        }
    }

    private void processAllRegexMatches(Matcher matcher, Queue<URL> urlsWithSameBase, Set<URL> visitedUrls) throws MalformedURLException {
        log.info("Initiating Regex Search for anchor elements.");
        while (matcher.find()) {
            String anchorEl = matcher.group();

            log.info("Anchor element found: " + anchorEl);

            String extractedUrl = anchorEl.split("href=\"")[1];
            int indexOfClosingQuote = extractedUrl.indexOf("\"");
            extractedUrl = extractedUrl.substring(0, indexOfClosingQuote);

            log.info("Extracted URL from Anchor: " + extractedUrl);

            if (!extractedUrl.contains("..")) {
                if (extractedUrl.endsWith(".html")) {
                    extractedUrl = BASE_URL + extractedUrl;
                }

                URL newUrl = new URL(extractedUrl);

                if (newUrl.getHost().equals(baseUrl.getHost())
                        && !visitedUrls.contains(newUrl)) {
                    urlsWithSameBase.add(newUrl);
                }
            }

        }
    }

    private Pattern getAnchorPattern() {
        StringBuilder anchorPattern = new StringBuilder("<a\\s+[^>]*?href=\\\"(?:http:\\/\\/|https:\\/\\/)?")
                .append(getRegexReadyBaseUrl()) //Model for the regex: "www\.google\.com\/"
                .append("(.\\/|\\??).*?\\\".*?>.*?<\\/a>");
        log.info("Anchor Pattern: " + anchorPattern);
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
