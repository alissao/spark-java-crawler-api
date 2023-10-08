package com.axreng.backend.domain.service;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CrawlingScheduler extends TimerTask implements CrawlingVisitor {

    private Logger log = Log.getLogger(CrawlingScheduler.class);

    private Timer timer;

    private boolean runningScheduler = false;

    private CrawlingService crawlingService;

    public CrawlingScheduler() {
        this.timer = new Timer();
    }

    @Override
    public void run() {
        if (runningScheduler == false) {
            log.info("Checking if there's Job to do. Queue size: "
                    + crawlingService.getQueueSize()
                    + ". Running another search? "
                    + runningScheduler
            );
        }
    }

    @Override
    public void visit(CrawlingHost crawlingHost) {
        //I wish i had IoC
        if (crawlingHost instanceof CrawlingService) {
            this.crawlingService = (CrawlingService) crawlingHost;
            log.info("IoC succesfull!");
            timer.schedule(this, 5, TimeUnit.SECONDS.toMillis(1));
        }
    }
}
