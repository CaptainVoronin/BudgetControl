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
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

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

public class FragmentCategories extends ABCFragment implements CompoundButton.OnCheckedChangeListener{

    Fragment[] fragments;

    List<Category> categories;

    private CategoryLoaderHandler categoryLoaderHandler;

    AlertDialog loadCategoriesDialog;

    WidgetCategoryHolder categoryHolder;
    private View fragmentView;

    BCPagerAdapter pagerAdapter;
    ViewPager viewPager;


    public FragmentCategories(MainActivity mainActivity )
    {
        super( mainActivity );
    }

    @Override
    public String getTitle() {
        return getMainActivity().getString( R.string.tab_title_categories );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if( getMainActivity().getCurrentWidget() != null )
            categoryHolder = new WidgetCategoryHolder( getMainActivity().getCurrentWidget().getCategories());
        fragmentView = inflater.inflate(R.layout.fragment_categories, container, false); 
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      /*  viewPager = view.findViewById( R.id.pager);
        BCPagerAdapter bcp = new BCPagerAdapter( getChildFragmentManager(), getMainActivity() );
        viewPager.setAdapter( bcp );*/
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
            AlertDialog.Builder dlg = new AlertDialog.Builder( getMainActivity() );
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dlg_load_layout, null);
            dlg.setView(dialogView);

            dlg.setNegativeButton( android.R.string.cancel, ((dialogInterface, i) ->{
                dialogInterface.dismiss();
                categoryLoaderHandler.cancelRequest();
            } ));

            loadCategoriesDialog = dlg.create();
            loadCategoriesDialog.show();

        } catch (MalformedURLException e)
        {
            categoryLoaderHandler.processError(e);
        }
    }

    class CategoryLoaderHandler extends AZenClientResponseHandler
    {

        @Override
        public void onNon200Code(@NotNull Response response) {
            if( response.code() == 401 || response.code() == 500 )
                getMainActivity().runOnUiThread(() -> {
                    loadCategoriesDialog.dismiss();
                    getMainActivity().showSettings(true);
                } );
        }

        @Override
        public void onResponseReceived(JSONObject jObject) throws JSONException
        {
            List<Category> cats = ResponseProcessor.getCategory(jObject);
            final List<Category> cs = ResponseProcessor.makeCategoryTree(cats);
            getMainActivity().runOnUiThread(()-> {
                loadCategoriesDialog.dismiss();
                bringCategoryListToFront(cs);
            });
        }

        @Override
        public void processError(Exception e)
        {
            getMainActivity().runOnUiThread( () -> {
                loadCategoriesDialog.dismiss();
                if (e instanceof java.net.UnknownHostException) {

                    AlertDialog.Builder dlg = new AlertDialog.Builder( getMainActivity() );
                    dlg.setNegativeButton( android.R.string.cancel, (dialog, i )->{
                        dialog.dismiss();
                        getMainActivity().showSettings(true);
                    });
                    dlg.setTitle( R.string.netwotk_error );
                    dlg.setMessage( e.getMessage() );
                    dlg.show();
                }
            });
        }
    }

    private void applyEnteredValues()
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
        ListView lv = (ListView) fragmentView.findViewById(R.id.lvCategories);

        flatList = flatList.stream().filter(c -> c.isOutcome()).collect(Collectors.toList());
        List<UUID> widgetCats = null;
        if (getMainActivity().getCurrentWidget() != null)
            widgetCats = getMainActivity().getCurrentWidget().getCategories();

        lv.setAdapter(new CategoryListViewAdapter(getContext(), this, flatList, widgetCats));
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
    {
        UUID id = (UUID) compoundButton.getTag();
        if (checked)
            categoryHolder.add(id);
        else
            categoryHolder.remove(id);
    }
}