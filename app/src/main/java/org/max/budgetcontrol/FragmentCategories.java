package org.max.budgetcontrol;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.AZenClientResponseHandler;
import org.max.budgetcontrol.datasource.ResponseProcessor;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.zentypes.Category;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import okhttp3.Response;

public class FragmentCategories extends ABCFragment implements MainActivity.SettingsCompleteListener
{

    List<Category> categories;

    private CategoryLoaderHandler categoryLoaderHandler;

    AlertDialog loadCategoriesDialog;

    WidgetCategoryHolder categoryHolder;

    private View fragmentView;

    public FragmentCategories(MainActivity mainActivity)
    {
        super(mainActivity);
    }

    @Override
    public String getTitle()
    {
        return getMainActivity().getString(R.string.tab_title_categories);
    }

    @Override
    public void initListeners(WidgetParamsStateListener paramsStateListener)
    {
        super.setParamsStateListener(paramsStateListener);
        categoryHolder = new WidgetCategoryHolder(getParamsStateListener(), getMainActivity().getCurrentWidget().getCategories());
        getMainActivity().addSettingsListener( this );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {

        fragmentView = inflater.inflate(R.layout.fragment_categories, container, false);
        if (getMainActivity().getSettings().isInit())
            loadCategories();

        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadCategories()
    {
        Log.d(this.getClass().getName(), "[loadCategories]");
        categoryLoaderHandler = new CategoryLoaderHandler();
        try
        {
            ZenMoneyClient client = new ZenMoneyClient(
                    new URL(getMainActivity().getSettings().getParameterAsString("url")),
                    getMainActivity().getSettings().getParameterAsString("token"),
                    categoryLoaderHandler);
            client.getAllCategories();
            AlertDialog.Builder dlg = new AlertDialog.Builder(getMainActivity());
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dlg_load_layout, null);
            dlg.setView(dialogView);

            dlg.setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> {
                dialogInterface.dismiss();
                categoryLoaderHandler.cancelRequest();
            }));

            loadCategoriesDialog = dlg.create();
            loadCategoriesDialog.show();

        } catch (MalformedURLException e)
        {
            categoryLoaderHandler.processError(e);
        }
    }

    @Override
    public void onSettingsComplete()
    {
        loadCategories();
    }

    class CategoryLoaderHandler extends AZenClientResponseHandler
    {

        @Override
        public void onNon200Code(@NotNull Response response)
        {
            if (response.code() == 401 || response.code() == 500)
                getMainActivity().runOnUiThread(() -> {
                    loadCategoriesDialog.dismiss();
                    getMainActivity().showSettings(true);
                });
        }

        @Override
        public void onResponseReceived(JSONObject jObject) throws JSONException
        {
            List<Category> cats = ResponseProcessor.getCategory(jObject);
            final List<Category> cs = ResponseProcessor.makeCategoryTree(cats);
            getMainActivity().runOnUiThread(() -> {
                loadCategoriesDialog.dismiss();
                bringCategoryListToFront(cs);
            });
        }

        @Override
        public void processError(Exception e)
        {
            getMainActivity().runOnUiThread(() -> {
                loadCategoriesDialog.dismiss();
                if (e instanceof java.net.UnknownHostException)
                {

                    AlertDialog.Builder dlg = new AlertDialog.Builder(getMainActivity());
                    dlg.setNegativeButton(android.R.string.cancel, (dialog, i) -> {
                        dialog.dismiss();
                        getMainActivity().showSettings(true);
                    });
                    dlg.setTitle(R.string.netwotk_error);
                    dlg.setMessage(e.getMessage());
                    dlg.show();
                }
            });
        }
    }

    @Override
    public void applyEnteredValues()
    {
        getMainActivity().getCurrentWidget().setCategories(categoryHolder.cats);
    }

    private void bringCategoryListToFront(List<Category> cats)
    {
        categories = cats;
        List<Category> flatList = new ArrayList<>();
        for (Category c : categories)
        {
            flatList.add(c);
            if (c.getChild().size() != 0)
                flatList.addAll(c.getChild());
        }
        ListView lv = fragmentView.findViewById(R.id.lvCategories);

        flatList = flatList.stream().filter(c -> c.isOutcome()).collect(Collectors.toList());
        List<UUID> widgetCats = null;
        if (getMainActivity().getCurrentWidget() != null)
            widgetCats = getMainActivity().getCurrentWidget().getCategories();

        lv.setAdapter(new CategoryListViewAdapter(getContext(), this, flatList, widgetCats));
    }

    public void setSelectedCategoriesList(List<UUID> selectedList, boolean checked)
    {
        categoryHolder.set(selectedList);
    }
}