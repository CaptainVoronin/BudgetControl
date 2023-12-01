package org.max.budgetcontrol;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.max.budgetcontrol.zentypes.WidgetParams;

class FragmentCategories extends Fragment {


    private final MainActivity mainActivity;

    public FragmentCategories(MainActivity mainActivity )
    {
        this.mainActivity = mainActivity;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_categories, container, false);
    }
}