package org.max.budgetcontrol;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public abstract class ValueChangeListener {
    EditText edit;
    protected WidgetParamsStateListener paramsStateListener;
    IValueChecker checker;
    public ValueChangeListener(EditText edit, WidgetParamsStateListener paramsStateListener, IValueChecker checker )
    {
        this.edit = edit;
        this.paramsStateListener = paramsStateListener;
        this.checker = checker;
        this.edit.addTextChangedListener( new TextChangeListener() );
    }

    class TextChangeListener implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            valueChanged( checker.check ( charSequence ) );
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    protected abstract void valueChanged( boolean checkResult );
}
