package org.max.budgetcontrol.charts.ui.charts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.R;
import org.max.budgetcontrol.charts.AddTransactionDialog;
import org.max.budgetcontrol.charts.ChartActivity;
import org.max.budgetcontrol.charts.IDataListener;
import org.max.budgetcontrol.charts.NewTransactionActivity;
import org.max.budgetcontrol.datasource.AZenClientResponseHandler;
import org.max.budgetcontrol.datasource.ResponseProcessor;
import org.max.budgetcontrol.datasource.ZenEntities;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.zentypes.Account;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.Transaction;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import okhttp3.Response;

/**
 * A fragment representing a list of Items.
 */
public class TransactionFragment extends Fragment implements AddTransactionDialog.ParamsCompletionListener, IDataListener
{
    private final ChartActivity chartActivity;

    List<Transaction> transactions;
    private View root;
    UUID currentCategoryID;
    FloatingActionButton btnAddTransaction;
    private ArrayList<Account> accounts;

    Category currentCategory;

    private ActivityResultLauncher<Intent> newTransactionLauncher;

    private UUID favoriteAccount;
    private TransactionListAdapter adapter;

    public TransactionFragment(ChartActivity chartActivity)
    {
        this.chartActivity = chartActivity;
        accounts = null;
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
        chartActivity.addDataReceiveListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_transactions, container, false);

        btnAddTransaction = root.findViewById(R.id.btnAddTransaction);
        if (btnAddTransaction != null)
        {
            //  btnAddTransaction.setEnabled(false);
            btnAddTransaction.setOnClickListener(view -> showAddTransaction());
            // Ввод новой транзакции
            newTransactionLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        int code = result.getResultCode();
                        Log.i(this.getClass().getName(), "[newTransactionLauncher] NewTransactionActivity returns " + code);
                        if (code == 1)
                            chartActivity.loadTransactions();
                    });
        }

        Switch sw = root.findViewById( R.id.showDetails );
        sw.setOnCheckedChangeListener((compoundButton, checked ) -> {
            if( adapter != null )
                adapter.showDetails( checked );
        });

        return root;
    }

    private void showAddTransaction()
    {
        if (accounts == null)
            loadAccountsAndGetTransaction();
        else
            showNewTransactionActivity();
    }

    public void showNewTransactionActivity()
    {
        Intent intent = new Intent(chartActivity, NewTransactionActivity.class);
        if (currentCategory != null)
            intent.putExtra(NewTransactionActivity.CATEGORY_UUID_EXTRA, currentCategoryID.toString());
        intent.putExtra(NewTransactionActivity.CATEGORY_LIST_EXTRA, chartActivity.getCategories());
        intent.putExtra(NewTransactionActivity.ACCOUNT_LIST_EXTRA, accounts);
        intent.putExtra(NewTransactionActivity.FAVORITE_ACCOUNT_EXTRA, favoriteAccount.toString());
        newTransactionLauncher.launch(intent);
    }

    // TODO: Перенести в NewTransactionActivity
    private void loadAccountsAndGetTransaction()
    {
        try
        {
            ZenMoneyClient client = getClient(new DataLoadedHandler(ZenEntities.account,
                    () -> showNewTransactionActivity()));
            client.getAccounts();
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private UUID getFavoriteAccount(List<Transaction> trs)
    {
        final Hashtable<UUID, Integer> hits = new Hashtable<>();
        UUID favorite = null;

        for (Transaction tr : trs)
        {
            if (hits.containsKey(tr.getOutcomeAccount()))
            {
                int val = hits.get(tr.getOutcomeAccount()).intValue();
                hits.put(tr.getOutcomeAccount(), val + 1);
            } else
                hits.put(tr.getOutcomeAccount(), 1);
        }

        int max = 0;

        for (UUID id : hits.keySet())
        {
            if (hits.get(id).intValue() > max)
            {
                max = hits.get(id).intValue();
                favorite = id;
            }
        }
        return favorite;
    }

    void filterTransactionsAndFillList()
    {
        Double amount = 0d;
        List<Transaction> filtered;
        if (currentCategoryID != null)
        {
            filtered = transactions.stream()
                    .filter(t -> t.getCategories().contains(currentCategoryID))
                    .collect(Collectors.toList());
            filtered = filtered.stream().sorted(Comparator.comparing(Transaction::getDate)).collect(Collectors.toList());
        } else
        {
            filtered = transactions;
        }
        amount = filtered.stream().mapToDouble(Transaction::getAmount).sum() * -1d;
        setSubHeader(amount);
        fillList(filtered);
    }

    private void setSubHeader(Double amount)
    {
        String categoryName;
        if (currentCategory != null)
            categoryName = currentCategory.getTitle();
        else
        {
            UUID catId = chartActivity.getCurrentWidget().getCategories().get(0);
            List<Category> flatList = makeFlat(chartActivity.getCategories());
            Category cat = flatList.stream().filter(c -> c.getId().equals(catId)).findFirst().get();
            categoryName = cat.getTitle();
        }
        String header = categoryName + " " + amount;
        TextView tv = root.findViewById(R.id.tvCategoryName);
        tv.setText(header);
    }

    private List<Category> makeFlat(List<Category> categories)
    {
        List<Category> flat = new ArrayList<>();
        for (Category c : categories)
        {
            flat.add(c);
            if (c.getChild().size() != 0)
                c.getChild().stream().forEach(cc -> flat.add(cc));
        }
        return flat;
    }

    private void fillList(List<Transaction> filtered)
    {
        ListView lv = root.findViewById(R.id.listTransactions);
        adapter  = new TransactionListAdapter(chartActivity, filtered, false);
        lv.setAdapter( adapter );
    }

    // TODO: удалить
    @Override
    public void setTransactionParams(@NonNull Double amount, @Nullable String comment, @NotNull Category category, @NonNull Account account)
    {
        Date date = new Date(System.currentTimeMillis());
        Transaction tr = new Transaction(UUID.randomUUID(),
                date,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                account.getUserId(),
                amount,
                Arrays.asList(category.getId()),
                account.getId(),
                account.getInstrument(),
                comment);

        try
        {
            ZenMoneyClient client = getClient(new DataLoadedHandler(ZenEntities.transaction, () -> {
            }));
            client.sendTransactions(tr);
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private ZenMoneyClient getClient(AZenClientResponseHandler handler) throws MalformedURLException
    {
        ZenMoneyClient client = new ZenMoneyClient(new URL(chartActivity.getSettings().getParameterAsString("url")),
                chartActivity.getSettings().getParameterAsString("token"), handler);
        return client;
    }

    @Override
    public void onCategoriesReceived(List<Category> categories)
    {
        // empty
    }

    @Override
    public void onTransactionsReceived(List<Transaction> transactions)
    {
        this.transactions = transactions;
        favoriteAccount = getFavoriteAccount(this.transactions);
        filterTransactionsAndFillList();
    }

    public void setCategoryId(String uuid)
    {
        currentCategoryID = UUID.fromString( uuid );
        filterTransactionsAndFillList();
    }

    class DataLoadedHandler extends AZenClientResponseHandler
    {
        Runnable afterCall;

        public DataLoadedHandler(ZenEntities entity, Runnable afterCall)
        {
            this.entity = entity;
            this.afterCall = afterCall;
        }

        ZenEntities entity;

        @Override
        public void onNon200Code(@NonNull Response response)
        {
            int code = response.code();
            try
            {
                String body = response.body().string();
                Log.d(this.getClass().getName(), "[onNon200Code] HTTP " + code + " " + body);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onResponseReceived(@NonNull JSONObject jObject) throws JSONException
        {
            Log.d(this.getClass().getName(), "[onResponseReceived]");
            // TODO: Ветку с account удалить после перенесения в NewTransaction
            if (entity == ZenEntities.account)
                TransactionFragment.this.accountsLoaded(jObject, afterCall);
            else if (entity == ZenEntities.transaction)
                chartActivity.loadTransactions();
        }

        @Override
        public void processError(@NonNull Exception e)
        {

        }
    }

    // TODO: Удалить после перенесения в NewTransaction
    private void accountsLoaded(JSONObject jObject, Runnable afterCall)
    {
        accounts = ResponseProcessor.getAccounts(jObject);
        chartActivity.runOnUiThread(() -> afterCall.run());
    }
}