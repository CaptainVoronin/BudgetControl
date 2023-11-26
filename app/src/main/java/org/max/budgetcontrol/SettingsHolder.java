package org.max.budgetcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

import androidx.preference.PreferenceManager;

/* Token
0yteuv8iwTQcJpaBQXJ3XDZ3nnh1RV
 */

public class SettingsHolder {
    Context context;

    Map<String, Object> props;

    boolean isInit;

    public SettingsHolder( Context context )
    {
        this.context = context;
        props = new HashMap<>();
        isInit = false;
    }

    public boolean init()
    {
        SharedPreferences pr = PreferenceManager.getDefaultSharedPreferences(context);
        props = (Map<String, Object>) pr.getAll();
        Object obj;

        if( ( obj = props.get( context.getString( R.string.token ) ) ) == null )
            isInit = false;
        else if( obj.toString().trim().length() == 0  )
            isInit = false;
        else
            isInit = true;

        return isInit;
    }

    public Object getParameter( String name )
    {
        assert isInit != false : "Application settings not initialized. Call init()!";
        return props.get( name );
    }

    public String getParameterAsString( String name )
    {
        assert isInit != false : "Application settings not initialized. Call init()!!";
        return props.get( name ).toString();
    }

    public Integer getParameterAsInt( String name )
    {
        assert isInit != false : "Application settings not initialized! Call init()!";
        return Integer.parseInt( props.get( name ).toString() );
    }

    public Long getParameterAsLong( String name )
    {
        assert isInit != false: "Application settings not initialized! Call init()!";
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
