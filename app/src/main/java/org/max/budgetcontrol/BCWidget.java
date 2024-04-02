package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.max.budgetcontrol.datasource.UpdateSelectedWidgetsHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of App Widget functionality.
 */
public class BCWidget extends AppWidgetProvider
{
    String token;
    URL url;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        String strIds = Arrays.stream(appWidgetIds).mapToObj(id -> Integer.toString(id)).collect(Collectors.joining(","));
        Log.i(this.getClass().getName(), "[onUpdate] Widgets update. ID list " + strIds);
        try
        {
            initApp(context);
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        } catch (JSONException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        BCDBHelper db = BCDBHelper.getInstance(context);
        List<WidgetParams> widgets = db.getWidgets(appWidgetIds);

        // При создании виджета его ID уже существует,
        // а сам виджет еще нет. Его нет и в БД.
        // Соответственно, и обновлять пока нечего.
        // Поэтому просто выходим
        if (widgets.size() == 0)
        {
            Log.i(this.getClass().getName(), "[onUpdate] Nothing to update. Exit function");
            return;
        }

        StartPeriodEncoding period = StartPeriodEncoding.year;

        Collections.sort(widgets, (a, b) -> Integer.compare(a.getStartPeriod().number(), b.getStartPeriod().number()));
        StartPeriodEncoding maxPeriod = widgets.get(widgets.size() - 1).getStartPeriod();
        long timestamp = AWidgetViewMaker.calculateStartDate(maxPeriod);

        UpdateSelectedWidgetsHandler handler = new UpdateSelectedWidgetsHandler(context, appWidgetManager, appWidgetIds, widgets);
        ZenMoneyClient client = new ZenMoneyClient(url, token, handler);
        client.loadTransactions(timestamp/1000L);
    }

    private void updateWithError(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, JSONException e)
    {

    }

    @Override
    public void onEnabled(Context context)
    {
    }

    @Override
    public void onDeleted(Context context, int[] appIds)
    {
        BCDBHelper db = BCDBHelper.getInstance(context);
        db.deleteWidgetsByAppId(appIds);
        super.onDeleted(context, appIds);
    }

    private void initApp(Context context) throws JSONException, IOException
    {
        SettingsHolder settingsHolder = new SettingsHolder(context);
        settingsHolder.init();
        token = settingsHolder.getParameterAsString("token");
        String strURL = settingsHolder.getParameterAsString("url");
        url = new URL(strURL);
    }

    @Override
    public void onDisabled(Context context)
    {
        BCDBHelper bcdbHelper = BCDBHelper.getInstance(context);
        bcdbHelper.clearWidgets();
    }

}