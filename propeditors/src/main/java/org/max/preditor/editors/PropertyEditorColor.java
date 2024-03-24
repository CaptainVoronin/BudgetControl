package org.max.preditor.editors;

import android.content.Context;
import android.graphics.Color;

public class PropertyEditorColor extends APropertyEditor<Color> {
    public PropertyEditorColor(Context context, ITypeConverter<Color> converter) {
        super(context, converter);
    }

    @Override
    public void show() {
        ColorDialog dialog = new ColorDialog(getContext());
        dialog.setPositiveButtonListener( (color)->{
            if( getListener()!= null )
                getListener().onOkPressed( color );
        });
        if( getValue()!= null )
            dialog.setSelectedColor( getValue().toArgb() );
        else
            dialog.setSelectedColor( Color.BLACK );
        dialog.show();
    }
}
