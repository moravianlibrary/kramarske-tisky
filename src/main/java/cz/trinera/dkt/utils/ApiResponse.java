package cz.trinera.dkt.utils;

import org.json.JSONObject;

public class ApiResponse {

    public final String requestUrl;
    public final String requestMethod;
    public final JSONObject requestBody;

    public final int responseCode;
    public final Object result;
    public final JSONObject errorMessage;

    public final float durationS;

    public ApiResponse(String requestUrl, String requestMethod, JSONObject requestBody, int responseCode, Object result, float durationS) {
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.requestBody = requestBody;
        this.responseCode = responseCode;
        this.result = result;
        this.errorMessage = null;
        this.durationS = durationS;
    }

    public boolean isOk() {
        return responseCode < 300 && errorMessage == null;
    }

    public ApiResponse(String requestUrl, String requestMethod, JSONObject requestBody, int responseCode, Object result, JSONObject errorMessage, float durationS) {
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.requestBody = requestBody;
        this.responseCode = responseCode;
        this.result = result;
        this.errorMessage = errorMessage;
        this.durationS = durationS;
    }

    @Override
    public String toString() {
        return "Response{" + "responseCode=" + responseCode + ", result=" + result + ", errorMessage='" + errorMessage + '\'' + '}';
    }

    public JSONObject toErrorJson(boolean includingUrl) {
        JSONObject errorJson = new JSONObject();
        //request
        JSONObject remoteApiRequest = new JSONObject();
        //if (includingUrl) {
        if (includingUrl && false) { //TODO: docasne nezahrnuju url nikdy, protoze produkcne pouzivame i neprodukcni endpointy
            remoteApiRequest.put("url", requestUrl);
        }
        remoteApiRequest.put("method", requestMethod);
        if (requestBody != null) {
            remoteApiRequest.put("body", requestBody);
        }
        errorJson.put("remoteApiRequest", remoteApiRequest);
        //response
        JSONObject remoteApiResponse = new JSONObject();
        remoteApiResponse.put("status", responseCode);
        remoteApiResponse.put("body", errorMessage);
        errorJson.put("remoteApiResponse", remoteApiResponse);
        //return
        return errorJson;
    }
}
