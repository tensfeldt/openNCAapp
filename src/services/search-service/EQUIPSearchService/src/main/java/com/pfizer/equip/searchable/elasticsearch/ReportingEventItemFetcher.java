package com.pfizer.equip.searchable.elasticsearch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.equip.searchable.dto.InstantDeserializer;
import com.pfizer.equip.searchable.dto.InstantSerializer;
import com.pfizer.equip.searchable.dto.ReportingEventItemData;
import com.pfizer.equip.searchable.dto.SearchableData;
import com.pfizer.modeshape.api.client.ModeshapeClient;
import com.pfizer.modeshape.api.client.ModeshapeClientException;

public class ReportingEventItemFetcher {
	private static Logger log = LoggerFactory.getLogger(ReportingEventItemFetcher.class);
	private static final String ES_INDEX_PREFIX = "162d9619a77200";
	private static String REPOSITORY = "equip";
	private static String WORKSPACE = "nca";
	private static String INDEX = "ereindx-nca";
	private static String TYPE = "nca";
	public static String SEARCHABLE_INDEX = "contentndx-nca";
	private static String ID_QUERY = "{ \"_source\":[\"_id\"], \"query\" : {\"match_all\": {}}, \"from\" : %d, \"size\" : 1 }";
	private static String ID_REGEX = "([a-f\\d]{22}(-[a-f\\d]{4}){3}-[a-f\\d]{12}?)";	
	private static Pattern ID_PATTERN = Pattern.compile(ID_REGEX, Pattern.CASE_INSENSITIVE);
	private static String COUNT_REGEX = "^(.*\"hits\":.*\\{\"total\":)([0-9]*)(.*)";
	private static Pattern COUNT_PATTERN = Pattern.compile(COUNT_REGEX, Pattern.CASE_INSENSITIVE);
	private static String SOURCE_PATTERN = "equip:*,jcr:*";
	private static String PUBLISH_STATUS_REGEX = "(\\\"equip:publishStatus\\\":\\\")([A-Z a-z \\s]+)(\\\",)";
	private static Pattern PUBLISH_STATUS_PATTERN = Pattern.compile(PUBLISH_STATUS_REGEX, Pattern.CASE_INSENSITIVE);

	private static String UPDATE_QUERY = "{ \"_source\":[\"_id\"], " + 
			"  \"query\" :  { \"bool\" : { \"must\" : [" + 
			"                                    { \"bool\" : { \"should\" : [{\"query_string\":{\"default_field\" : \"jcr:created\", \"query\" : \">%d\"}}," + 
			"						                                     {\"query_string\":{\"default_field\" : \"jcr:lastModified\", \"query\" : \">%d\"}}]}}]}}," + 
			"  \"sort\" : [{\"jcr:lastModified\" : {\"order\" : \"asc\"}}, {\"jcr:created\" : {\"order\" : \"asc\"}}],	" + 
			"  \"from\" : %d, \"size\" : %d }";
	
	
	private ElasticSearchClient esc;
	private ModeshapeClient msc;
	private SearchableFetcher sf;
	private Gson gson;

	public ReportingEventItemFetcher(ElasticSearchClient esc, ModeshapeClient msc) {
		this.esc = esc;
		this.msc = msc;
		sf = new SearchableFetcher(esc);
		gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
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
	
	public List<String> getIdsUpdatedSince(Instant lastUpdate, int count) throws ElasticSearchClientException {
		long lastUpdateMillis = lastUpdate.toEpochMilli();
		esc.refreshIndex(INDEX);
		return getIds(esc.searchIndex(INDEX, TYPE, String.format(UPDATE_QUERY, lastUpdateMillis, lastUpdateMillis, 0, count)));
	}
	
	public ReportingEventItemData getData(String id) throws ElasticSearchClientException {
		String json = getDataJson(id);
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		ReportingEventItemData returnValue = gson.fromJson(json, ReportingEventItemData.class);
		returnValue.getEquipStudyId();
		if (returnValue.getEquipAssemblyId() != null && !returnValue.getEquipAssemblyId().isEmpty()) {
			String[] equipIds = { sf.getEquipIdForId(returnValue.getEquipAssemblyId()) };
			returnValue.setEquipAssemblyEquipIds(equipIds);
		}
		if (returnValue.getEquipDataframeId() != null && !returnValue.getEquipDataframeId().isEmpty()) {
			SearchableData sd = sf.getSearchableDataForId(returnValue.getEquipDataframeId());
			String[] equipIds = { sd.getEquipEquipId() };
			returnValue.setEquipDataframeEquipIds(equipIds);
			returnValue.setEquipPromotionStatus(sd.getEquipPromotionStatus());
			returnValue.setEquipDataStatus(sd.getEquipDataStatus());
			returnValue.setEquipDataBlindingStatus(sd.getEquipDataBlindingStatus());
			returnValue.setEquipRestrictionStatus(sd.getEquipRestrictionStatus());
			returnValue.setEquipPublished(sd.getEquipPublished());
		}
		if (returnValue.getEquipParentReportingEventId() != null && !returnValue.getEquipParentReportingEventId().isEmpty()) {
			returnValue.setEquipParentEquipId(sf.getEquipIdForId(returnValue.getEquipParentReportingEventId()));
			List<String> statuses = sf.getMetadataValuesForParentPathAndKey(
					sf.getPathForId(returnValue.getEquipParentReportingEventId()), 
					"reportingEventReleaseStatusKey");
			if (!statuses.isEmpty()) {
				String status = statuses.get(0);
				returnValue.setEquipReleaseStatus(status);
				returnValue.setEquipReleased("Released".equalsIgnoreCase(status));
			}
		}
		// Get publishedItem if it exists.
		String publishedItemPath = returnValue.getJcrPath() + "/equip:publishedItem";
		try {
			String publishedItemJson = msc.retrieveNodeOrProperty(REPOSITORY, WORKSPACE, publishedItemPath);
			Matcher m = PUBLISH_STATUS_PATTERN.matcher(publishedItemJson);
			if (m.find()) {
				String publishStatus = m.group(2);
				returnValue.setEquipPublishStatus(publishStatus);
				returnValue.setEquipPublished("Published".equalsIgnoreCase(publishStatus));
			}

		} catch (ModeshapeClientException e) {
			// Not found
		}
		returnValue.setIndexKey(id);
		return returnValue;
	}
	
	public boolean updateReportingEventItems(long lastUpdateTime) throws ElasticSearchClientException {
		List<String> ids = getIdsUpdatedSince(Instant.ofEpochMilli(lastUpdateTime), 100);
		boolean returnValue = !ids.isEmpty();
		ids.stream().forEach(id -> {
			try {
				updateReportingEventItem(id);
			} catch (ElasticSearchClientException ex) {
				log.error("", ex);
			}
		});
		return returnValue;
	}

	public void updateReportingEventItem(String id) throws ElasticSearchClientException {
		ReportingEventItemData reid = getData(id);
		String json = gson.toJson(reid);
		String indexId = reid.getIndexKey();
		esc.putIndex(SEARCHABLE_INDEX, TYPE, indexId, json);
		// Update the parent Reporting Event
		// SearchableData sd = sf.getSearchableDataForId(reid.getEquipParentEquipId()); // Bug 
		SearchableData sd = sf.getSearchableDataForId(reid.getEquipParentReportingEventId());
		json = gson.toJson(sd);
		esc.putIndex(SEARCHABLE_INDEX, TYPE, sd.getIndexKey(), json);
	}

	private String getId(String nodeData) {
		Matcher m = ID_PATTERN.matcher(nodeData);
		String returnValue = null;
		if (m.find()) {
			returnValue = m.group();
			if (returnValue.startsWith("\"")) {
				returnValue = returnValue.substring(1);
			}
		}
		return returnValue;
	}

	private List<String> getIds(String nodeData) {
		List<String> returnValue = new ArrayList<>();
		Matcher m = ID_PATTERN.matcher(nodeData);
		while (m.find()) {
			String id = m.group();
			if (id.startsWith("\"")) {
				id = id.substring(1);
			}
			returnValue.add(id);
		}
		return returnValue;
	}

	private String getDataJson(String id) throws ElasticSearchClientException {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("_source", SOURCE_PATTERN);
		String esIndexId = id;
		if (esIndexId.length() < 50) {
			esIndexId = ES_INDEX_PREFIX + esIndexId;
		}
		String json = esc.getIndexValue(INDEX, TYPE,  esIndexId, parameters);
		if ((json != null) && !json.isEmpty()) {
			int start = json.indexOf("\"_source\":");
			json = json.substring(start + "\"_source\":".length(), json.lastIndexOf('}'));
		}
		return json;
	}

}
