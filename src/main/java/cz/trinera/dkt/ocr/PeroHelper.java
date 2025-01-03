package cz.trinera.dkt.ocr;

import cz.trinera.dkt.utils.ApiResponse;
import cz.trinera.dkt.utils.MyApacheHttpClientProvider;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class PeroHelper {

    private final String baseUrl;
    private final String apiKey;

    public PeroHelper(String baseUrl, String apiKey) {
        this.baseUrl = normalizeUrl(baseUrl);
        this.apiKey = apiKey;
    }

    private String normalizeUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            return baseUrl;
        }
    }

    /**
     * @see https://app.swaggerhub.com/apis-docs/LachubCz/PERO-API/1.0.4
     */
    public ApiResponse queryGet(String urlPath) throws IOException {
        try (CloseableHttpClient httpClient = MyApacheHttpClientProvider.getClient()) {
            String url = buildUrl(urlPath);
            //System.out.println("url: " + url);
            ClassicHttpRequest httpGet = ClassicRequestBuilder.get(url)
                    .setHeader("api-key", this.apiKey)
                    .build();
            long start = System.currentTimeMillis();
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                float durationS = (System.currentTimeMillis() - start) / 1000f;
                //System.out.println(response.getCode() + " " + response.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                if (response.getCode() < 300) { //ok
                    String responseBody = EntityUtils.toString(entity);
                    //System.out.println("Response: " + responseBody);
                    EntityUtils.consume(entity);//to make sure the connection is released
                    String contentType = response.getHeader("content-type").getValue();
                    System.out.println("content-type: " + contentType);
                    if (contentType.startsWith("application/json")) {
                        return new ApiResponse(url, "GET", null, response.getCode(), new JSONObject(responseBody), durationS);
                    } else if (contentType.startsWith("application/xml")) {
                        nu.xom.Builder builder = new nu.xom.Builder();
                        nu.xom.Document xmlDocument = builder.build(responseBody, null);
                        return new ApiResponse(url, "GET", null, response.getCode(), xmlDocument, durationS);
                    } else {
                        return new ApiResponse(url, "GET", null, response.getCode(), responseBody, durationS);
                    }
                } else {
                    JSONObject errorJson = null;
                    String responseBodyStr = EntityUtils.toString(entity);
                    try {
                        errorJson = new JSONObject(responseBodyStr);
                    } catch (JSONException e) {
                        String responseMessage = response.getReasonPhrase();
                        if (responseMessage != null && !responseMessage.trim().isEmpty()) {
                            errorJson = new JSONObject();
                            errorJson.put("error", responseMessage);
                        }
                    }
                    System.err.println("Error: " + response.getCode() + " - " + errorJson);
                    EntityUtils.consume(entity);//to make sure the connection is released
                    return new ApiResponse(url, "POST", null, response.getCode(), null, errorJson, durationS);
                }
            } catch (ParseException e) {
                throw new IOException(e);
            } catch (ProtocolException e) {
                throw new IOException(e);
            } catch (ValidityException e) {
                throw new IOException(e);
            } catch (ParsingException e) {
                throw new IOException(e);
            }
        } finally {
            //MyApacheHttpClientProvider.cleanup();
        }
    }

    public ApiResponse queryPost(String urlPath, JSONObject body) throws IOException {
        try (CloseableHttpClient httpClient = MyApacheHttpClientProvider.getClient()) {
            String url = buildUrl(urlPath);
            //System.out.println("url: " + url);
            ClassicHttpRequest httpPost = ClassicRequestBuilder.post(url)
                    .setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON))
                    .setHeader("api-key", this.apiKey)
                    .build();
            long start = System.currentTimeMillis();
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                float durationS = (System.currentTimeMillis() - start) / 1000f;
                //System.out.println(response.getCode() + " " + response.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                if (response.getCode() < 300) { //ok
                    String responseBody = EntityUtils.toString(entity);
                    //System.out.println("Response: " + responseBody);
                    EntityUtils.consume(entity);//to make sure the connection is released
                    return new ApiResponse(url, "POST", body, response.getCode(), new JSONObject(responseBody), durationS);
                } else {
                    JSONObject errorJson = null;
                    String responseBodyStr = EntityUtils.toString(entity);
                    try {
                        errorJson = new JSONObject(responseBodyStr);
                    } catch (JSONException e) {
                        String responseMessage = response.getReasonPhrase();
                        if (responseMessage != null && !responseMessage.trim().isEmpty()) {
                            errorJson = new JSONObject();
                            errorJson.put("error", responseMessage);
                        }
                    }
                    System.err.println("Error: " + response.getCode() + " - " + errorJson);
                    EntityUtils.consume(entity);//to make sure the connection is released
                    return new ApiResponse(url, "POST", body, response.getCode(), null, errorJson, durationS);
                }
            } catch (ParseException e) {
                throw new IOException(e);
            }
        } finally {
            //MyApacheHttpClientProvider.cleanup();
        }
    }

    public ApiResponse queryPostMultipart(String urlPath, File inFile) throws IOException {
        try (CloseableHttpClient httpClient = MyApacheHttpClientProvider.getClient()) {
            String url = buildUrl(urlPath);
            //System.out.println("url: " + url);
            HttpEntity multipartEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", inFile, ContentType.DEFAULT_BINARY, inFile.getName())
                    .build();
            ClassicHttpRequest httpPost = ClassicRequestBuilder.post(url)
                    .setEntity(multipartEntity)
                    .setHeader("api-key", this.apiKey)
                    .build();
            long start = System.currentTimeMillis();
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                float durationS = (System.currentTimeMillis() - start) / 1000f;
                //System.out.println(response.getCode() + " " + response.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                if (response.getCode() < 300) { //ok
                    String responseBody = EntityUtils.toString(entity);
                    //System.out.println("Response: " + responseBody);
                    EntityUtils.consume(entity);//to make sure the connection is released
                    return new ApiResponse(url, "POST", null, response.getCode(), new JSONObject(responseBody), durationS);
                } else {
                    JSONObject errorJson = null;
                    String responseBodyStr = EntityUtils.toString(entity);
                    try {
                        errorJson = new JSONObject(responseBodyStr);
                    } catch (JSONException e) {
                        String responseMessage = response.getReasonPhrase();
                        if (responseMessage != null && !responseMessage.trim().isEmpty()) {
                            errorJson = new JSONObject();
                            errorJson.put("error", responseMessage);
                        }
                    }
                    System.err.println("Error: " + response.getCode() + " - " + errorJson);
                    EntityUtils.consume(entity);//to make sure the connection is released
                    return new ApiResponse(url, "POST", null, response.getCode(), null, errorJson, durationS);
                }
            } catch (ParseException e) {
                throw new IOException(e);
            }
        } finally {
            //MyApacheHttpClientProvider.cleanup();
        }
    }

    private String buildUrl(String urlPath) {
        return this.baseUrl + "/" + urlPath;
    }

}
