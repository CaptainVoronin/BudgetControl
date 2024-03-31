package org.max.budgetcontrol.charts;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.AWidgetViewMaker;
import org.max.budgetcontrol.R;
import org.max.budgetcontrol.SettingsHolder;
import org.max.budgetcontrol.charts.ui.charts.ChartFragment;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Response;

import static org.max.budgetcontrol.charts.ui.charts.SectionsPagerAdapter.CHART_FRAGMENT_INDEX;
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
    private List<Category> categories;
    private List<Pair<Category, Double>> groups;
    private List<IDataListener> dataListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        settings = new SettingsHolder(getApplicationContext());
        settings.init();

        binding = ActivityChartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        Intent intent = getIntent();

        // Создать или открыть БД
        db = BCDBHelper.getInstance(getApplicationContext());

        Bundle extras = intent.getExtras();
        dataListeners = new ArrayList<>();
        Log.d(ChartActivity.class.getName(), "[onCreate]");
        if (extras != null)
        {
            int appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            currentWidget = db.loadWidgetParamsByAppId(appWidgetId);
            loadData();
        } else
        {

            /*currentWidget = new WidgetParams();
            currentWidget.setAppId(AppWidgetManager.INVALID_APPWIDGET_ID);*/
        }
    }

    private void loadTransactions()
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

        }
    }

    public void setTransactions(List<Transaction> transactions)
    {
        this.transactions = transactions;
        makeGroups();
        dataListeners.stream().forEach(listener -> listener.onTransactionsReceived(transactions));
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
            groups.add(new Pair<>(c, -1 * summ));

            if (c.getChild().size() != 0)
            {
                for (Category subC : c.getChild())
                {
                    summ = transactions.stream()
                            .filter(t -> t.getCategories().contains(subC.getId()))
                            .mapToDouble(t -> t.getAmount())
                            .sum();
                    if (summ.intValue() != 0)
                        groups.add(new Pair<>(subC, -1 * summ));
                }
            }
        }
    }

    public void setDataReceiveListener(IDataListener dataReceiveListener)
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
                List<Category> cats = ResponseProcessor.makeCategoryTree(flatCats);
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

    public void setActivePage(int index)
    {
        viewPager.setCurrentItem(index);
    }

    private void setCategories(List<Category> cats)
    {
        this.categories = cats;
        List<UUID> widgetCats = currentWidget.getCategories();
        categories = categories.stream().filter(cat -> widgetCats.contains(cat.getId()))
                .collect(Collectors.toList());
        dataListeners.stream().forEach(listener -> listener.onCategoriesReceived(categories));
    }

    public List<Transaction> getTransactions()
    {
        return transactions;
    }

    public List<Category> getCategories()
    {
        return categories;
    }

    public List<Pair<Category, Double>> getGroups()
    {
        return groups;
    }
}