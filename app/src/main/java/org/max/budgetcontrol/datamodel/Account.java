package org.max.budgetcontrol.datamodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * {
 *     id:         String // UUID
 *     changed:    Int    // Unix timestamp
 *     user:       Int  -> User.id
 *     role:       Int? -> User.id?
 *     instrument: Int? -> Instrument.id
 *     company:    Int? -> Company.id
 *     type: ('cash' | 'ccard' | 'checking' | 'loan' | 'deposit' | 'emoney' | 'debt')
 *     title:   String
 *     syncID: [String]?
 *
 *     balance:      Double?
 *     startBalance: Double?
 *     creditLimit:  Double? >= 0
 *
 *     inBalance:        Bool
 *     savings:          Bool?
 *     enableCorrection: Bool
 *     enableSMS:        Bool
 *     archive:          Bool
 *
 *     //Для счетов с типом отличных от 'loan' и 'deposit' в  этих полях можно ставить null
 *     capitalization: Bool
 *     percent: Double >= 0 && < 100
 *     startDate: 'yyyy-MM-dd'
 *     endDateOffset: Int
 *     endDateOffsetInterval: ('day' | 'week' | 'month' | 'year')
 *     payoffStep: Int?
 *     payoffInterval: ('month' | 'year')?
 * }
 */
class Account
{
   UUID id;
   String title;

   public Account(JSONObject obj) throws JSONException
   {
      String buff = obj.getString( "id" );
      id = UUID.fromString( buff );

      buff = obj.getString( "title" );

      if( buff == null )
         throw new IllegalArgumentException( "Date can't be null" );


   }
}
