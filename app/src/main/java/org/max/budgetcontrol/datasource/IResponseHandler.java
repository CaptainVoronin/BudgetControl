package org.max.budgetcontrol.datasource;

import org.json.JSONObject;


public interface IResponseHandler {
    void processResponse( JSONObject jObject );
}
