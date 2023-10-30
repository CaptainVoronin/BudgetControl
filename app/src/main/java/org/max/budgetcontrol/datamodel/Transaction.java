package org.max.budgetcontrol.datamodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * {
 *     id:      String // UUID
 *     changed: Int    // Unix timestamp
 *     created: Int    // Unix timestamp
 *     user:    Int -> User.id
 *     deleted: Bool
 *     hold:    Bool?
 *
 *     incomeInstrument:  Int    -> Instrument.id
 *     incomeAccount:     String -> Account.id
 *     income:            Double >= 0
 *     outcomeInstrument: Int    -> Instrument.id
 *     outcomeAccount:    String -> Account.id
 *     outcome:           Double >= 0
 *
 *     tag:      [String  -> Tag.id]?
 *     merchant:  String? -> Merchant.id
 *     payee:         String?
 *     originalPayee: String?
 *     comment:       String?
 *
 *     date: 'yyyy-MM-dd'
 *
 *     mcc: Int?
 *
 *     reminderMarker: String? -> ReminderMarker.id
 *
 *     opIncome:            Double? >= 0
 *     opIncomeInstrument:  Int? -> Instrument.id
 *     opOutcome:           Double? >= 0
 *     opOutcomeInstrument: Int? -> Instrument.id
 *
 *     latitude:  Double? >= -90  && <= 90
 *     longitude: Double? >= -180 && <= 180
 * }
 */

class Transaction
{
    public UUID getCategoryId()
    {
        return categoryId;
    }

    UUID categoryId;
    UUID id;

    public Date getDate()
    {
        return date;
    }

    Date date;

    public Double getAmount()
    {
        return amount;
    }

    Double amount;

    boolean isOutcome;

    static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat( "yyyy-MM-dd");
    }

    public Transaction(JSONObject obj) throws JSONException, ParseException
    {
        String buff = obj.getString( "id" );
        id = UUID.fromString( buff );

        buff = obj.getString( "date" );

        if( buff == null )
            throw new IllegalArgumentException( "Date can't be null" );

        date = sdf.parse( buff );

        buff = obj.getString( "income" );
        String buff1 = obj.getString( "outcome" );

        Double inc = Double.parseDouble( buff );

        Double out = Double.parseDouble( buff1 );

        isOutcome = out != 0;

        amount = isOutcome ? out : inc;

        buff = obj.getString( "tag" );

        if( buff == null )
            throw new IllegalArgumentException( "Category can't be null" );

        categoryId = UUID.fromString( buff );
    }

}
