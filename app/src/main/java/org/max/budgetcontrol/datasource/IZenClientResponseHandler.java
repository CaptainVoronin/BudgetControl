package org.max.budgetcontrol.datasource;

import org.json.JSONException;
import org.json.JSONObject;

public interface IZenClientResponseHandler {
    void updateWidgets(JSONObject jObject ) throws JSONException;
    void processError( Exception e );
}
