package it.lockless.psidemoserver.cron;

import it.lockless.psidemoserver.service.BloomFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BloomFilterCron {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterCron.class);

    @Value("${bloomfilter.enabled}")
    private boolean bloomFilterEnabled;

    private final BloomFilterService bloomFilterService;

    public BloomFilterCron(BloomFilterService bloomFilterService) {
        this.bloomFilterService = bloomFilterService;
    }

    // The Bloom Filter is periodically computed
    @Scheduled(fixedRateString = "${bloomfilter.interval.mseconds:300000}", initialDelayString = "${bloomfilter.interval.mseconds:300000}")
    public void updateBloomFilter(){
        if(bloomFilterEnabled) {
            log.debug("Called updateBloomFilter()");
            bloomFilterService.computeAndSaveSerializedBloomFilter();
        }
    }
}
