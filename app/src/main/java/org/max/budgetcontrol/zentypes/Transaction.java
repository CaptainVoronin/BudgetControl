package org.max.budgetcontrol.zentypes;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ZenEntities;

import java.text.ParseException;
import java.util.*;

public class Transaction extends AZenType implements Comparable<Transaction>
{
    private Integer inInstrument;
    private Integer outInstrument;
    double amount;
    List<UUID> category;
    UUID outcomeAccount;

    String comment;

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public UUID getIncomeAccount()
    {
        return incomeAccount;
    }

    UUID incomeAccount;

    public UUID getOutcomeAccount()
    {
        return outcomeAccount;
    }

    public Transaction(UUID id, Long created, Long changed, Integer userId, double amount,
                       @NotNull List<UUID> category,
                       UUID outAccount,
                       Integer outInstrument,
                       UUID inAccount,
                       Integer inInstrument,
                       String comment)
    {
        super(id, "", created, changed, userId, ZenEntities.tag);
        this.amount = amount;
        this.category = category;
        this.outcomeAccount = outAccount;
        this.incomeAccount = inAccount;
        this.inInstrument = inInstrument;
        this.outInstrument = outInstrument;
        this.comment = comment;
    }

    public Transaction(UUID id, Long created, Long changed, Integer userId, double amount,
                       @NotNull List<UUID> category, UUID account, Integer instrument, String comment)
    {
        super(id, "", created, changed, userId, ZenEntities.tag);
        this.amount = amount;
        this.category = category;
        this.outcomeAccount = account;
        this.incomeAccount = account;
        this.inInstrument = instrument;
        this.outInstrument = instrument;
        this.comment = comment;
    }

    public JSONObject toJSONObject() throws JSONException
    {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(created().mills());
        JSONObject job = new JSONObject();
        for( TRANSACTION_NODES node : TRANSACTION_NODES.values() )
            job.put( node.name(), JSONObject.NULL );

        job.put(TRANSACTION_NODES.id.name(), getId());
        job.put(TRANSACTION_NODES.date.name(),
                String.format("%d-%d-%d",
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH) + 1,
                        date.get(Calendar.DAY_OF_MONTH)));

        job.put(TRANSACTION_NODES.user.name(), getUserId());
        job.put(TRANSACTION_NODES.outcomeAccount.name(), getOutcomeAccount());
        job.put(TRANSACTION_NODES.incomeAccount.name(), getIncomeAccount());
        job.put(TRANSACTION_NODES.created.name(), created().sec());
        job.put(TRANSACTION_NODES.changed.name(), changed().sec());
        job.put(TRANSACTION_NODES.income.name(), getAmount());
        job.put(TRANSACTION_NODES.outcome.name(), getAmount());
        job.put(TRANSACTION_NODES.deleted.name(), false);
        job.put(TRANSACTION_NODES.viewed.name(), true); // ?
        job.put(TRANSACTION_NODES.outcomeInstrument.name(), getOutcomeInstrument());
        job.put(TRANSACTION_NODES.incomeInstrument.name(), getIncomeInstrument());
        JSONArray jar = new JSONArray();
        getCategories().stream().forEach(cat -> jar.put(cat));
        job.put(TRANSACTION_NODES.tag.name(), jar);
        if (getComment() != null && getComment().trim().length() > 0)
            job.put("comment", getComment());
        return job;
    }

    private Integer getOutcomeInstrument()
    {
        return outInstrument;
    }

    private Integer getIncomeInstrument()
    {
        return inInstrument;
    }

    public static Transaction fromJSONObject(@NotNull JSONObject obj) throws ParseException, JSONException
    {

        UUID uuid = UUID.fromString(obj.getString("id"));
        if (obj.isNull("tag"))
        {
            return null;
        }
        JSONArray oTags = obj.getJSONArray("tag");
        List<UUID> uuids = new ArrayList<>();

        for (int i = 0; i < oTags.length(); i++)
            uuids.add(UUID.fromString(oTags.getString(i)));

        double inc = obj.getDouble(TRANSACTION_NODES.income.name());
        double out = obj.getDouble(TRANSACTION_NODES.outcome.name());
        double amount = out != 0 ? (out * -1) : inc;
        String buff = obj.getString(TRANSACTION_NODES.created.name());
        long timestamp = Long.parseLong(buff) * 1000;
        buff = obj.getString(TRANSACTION_NODES.outcomeAccount.name());
        UUID outAccount = UUID.fromString(buff);
        buff = obj.getString(TRANSACTION_NODES.incomeAccount.name());
        UUID inAccount = UUID.fromString(buff);
        Integer inInstrument = obj.getInt(TRANSACTION_NODES.incomeInstrument.name());
        Integer outInstrument = obj.getInt(TRANSACTION_NODES.outcomeInstrument.name());
        Integer userId = obj.getInt(TRANSACTION_NODES.user.name());
        Long created = obj.getLong(TRANSACTION_NODES.created.name());
        Long changed = obj.getLong(TRANSACTION_NODES.changed.name());

        String comment = obj.getString("comment");

        return new Transaction(uuid,
                created,
                changed,
                userId,
                amount,
                uuids,
                outAccount,
                outInstrument,
                inAccount,
                inInstrument,
                comment);
    }

    /*private static void getCategories(List<UUID> trTagIds, List<Category> categories, List<Category> transCats ) {
        for ( Category c : categories ) {
            if( trTagIds.contains( c.getId() ) )
                transCats.add( c );
            if( c.getChild() != null )
                getCategories( trTagIds, c.getChild(), transCats );
        }
    }*/

    public UUID getId()
    {
        return id;
    }

    public double getAmount()
    {
        return amount;
    }

    public List<UUID> getCategories()
    {
        return category;
    }
/*

    public boolean hasCategory( UUID categoryId )
    {
        return category.contains( categoryId );
    }
*/

    public boolean hasCategory(List<UUID> categoryIds)
    {
        for (UUID uuid : categoryIds)
        {
            if (category.contains(uuid))
                return true;
        }
        return false;
    }

    @Override
    public int compareTo(Transaction t)
    {
        return UnixTimestamp.compare(created, t.created());
    }

    public enum TRANSACTION_NODES
    {
        id,
        user,
        date,
        income,
        outcome,
        changed,
        incomeInstrument,
        outcomeInstrument,
        created,
        originalPayee,
        deleted,
        viewed,
        hold,
        qrCod,
        source,
        incomeAccount,
        outcomeAccount,
        tag,
        comment,
        payee,
        opIncome,
        opOutcome,
        opIncomeInstrument,
        opOutcomeInstrument,
        latitude,
        longitude,
        merchant,
        incomeBankID,
        outcomeBankID,
        reminderMarker
    }
}
