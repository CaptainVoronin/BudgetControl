package org.max.budgetcontrol.zentypes;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ZenEntities;

import java.io.Serializable;
import java.text.ParseException;
import java.util.UUID;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class Account extends AZenType implements Serializable
{

    public Integer getInstrument()
    {
        return instrument;
    }

    public void setInstrument(Integer instrument)
    {
        this.instrument = instrument;
    }

    Integer instrument;

    public Account(UUID id, String title, Long created, Long changed, Integer userId, Integer instrument)
    {
        super(id, title, ZenEntities.account, userId, new UnixTimestamp(created), new UnixTimestamp(changed));
        this.instrument = instrument;
    }

    public static Account fromJSONObject(@NotNull JSONObject obj) throws ParseException, JSONException
    {
        UUID id = UUID.fromString(obj.getString("id"));
        Long changed = obj.getLong("changed");
        Integer userId = obj.getInt("user");
        Integer instrument = obj.getInt("instrument");
        String title = obj.getString("title");
        return new Account(id, title, changed, changed, userId, instrument);
    }

    @Override
    public String toString()
    {
        return getTitle();
    }
}
