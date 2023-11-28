package org.max.budgetcontrol.datasource;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

public interface IZenClientResponseHandler {
    void onNon200Code(Response responze);
    void onResponseReceived(JSONObject jObject) throws JSONException;
    void processError( Exception e );
}
