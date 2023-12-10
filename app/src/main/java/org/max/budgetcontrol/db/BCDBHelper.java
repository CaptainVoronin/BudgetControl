package org.max.budgetcontrol.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.util.Log;

import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BCDBHelper
{
    private Context context;
    private BCDB bcdb;
    private SQLiteDatabase db;

    private static BCDBHelper instance;

    final String select = "select id, " +
            "app_id, " +
            "limit_amount, " +
            "start_period, " +
            "title, " +
            "current_amount," +
            "title_bg_color, " +
            "title_text_color, " +
            "amount_bg_color, " +
            "amount_text_color, " +
            "period_bg_color, " +
            "period_text_color " +
            "from widget ";

    protected BCDBHelper(Context context)
    {
        this.context = context;
    }

    public static BCDBHelper getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new BCDBHelper(context);
            instance.open();
        }
        return instance;
    }

    private void open() throws SQLiteException
    {
        String startPragma = "PRAGMA foreign_keys = ON;";
        bcdb = new BCDB(context);
        db = bcdb.getWritableDatabase();
        db.rawQuery(startPragma, null);

    }

    public WidgetParams loadWidgetParamsByAppId(Integer appId)
    {
        assert db != null : "Database is not opened";

        WidgetParams wp = null;
        String queryWidget = select + "where app_id = ?";
        String queryCats = "select category_id from widget_cats where widget_id = ?";

        Cursor crs = db.rawQuery(queryWidget, new String[]{appId.toString()});

        if (crs.getCount() != 0)
        {
            crs.moveToNext();
            wp = createFromCursor(crs);
            crs.close();

            crs = db.rawQuery(queryCats, new String[]{Integer.toString(wp.getId())});
            List<UUID> cats = new ArrayList<>();

            while (crs.moveToNext())
            {
                String strStartPeriod = crs.getString(0);
                cats.add(UUID.fromString(strStartPeriod));
            }
            crs.close();
            wp.setCategories(cats);

        }
        return wp;
    }

    public WidgetParams loadWidgetParamsById(Integer id)
    {
        assert db != null : "Database is not opened";

        WidgetParams wp = null;
        String queryWidget = select + "where id = ?";
        String queryCats = "select category_id from widget_cats where widget_id = ?";

        Cursor crs = db.rawQuery(queryWidget, new String[]{id.toString()});

        if (crs.getCount() != 0)
        {
            crs.moveToNext();
            wp = createFromCursor(crs);
            crs.close();

            crs = db.rawQuery(queryCats, new String[]{Integer.toString(wp.getId())});
            List<UUID> cats = new ArrayList<>();

            while (crs.moveToNext())
            {
                String strStartPeriod = crs.getString(0);
                cats.add(UUID.fromString(strStartPeriod));
            }
            crs.close();
            wp.setCategories(cats);

        }
        return wp;
    }

    public WidgetParams insertWidgetParams(WidgetParams wp)
    {
        Log.d(this.getClass().getName(), "[insertWidgetParams]");
        assert db != null : "Database is not opened";

        ContentValues cv = makeContentValues( wp );

        db.beginTransaction();

        long id = db.insert(BCDB.TABLE_WIDGET, null, cv);
        cv.clear();

        List<UUID> cats = wp.getCategories();

        for (UUID uuid : cats)
        {
            cv.put("widget_id", id);
            cv.put("category_id", uuid.toString());
            db.insert(BCDB.TABLE_WIDGET_CATS, null, cv);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        wp.setId((int) id);
        return wp;
    }

    public void updateWidgetParams(WidgetParams wp)
    {
        Log.d(this.getClass().getName(), "[updateWidgetParams]");

        ContentValues cv = makeContentValues( wp ); //

        db.beginTransaction();

        int updated = db.update(BCDB.TABLE_WIDGET, cv, "id=" + wp.getId(), null);
        cv.clear();
        Log.d(this.getClass().getName(), "[updateWidgetParams] Updated " + updated);

        List<UUID> cats = wp.getCategories();

        db.delete(BCDB.TABLE_WIDGET_CATS, "widget_id=?", new String[]{Integer.toString(wp.getId())});

        for (UUID uuid : cats)
        {
            cv.put("widget_id", wp.getId());
            cv.put("category_id", uuid.toString());
            db.insert(BCDB.TABLE_WIDGET_CATS, null, cv);
            cv.clear();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private ContentValues makeContentValues(WidgetParams wp)
    {
        ContentValues cv = new ContentValues();
        cv.put(BCDB.TW_APP_ID, wp.getAppId());
        cv.put(BCDB.TW_LIMIT_AMOUNT, wp.getLimitAmount());
        cv.put(BCDB.TW_START_PERIOD, wp.getStartPeriod().toString());
        cv.put(BCDB.TW_TITLE, wp.getTitle());
        cv.put(BCDB.TW_CURRENT_AMOUNT, wp.getCurrentAmount());
        cv.put(BCDB.TW_LIMIT_AMOUNT, wp.getLimitAmount());
        cv.put(BCDB.TW_TITLE_TEXT_COLOR, wp.getTitleParams().getFontColor().toArgb());
        cv.put(BCDB.TW_TITLE_BGCOLOR, wp.getTitleParams().getBackColor().toArgb());
        cv.put(BCDB.TW_AMOUNT_TEXT_COLOR, wp.getAmountParams().getFontColor().toArgb());
        cv.put(BCDB.TW_AMOUNT_BGCOLOR, wp.getAmountParams().getBackColor().toArgb());
        cv.put(BCDB.TW_PERIOD_TEXT_COLOR, wp.getPeriodParams().getFontColor().toArgb());
        cv.put(BCDB.TW_PERIOD_BGCOLOR, wp.getPeriodParams().getBackColor().toArgb());
        return cv;
    }

    protected void onDestroy()
    {
        bcdb.close();
    }

    public List<WidgetParams> getAllWidgets()
    {
        Log.d(this.getClass().getName(), "[getAllWidgets]");

        assert db != null : "Database is not opened";
        final String queryAllWidgets = select;
        final String queryAllWidgetCats = "select widget_id, category_id from widget_cats";

        Cursor crs = db.rawQuery(queryAllWidgets, null);

        List<WidgetParams> widgets = new ArrayList<>(crs.getCount());

        while (crs.moveToNext())
            widgets.add(createFromCursor(crs));

        crs.close();

        crs = db.rawQuery(queryAllWidgetCats, null);
        Map<UUID, Integer> cats = new HashMap<>(crs.getCount());

        while (crs.moveToNext())
        {

            Integer widgetId = crs.getInt(0);
            UUID catId = UUID.fromString(crs.getString(1));

            cats.put(catId, widgetId);
        }
        crs.close();

        for (WidgetParams w : widgets)
        {
            Iterator<UUID> it = cats.keySet().iterator();

            while (it.hasNext())
            {
                UUID uuid = it.next();
                Integer wId = cats.get(uuid);
                if (w.getId() == wId.intValue()) w.addCategoryId(uuid);
            }
        }

        return widgets;
    }

    public List<WidgetParams> getWidgets(int[] ids)
    {
        Log.d(this.getClass().getName(), "[getWidgets] Is going to be updated " + ids.length);

        assert db != null : "Database is not opened";
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();


        String inArgs = Arrays.stream(ids).mapToObj(id -> Integer.toString(id)).collect(Collectors.joining(","));

        String queryAllWidgets = select + " where app_id in ( " + inArgs + ")";

        Cursor crs = db.rawQuery(queryAllWidgets, null);

        Log.d(this.getClass().getName(), "[getWidgets] Found in DB " + crs.getCount() + " from " + ids.length);

        List<WidgetParams> widgets = new ArrayList<>(crs.getCount());

        Log.d(this.getClass().getName(), "[getWidgets] Has been read " + crs.getCount());

        while (crs.moveToNext())
            widgets.add(createFromCursor(crs));

        crs.close();

        String strWIds = widgets.stream().map(w -> Integer.toString(w.getId())).collect(Collectors.joining(","));

        String queryAllWidgetCats = "select widget_id, category_id from widget_cats where widget_id in ("
                + strWIds + ")";

        crs = db.rawQuery(queryAllWidgetCats, null);
        Map<UUID, Integer> cats = new HashMap<>(crs.getCount());

        while (crs.moveToNext())
        {
            Integer widgetId = crs.getInt(0);
            UUID catId = UUID.fromString(crs.getString(1));

            cats.put(catId, widgetId);
        }
        crs.close();

        for (WidgetParams w : widgets)
        {
            Iterator<UUID> it = cats.keySet().iterator();

            while (it.hasNext())
            {
                UUID uuid = it.next();
                Integer wId = cats.get(uuid);
                if (w.getId() == wId.intValue()) w.addCategoryId(uuid);
            }
        }

        return widgets;
    }

    public int deleteLost(List<Integer> lost)
    {
        Log.d(this.getClass().getName(), "[deleteLost]");

        assert db != null : "Database is not opened";

        String[] strIds = new String[lost.size()];

        for (int i = 0; i < lost.size(); i++)
            strIds[i] = lost.get(i).toString();

        long count = db.delete(BCDB.TABLE_WIDGET, "id in ?", strIds);

        Log.d(this.getClass().getName(), "[deleteLost] Deleted " + count);

        return (int) count;
    }

    //TODO: Обработать исключение в транзакции
    public void clearWidgets()
    {
        Log.d(this.getClass().getName(), "[clearWidgets]");

        assert db != null : "Database is not opened";

        db.beginTransaction();
        long count = db.delete(BCDB.TABLE_WIDGET_CATS, null, null);
        db.delete(BCDB.TABLE_WIDGET, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        Log.d(this.getClass().getName(), "[clearWidgets] Deleted " + count);
    }

    WidgetParams createFromCursor(Cursor cursor)
    {
        int wID = cursor.getInt(0);
        int appId = cursor.getInt(1);
        double limit = cursor.getDouble(2);
        String strStartPeriod = cursor.getString(3);
        String title = cursor.getString(4);
        double currentAmount = cursor.getDouble(5);

        WidgetParams wp = new WidgetParams();
        wp.setAppId(appId);
        wp.setId(wID);
        wp.setLimitAmount(limit);
        wp.setStartPeriod(StartPeriodEncoding.valueOf(strStartPeriod));
        wp.setTitle(title);
        wp.setCurrentAmount(currentAmount);
        // title
        int bgColor = cursor.getInt(6);
        int textColor = cursor.getInt(7);
        WidgetParams.LabelParams lp = new WidgetParams.LabelParams(Color.valueOf(bgColor), Color.valueOf(textColor));
        wp.setTitleParams(lp);

        // Amount
        bgColor = cursor.getInt(8);
        textColor = cursor.getInt(9);
        lp = new WidgetParams.LabelParams(Color.valueOf(bgColor), Color.valueOf(textColor));
        wp.setAmountParams(lp);
        // Period
        bgColor = cursor.getInt(10);
        textColor = cursor.getInt(11);
        lp = new WidgetParams.LabelParams(Color.valueOf(bgColor), Color.valueOf(textColor));
        wp.setPeriodParams(lp);

        return wp;
    }

    public void deleteWidgetsByAppId(int[] ids)
    {
        assert db != null : "Database is not opened";

        String strIds = Arrays.stream(ids)
                .mapToObj(String::valueOf).collect(Collectors.joining(","));

        Log.d(this.getClass().getName(), "[deleteWidgetsByAppId] " + strIds);

        int count = db.delete(BCDB.TABLE_WIDGET, "app_id in ( ? )", new String[]{strIds});
        Log.d(this.getClass().getName(), "[deleteWidgetsByAppId] Deleted " + count);
    }
}