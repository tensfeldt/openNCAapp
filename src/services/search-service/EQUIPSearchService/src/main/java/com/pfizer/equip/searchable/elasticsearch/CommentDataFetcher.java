package com.pfizer.equip.searchable.elasticsearch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.equip.searchable.dto.CommentData;
import com.pfizer.equip.searchable.dto.CommentDataSource;
import com.pfizer.equip.searchable.dto.InstantDeserializer;

/**
 * Retrieves equip:comment nodes from elasticsearch index.
 * 
 * @author HeinemanWP
 *
 */
public class CommentDataFetcher {
	private static String INDEX = "escommentndx-nca";
	private static String TYPE = "nca";
	private static String ID_QUERY = "{ \"_source\":[\"_id\"], \"from\" : %d, \"size\" : 1 }";
	// private static String ID_REGEX = "^(.*\"_id\".*:.*\")([a-f 0-9 -]*)(\".*)";
	private static String ID_REGEX = "([a-f\\d]{8}(-[a-f\\d]{4}){3}-[a-f\\d]{12}?)";	
	private static Pattern ID_PATTERN = Pattern.compile(ID_REGEX,	Pattern.CASE_INSENSITIVE);
	private static String PATH_QUERY = "{ \"_source\":[\"_id\",\"equip:*\",\"jcr:*\"], \"query\" : {\"match_phrase\":{\"jcr:path\" : \"%s/equip:comment\"}}, \"from\" : %d, \"size\" : %d }";
	private static String COUNT_REGEX = "^(.*\"hits\":.*\\{\"total\":)([0-9]*)(.*)";
	private static Pattern COUNT_PATTERN = Pattern.compile(COUNT_REGEX, Pattern.CASE_INSENSITIVE);
	private static String SOURCE_PATTERN = "equip:*,jcr:*";

	private ElasticSearchClient esc;

	public CommentDataFetcher(ElasticSearchClient esc) {
		this.esc = esc;
	}
	
	public int getCount() throws ElasticSearchClientException {
		int returnValue = 0;
		String result = esc.searchIndex(INDEX, TYPE, String.format(ID_QUERY, 0));
		Matcher m = COUNT_PATTERN.matcher(result);
		if (m.matches()) {
			returnValue = Integer.parseInt(m.group(2));
		}
		return returnValue;
	}
	
	public String getId(int offset) throws ElasticSearchClientException {
		return getId(esc.searchIndex(INDEX, TYPE, String.format(ID_QUERY, offset)));
	}
	
	private String getId(String nodeData) {
		Matcher m = ID_PATTERN.matcher(nodeData);
		String returnValue = null;
		if (m.matches()) {
			returnValue = m.group();
			if (returnValue.startsWith("\"")) {
				returnValue = returnValue.substring(1);
			}
		}
		return returnValue;
	}

	
	public CommentData getData(String id) throws ElasticSearchClientException {
		String json = getDataJson(id);
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		CommentData returnValue = gson.fromJson(json, CommentData.class);
		returnValue.setIndexKey(id);
		return returnValue;
	}
	
	private String getDataJson(String id) throws ElasticSearchClientException {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("_source", SOURCE_PATTERN);
		String json = esc.getIndexValue(INDEX, TYPE, id, parameters);
		if ((json != null) && !json.isEmpty()) {
			int start = json.indexOf("\"_source\":");
			json = json.substring(start + "\"_source\":".length(), json.lastIndexOf('}'));
		}
		return json;
	}

	public List<CommentData> getDataByPath(String path, int offset, int count) throws ElasticSearchClientException {
		String json = getDataByPathJson(path, offset, count);
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		CommentDataSource[] sources = gson.fromJson(json, CommentDataSource[].class);
		List<CommentData> returnValue = new ArrayList<>();
		for (CommentDataSource source : sources) {
			returnValue.add(source.getCommentData());
		}
		return returnValue;
	}
	
	private String getDataByPathJson(String path, int offset, int count) throws ElasticSearchClientException {
		String json = esc.searchIndex(INDEX, TYPE, String.format(PATH_QUERY, path, offset, count));
		if ((json != null) && !json.isEmpty()) {
			int start = json.indexOf("\"hits\":[");
			int end = json.lastIndexOf(']');
			json = json.substring(start + "\"hits\":[".length() - 1, end + 1);
		}
		return json;
	}
	
}
