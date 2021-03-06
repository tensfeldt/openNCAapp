package com.pfizer.equip.searchservice.search;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.equip.searchservice.resource.ResourceCommon;
import com.pfizer.equip.service.client.ServiceCallerException;

/**
 * Manages thread that handles searches.
 * 
 * @author HeinemanWP
 *
 */
public class Searches {
	private static Logger log = LoggerFactory.getLogger(Searches.class);	
	private static final int EXPIRY_SLEEP_TIME = 5000;
	private final Map<String, BaseSearch> searchesMap = new ConcurrentHashMap<>();
	private ExecutorService expiryService;
	private boolean running = false;
	
	public Searches() {
		expiryService = Executors.newCachedThreadPool();
		start();
	}
	
	public void start() {
		if (!running) {
			expiryService.execute(() -> {
				while (true) {
					try {
						process();
					} catch (InterruptedException e) {
						// Do nothing.
					}
				}
			});
			running = true;
		}
	}
	

	
	public void stop() {
		if (!expiryService.isShutdown()) {
			expiryService.shutdown();
		}
		running = false;
	}

	
	private void process() throws InterruptedException {
		List<String> toRemove = new ArrayList<>();
		ElasticSearchClient esc;
		try {
			esc = new ElasticSearchClient(
					ResourceCommon.ELASTICSEARCH_SERVER,
					ResourceCommon.ELASTICSEARCH_USERNAME,
					ResourceCommon.ELASTICSEARCH_PASSWORD);
		} catch (ServiceCallerException ex) {
			log.error("", ex);
			return;
		}
		try {
			Map<String, Instant> searches = Search.retrieveAllSearches(esc);
			Instant now = Instant.now();
			for (Map.Entry<String, Instant> entry : searches.entrySet()) {
				if (now.isAfter(entry.getValue())) {
					toRemove.add(entry.getKey());
				}
			}
		} catch (ElasticSearchClientException | ParseException ex) {
			log.error("", ex);
		}
		for (String searchId: toRemove) {
			try {
				Search.deleteSearch(esc, searchId);
			} catch (ElasticSearchClientException ex) {
				// log.error("", ex);	// Ignoring
			}
		}
		Thread.sleep(EXPIRY_SLEEP_TIME);
	}
	
}
