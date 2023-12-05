package org.max.budgetcontrol.zentypes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;

public class WidgetParamsConverter {
    public static final JSONObject toJSON( WidgetParams widget ) throws JSONException {
        JSONObject job = new JSONObject();
        job.put( "title", widget.getTitle() );
        job.put( "limit_amount", widget.getLimitAmount() );
        job.put( "period", widget.getStartPeriod().name );
        job.put( "id", widget.getId() );
        job.put( "app_id", widget.getAppId() );
        JSONArray arr = new JSONArray();
        for( UUID uuid : widget.getCategories())
            arr.put( uuid );
        job.put( "categories", arr );
        return job;
    }

    public static final WidgetParams toWidget( JSONObject job ) throws JSONException {
        WidgetParams widget = new WidgetParams();
        widget.setId(job.getInt( "id" ) );
        widget.setAppId(job.getInt( "app_id" ) );
        widget.setTitle(job.getString( "title" ) );
        widget.setLimitAmount( job.getDouble( "limit_amount" ) );
        widget.setStartPeriod( StartPeriodEncoding.valueOf( job.get( "period" ).toString() ) );

        JSONArray arr = job.getJSONArray( "categories" );
        for( int i = 0; i < arr.length(); i++ )
            widget.addCategoryId( UUID.fromString( arr.getString( i ) ) );

        return widget;
    }
}
