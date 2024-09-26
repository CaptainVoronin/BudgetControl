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
import java.util.List;

import androidx.annotation.NonNull;

class TransactionListAdapter extends ArrayAdapter<Transaction>
{
    private final SimpleDateFormat sdf;

    boolean showDetails;

    public TransactionListAdapter(@NonNull Context context, List<Transaction> items,boolean showDetails )
    {
        super(context, R.layout.transaction_list_item, items);
        sdf = new SimpleDateFormat("dd E");
        this.showDetails = showDetails;
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

        TextView tv = view.findViewById(R.id.tvDate);
        tv.setText(sdf.format(tr.getDate()));

        tv = view.findViewById(R.id.tvAmount);
        tv.setText("" + -1 * tr.getAmount());

        tv = view.findViewById(R.id.tvComment);
        if (showDetails)
            tv.setText(tr.getComment() != null ? tr.getComment() : "...");

        tv.setVisibility(showDetails ? View.VISIBLE : View.GONE);

        return view;
    }

    public void showDetails(boolean flag)
    {
        if (showDetails != flag)
        {
            showDetails = flag;
            notifyDataSetInvalidated();
        }
    }
}
