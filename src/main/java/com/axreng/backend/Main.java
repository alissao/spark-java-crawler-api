package com.axreng.backend;

import com.axreng.backend.view.CrawlingController;
import org.eclipse.jetty.util.log.Log;

public class Main {
    public static void main(String[] args) {
        final String BASE_URL = System.getenv("BASE_URL");
        var log = Log.getLogger(Main.class);
        if (BASE_URL.isEmpty()) {
            log.warn("Can't Crawl. Please insert Base Url (BASE_URL)!");
            System.exit(100);
        }

        log.info("Crawling on URL: " + BASE_URL);
        CrawlingController crawlingController = new CrawlingController(BASE_URL);
    }
}
