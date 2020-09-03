package com.profittrailer.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

@Log4j2
public class HttpClientManager {

	private static HttpClient client;
	@Getter
	private static RequestConfig config;

	private HttpClientManager() {
		int TIMEOUT = 2;
		config = RequestConfig.custom()
				.setConnectTimeout(TIMEOUT * 1000)
				.setConnectionRequestTimeout(TIMEOUT * 1000)
				.setSocketTimeout(TIMEOUT * 1000).build();
	}

	public static HttpClient getHttpClientInstance() {
		if (client == null) {
			client = HttpClientBuilder.create()
					.setDefaultRequestConfig(config)
					.build();
		}
		return client;
	}

	public static Pair<Integer, String> postHttp(String url, List<NameValuePair> params, List<NameValuePair> headers) throws IOException {
		String result = null;
		ResponseHolder holder = postHttpWithResponse(url, params, headers);
		HttpResponse response = holder.getResponse();
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity);
		}

		holder.getRequest().releaseConnection();
		return ImmutablePair.of(response.getStatusLine().getStatusCode(), result);
	}

	public static ResponseHolder postHttpWithResponse(String url, List<NameValuePair> params, List<NameValuePair> headers) throws IOException {
		HttpPost post = new HttpPost(url);
		if (params != null && params.size() > 0) {
			post.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
		}

		if (headers != null) {
			for (NameValuePair header : headers) {
				post.addHeader(header.getName(), header.getValue());
			}
		}
		return new ResponseHolder(post, getHttpClientInstance().execute(post));
	}

	public static Pair<Integer, String> postHttpJson(String url, JsonObject postParams, List<NameValuePair> headers) throws IOException {
		String result = null;
		ResponseHolder holder = postHttpWithResponseJson(url, postParams, headers);
		HttpResponse response = holder.getResponse();
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity);
		}

		holder.getRequest().releaseConnection();
		return ImmutablePair.of(response.getStatusLine().getStatusCode(), result);
	}

	public static ResponseHolder postHttpWithResponseJson(String url, JsonObject postParams, List<NameValuePair> headers) throws IOException {
		Gson gson = new Gson();

		HttpPost post = new HttpPost(url);
		StringEntity params = new StringEntity(gson.toJson(postParams));
		post.setEntity(params);
		if (headers != null) {
			for (NameValuePair pair : headers) {
				post.setHeader(pair.getName(), pair.getValue());
			}
		}
		return new ResponseHolder(post, getHttpClientInstance().execute(post));
	}

	public static ResponseHolder putHttpWithResponse(String url, List<NameValuePair> params, List<NameValuePair> headers) throws IOException {
		HttpPut put = new HttpPut(url);
		if (params != null) {
			put.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
		}

		if (headers != null) {
			for (NameValuePair header : headers) {
				put.addHeader(header.getName(), header.getValue());
			}
		}
		return new ResponseHolder(put, getHttpClientInstance().execute(put));
	}

	public static Pair<Integer, String> getHttp(String url, List<NameValuePair> headers) throws Exception {
		String result = null;
		ResponseHolder holder = getHttpWithResponse(url, headers);
		HttpResponse response = holder.getResponse();
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity);
		}

		holder.getRequest().releaseConnection();
		return ImmutablePair.of(response.getStatusLine().getStatusCode(), result);
	}

	public static String deleteHttp(String url, List<NameValuePair> headers) throws Exception {
		String result = null;
		ResponseHolder holder = deleteHttpWithResponse(url, headers);
		HttpResponse response = holder.getResponse();
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity);
		} else if (response.getStatusLine().getStatusCode() == 204) {
			JsonObject success = new JsonObject();
			success.addProperty("success", 1);
			result = success.toString();
		}
		holder.getRequest().releaseConnection();
		return result;
	}

	public static ResponseHolder getHttpWithResponse(String url, List<NameValuePair> headers) throws IOException {
		HttpRequestBase request = new HttpGet(url);

		if (headers != null) {
			for (NameValuePair header : headers) {
				request.addHeader(header.getName(), header.getValue());
			}
		}

		return new ResponseHolder(request, getHttpClientInstance().execute(request));
	}

	public static ResponseHolder deleteHttpWithResponse(String url, List<NameValuePair> headers) throws IOException {
		HttpRequestBase request = new HttpDelete(url);

		if (headers != null) {
			for (NameValuePair header : headers) {
				request.addHeader(header.getName(), header.getValue());
			}
		}

		return new ResponseHolder(request, getHttpClientInstance().execute(request));
	}

	public static class ResponseHolder {

		private HttpRequestBase request;
		private HttpResponse response;

		public ResponseHolder(HttpRequestBase request, HttpResponse response) {
			this.response = response;
			this.request = request;
		}

		public HttpRequestBase getRequest() {
			return request;
		}

		public HttpResponse getResponse() {
			return response;
		}
	}
}

