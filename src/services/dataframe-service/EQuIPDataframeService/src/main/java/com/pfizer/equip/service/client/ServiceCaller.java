package com.pfizer.equip.service.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


public class ServiceCaller {

	private static CookieManager cookieManager;

    
	public ServiceCaller() throws ServiceCallerException {
		if (cookieManager == null) {
			cookieManager = new CookieManager();
			CookieHandler.setDefault(cookieManager);
		}
	}

	
	private HttpURLConnection createConnection(String uri, String method, Map<String, String> headers, Map<String, String> parameters)
			throws IOException {
		// Create connection...
		String uriPlusParams = appendParameters(uri, parameters);
		URL url = new URL(uriPlusParams);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		return connection;
	}

	private HttpURLConnection createConnection(String uri, String method, Map<String, String> headers)
			throws IOException {
		// Create connection...
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		for (Map.Entry<String, String> entry: headers.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		return connection;
	}

	public ServiceResponse get(String uri, Map<String, String> headers) throws ServiceCallerException {
		return get(uri, headers, null);
	}

	public ServiceResponse get(String uri, Map<String, String> headers, Map<String, String> parameters) throws ServiceCallerException {
		try {
			// Create connection...
			HttpURLConnection connection = createConnection(uri, "GET", headers, parameters);
	
			// Return response...
			return getResponse(connection);
		} catch(Exception ex) {
			throw new ServiceCallerException(ex);
		}
	}

	public ServiceResponse put(String uri, Map<String, String> headers) throws ServiceCallerException {
		return put(uri, headers, null);
	}
	
	public ServiceResponse put(String uri, Map<String, String> headers, Map<String, String> parameters) throws ServiceCallerException {
		try {
			// Create connection...
			HttpURLConnection connection = createConnection(uri, "PUT", headers, parameters);
	
			// Return response...
			return getResponse(connection);
		} catch(Exception ex) {
			throw new ServiceCallerException(ex);
		}
	}
	
	public ServiceResponse post(String uri, Map<String, String> headers, String postData) throws ServiceCallerException {
		return post(uri, headers, null, postData);
	}

	public ServiceResponse post(String uri, Map<String, String> headers, Map<String, String> parameters, String postData) throws ServiceCallerException {
		try {
			// Create connection...
			HttpURLConnection connection = createConnection(uri, "POST", headers, parameters);
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			try (OutputStream os = connection.getOutputStream()) {
				os.write(postData.getBytes());
				os.flush();
				// Return response...
				return getResponse(connection);
			}
		} catch(Exception ex) {
			throw new ServiceCallerException(ex);
		}
	}

	public ServiceResponse delete(String uri, Map<String, String> headers) throws ServiceCallerException {
		return delete(uri, headers, null);
	}
	
	public ServiceResponse delete(String uri, Map<String, String> headers, Map<String, String> parameters) throws ServiceCallerException {
		try {
			// Create connection...
			HttpURLConnection connection = createConnection(uri, "DELETE", headers, parameters);
	
			// Return response...
			return getResponse(connection);
		} catch(Exception ex) {
			throw new ServiceCallerException(ex);
		}
	}
	
	private String appendParameters(String uri, Map<String, String> parameters) throws UnsupportedEncodingException {
		String uriPlusParams = uri;
		if (parameters != null) {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				if (sb.length() == 0) {
					sb.append('?');
				} else {
					sb.append('&');
				}
				sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				sb.append('=');
				sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			}
			uriPlusParams += sb.toString();
		}
		return uriPlusParams;
	}

	private ServiceResponse getResponse(HttpURLConnection connection) throws IOException {
		int responseCode = connection.getResponseCode();
		java.io.InputStream is = null;
		java.io.InputStream es = null;
		if (responseCode != 500) {
			is = connection.getInputStream();
			es = connection.getErrorStream();
		}
		return new ServiceResponse(connection, responseCode, is, es);
	}

}
