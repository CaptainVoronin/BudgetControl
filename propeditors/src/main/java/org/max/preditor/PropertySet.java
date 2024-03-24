package org.max.preditor;

import static org.max.preditor.IPropertyAdapter.PR_DEFAULT_VALUE_INDEX;
import static org.max.preditor.IPropertyAdapter.PR_KEY_VALUE;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PropertySet {

    public static final String PR_KEY_KEY = "key";
    public static final String PR_KEY_HEADER = "header";
    public static final String PR_KEY_TYPE = "type";
    private static final String PR_KEY_ITEMS = "items";
    private static final String PR_LAYOUT_ID = "layoutId";
    private Map<String, IPropertyAdapter<?>> properties;

    public PropertySet(Context context, ViewGroup parent, int resourceId, int staticLayoutId) {
        this.context = context;
        this.parent = parent;
        this.resourceId = resourceId;
        this.staticLayoutId = staticLayoutId;
    }

    Context context;
    ViewGroup parent;
    int resourceId;

    int staticLayoutId;

    public void createViews() throws JSONException {
        JSONObject job = readResourceFile();
        createProperties(job);
    }

    private void createProperties(JSONObject job) throws JSONException {
        properties = new HashMap<>();
        JSONArray props = job.getJSONArray("property");
        for (int i = 0; i < props.length(); i++) {
            JSONObject jProp = props.getJSONObject(i);
            IPropertyAdapter<?> pe = createPropertyAdapter(jProp);
            properties.put(pe.getKey(), pe);
            View v = pe.getView();
            parent.addView(v);
        }
    }

    private IPropertyAdapter<? extends Object> createPropertyAdapter(JSONObject jProp) throws JSONException {
        String type = jProp.getString(PR_KEY_TYPE);
        String header = jProp.getString(PR_KEY_HEADER);
        String key = jProp.getString(PR_KEY_KEY);
        String layoutId = null;
        if( jProp.has( PR_LAYOUT_ID ) ) {
            layoutId = jProp.getString(PR_LAYOUT_ID);
            staticLayoutId = context.getResources().getIdentifier( layoutId, "layout", context.getPackageName());
        }

        String value = null;
        if( jProp.has( PR_KEY_VALUE ))
            value = jProp.getString( PR_KEY_VALUE );

        List<Object> items = null;
        int default_value_index = IPropertyAdapter.INVALID_DEFAULT_VALUE_INDEX;
        if( jProp.has( "items") )
        {
            JSONArray jItems = jProp.getJSONArray( PR_KEY_ITEMS );

            items = new ArrayList<>();
            for( int i = 0; i < jItems.length(); i++ )
              items.add( jItems.get(i) );
            if( jProp.has( PR_DEFAULT_VALUE_INDEX ))
                default_value_index = jProp.getInt( PR_DEFAULT_VALUE_INDEX );
         }

        if (type.trim().equalsIgnoreCase("string"))
            return new PropertyAdapterString(context, staticLayoutId, key, header, value, items, default_value_index);
        else if (type.trim().equalsIgnoreCase("double"))
            return new PropertyAdapterDouble(context, staticLayoutId, key, header, value != null ? Double.valueOf(value) : 0, items, default_value_index);
        else if (type.trim().equalsIgnoreCase("integer"))
            return new PropertyAdapterInteger(context, staticLayoutId, key, header, value != null ? Integer.valueOf(value) : 0, items, default_value_index);
        else if (type.trim().equalsIgnoreCase("color"))
            return new PropertyAdapterColor(context, staticLayoutId, key, header, value != null ? Color.valueOf(Color.parseColor(value)) : null, items, default_value_index);
        else
            throw new InvalidParameterException("Property type " + type + " is unknown");
    }

    private @Nullable JSONObject readResourceFile() {
        JSONObject job = null;
        Resources res = context.getResources();
        InputStream resourceReader = res.openRawResource(resourceId);
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(resourceReader, "UTF-8"));
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                line = reader.readLine();
            }
            reader.close();
            job = new JSONObject(buffer.toString());
            return job;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return job;
        }
    }

    public IPropertyAdapter<?> getByKey(String key) {
        return properties.get( key );
    }
}
