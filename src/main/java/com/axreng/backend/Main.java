package com.axreng.backend;

import org.eclipse.jetty.util.log.Log;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        final String BASE_URL = System.getenv("BASE_URL");
        var log = Log.getLogger(Main.class);
        if (BASE_URL.isEmpty()) {
            log.warn("Can't Crawl. Please insert Base Url (BASE_URL)!");
            System.exit(100);
        }

        log.info("Crawling on URL: " + BASE_URL);

        get("/crawl/:id", (req, res) ->
                "GET /crawl/" + req.params("id"));
        post("/crawl", (req, res) -> {
            return "POST /crawl" + System.lineSeparator() + req.body();
        });
    }
}
