package com.axreng.backend;

import com.axreng.backend.domain.service.CrawlingScheduler;
import com.axreng.backend.view.CrawlingController;
import org.eclipse.jetty.util.log.Log;

import java.net.MalformedURLException;

public class Main {
    public static void main(String[] args) {
        final String BASE_URL = System.getenv("BASE_URL");
        var log = Log.getLogger(Main.class);
        if (BASE_URL.isEmpty()) {
            log.warn("Can't Crawl. Please insert Base Url (BASE_URL)!");
            System.exit(100);
        }

        log.info("Crawling on URL: " + BASE_URL);
        try {
            CrawlingScheduler scheduler = new CrawlingScheduler();
            CrawlingController crawlingController = new CrawlingController(BASE_URL);

            //IoC
            crawlingController.accept(scheduler);
        } catch (MalformedURLException urlEx) {
            log.warn("Couldn't parse BASE_URL. Shutting Down.");
            System.exit(100);
        }
    }
}
