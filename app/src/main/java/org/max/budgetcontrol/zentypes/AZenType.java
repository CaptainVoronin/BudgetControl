package org.max.budgetcontrol.zentypes;

import org.max.budgetcontrol.datasource.ZenEntities;

import java.io.Serializable;
import java.util.UUID;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class AZenType implements Serializable
{
    UUID id;

    String title;

    ZenEntities type;

    Integer userId;

    UnixTimestamp created;

    UnixTimestamp changed;

    public AZenType(UUID id, String title, ZenEntities type, Integer userId, UnixTimestamp created, UnixTimestamp changed)
    {
        this.id = id;
        this.title = title;
        this.type = type;
        this.userId = userId;
        this.created = created;
        this.changed = changed;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public ZenEntities getType()
    {
        return type;
    }

    public void setType(ZenEntities type)
    {
        this.type = type;
    }

    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }

    public UnixTimestamp getCreated()
    {
        return created;
    }

    public void setCreated(UnixTimestamp created)
    {
        this.created = created;
    }

    public UnixTimestamp getChanged()
    {
        return changed;
    }

    public void setChanged(UnixTimestamp changed)
    {
        this.changed = changed;
    }
}
