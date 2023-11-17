package org.max.budgetcontrol.datasource;

import org.json.JSONException;
import org.json.JSONObject;


public interface IZenClientResponseHandler {
    void processResponse( JSONObject jObject ) throws JSONException;
    void processError( Exception e );
}
