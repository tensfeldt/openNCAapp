package com.pfizer.equip.searchservice.resource;

import java.time.Instant;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.equip.searchable.dto.InstantSerializer;
import com.pfizer.equip.searchservice.dto.SearchResults;
import com.pfizer.equip.searchservice.search.BaseSearch;
import com.pfizer.equip.searchservice.search.Search;
import com.pfizer.equip.searchservice.util.HTTPStatusCodes;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Implements SparkJava Route for search results
 * 
 * @author HeinemanWP
 *
 */
public class SearchResultsRoute implements Route {

	@Override
	public Object handle(Request request, Response response) throws Exception {
		String searchId = request.params(":id");
		String userId = request.headers(ResourceCommon.IAMPFIZERUSERCN);
		if (userId == null) {
			Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
		}
		// BaseSearch search = Search.searches.getSearch(searchId);
		Search search = null;
		search = Search.retrieveSearch(
				ResourceCommon.ELASTICSEARCH_SERVER,
				ResourceCommon.ELASTICSEARCH_USERNAME,
				ResourceCommon.ELASTICSEARCH_PASSWORD,
				searchId);
		if (search == null) {
			Spark.halt(HTTPStatusCodes.NOT_FOUND, String.format("Search %s not found.", searchId));
		}
		String offsetString = request.queryParams("offset");
		int offset = 0;
		if ((offsetString != null) && !offsetString.isEmpty()) {
			offset = Integer.parseInt(offsetString);
		}
		String countString = request.queryParams("count");
		int count = 100;
		if ((countString != null) && !countString.isEmpty()) {
			count = Integer.parseInt(countString);
		}
		
		Object returnValue = marshallSearchResults(
				search.searchResults(
					userId,
					ResourceCommon.ELASTICSEARCH_SERVER,
					ResourceCommon.ELASTICSEARCH_USERNAME,
					ResourceCommon.ELASTICSEARCH_PASSWORD,
					ResourceCommon.SEARCH_TYPE,
					offset,
					count));
		search.resetExpiration();
		Search.updateSearch(
				ResourceCommon.ELASTICSEARCH_SERVER,
				ResourceCommon.ELASTICSEARCH_USERNAME,
				ResourceCommon.ELASTICSEARCH_PASSWORD,
				search);
		response.header("Content-Type", "application/json");
		response.body((String) returnValue);
		return response;
	}

	private Object marshallSearchResults(SearchResults searchResults) {
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
		return gson.toJson(searchResults);
	}
}
