package com.axreng.backend.view;

import com.axreng.backend.domain.model.BadKeywordException;
import com.axreng.backend.domain.model.KeywordNotCrawledException;
import com.axreng.backend.domain.model.dto.CrawlingRequest;
import com.axreng.backend.domain.model.dto.ErrorResponse;
import com.axreng.backend.domain.service.CrawlingService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import static spark.Spark.*;
import static spark.Spark.exception;

public class CrawlingController {

    private String BASE_URL;

    private CrawlingService crawlingService;

    public CrawlingController(String baseURL) {
        this.BASE_URL = baseURL;
        this.crawlingService = new CrawlingService(BASE_URL);
        initiateRoutes();
    }

    private void initiateRoutes() {
        crawlingRoutes();
        exceptionHandlers();
    }

    private void crawlingRoutes() {
        get("/crawl/:id", (req, res) ->
                "GET /crawl/" + req.params("id")
        );

        post("/crawl", (req, res) -> {
            CrawlingRequest crawlReq = new Gson().fromJson(req.body(), CrawlingRequest.class);
            crawlingService.crawlForKeyword(crawlReq, res);
            return res;
        });
    }

    private void exceptionHandlers() {
        exception(JsonSyntaxException.class, (ex, req, res) -> {
            ErrorResponse errResp = new ErrorResponse("Invalid Request Body.");
            res.type("application/json");
            res.status(400);
            res.body(new Gson().toJsonTree(errResp).toString());
        });

        exception(KeywordNotCrawledException.class, (ex, req, res) -> {
            ErrorResponse errResp = new ErrorResponse("Keyword wasn't searched yet.");
            res.type("application/json");
            res.status(404);
            res.body(new Gson().toJsonTree(errResp).toString());
        });

        exception(BadKeywordException.class, (ex, req, res) -> {
            ErrorResponse errResp = new ErrorResponse("Keyword out of possible size.");
            res.type("application/json");
            res.status(400);
            res.body(new Gson().toJsonTree(errResp).toString());
        });
    }

}
