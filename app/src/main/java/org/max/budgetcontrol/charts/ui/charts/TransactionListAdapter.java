package org.max.budgetcontrol.charts.ui.charts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.max.budgetcontrol.R;
import org.max.budgetcontrol.zentypes.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

class TransactionListAdapter extends ArrayAdapter<Transaction>
{
   private final SimpleDateFormat sdf;

   public TransactionListAdapter(@NonNull Context context, List<Transaction> items)
   {
      super(context, R.layout.transaction_list_item, items);
      sdf = new SimpleDateFormat("dd E HH:mm");
   }

   @Override
   public View getView(int i, View view, ViewGroup viewGroup)
   {
      Transaction tr = super.getItem(i);
      if (view == null)
      {
         LayoutInflater infalInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         view = infalInflater.inflate(R.layout.transaction_list_item, null);
      }

      TextView tv = view.findViewById( R.id.tvDate );
      tv.setText( sdf.format( tr.getDate() ) );

      tv = view.findViewById( R.id.tvAmount );
      tv.setText( "" + -1 * tr.getAmount() );

      return view;
   }


}
