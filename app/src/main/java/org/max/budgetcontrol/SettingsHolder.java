package org.max.budgetcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

public class SettingsHolder {
    Context context;

    Map<String, Object> props;

    public SettingsHolder( Context context )
    {
        this.context = context;
        props = new HashMap<>();
    }

    public void init()
    {
        SharedPreferences pr = context.getSharedPreferences( "app.properties", Context.MODE_PRIVATE );
        props = (Map<String, Object>) pr.getAll();
        String strURL = pr.getString( context.getString( R.string.url ), null );
        String token = pr.getString( context.getString( R.string.token ), null );

        if( strURL == null ) {
            token = context.getString(R.string.token_value);
            strURL = context.getString(R.string.url_value);
            SharedPreferences.Editor ed = pr.edit();
            ed.putString( context.getString( R.string.url ), strURL );
            ed.putString( context.getString( R.string.token ), token );
            ed.commit();
        }
    }

    public Object getParameter( String name )
    {
        return props.get( name );
    }

    public String getParameterAsString( String name )
    {
        return props.get( name ).toString();
    }

    public Integer getParameterAsInt( String name )
    {
        return Integer.parseInt( props.get( name ).toString() );
    }

    public Long getParameterAsLong( String name )
    {
        return Long.parseLong( props.get( name ).toString() );
    }

    public void setParameter( String key, Object value )
    {
        props.put( key, value );
        SharedPreferences pr = context.getSharedPreferences( "app.properties", Context.MODE_PRIVATE );
        SharedPreferences.Editor ed = pr.edit();
        ed.putString( key, value.toString() );
        ed.commit();
    }
}
