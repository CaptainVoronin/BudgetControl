package org.max.budgetcontrol.datasource;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.ResponseProcessor;

import java.util.List;

public class InitialRequestResponseHandler implements  IResponseHandler{
    Context context;
    public InitialRequestResponseHandler( Context context )
    {
        this.context = context;
    }
    @Override
    public void processResponse(JSONObject jObject) throws JSONException {
        List< Category > cats = ResponseProcessor.getCategory( jObject );
        saveCategoryListInDB( context, cats );
    }

    private void saveCategoryListInDB(Context context, List<Category> cats) {


    }
}
