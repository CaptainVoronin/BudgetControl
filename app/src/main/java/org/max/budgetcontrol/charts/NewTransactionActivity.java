package org.max.budgetcontrol.charts;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.R;
import org.max.budgetcontrol.SettingsHolder;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.Response;

public class NewTransactionActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener
{

    public static final String ACCOUNT_LIST_EXTRA = "ACCOUNT_LIST_EXTRA";
    public static final String FAVORITE_ACCOUNT_EXTRA = "FAVORITE_ACCOUNT_EXTRA";
    public static String CATEGORY_UUID_EXTRA = "CATEGORY_UUID_EXTRA";
    public static String CATEGORY_LIST_EXTRA = "CATEGORY_LIST_EXTRA";

    Double amount;
    String comment;
    Account account;

    Category selectedCategory;

    List<Category> categories;

    private List<Account> accounts;

    EditText edAmount;
    EditText edComment;
    private SettingsHolder settings;
    private MenuItem itemSave;
    private UUID favoriteAccount;
    private AlertDialog dlgPostTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent bundle = getIntent();
        if (bundle != null)
        {
            categories = bundle.getSerializableExtra(CATEGORY_LIST_EXTRA, ArrayList.class);
            String buff = bundle.getStringExtra(FAVORITE_ACCOUNT_EXTRA);

            if (buff != null)
                favoriteAccount = UUID.fromString(buff);

            accounts = bundle.getSerializableExtra(ACCOUNT_LIST_EXTRA, ArrayList.class);
            buff = bundle.getStringExtra(CATEGORY_UUID_EXTRA);
            if (buff != null)
            {
                UUID uuid = UUID.fromString(buff);
                List<Category> list = categories.stream().map(category -> findById(uuid, category)).filter( item -> item != null ).collect(Collectors.toList());
                if (list.size() != 0)
                    selectedCategory = list.get(0);
            }

            settings = new SettingsHolder(getApplicationContext());
            settings.init();
            setupCategoryList();
        }

        edAmount = findViewById(R.id.ed_transaction_amount);

        edAmount.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                String text = editable.toString();
                try
                {
                    amount = Double.parseDouble(text);
                    checkParams();
                } catch (NumberFormatException e)
                {
                    amount = null;
                }
            }
        });

        edComment = findViewById(R.id.ed_transaction_comment);

        Spinner spinner = findViewById(R.id.listAccounts);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                account = accounts.get(i);
                checkParams();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                account = null;
                checkParams();
            }
        });

        AtomicInteger i = new AtomicInteger();
        int index = accounts.stream()
                .peek(v -> i.incrementAndGet())
                .anyMatch(acc -> acc.getId().equals(favoriteAccount)) ? // your predicate
                i.get() - 1 : -1;

        ArrayAdapter<Account> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accounts);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection( index );
    }

    private Category findById(UUID id, Category category)
    {
        if (category.getId().equals(id))
            return category;
        Optional<Category> opt = category.getChild().stream().filter(children -> children.getId().equals(id)).findFirst();
        if (opt.isPresent())
            return opt.get();
        else
            return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.action_bar_menu, menu);
        MenuItem item = menu.findItem(R.id.idCancel);
        item.setOnMenuItemClickListener(menuItem -> {
            setResult(0);
            finish();
            return false;
        });

        itemSave = menu.findItem(R.id.idSave);
        itemSave.setOnMenuItemClickListener(menuItem -> {
            comment = edComment.getText().toString();
            if (comment != null)
                comment = comment.trim();
            postNewTransaction(amount, comment, selectedCategory, account);
            return false;
        });
        checkParams();
        return true;
    }

    private void setupCategoryList()
    {
        List<Category> flatList = new ArrayList<>();
        for (Category c : categories)
        {
            flatList.add(c);
            if (c.getChild().size() != 0)
                flatList.addAll(c.getChild());
        }
        ListView lv = findViewById(R.id.lvCategories);

        flatList = flatList.stream().filter(c -> c.isOutcome()).collect(Collectors.toList());

        lv.setAdapter(new WidgetCategoryListViewAdapter(this, flatList, selectedCategory != null ? selectedCategory.getId() : null, this));
    }

    private void checkParams()
    {
        if (itemSave != null)
            itemSave.setEnabled(amount != null &&
                    amount > 0D &&
                    account != null &&
                    selectedCategory != null);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean selected)
    {
        Category cat = (Category) compoundButton.getTag();
        if (selected)
            selectedCategory = cat;
        else if (selectedCategory != null)
            if (selectedCategory.getId().equals(cat.getId()))
                selectedCategory = null;

        checkParams();
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
            Log.d(this.getClass().getName(), "[onResponseReceived] Entity " + entity);
            if (entity == ZenEntities.account)
                accountsLoaded(jObject, afterCall);
            if (entity == ZenEntities.transaction)
            {
                if( dlgPostTransaction != null )
                    dlgPostTransaction.cancel();
                runOnUiThread(() -> afterCall.run());
            }
        }

        @Override
        public void processError(@NonNull Exception e)
        {

        }
    }

    public void postNewTransaction(@NonNull Double amount, @Nullable String comment, @NotNull Category category, @NonNull Account account)
    {
        Date date = new Date( System.currentTimeMillis() );
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
                setResult(1);
                Log.i(this.getClass().getName(), "[setTransactionParams] Finish NewTransaction activity");
                finish();
            }));
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dlg_post_transaction, null);
            dlg.setView(dialogView);
            dlg.setCancelable( false );
            dlgPostTransaction = dlg.create();
            dlgPostTransaction.show();
            client.sendTransactions(tr);
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private ZenMoneyClient getClient(AZenClientResponseHandler handler) throws MalformedURLException
    {
        ZenMoneyClient client = new ZenMoneyClient(new URL(settings.getParameterAsString("url")),
                settings.getParameterAsString("token"), handler);
        return client;
    }

    private void accountsLoaded(JSONObject jObject, Runnable afterCall)
    {
        accounts = ResponseProcessor.getAccounts(jObject);
    }


}