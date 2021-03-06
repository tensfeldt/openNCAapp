package com.pfizer.equip.searchservice.search;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.elasticsearch.dto.Query;
import com.pfizer.equip.searchable.dto.InstantDeserializer;
import com.pfizer.equip.searchable.dto.InstantSerializer;
import com.pfizer.equip.searchable.dto.SearchableData;
import com.pfizer.equip.searchservice.dto.CommentsSearchResult;
import com.pfizer.equip.searchservice.dto.FileDataSearchResult;
import com.pfizer.equip.searchservice.dto.MetaDataSearchResult;
import com.pfizer.equip.searchservice.dto.SearchResponse;
import com.pfizer.equip.searchservice.dto.SearchResults;
import com.pfizer.equip.searchservice.exception.SearchException;
import com.pfizer.equip.searchservice.indexer.IndexerException;
import com.pfizer.equip.searchservice.resource.ResourceCommon;
import com.pfizer.equip.searchservice.resource.SearchLineageRoute;
import com.pfizer.equip.searchservice.resource.SearchResource;
import com.pfizer.equip.service.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationRequestBody;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationResponseBody;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationServiceClient;

/**
 * Encapsulates data for a search against elasticsearch.
 * 
 * @author HeinemanWP
 *
 */
public class Search extends BaseSearch {
	private static Logger log = LoggerFactory.getLogger(Search.class);
	private static final String SEARCHES_NDX = "searchesndx-nca";
	public static final String TYPE = "nca";
	private static final String CONTENT_NDX = "contentndx-nca";
	private static final String FILEDATA_NDX = "filedatandx-nca";
	private static final String COMMENTS_NDX = "escommentndx-nca";
	private static final String SEARCHABLE_NDX = "esvalndx-nca";
	public static Searches searches = new Searches();
	private int count;
	private String server;
	private String index;
	private String type;
	// private Query query;
	String zResults;
	AuthorizationServiceClient authorizationServiceClient;

	public Search(AuthorizationServiceClient authorizationServiceClient) {
		super();
		this.authorizationServiceClient = authorizationServiceClient;
	}

	public static Searches getSearches() {
		return searches;
	}

	public static void setSearches(Searches searches) {
		Search.searches = searches;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	// public Query getQuery() {
	// return query;
	// }
	//
	// public void setQuery(Query query) {
	// this.query = query;
	// }

	// public SearchResponse initiateSearchOriginal(
	// String server,
	// String username,
	// String password,
	// String index,
	// String type,
	// Query query) throws SearchException {
	// this.server = server;
	// this.index = index;
	// this.query = query;
	// SearchResponse returnValue = new SearchResponse();
	// query.setFrom(0);
	// query.setSize(0);
	// try {
	// ElasticSearchClient esc = new ElasticSearchClient(server, username,
	// password);
	// String result = esc.searchIndex(index, type, query.toJson());
	// searchId = UUID.randomUUID().toString();
	// expires = computeExpirationTime();
	// count = getCount(result);
	// searches.addSearch(searchId, this);
	// returnValue.setSearchId(searchId);
	// returnValue.setExpires(expires.toString());
	// returnValue.setCount(count);
	// returnValue.setQuery(query);
	// returnValue.setIndex(index);
	// return returnValue;
	// } catch (ServiceCallerException | ElasticSearchClientException e) {
	// throw new SearchException(e);
	// }
	// }

	public SearchResponse initiateSearch(String searchUser, String server, String username, String password,
			String index, String type, Query query) throws SearchException {
		this.server = server;
		this.index = index;
		SearchResponse returnValue = new SearchResponse();
		query.setFrom(0);
		query.setSize(1000);
		try {
			ElasticSearchClient esc = new ElasticSearchClient(server, username, password);
			esc.refreshIndex(index);
			String result = esc.searchIndex(index, type, query.toJson());
			count = processResults(searchUser, result);
			searchId = UUID.randomUUID().toString();
			expires = computeExpirationTime();
			returnValue.setSearchId(searchId);
			returnValue.setExpires(expires.toString());
			returnValue.setCount(count);
			returnValue.setQuery(query);
			returnValue.setIndex(index);
			storeSearch(esc, searchId, this);
			return returnValue;
		} catch (ServiceCallerException | ElasticSearchClientException | IOException
				| com.pfizer.pgrd.equip.services.client.ServiceCallerException | IndexerException | InterruptedException e) {
			throw new SearchException(e);
		}
	}

	private int processResults(String searchUser, String searchResultsJson)
			throws IOException, com.pfizer.pgrd.equip.services.client.ServiceCallerException, ServiceCallerException,
			ElasticSearchClientException, IndexerException, InterruptedException {
		Map<String, SearchableData> resultsMap = new LinkedHashMap<>();
		int totalCount = getCount(searchResultsJson);
		List<Object> searchResults = marshallResults(searchResultsJson);
		for (Object o : searchResults) {
			SearchableData sd = (SearchableData) o;
			String indexKey = sd.getUniqueId();
			if ((resultsMap.containsKey(indexKey)) && (!shouldReplace(searchUser, sd, resultsMap.get(indexKey)))) {
				continue;
			}
			resultsMap.put(indexKey, sd);
		}
		List<SearchableData> resultsList = filterByUserAccess(searchUser, new ArrayList<>(resultsMap.values()));
		zResults = zipResults(resultsList);
		return totalCount == resultsList.size() ? totalCount : resultsList.size();
	}

	private List<SearchableData> filterByUserAccess(String searchUser, final List<SearchableData> resultsList)
			throws com.pfizer.pgrd.equip.services.client.ServiceCallerException, ServiceCallerException,
			ElasticSearchClientException, IndexerException, IOException, InterruptedException {
		// filter out nodes that are uncommitted and not created by the user.
		List<SearchableData> theResultsList = new ArrayList<>(resultsList);
		
		if (authorizationServiceClient == null) {
			return theResultsList;
		}
		List<SearchableData> resultsWithEquipIdsList = theResultsList.stream()
				.filter(rs -> rs.getEquipEquipId() != null)
				.collect(Collectors.toList());
		Map<String, SearchableData> equipIdToSearchableData = resultsWithEquipIdsList.stream()
				.collect(Collectors.toMap(SearchableData::getEquipEquipId, Function.identity()));
		// Find any reporting events and extract the dataframes from their items
		// to check if authorized.
		List<SearchableData> reportingEvents = theResultsList.stream()
				.filter(rs -> (rs.getEquipAssemblyType() != null) && rs.getEquipAssemblyType().equalsIgnoreCase("Reporting Event"))
				.collect(Collectors.toList());
		theResultsList.removeAll(reportingEvents);
		if (!reportingEvents.isEmpty()) {
			extractDataframesFromReportingEventsForAuth(searchUser, theResultsList, equipIdToSearchableData, reportingEvents);
		}
		// Find any regular assemblies and extract the dataframes to check
		// if authorized.
		List<SearchableData> assemblies = theResultsList.stream()
				.filter(rs -> rs.getEquipAssemblyType() != null)
				.collect(Collectors.toList());
		theResultsList.removeAll(assemblies);
		if (!assemblies.isEmpty()) {
			extractDataframesFromAssembliesForAuth(searchUser, theResultsList, equipIdToSearchableData, assemblies);
		}
		
		List<AuthorizationRequestBody> requests = new ArrayList<>();
		List<SearchableData> libraryResults = new ArrayList<>();
		for (SearchableData result : theResultsList) {
			if (result.getJcrPrimaryType().toLowerCase().startsWith("equiplibrary:")) {
				libraryResults.add(result);
				continue;
			}
			AuthorizationRequestBody arb = new AuthorizationRequestBody();
			arb.setDataframeId(result.getJcrUuid());
			arb.setDataframeType(result.getEquipDataframeType() != null ? result.getEquipDataframeType() : "");
			arb.setDataBlindingStatus(
					result.getEquipDataBlindingStatus() != null ? result.getEquipDataBlindingStatus() : "");
			arb.setPromotionStatus(result.getEquipPromotionStatus() != null ? result.getEquipPromotionStatus() : "");
			arb.setRestrictionStatus(
					result.getEquipRestrictionStatus() != null ? result.getEquipRestrictionStatus() : "");
			arb.setstudyIds(result.getEquipStudyId() != null ? Arrays.asList(result.getEquipStudyId())
					: new ArrayList<String>());
			requests.add(arb);
		}
		// Remove the library results from the other results
		theResultsList.removeAll(libraryResults);
		
		List<SearchableData> returnValue = new ArrayList<>();
		if (!requests.isEmpty()) {
			String json = new Gson().toJson(requests);
			AuthorizationResponseBody authResponseBody = authorizationServiceClient
					.getMultipleDataframePrivileges(searchUser, json);
			Map<String, Boolean> permissions = authResponseBody.getPermissionsInfo();
			if (permissions == null) {
				log.info("Permissions are null - permissions: " + permissions);
				return theResultsList;
			}
			returnValue = theResultsList.stream().filter(r -> permissions.get(r.getJcrUuid()))
					.collect(Collectors.toList());
		} else {
			returnValue.addAll(theResultsList);
		}
		// Add any screened Assemblies
		if (!assemblies.isEmpty()) {
			returnValue.addAll(assemblies);
		}
		
		// Add any screened Reporting Events
		if (!reportingEvents.isEmpty()) {
			returnValue.addAll(reportingEvents);
		}
		
		// Add back the library items
		returnValue.addAll(libraryResults);
		return returnValue;
	}

	private void extractDataframesFromAssembliesForAuth(String searchUser, List<SearchableData> theResultsList,
			Map<String, SearchableData> equipIdToSearchableData, List<SearchableData> assemblies) 
		throws ServiceCallerException, ElasticSearchClientException, IndexerException, IOException, 
		InterruptedException, com.pfizer.pgrd.equip.services.client.ServiceCallerException {
		Map<String, String[]> queryParams = new HashMap<>();
		List<String> asEquipIds = assemblies.stream().map(SearchableData::getEquipEquipId)
				.collect(Collectors.toList());
		queryParams.put("equipId", asEquipIds.toArray(new String[asEquipIds.size()]));
		String json = searchLineageFor(queryParams);
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(json);
		JsonArray ja = jelem.getAsJsonArray();
		Map<String, String> dataframeIdsMap = new HashMap<>();
		Map<String, List<String>> dataframeIdToAssemblyEquipId = new HashMap<>();
		// Iterate through the reporting events and pull out
		// the reporting event item dataframe ids.
		Iterator<JsonElement> assembliesIterator = ja.iterator();
		while (assembliesIterator.hasNext()) {
			JsonElement arrayElem = assembliesIterator.next();
			JsonObject reJobject = arrayElem.getAsJsonObject();
			String assemblyEquipId = reJobject.get("equipId").getAsString();
			JsonArray dfArray = reJobject.getAsJsonArray("dataframeIds");
			if (dfArray != null) {
				Iterator<JsonElement> dfIterator = dfArray.iterator();
				while (dfIterator.hasNext()) {
					JsonElement dfiElem = dfIterator.next();
					String dataframeId = dfiElem.getAsString();
					dataframeIdsMap.put(dataframeId, null);
					if (!dataframeIdToAssemblyEquipId.containsKey(dataframeId)) {
						dataframeIdToAssemblyEquipId.put(dataframeId, new ArrayList<>());
					}
					dataframeIdToAssemblyEquipId.get(dataframeId).add(assemblyEquipId);
				}
			}
		}
		// Get the list of unique dataframe ids.
		List<String> dataframeIds = new ArrayList<>(dataframeIdsMap.keySet());
		List<AuthorizationRequestBody> requests = new ArrayList<>();
		if (!dataframeIds.isEmpty()) {
			getDataframeDetailsForAuth(queryParams, jp, dataframeIds, requests);
			json = new Gson().toJson(requests);
			AuthorizationResponseBody authResponseBody = authorizationServiceClient
					.getMultipleDataframePrivileges(searchUser, json);
			Map<String, Boolean> permissions = authResponseBody.getPermissionsInfo();
			if (permissions != null) {
				final List<String> forbiddenDataframeIds = dataframeIds.stream()
						.filter(dfId -> permissions.containsKey(dfId) && !permissions.get(dfId))
						.collect(Collectors.toList());
				for (String dfId : forbiddenDataframeIds) {
					for (String assemblyEquipId : dataframeIdToAssemblyEquipId.get(dfId)) {
						assemblies.remove(equipIdToSearchableData.get(assemblyEquipId));
					}
				}
			}
		}
	}

	private void extractDataframesFromReportingEventsForAuth(String searchUser, List<SearchableData> theResultsList,
			Map<String, SearchableData> equipIdToSearchableData, List<SearchableData> reportingEvents)
			throws ServiceCallerException, ElasticSearchClientException, IndexerException, IOException,
			InterruptedException, com.pfizer.pgrd.equip.services.client.ServiceCallerException {
		// Remove the reporting Events from the original resultList.
		// Look up the Reporting Event Item contents and use those to
		// figure out what to submit to the Auth service.
		// Afterwards we'll merge with the other auth results.
		Map<String, String[]> queryParams = new HashMap<>();
		List<String> reEquipIds = reportingEvents.stream().map(SearchableData::getEquipEquipId)
				.collect(Collectors.toList());
		queryParams.put("equipId", reEquipIds.toArray(new String[reEquipIds.size()]));
		String json = searchLineageFor(queryParams);
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(json);
		JsonArray ja = jelem.getAsJsonArray();
		Map<String, String> dataframeIdsMap = new HashMap<>();
		Map<String, List<String>> dataframeIdToReportingEventEquipId = new HashMap<>();
		// Iterate through the reporting events and pull out
		// the reporting event item dataframe ids.
		Iterator<JsonElement> reportingEventsIterator = ja.iterator();
		while (reportingEventsIterator.hasNext()) {
			JsonElement arrayElem = reportingEventsIterator.next();
			JsonObject reJobject = arrayElem.getAsJsonObject();
			String reportingEventEquipId = reJobject.get("equipId").getAsString();
			JsonArray reiArray = reJobject.getAsJsonArray("reportingItems");
			if (reiArray != null) {
				Iterator<JsonElement> reiIterator = reiArray.iterator();
				while (reiIterator.hasNext()) {
					JsonElement reiElem = reiIterator.next();
					JsonObject reiJobject = reiElem.getAsJsonObject();
					String dataframeId = reiJobject.get("dataframeId").getAsString();
					dataframeIdsMap.put(dataframeId, null);
					if (!dataframeIdToReportingEventEquipId.containsKey(dataframeId)) {
						dataframeIdToReportingEventEquipId.put(dataframeId, new ArrayList<>());
					}
					dataframeIdToReportingEventEquipId.get(dataframeId).add(reportingEventEquipId);
				}
			}
		}
		// Get the list of unique dataframe ids.
		List<String> dataframeIds = new ArrayList<>(dataframeIdsMap.keySet());
		List<AuthorizationRequestBody> requests = new ArrayList<>();
		if (!dataframeIds.isEmpty()) {
			getDataframeDetailsForAuth(queryParams, jp, dataframeIds, requests);
			json = new Gson().toJson(requests);
			AuthorizationResponseBody authResponseBody = authorizationServiceClient
					.getMultipleDataframePrivileges(searchUser, json);
			Map<String, Boolean> permissions = authResponseBody.getPermissionsInfo();
			if (permissions != null) {
				final List<String> forbiddenDataframeIds = dataframeIds.stream()
						.filter(dfId -> permissions.containsKey(dfId) && !permissions.get(dfId))
						.collect(Collectors.toList());
				for (String dfId : forbiddenDataframeIds) {
					for (String reportingEventEquipId : dataframeIdToReportingEventEquipId.get(dfId)) {
						reportingEvents.remove(equipIdToSearchableData.get(reportingEventEquipId));
					}
				}
			}
		}
	}

	private void getDataframeDetailsForAuth(Map<String, String[]> queryParams, JsonParser jp, List<String> dataframeIds,
			List<AuthorizationRequestBody> requests) throws ServiceCallerException, ElasticSearchClientException,
			IndexerException, IOException, InterruptedException {
		String json;
		JsonElement jelem;
		JsonArray ja;
		// Fetch the dataframe details
		queryParams.clear();
		queryParams.put("id", dataframeIds.toArray(new String[dataframeIds.size()]));
		json = searchLineageFor(queryParams);
		jelem = jp.parse(json);
		ja = jelem.getAsJsonArray();
		Iterator<JsonElement> dataframesIterator = ja.iterator();
		while (dataframesIterator.hasNext()) {
			JsonElement arrayElem = dataframesIterator.next();
			JsonObject dfJobject = arrayElem.getAsJsonObject();
			String dataframeId = dfJobject.get("id").getAsString();
			String dataframeType = dfJobject.get("dataframeType").getAsString();
			String dataBlindingStatus = dfJobject.get("dataBlindingStatus").getAsString();
			String promotionStatus = dfJobject.get("promotionStatus").getAsString();
			String restrictionStatus = dfJobject.get("restrictionStatus").getAsString();
			JsonArray jaStudyIds = dfJobject.get("studyIds").getAsJsonArray();
			List<String> studyIds = new ArrayList<>();
			Iterator<JsonElement> studyIdsIterator = jaStudyIds.iterator();
			while (studyIdsIterator.hasNext()) {
				studyIds.add(studyIdsIterator.next().getAsString());
			}
			AuthorizationRequestBody arb = new AuthorizationRequestBody();
			arb.setDataframeId(dataframeId);
			arb.setDataframeType(dataframeType != null ? dataframeType : "");
			arb.setDataBlindingStatus(dataBlindingStatus != null ? dataBlindingStatus : "");
			arb.setPromotionStatus(promotionStatus != null ? promotionStatus : "");
			arb.setRestrictionStatus(restrictionStatus != null ? restrictionStatus : "");
			arb.setstudyIds(studyIds);
			requests.add(arb);
		}
	}

	private String searchLineageFor(Map<String, String[]> queryParams)
			throws ServiceCallerException, ElasticSearchClientException, IndexerException, IOException, InterruptedException {
		String[] deleteFlags = { "false" };
		queryParams.put("deleteFlag", deleteFlags);
		String[] versionSuperSeded = { "false" };
		queryParams.put("versionSuperSeded", versionSuperSeded);
		// Get Report Event details
		SearchLineageRoute slr = (SearchLineageRoute) SearchResource.getSearchLineage;
		return slr.extractLineageFromSearchResult(slr.searchLineages(ResourceCommon.ELASTICSEARCH_SERVER,
				ResourceCommon.ELASTICSEARCH_USERNAME, ResourceCommon.ELASTICSEARCH_PASSWORD, "all", queryParams));
	}

	private boolean shouldReplace(String searchUser, SearchableData newSd, SearchableData currentSd) {
		String newUser = (newSd.getEquipModifiedBy() != null) ? newSd.getEquipModifiedBy() : "";
		Long newVersionNumber = (newSd.getEquipVersionNumber() != null) ? newSd.getEquipVersionNumber() : -1L;
		Long currentVersionNumber = (currentSd.getEquipVersionNumber() != null) ? currentSd.getEquipVersionNumber()
				: -1L;
		Boolean newVersionCommitted = (newSd.getEquipVersionCommitted() != null) ? newSd.getEquipVersionCommitted()
				: false;
		Boolean currentVersionCommitted = (currentSd.getEquipVersionCommitted() != null)
				? currentSd.getEquipVersionCommitted()
				: false;
		Boolean newVersionSuperceded = (newSd.getEquipVersionSuperseded() != null) ? newSd.getEquipVersionSuperseded()
				: true;
		Boolean currentVersionSuperceded = (currentSd.getEquipVersionSuperseded() != null)
				? currentSd.getEquipVersionSuperseded()
				: true;
		if ((newUser.isEmpty() || newUser.equalsIgnoreCase(searchUser)) && newVersionNumber.equals(currentVersionNumber)
				&& (newVersionCommitted.equals(currentVersionCommitted))
				&& (newVersionSuperceded.equals(currentVersionSuperceded))) {
			if (((newSd.getEquipModified() != null) && (currentSd.getEquipModified() == null)) 
					|| ((newSd.getEquipModified() != null) && newSd.getEquipModified().isAfter(currentSd.getEquipModified()))) {
				return true;
			} else {
				return false;
			}
		}
		return (newUser.equalsIgnoreCase(searchUser) && (newVersionNumber > currentVersionNumber))
				|| (!newUser.equalsIgnoreCase(searchUser) && newVersionCommitted && !newVersionSuperceded);
	}

	public SearchResults searchResults(String searchUser, String server, String username, String password, String type,
			int offset, int count) throws SearchException {
		List<SearchableData> resultsList;
		try {
			resultsList = unzipResults(zResults, offset, count);
		} catch (IOException ex) {
			throw new SearchException(ex);
		}
		List<Object> searchResults = new ArrayList<>();
		for (SearchableData result : resultsList) {
			searchResults.add(result);
		}
		expires = computeExpirationTime();
		int resultCount = resultsList.size();
		SearchResults returnValue = new SearchResults();
		returnValue.setSearchId(searchId);
		returnValue.setExpires(expires.toString());
		returnValue.setCount(resultCount);
		returnValue.setResults(searchResults);
		return returnValue;
	}

	public void resetExpiration() {
		expires = computeExpirationTime();
	}

	private Date computeExpirationTime() {
		Date current = new Date();
		return new Date(current.getTime() + 5L * 60L * 1000L);
	}

	private int getCount(String result) {
		String s = "\"hits\":{\"total\":";
		int start = result.indexOf(s) + s.length();
		int stop = result.indexOf(',', start);
		String hitCount = result.substring(start, stop);
		return Integer.parseInt(hitCount);
	}

	private List<Object> marshallResults(String result) {
		switch (index) {
			case SEARCHABLE_NDX:
				return marshallMetaDataSearchResults(result);
			case COMMENTS_NDX:
				return marshallCommentsSearchResults(result);
			case FILEDATA_NDX:
				return marshallFileDataSearchResults(result);
			case CONTENT_NDX:
				return marshallFileTextSearchResults(result);
			default:
				break;
		}
		return new ArrayList<>();
	}

	private List<Object> marshallMetaDataSearchResults(String result) {
		List<Object> returnValue = new ArrayList<>();
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(result);
		JsonArray hits = jelem.getAsJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		for (int i = 0, n = hits.size(); i < n; i++) {
			JsonElement re = hits.get(i).getAsJsonObject().get("_source");
			MetaDataSearchResult mdsr = gson.fromJson(re, MetaDataSearchResult.class);
			returnValue.add(mdsr);
		}

		return returnValue;
	}

	private List<Object> marshallCommentsSearchResults(String result) {
		List<Object> returnValue = new ArrayList<>();
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(result);
		JsonArray hits = jelem.getAsJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		for (int i = 0, n = hits.size(); i < n; i++) {
			JsonElement re = hits.get(i).getAsJsonObject().get("_source");
			CommentsSearchResult mdsr = gson.fromJson(re, CommentsSearchResult.class);
			returnValue.add(mdsr);
		}

		return returnValue;
	}

	private List<Object> marshallFileDataSearchResults(String result) {
		List<Object> returnValue = new ArrayList<>();
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(result);
		JsonArray hits = jelem.getAsJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		for (int i = 0, n = hits.size(); i < n; i++) {
			JsonElement re = hits.get(i).getAsJsonObject().get("_source");
			FileDataSearchResult fdsr = gson.fromJson(re, FileDataSearchResult.class);
			returnValue.add(fdsr);
		}

		return returnValue;
	}

	private List<Object> marshallFileTextSearchResults(String result) {
		List<Object> returnValue = new ArrayList<>();
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(result);
		JsonArray hits = jelem.getAsJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		for (int i = 0, n = hits.size(); i < n; i++) {
			String fileId = hits.get(i).getAsJsonObject().get("_id").getAsString();
			JsonElement re = hits.get(i).getAsJsonObject().get("_source");
			SearchableData sr = gson.fromJson(re, SearchableData.class);
			sr.setFileId(fileId);
			returnValue.add(sr);
		}

		return returnValue;
	}

	public static void storeSearch(ElasticSearchClient esc, String searchId, Search search)
			throws ElasticSearchClientException {
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
		String json = gson.toJson(search);
		esc.putIndex(SEARCHES_NDX, TYPE, searchId, json);
	}

	public static Search retrieveSearch(ElasticSearchClient esc, String searchId) throws ElasticSearchClientException {
		Map<String, String> parameters = new HashMap<>();
		String json = esc.getIndexValue(SEARCHES_NDX, TYPE, searchId, parameters);
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(json);
		return gson.fromJson(jelem.getAsJsonObject().get("_source"), Search.class);
	}

	public static Search retrieveSearch(String elServer, String elUsername, String elPassword, String searchId)
			throws ElasticSearchClientException {
		ElasticSearchClient esc;
		try {
			esc = new ElasticSearchClient(elServer, elUsername, elPassword);
			return retrieveSearch(esc, searchId);
		} catch (ServiceCallerException ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public static Map<String, Instant> retrieveAllSearches(ElasticSearchClient esc)
			throws ElasticSearchClientException, ParseException {
		Map<String, Instant> returnValue = new HashMap<>();
		Map<String, String> parameters = new HashMap<>();
		parameters.put("_source", "searchId,expires");
		String json = esc.getIndex(SEARCHES_NDX, parameters);
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(json);
		JsonElement resultsElem = jelem.getAsJsonObject().get("hits");
		DateFormat df = DateFormat.getDateTimeInstance();
		JsonArray hits = resultsElem.getAsJsonObject().get("hits").getAsJsonArray();
		for (int i = 0, n = hits.size(); i < n; i++) {
			JsonObject sourceObj = hits.get(i).getAsJsonObject().get("_source").getAsJsonObject();
			String id = sourceObj.get("searchId").getAsString();
			String expires = sourceObj.get("expires").getAsString();
			returnValue.put(sourceObj.get("searchId").getAsString(),
					Instant.ofEpochMilli(df.parse(sourceObj.get("expires").getAsString()).getTime()));
		}
		return returnValue;
	}

	public static void updateSearch(ElasticSearchClient esc, Search search) throws ElasticSearchClientException {
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
		String json = gson.toJson(search);
		esc.updateIndex(SEARCHES_NDX, TYPE, search.getSearchId(), json);
	}

	public static void updateSearch(String elServer, String elUsername, String elPassword, Search search)
			throws ElasticSearchClientException {
		ElasticSearchClient esc;
		try {
			esc = new ElasticSearchClient(elServer, elUsername, elPassword);
			updateSearch(esc, search);
		} catch (ServiceCallerException ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public static void deleteSearch(ElasticSearchClient esc, String searchId) throws ElasticSearchClientException {
		esc.deleteIndex(SEARCHES_NDX, TYPE, searchId);
	}

	private static String zipResults(List<SearchableData> searchResults) throws IOException {
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
		String json = gson.toJson(searchResults);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
				zipOut.setMethod(ZipOutputStream.DEFLATED);
				zipOut.setLevel(4);
				ZipEntry zipEntry = new ZipEntry("results");
				zipOut.putNextEntry(zipEntry);
				zipOut.write(json.getBytes());
				zipOut.closeEntry();
			}
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
	}

	private List<SearchableData> unzipResults(String encodedZippedResults, int offset, int count) throws IOException {
		byte[] zippedResults = Base64.getDecoder().decode(encodedZippedResults);
		List<SearchableData> returnValue = new ArrayList<>();
		try (ByteArrayInputStream bais = new ByteArrayInputStream(zippedResults)) {
			try (ZipInputStream zipIn = new ZipInputStream(bais)) {
				ZipEntry ze = zipIn.getNextEntry();
				if (ze != null) {
					try {
						Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer())
								.create();
						byte[] buffer = new byte[2200000];
						int bytesRead = 0;
						int byteCount = 0;
						while (byteCount > -1) {
							byteCount = zipIn.read(buffer, bytesRead, buffer.length - bytesRead);
							if (byteCount > -1) {
								bytesRead += byteCount;
							}
						}
						byte[] jsonBuffer = new byte[bytesRead];
						for (int i = 0, n = bytesRead; i < n; i++) {
							jsonBuffer[i] = buffer[i];
						}
						String json = new String(jsonBuffer);
						Type listType = new TypeToken<ArrayList<SearchableData>>() {
						}.getType();
						returnValue = gson.fromJson(json, listType);
						returnValue = returnValue.stream().skip(offset).limit(count).collect(Collectors.toList());
					} finally {
						zipIn.closeEntry();
					}
				}
			}
		}
		return returnValue;
	}

}
