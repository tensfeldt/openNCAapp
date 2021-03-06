package com.pfizer.equip.searchservice.resource;

import java.util.List;

import com.google.gson.Gson;
import com.pfizer.elasticsearch.dto.Predicate;
import com.pfizer.elasticsearch.dto.Query;
import com.pfizer.equip.searchservice.Application;
import com.pfizer.equip.searchservice.dto.FileTextSearchRequest;
import com.pfizer.equip.searchservice.dto.SearchResponse;
import com.pfizer.equip.searchservice.exception.SearchException;
import com.pfizer.equip.searchservice.search.Search;
import com.pfizer.equip.searchservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Implements SparkJava Route for full text searches
 * 
 * @author HeinemanWP
 *
 */
public class FileTextSearchRoute implements Route {
	private AuthorizationServiceClient authorizationServiceClient;
	
	@Override
	public Object handle(Request request, Response response) throws Exception {
		if (authorizationServiceClient == null) {
			authorizationServiceClient = new AuthorizationServiceClient(
					ResourceCommon.AUTH_SERVER,
					Integer.parseInt(ResourceCommon.AUTH_PORT),
					ResourceCommon.AUTH_SYSTEM);
		}
		String userId = request.headers(ResourceCommon.IAMPFIZERUSERCN);
		if (userId == null) {
			Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
		}
		String idsOnlyString = request.queryParams("idsOnly");
		boolean idsOnly = false;
		if ((idsOnlyString != null) && !idsOnlyString.isEmpty()) {
			idsOnly = Boolean.parseBoolean(idsOnlyString);
		}
		FileTextSearchRequest ftsRequest = unmarshallSearchFileTextRequest(request);
		SearchResponse searchResponse = initiateFileTextSearch(userId, ftsRequest, idsOnly);
		response.header("Content-Type", "application/json");
		response.body((String) SearchResource.marshallSearchResponse(searchResponse));
		return response;
	}

	private FileTextSearchRequest unmarshallSearchFileTextRequest(Request request) {
		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		List<String> texts =  gson.fromJson(request.body(), List.class);
		FileTextSearchRequest ftsr = new FileTextSearchRequest();
		ftsr.setTexts(texts);
		return ftsr;
	}

	private SearchResponse initiateFileTextSearch(String userId, FileTextSearchRequest ftsRequest, boolean idsOnly) throws SearchException {
		String[] sourcesIncluded = {"jcr:uuid", "jcr:primaryType"};
		String[] sourcesExcluded = {};
		if (!idsOnly) {
			sourcesIncluded = ResourceCommon.FILETEXT_SEARCH_SOURCES_INCLUDE;
			sourcesExcluded = ResourceCommon.FILETEXT_SEARCH_SOURCES_EXCLUDE;
		}
		Predicate predicate = ftsRequest.toElasticSearch();
		Query query = new Query(sourcesIncluded, sourcesExcluded, predicate);
		Search search = new Search(authorizationServiceClient);
		return search.initiateSearch(
				userId,
				ResourceCommon.ELASTICSEARCH_SERVER,
				ResourceCommon.ELASTICSEARCH_USERNAME,
				ResourceCommon.ELASTICSEARCH_PASSWORD,
				ResourceCommon.FILETEXT_SEARCH_INDEX,
				ResourceCommon.SEARCH_TYPE,
				query);
	}
	
}
