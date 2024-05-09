package org.max.budgetcontrol.zentypes;

import org.max.budgetcontrol.datasource.ZenEntities;

import java.util.UUID;

public abstract class AZenType implements IZenType
{
    UUID id;

    String title;

    UnixTimestamp changed;
    UnixTimestamp created;

    Integer userId;

    ZenEntities entity;

    public AZenType(UUID id, String title, Long created, Long changed, Integer userId, ZenEntities entity)
    {
        this.id = id;
        this.title = title;
        this.changed = new UnixTimestamp(changed);
        this.created = new UnixTimestamp( created );
        this.userId = userId;
        this.entity = entity;
    }

    @Override
    public UUID getId()
    {
        return id;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public Integer getUserId()
    {
        return userId;
    }

    @Override
    public UnixTimestamp changed()
    {
        return changed;
    }

    public UnixTimestamp created()
    {
        return created;
    }

    @Override
    public ZenEntities getType()
    {
        return entity;
    }
}
