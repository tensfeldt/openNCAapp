package com.pfizer.equip.searchservice.resource;

import java.util.Properties;

import com.pfizer.equip.searchservice.AppPropertyNames;
import com.pfizer.equip.searchservice.Application;

public class ResourceCommon {
	public static final String IAMPFIZERUSERCN = "IAMPFIZERUSERCN";
	public static Properties properties;
	public static String VERSION;
	public static Boolean UPDATE_INDEXES;
	public static String ELASTICSEARCH_SERVER;
	public static String ELASTICSEARCH_USERNAME;
	public static String ELASTICSEARCH_PASSWORD;
	public static String MODESHAPE_SERVER;
	public static String MODESHAPE_USERNAME;
	public static String MODESHAPE_PASSWORD;
	public static String SEARCH_TYPE = "nca";
	public static String METADATA_SEARCH_INDEX;
	public static String[] METADATA_SEARCH_SOURCES_INCLUDE;
	public static String[] METADATA_SEARCH_SOURCES_EXCLUDE;
	public static String COMMENTS_SEARCH_INDEX;
	public static String[] COMMENTS_SEARCH_SOURCES_INCLUDE;
	public static String[] COMMENTS_SEARCH_SOURCES_EXCLUDE;
	public static String FILEDATA_SEARCH_INDEX;
	public static String[] FILEDATA_SEARCH_SOURCES_INCLUDE;
	public static String[] FILEDATA_SEARCH_SOURCES_EXCLUDE;
	public static String FILETEXT_SEARCH_INDEX;
	public static String[] FILETEXT_SEARCH_SOURCES_INCLUDE;
	public static String[] FILETEXT_SEARCH_SOURCES_EXCLUDE;
	public static String[] UNIFIED_SEARCH_SOURCES_INCLUDE;
	public static String[] UNIFIED_SEARCH_SOURCES_EXCLUDE;
	public static String AUTH_SERVER;
	public static String AUTH_PORT;
	public static String AUTH_SYSTEM = "nca";

	static {
		initializeProperties();
	}

	private ResourceCommon() {}
	
	private static void initializeProperties() {
		if (ResourceCommon.properties == null) {
			ResourceCommon.properties = getProperties();
			ResourceCommon.VERSION = properties.getProperty(AppPropertyNames.VERSION);
			ResourceCommon.UPDATE_INDEXES = Boolean.parseBoolean(properties.getProperty(AppPropertyNames.UPDATE_INDEXES));
			ResourceCommon.ELASTICSEARCH_SERVER = properties.getProperty(AppPropertyNames.ELASTICSEARCH_SERVER);
			ResourceCommon.ELASTICSEARCH_USERNAME = properties.getProperty(AppPropertyNames.ELASTICSEARCH_USERNAME);
			ResourceCommon.ELASTICSEARCH_PASSWORD = properties.getProperty(AppPropertyNames.ELASTICSEARCH_PASSWORD);
			ResourceCommon.MODESHAPE_SERVER = properties.getProperty(AppPropertyNames.MODESHAPE_SERVER);
			ResourceCommon.MODESHAPE_USERNAME = properties.getProperty(AppPropertyNames.MODESHAPE_USERNAME);
			ResourceCommon.MODESHAPE_PASSWORD = properties.getProperty(AppPropertyNames.MODESHAPE_PASSWORD);
			ResourceCommon.METADATA_SEARCH_INDEX = properties.getProperty(AppPropertyNames.SEARCH_METADATA_INDEX);
			ResourceCommon.METADATA_SEARCH_SOURCES_INCLUDE = properties.getProperty(AppPropertyNames.METADATA_SEARCH_SOURCES_INCLUDE).split(",");
			ResourceCommon.METADATA_SEARCH_SOURCES_EXCLUDE = properties.getProperty(AppPropertyNames.METADATA_SEARCH_SOURCES_EXCLUDE).split(",");
			ResourceCommon.COMMENTS_SEARCH_INDEX = properties.getProperty(AppPropertyNames.SEARCH_COMMENTS_INDEX);
			ResourceCommon.COMMENTS_SEARCH_SOURCES_INCLUDE = properties.getProperty(AppPropertyNames.COMMENTS_SEARCH_SOURCES_INCLUDE).split(",");
			ResourceCommon.COMMENTS_SEARCH_SOURCES_EXCLUDE = properties.getProperty(AppPropertyNames.COMMENTS_SEARCH_SOURCES_EXCLUDE).split(",");
			ResourceCommon.FILEDATA_SEARCH_INDEX = properties.getProperty(AppPropertyNames.SEARCH_FILEDATA_INDEX);
			ResourceCommon.FILEDATA_SEARCH_SOURCES_INCLUDE = properties.getProperty(AppPropertyNames.FILEDATA_SEARCH_SOURCES_INCLUDE).split(",");
			ResourceCommon.FILEDATA_SEARCH_SOURCES_EXCLUDE = properties.getProperty(AppPropertyNames.FILEDATA_SEARCH_SOURCES_EXCLUDE).split(",");
			ResourceCommon.FILETEXT_SEARCH_INDEX = properties.getProperty(AppPropertyNames.SEARCH_FILETEXT_INDEX);
			ResourceCommon.FILETEXT_SEARCH_SOURCES_INCLUDE = properties.getProperty(AppPropertyNames.FILETEXT_SEARCH_SOURCES_INCLUDE).split(",");
			ResourceCommon.FILETEXT_SEARCH_SOURCES_EXCLUDE = properties.getProperty(AppPropertyNames.FILETEXT_SEARCH_SOURCES_INCLUDE).split(",");
			ResourceCommon.UNIFIED_SEARCH_SOURCES_INCLUDE = properties.getProperty(AppPropertyNames.UNIFIED_SEARCH_SOURCES_INCLUDE).split(",");
			ResourceCommon.UNIFIED_SEARCH_SOURCES_EXCLUDE = properties.getProperty(AppPropertyNames.UNIFIED_SEARCH_SOURCES_EXCLUDE).split(",");
			ResourceCommon.AUTH_SERVER = properties.getProperty(AppPropertyNames.AUTH_SERVICE_HOST);
			ResourceCommon.AUTH_PORT = properties.getProperty(AppPropertyNames.AUTH_SERVICE_PORT);
		}
	}

	private static Properties getProperties() {
		return Application.getAppProperties();
	}

}
