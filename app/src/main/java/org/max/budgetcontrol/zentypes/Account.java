package org.max.budgetcontrol.zentypes;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ZenEntities;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

public class Account extends AZenType
{
    public Integer getInstrument()
    {
        return instrument;
    }

    Integer instrument;

    public Account(UUID id, String title, Long created, Long changed, Integer userId, Integer instrument)
    {
        super(id, title, created, changed, userId, ZenEntities.account);
        this.instrument = instrument;
    }

    @NonNull
    @Override
    public String toString()
    {
        return title;
    }

    public static Account fromJSONObject(@NotNull JSONObject obj) throws ParseException, JSONException
    {
        UUID id = UUID.fromString(obj.getString("id"));
        Long changed = obj.getLong( "changed" );
        Integer userId = obj.getInt( "user");
        Integer instrument = obj.getInt( "instrument");
        String title = obj.getString( "title");
        return new Account(id, title, changed, changed, userId, instrument );
    }
}
