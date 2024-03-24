package org.max.preditor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.max.preditor.editors.IPropertyEditor;
import org.max.preditor.editors.ITypeConverter;
import org.max.preditor.editors.PropertyEditorColor;

import java.util.List;

public class PropertyAdapterColor extends APropertyAdapter<Color>
{
    Color colorValue;

    public PropertyAdapterColor(Context context, int layoutId, String key, String header, Color value, List<Object> items, int default_value_index)
    {
        super(context, layoutId, key, header, value, items, default_value_index);
    }

    @Override
    public View getView()
    {
        if (view == null)
        {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            view = inflater.inflate(getLayoutId(), null);
            TextView tv = view.findViewWithTag(PROPERTY_HEADER_TAG);
            tv.setText(getHeader());
            ImageView iv = view.findViewWithTag(PROPERTY_COLOR_PATCH);
            iv.setOnClickListener(this);
        }
        return view;
    }

    @Override
    public ITypeConverter<Color> getTypeConverter()
    {
        return value -> {
            Color color = null;
            assert value != null : "Parameter can not be null";
            if (value instanceof Integer)
                color = Color.valueOf(((Integer) value).intValue());
            else if (value instanceof String)
            {
                String strValue = (String) value;
                color = Color.valueOf(Color.parseColor(strValue));
            } else if (value instanceof Color)
                color = (Color) value;

            return color;
        };
    }

    @Override
    public boolean setValue(Color newValue)
    {
        boolean result = false;

        if (getValidator() != null)
        {
            if (getValidator().isValid(newValue))
            {
                colorValue = newValue;
                result = true;
            }
        } else
        {
            colorValue = newValue;
            result = true;
        }
        if (result)
        {
            if (getChangeListener() != null)
                getChangeListener().onChanged(colorValue);

            ImageView img = view.findViewWithTag("color_patch");

            Drawable drw =  img.getBackground();
            Log.d( "", drw.getClass().toString() );
/*


            ((ShapeDrawable)drw).getPaint().setColor(ContextCompat.getColor(mContext,R.color.colorToSet));
            drw.setColorFilter( new PorterDuffColorFilter( newValue.toArgb(), PorterDuff.Mode.MULTIPLY ) );
*/

        }
        return true;
    }

    @Override
    public IPropertyEditor<Color> getPropertyEditor(Context context)
    {
        return new PropertyEditorColor(context, getTypeConverter());
    }
}
