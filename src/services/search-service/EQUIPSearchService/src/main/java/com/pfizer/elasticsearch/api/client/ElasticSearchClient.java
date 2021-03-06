package com.pfizer.elasticsearch.api.client;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.pfizer.equip.service.client.ServiceCaller;
import com.pfizer.equip.service.client.ServiceCallerException;
import com.pfizer.equip.service.client.ServiceResponse;

/**
 * Client for Elasticsearch's RESTful API
 * 
 * @author HeinemanWP
 *
 */
public class ElasticSearchClient {
	private static Logger log = LoggerFactory.getLogger(ElasticSearchClient.class);	
	private static final String ACCEPT = "Accept";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String AUTHORIZATION = "Authorization";
	private static final String APPLICATION_JSON = "application/json;charset=UTF-8";
	private String baseUri;
	private String authorization;
	private ServiceCaller sc;

	public ElasticSearchClient(String server) throws ServiceCallerException {
		baseUri = server + "/";
		sc = new ServiceCaller();
	}

	public ElasticSearchClient(String server, String username, String password) throws ServiceCallerException {
		this(server);
		if ((username != null) && !username.isEmpty()) {
			authorization = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
		}
	}

	public String getIndex(String index, Map<String, String> parameters) throws ElasticSearchClientException {
		String uri = String.format("%s%s/_search", baseUri, index);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		try {
			ServiceResponse sr = sc.get(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}
	
	public String getIndexValue(String index, String type, String id, Map<String, String> parameters) throws ElasticSearchClientException {
		String uri = String.format("%s%s/%s/%s", baseUri, index, type, id);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		try {
			ServiceResponse sr = sc.get(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}
	
	public InputStream getIndexValueStream(String index, String type, String id, Map<String, String> parameters) throws ElasticSearchClientException {
		String uri = String.format("%s%s/%s/%s", baseUri, index, type, id);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		try {
			ServiceResponse sr = sc.get(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getInputStream();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}
	
	public String searchIndex(String index, String type, String query) throws ElasticSearchClientException {
		// log.debug(String.format("index: %s type: %s query: %s", index, type, query));
		String uri = baseUri + index + "/" + type + "/_search";
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		String postData = query;
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, postData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public InputStream searchIndexStream(String index, String type, String query) throws ElasticSearchClientException {
		// log.debug(String.format("index: %s type: %s query: %s", index, type, query));
		String uri = baseUri + index + "/" + type + "/_search";
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		String postData = query;
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, postData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getInputStream();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public String searchIndexScrolledRequest(String index, String type, String query, int scrollSize,
			String scrollExpiry) throws ElasticSearchClientException {
		JsonParser jp = new JsonParser();
		JsonElement jelem = jp.parse(query);
		JsonObject jobj = jelem.getAsJsonObject();
		jobj.add("size", new JsonPrimitive(scrollSize));
		String postData = jobj.toString();
		String uri = baseUri + index + "/" + type + "/_search?scroll=" + scrollExpiry;
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, postData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public String searchIndexScrolledRequest(String index, String type, String scrollId, String scrollExpiry)
			throws ElasticSearchClientException {
		JsonObject jobj = new JsonObject();
		jobj.add("scroll", new JsonPrimitive(scrollExpiry));
		String postData = jobj.toString();
		String uri = baseUri + index + "/" + type + "/_search/scroll";
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, postData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public String putIndex(String index, String type, String id, String source) throws ElasticSearchClientException {
		String uri = baseUri + index + "/" + type + "/" + id;
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.put(uri, headers, parameters, new String(source.getBytes(StandardCharsets.UTF_8)));
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public String deleteIndex(String index, String type, String id) throws ElasticSearchClientException {
		String uri = baseUri + index + "/" + type + "/" + id;
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.delete(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public String updateIndex(String index, String type, String id, String source) throws ElasticSearchClientException {
		String uri = baseUri + index + "/" + type + "/" + id + "/_update?retry_on_conflict=1";
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		String postData = String.format("{\"doc\" : %s, \"doc_as_upsert\" : true}", new String(source.getBytes(StandardCharsets.UTF_8)));
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, postData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public String closeIndex(String index) throws ElasticSearchClientException {
		String uri = String.format("%s%s/_close", baseUri, index);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, "");
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}
	
	public String openIndex(String index) throws ElasticSearchClientException {
		String uri = String.format("%s%s/_open", baseUri, index);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, "");
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}
	
	public String setIndexSettings(String index, String json) throws ElasticSearchClientException {
		String uri = String.format("%s%s/_settings", baseUri, index);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.put(uri, headers, parameters, json);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public void flushIndex(String index) throws ElasticSearchClientException {
		String uri = String.format("%s%s/_flush", baseUri, index);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, "");
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				// do nothing
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public void refreshIndex(String index) throws ElasticSearchClientException {
		String uri = String.format("%s%s/_refresh", baseUri, index);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, "");
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				// do nothing
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

	public void deleteByQuery(String index, String json) throws ElasticSearchClientException {
		String uri = String.format("%s%s/_delete_by_query?conflicts=proceed", baseUri, index);
		Map<String, String> headers = new HashMap<>();
		if (authorization != null) {
			headers.put(AUTHORIZATION, authorization);
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, json);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				// NA
			} else {
				throw new ElasticSearchClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ElasticSearchClientException(ex);
		}
	}

}
