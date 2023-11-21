package org.max.budgetcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.max.budgetcontrol.zentypes.StartPeriodEncoding;

public class StartPeriodSpinAdapter extends ArrayAdapter<String> {

    String mWeek;
    String mMonth;

    String mYear;

    StartPeriodEncoding[] codes;

    Context context;
    public StartPeriodSpinAdapter(@NonNull Context context) {
        super(context, R.layout.period_spinner_item, R.id.tvPeriodName );
        this.context = context;
        init(  );
    }

    private void init() {
        mWeek = context.getString( R.string.week_code );
        mMonth = context.getString( R.string.month_code );
        mYear = context.getString( R.string.year_code );
        codes = new StartPeriodEncoding[] {StartPeriodEncoding.week,
                                           StartPeriodEncoding.month,
                                           StartPeriodEncoding.year };
    }

    String getPeriodLocalizedName( StartPeriodEncoding item )
    {
        switch ( item )
        {
            case month:
                return mMonth;
            case week:
                return mWeek;
            case year:
                return mYear;
            default:
                return mMonth;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return getPeriodLocalizedName( codes[position] );
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        LinearLayout ll = ( LinearLayout)  super.getDropDownView(position, convertView, parent);
        TextView tv = ll.findViewById( R.id.tvPeriodName );
        tv.setText(getPeriodLocalizedName( codes[position] ));
        return ll;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        View v = super.getView(position, view, parent );
        TextView tv = v.findViewById(R.id.tvPeriodName);
        tv.setText(getPeriodLocalizedName( codes[position] ));
        tv.setTag( codes[position] );
        return tv;
    }

}
