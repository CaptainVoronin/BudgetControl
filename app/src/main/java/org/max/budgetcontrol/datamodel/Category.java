package org.max.budgetcontrol.datamodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *  id:      String // UUID
 *     changed: Int    // Unix timestamp
 *     user:    Int  -> User.id
 *
 *     title:   String
 *     parent:  String? -> Tag.id
 *     icon:    String?
 *     picture: String?
 *     color:   Int?
 *
 *     showIncome:    Bool
 *     showOutcome:   Bool
 *     budgetIncome:  Bool
 *     budgetOutcome: Bool
 *     required:      Bool?
 */
class Category
{
   UUID id;
   String title;
   UUID parent;

   public String getTitle()
   {
      return title;
   }

   public Category(@NonNull UUID id, @NonNull String title, UUID parent )
   {
      this.id = id;
      this.title = title;
      this.parent = parent;
   }

   public Category( JSONObject obj ) throws JSONException, IllegalArgumentException
   {
      String buff = obj.getString( "id" );
      id = UUID.fromString( buff );
      title = obj.getString( "title" );
      if( title == null && title.length() == 0 )
         throw new IllegalArgumentException( "Title can't be null" );
      buff = obj.getString( "parent" );

      if( buff != null )
         parent = UUID.fromString( buff );
   }

   public UUID getParent()
   {
      return parent;
   }

   public UUID getId()
   {
      return id;
   }

   @Override
   public boolean equals(@Nullable Object obj)
   {
      if( obj == null ) return false;
      if( obj instanceof Category )
        return ((Category) obj).getId().equals( this.id );
      else if( obj instanceof UUID )
       return ((UUID) obj).equals( this.id );
      else
         return false;
   }
}
