package org.max.budgetcontrol.charts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.AWidgetViewMaker;
import org.max.budgetcontrol.R;
import org.max.budgetcontrol.SettingsHolder;
import org.max.budgetcontrol.charts.ui.charts.SectionsPagerAdapter;
import org.max.budgetcontrol.charts.ui.charts.TransactionFragment;
import org.max.budgetcontrol.databinding.ActivityChartBinding;
import org.max.budgetcontrol.datasource.AZenClientResponseHandler;
import org.max.budgetcontrol.datasource.ResponseProcessor;
import org.max.budgetcontrol.datasource.ZenEntities;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.Transaction;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Response;

import static org.max.budgetcontrol.charts.ui.charts.SectionsPagerAdapter.TRANSACTION_FRAGMENT_INDEX;

public class ChartActivity extends AppCompatActivity
{
    private ActivityChartBinding binding;
    private ViewPager viewPager;

    public WidgetParams getCurrentWidget()
    {
        return currentWidget;
    }

    WidgetParams currentWidget;
    private BCDBHelper db;
    private SettingsHolder settings;
    private List<Transaction> transactions;
    private ArrayList<Category> categories;
    private List<Pair<Category, Double>> groups;
    private List<IDataListener> dataListeners;

    public SettingsHolder getSettings()
    {
        return settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        boolean showCharts = false;
        super.onCreate(savedInstanceState);
        Log.d(ChartActivity.class.getName(), "[onCreate]");

        settings = new SettingsHolder(getApplicationContext());
        settings.init();

        binding = ActivityChartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = BCDBHelper.getInstance(getApplicationContext());

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            String action = intent.getAction();
            int appWidgetId = Integer.parseInt(action);
            currentWidget = db.loadWidgetParamsByAppId(appWidgetId);
            showCharts = currentWidget.getCategories().size() > 1;
            loadData();
        }

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), showCharts);
        viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        // Создать или открыть БД

        dataListeners = new ArrayList<>();
    }

    public void loadTransactions()
    {
        try
        {
            ZenMoneyClient client = new ZenMoneyClient(new URL(settings.getParameterAsString("url")),
                    settings.getParameterAsString("token"),
                    new DataLoadedHandler(ZenEntities.transaction));

            long timestamp = AWidgetViewMaker.calculateStartDate(currentWidget.getStartPeriod());
            client.loadTransactions(timestamp / 1000l);
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    private void loadData()
    {
        Log.d(this.getClass().getName(), "[loadCategories]");

        try
        {
            ZenMoneyClient client = new ZenMoneyClient(
                    new URL(settings.getParameterAsString("url")),
                    settings.getParameterAsString("token"),
                    new DataLoadedHandler(ZenEntities.tag));
            client.getAllCategories();

        } catch (MalformedURLException e)
        {
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }

    public void setTransactions(List<Transaction> trs)
    {
        if (trs != null && trs.size() != 0)
        {

            transactions = filterTransactions(trs);
            makeGroups();
            dataListeners.stream().forEach(listener -> listener.onTransactionsReceived(transactions));
        }
    }

    /**
     * Профильтровать транзакции. Останутся только те категории,
     * которые есть в виджете.
     *
     * @param trs
     * @return фильтрованный список транзакций
     */
    private List<Transaction> filterTransactions(List<Transaction> trs)
    {
        List<UUID> cats = this.currentWidget.getCategories();
        List<Transaction> result = new LinkedList<>();
        for (Transaction tr : trs)
            if (tr.hasCategory(cats))
                result.add(tr);
        return result;
    }

    private void makeGroups()
    {
        Double summ;
        groups = new ArrayList<>();
        for (Category c : categories)
        {
            summ = transactions.stream()
                    .filter(t -> t.getCategories().contains(c.getId()))
                    .mapToDouble(t -> t.getAmount())
                    .sum();

            if (c.getChild().size() != 0 || summ.intValue() != 0)
            {
                groups.add(new Pair<>(c, -1 * summ));
                Log.d(this.getClass().getName(), "[makeGroups] " + c.getTitle() + " " + summ);
            }

            if (c.getChild().size() != 0)
            {
                for (Category subC : c.getChild())
                {
                    summ = transactions.stream()
                            .filter(t -> t.getCategories().contains(subC.getId()))
                            .mapToDouble(t -> t.getAmount())
                            .sum();
                    if (summ.intValue() != 0)
                    {
                        groups.add(new Pair<>(subC, -1 * summ));
                        Log.d(this.getClass().getName(), "[makeGroups] " + subC.getTitle() + " " + summ);
                    }
                }
            }
        }
    }

    public void addDataReceiveListener(IDataListener dataReceiveListener)
    {
        dataListeners.add(dataReceiveListener);
    }

    class DataLoadedHandler extends AZenClientResponseHandler
    {
        ZenEntities entityKind;

        public DataLoadedHandler(ZenEntities entityKind)
        {
            this.entityKind = entityKind;
        }

        @Override
        public void onNon200Code(@NonNull Response response)
        {

        }

        @Override
        public void onResponseReceived(@NonNull JSONObject jObject) throws JSONException
        {
            if (entityKind == ZenEntities.tag)
            {
                List<Category> flatCats = ResponseProcessor.getCategory(jObject);
                ArrayList<Category> cats = ResponseProcessor.makeCategoryTree(flatCats);
                ChartActivity.this.runOnUiThread(() ->
                {

                    ChartActivity.this.setCategories(cats);
                    loadTransactions();

                });
            } else if (entityKind == ZenEntities.transaction)
            {
                try
                {
                    List<Transaction> transactions = ResponseProcessor.getTransactions(jObject);
                    ChartActivity.this.runOnUiThread(() -> ChartActivity.this.setTransactions(transactions));
                } catch (ParseException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void processError(@NonNull Exception e)
        {

        }
    }

    public void bringTransactionsFragment(String uuid)
    {
        runOnUiThread(() -> {
            TransactionFragment f = (TransactionFragment) ((SectionsPagerAdapter) viewPager.getAdapter()).getItem(TRANSACTION_FRAGMENT_INDEX);
            f.setCategoryId(uuid);
            viewPager.setCurrentItem(TRANSACTION_FRAGMENT_INDEX, true);
        });
    }

    private void setCategories(List<Category> cats)
    {

        List<UUID> widgetCats = currentWidget.getCategories();
        cats = cats.stream().filter(cat -> widgetCats.contains(cat.getId()))
                .collect(Collectors.toList());
        categories = new ArrayList<>();
        categories.addAll(cats);
        dataListeners.stream().forEach(listener -> listener.onCategoriesReceived(categories));
    }

    public List<Transaction> getTransactions()
    {
        return transactions;
    }

    public ArrayList<Category> getCategories()
    {
        return categories;
    }

    public List<Pair<Category, Double>> getGroups()
    {
        return groups;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.idSave).setVisible(false);
        menu.findItem(R.id.idSettings).setVisible(false);
        MenuItem itemAbout = menu.findItem(R.id.idAbout);
        itemAbout.setOnMenuItemClickListener(menuItem -> {
            showAboutDialog();
            return false;
        });
        return true;
    }

    private void showAboutDialog()
    {
        try
        {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            String message = "Budget control " + versionName;

            builder.setTitle(R.string.dlg_about_title)
                    .setNegativeButton(android.R.string.cancel,
                            (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton(android.R.string.ok, ((dialogInterface, i) -> {
                        updateTransactions();
                    }))
                    .setMessage(message).setIcon(R.mipmap.ic_launcher);
            builder.show();

        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            return;
        }

    }

    private void updateTransactions()
    {
        loadTransactions();
    }

}