package org.max.budgetcontrol.charts.ui.charts;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.TreeDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface.OnClickListener;
import com.anychart.charts.TreeMap;
import com.anychart.core.ui.Title;
import com.anychart.enums.SelectionMode;
import com.anychart.enums.TreeFillingMethod;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import org.max.budgetcontrol.R;
import org.max.budgetcontrol.charts.ChartActivity;
import org.max.budgetcontrol.charts.IDataListener;
import org.max.budgetcontrol.databinding.FragmentChartBinding;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.Transaction;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChartFragment extends Fragment implements IDataListener
{

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    private FragmentChartBinding binding;

    ChartActivity chartActivity;
    private List<Category> categories;
    private List<Transaction> transactions;
    private View root;

    public ChartFragment(ChartActivity chartActivity)
    {
        this.chartActivity = chartActivity;
    }

    public static ChartFragment newInstance(ChartActivity chartActivity, int index)
    {
        ChartFragment fragment = new ChartFragment(chartActivity);
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null)
        {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }

        chartActivity.setDataReceiveListener(this);
        pageViewModel.setIndex(index);
    }

    private void makeChart()
    {
        WidgetParams wp = chartActivity.getCurrentWidget();

        List<DataEntry> data = new ArrayList<>();
        List<Pair<Category, Double>> groups = chartActivity.getGroups();

        String rootKey = wp.getTitle();
        data.add(new CustomTreeDataEntry(rootKey, null, rootKey));
        Double min;
        Double max;
        List<Pair<Category, Double>> nonZero =  groups.stream().filter( group-> group.second.intValue() != 0 ).collect(Collectors.toList());
        min = nonZero.stream().map(item -> item.second).min(Comparator.comparingDouble(a -> a)).get();
        max = nonZero.stream().map(item -> item.second).max(Comparator.comparingDouble(a -> a)).get();
        Integer[] intervals = getIntervals( min, max );
        String[] colorScale = getColorScale( intervals );

        for (Pair<Category, Double> pair : groups)
        {
            Category c = pair.first;
            String parent = c.getParent() == null ? rootKey : categories.stream()
                    .filter(item -> item.getId().equals(c.getParent()))
                    .findFirst()
                    .get()
                    .getName();
            System.out.println(c.getName() + " " + parent + " " + c.getName() + " " + pair.second.intValue());
            data.add(new CustomTreeDataEntry(c.getName(), parent, c.getName(), pair.second.intValue(), c));
        }

        TreeMap treeMap = AnyChart.treeMap();
        treeMap.data(data, TreeFillingMethod.AS_TABLE);

        Title title = treeMap.title();
        title.text(wp.getTitle());
        title.enabled(true);
        title.useHtml(true);
        title.padding(0d, 0d, 20d, 0d);

        treeMap.colorScale().colors(new String[]{
                "#ffee58", "#fbc02d", "#f57f17", "#c0ca33", "#689f38", "#2e7d32"
        });
        treeMap.colorScale().ranges( colorScale );
        treeMap.padding(10d, 10d, 10d, 20d);
        treeMap.maxDepth(2d);
        treeMap.hovered().fill("#bdbdbd", 1d);
        treeMap.selectionMode(SelectionMode.NONE);
        treeMap.setOnClickListener(new OnClickListener(new String[]{"category"})
        {
            @Override
            public void onClick(Event event)
            {
                String uuid = event.getData().get("category");
                chartActivity.bringTransactionsFragment(uuid);
            }
        });

        /*treeMap.legend().enabled(true);
        treeMap.legend()
                .padding(0d, 0d, 0d, 20d)
                .position(Orientation.RIGHT)
                .align(Align.TOP)
                .itemsLayout(LegendLayout.VERTICAL); */

        treeMap.labels().useHtml(true);
        treeMap.labels().fontColor("#212121");
        treeMap.labels().fontSize(12d);
        treeMap.labels().format(
                "function() {\n" +
                        "      return this.getData('product') + ' ' + anychart.format.number(this.value, {groupsSeparator: ' '});\n" +
                        "    }");

        treeMap.headers().format(
                "function() {\n" +
                        "    return this.getData('product')\n" +
                        "  }");

        treeMap.tooltip()
                .useHtml(true)
                .titleFormat("{%product}")
                .format("function() {\n" +
                        "      return '<span style=\"color: #bfbfbf\"></span>₽' +\n" +
                        "        anychart.format.number(this.value, {\n" +
                        "          groupsSeparator: ' '\n" +
                        "        });\n" +
                        "    }");

        AnyChartView anyChartView = root.findViewById(R.id.any_chart_view);
        anyChartView.setChart(treeMap);
    }

    private String[] getColorScale(Integer[] intervals)
    {
        if( intervals.length == 1 )
        {
            return new String[]{String.format( "{ less: %d }", intervals[0] )};
        }
        else
        {
            String[] scales = new String[6];
            scales[0] = String.format("{ less: %d }", intervals[0]);
            scales[1] = String.format("{ from: %d, to: %d }", intervals[0], intervals[1]);
            scales[2] = String.format("{ from: %d, to: %d }", intervals[1], intervals[2]);
            scales[3] = String.format("{ from: %d, to: %d }", intervals[2], intervals[3]);
            scales[4] = String.format("{ from: %d, to: %d }", intervals[3], intervals[4]);
            scales[5] = String.format("{ greater: %d }", intervals[4]);
            return scales;
        }
    }

    private Integer[] getIntervals( Double min, Double max)
    {
        int delta = max.intValue() - min.intValue();
        int step = delta / 6;
        if( step == 0 )
        {
            return new Integer[]{ max.intValue() *2 };
        }
        Integer[] intervals = new Integer[6];
        int current = min.intValue() + step;
        for( int i = 0; i < 5; i++ )
        {
            intervals[i] = current;
            current += step;
        }
        return intervals;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        binding = FragmentChartBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCategoriesReceived(List<Category> categories)
    {
        this.categories = categories;
    }

    @Override
    public void onTransactionsReceived(List<Transaction> transactions)
    {
        this.transactions = transactions;
        makeChart();
    }

    private class CustomTreeDataEntry extends TreeDataEntry
    {
        CustomTreeDataEntry(String id, String parent, String product, Integer value, Category category)
        {
            super(id, parent, value);
            setValue("product", product);
            setValue("category", category.getId().toString());
        }

        CustomTreeDataEntry(String id, String parent, String product)
        {
            super(id, parent);
            setValue("product", product);
            setValue("category", "");
        }
    }
}