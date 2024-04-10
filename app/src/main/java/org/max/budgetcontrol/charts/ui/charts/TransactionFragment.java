package org.max.budgetcontrol.charts.ui.charts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.max.budgetcontrol.R;
import org.max.budgetcontrol.charts.ChartActivity;
import org.max.budgetcontrol.zentypes.Transaction;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A fragment representing a list of Items.
 */
public class TransactionFragment extends Fragment
{

    private final ChartActivity chartActivity;

    List<Transaction> transactions;
    private View root;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransactionFragment(ChartActivity chartActivity)
    {
        this.chartActivity = chartActivity;
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TransactionFragment newInstance(ChartActivity chartActivity, int columnCount)
    {
        TransactionFragment fragment = new TransactionFragment(chartActivity);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_transaction_item_list, container, false);
        return root;
    }

    public void setCategoryId(String uuidString )
    {
        List<Transaction> filtered;
        if( uuidString != null )
        {
            UUID uuid = UUID.fromString(uuidString);
            List<Transaction> transactions = chartActivity.getTransactions();
            filtered = transactions.stream()
                    .filter(t -> t.getCategories().contains(uuid))
                    .collect(Collectors.toList());
            filtered = filtered.stream().sorted(Comparator.comparingLong(Transaction::getTimestamp)).collect(Collectors.toList());
        }
        else
            filtered = transactions;
        fillList( filtered );
    }

    private void fillList(List<Transaction> filtered)
    {
        ListView lv = root.findViewById( R.id.listTransactions );
        lv.setAdapter( new TransactionListAdapter( chartActivity, filtered ) );
    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            onResume();
        }
    }

}