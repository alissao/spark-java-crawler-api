package com.axreng.backend.view;

import com.axreng.backend.domain.model.BadKeywordException;
import com.axreng.backend.domain.model.KeywordNotCrawledException;
import com.axreng.backend.domain.model.dto.CrawlingRequest;
import com.axreng.backend.domain.model.dto.ErrorResponse;
import com.axreng.backend.domain.service.CrawlingHost;
import com.axreng.backend.domain.service.CrawlingService;
import com.axreng.backend.domain.service.CrawlingVisitor;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.MalformedURLException;

import static spark.Spark.*;
import static spark.Spark.exception;

public class CrawlingController implements CrawlingHost {

    private String BASE_URL;

    private CrawlingService crawlingService;

    public CrawlingController(String baseURL) throws MalformedURLException {
        this.BASE_URL = baseURL;
        this.crawlingService = new CrawlingService(BASE_URL);
        initiateRoutes();
    }

    private void initiateRoutes() {
        crawlingRoutes();
        exceptionHandlers();
    }

    private void crawlingRoutes() {
        get("/crawl/:id", (req, res) -> {
            res.type("application/json");
            res.status(200);
            res
                .body(
                    new Gson()
                            .toJsonTree(crawlingService.findSearchById(req.params("id"))
                            ).toString()
                );
            return res.body();
        });

        post("/crawl", (req, res) -> {
            CrawlingRequest crawlReq = new Gson().fromJson(req.body(), CrawlingRequest.class);
            crawlingService.enqueueKeyword(crawlReq, res);
            res.type("application/json");
            return res.body();
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

    @Override
    public void accept(CrawlingVisitor visitor) {
        visitor.visit(this.crawlingService);
    }
}
