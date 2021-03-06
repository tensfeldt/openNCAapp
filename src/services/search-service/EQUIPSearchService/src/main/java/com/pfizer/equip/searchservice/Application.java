package com.pfizer.equip.searchservice;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.equip.searchservice.exception.SearchException;
import com.pfizer.equip.searchservice.resource.ResourceCommon;
import com.pfizer.equip.searchservice.resource.SearchResource;
import com.pfizer.equip.searchservice.util.StackTraceDump;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationServiceClient;

import spark.Spark;
import spark.servlet.SparkApplication;

/**
 * Main class for SparkJava servlet
 * 
 * @author HeinemanWP
 *
 */
public class Application implements SparkApplication {
	private static Logger log = LoggerFactory.getLogger(Application.class);	
	private static Properties appProperties = new Properties();
	private static final String APPLICATION_PROPERTIES_FILE = "/app/3rdparty/equip/EquipSearchService/application.properties";
	private static final String SEARCH_VERSION = "/version";
	private static final String SEARCH_METADATA = "/searchMetaData";
	private static final String SEARCH_COMMENTS = "/searchComments";
	private static final String SEARCH_FILEDATA = "/searchFileData";
	private static final String SEARCH_FILETEXT = "/searchFileText";
	private static final String SEARCH_UNIFIED = "/searchUnified";
	private static final String SEARCH_RESULTS = "/:id/results";
	private static final String SEARCH_LINEAGE = "/searchLineage/:studyId";
	
	@Override
	public void init() {
		// This (staticFiles.location(..)) must be called before anything else in init.
		// The external location provided is in the WebContent folder.
		// Try {serviceroot}/public/README.txt from the browser.
		//allow static files so we can integrate swagger
		staticFiles.location("/static");

		// Exception handling
		Spark.exception(SearchException.class, (exception, request, response) -> {
		    // Handle the exception here
			log.error("", exception);
		    response.status(500);
		    try {
				response.body(StackTraceDump.dump(exception));
			} catch (IOException ex) {
				log.error("", ex);
			}
		});

		// Echo any OPTIONS requests
		Spark.options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}

			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}

			return "OK";
		});

		// Enable Cross Origin Resource Sharing (CORS)
		Spark.before((request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
			response.header("Access-Control-Allow-Methods", "*");
			response.header("Access-Control-Allow-Headers", "*");
		});

		// Load application properties
		try (FileReader fr = new FileReader(APPLICATION_PROPERTIES_FILE)) {
			appProperties.load(fr);
			log.info(String.format("ElasticSearch.server: %s", Application.getAppProperties().getProperty(
				AppPropertyNames.ELASTICSEARCH_SERVER)));
		} catch (IOException ex) {
			log.error(String.format("Failed to load application properties file %s", 
					APPLICATION_PROPERTIES_FILE), ex);
		}

		initializeSearchIndices();
		initSearchService();
	}

	private void initializeSearchIndices() {
		if (ResourceCommon.UPDATE_INDEXES) {
			try {
				ElasticSearchClient esc = new ElasticSearchClient(
						ResourceCommon.ELASTICSEARCH_SERVER,
						ResourceCommon.ELASTICSEARCH_USERNAME,
						ResourceCommon.ELASTICSEARCH_PASSWORD);
				esc.closeIndex("contentndx-nca");
				esc.setIndexSettings("contentndx-nca", "{ \"index.mapping.ignore_malformed\": true }");
				esc.openIndex("contentndx-nca");
				esc.setIndexSettings("contentndx-nca", "{ \"index.mapping.total_fields.limit\": 10000 }");
				esc.setIndexSettings("searchesndx-nca", "{ \"index.mapping.total_fields.limit\": 10000 }");
				log.info("search indices initialized");
			} catch (Exception ex) {
				log.error("Failed to initialize search indices", ex);
			}
		}
	}

	/**
	 * Initializes all URIs for the Search service.
	 */
	private void initSearchService() {
		path("/", () -> {
			before("/*", (request, response) -> log
					.info(String.format("Received api call: %s from %s", request.pathInfo(), request.ip())));
			after("/*", (request, response) -> {
				String responseBody =  response.body();
				if (responseBody == null) {
					responseBody = "";
				} else if (responseBody.length() > 64) {
					responseBody = responseBody.substring(0, 64) + "...";
				}
				log.info(String.format("Returned: %s %s", response.status(), responseBody));
				});

			//redirect base uri to swagger
			Spark.redirect.get("/", "/EQUIPSearchService/SearchSwagger/index.html");

			get(SEARCH_VERSION, SearchResource.getVersion);
			post(SEARCH_METADATA, SearchResource.postSearchMetaData);
			post(SEARCH_COMMENTS, SearchResource.postSearchComments);
			post(SEARCH_FILEDATA, SearchResource.postSearchFileData);
			post(SEARCH_FILETEXT, SearchResource.postSearchFileText);
			post(SEARCH_UNIFIED, SearchResource.postSearchUnified);
			get(SEARCH_RESULTS, SearchResource.getSearchResults);
			get(SEARCH_LINEAGE, SearchResource.getSearchLineage);
		});	
	}
	
	public static Properties getAppProperties() {
		return appProperties;
	}

}
