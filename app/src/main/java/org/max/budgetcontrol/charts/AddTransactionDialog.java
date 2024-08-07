package org.max.budgetcontrol.charts;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.jetbrains.annotations.NotNull;
import org.max.budgetcontrol.R;
import org.max.budgetcontrol.zentypes.Account;
import org.max.budgetcontrol.zentypes.Category;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddTransactionDialog extends Dialog
{

    private final ParamsCompletionListener listener;
    private final List<Account> accounts;
    Double amount;
    String comment;
    Account account;
    Category category;

    UUID favoriteAccount;

    EditText edAmount;
    EditText edComment;

    Button btnOk;

    public Double getAmount()
    {
        return amount;
    }

    public String getComment()
    {
        return comment;
    }

    public Account getAccount()
    {
        return account;
    }

    public Category getCategory()
    {
        return category;
    }

    public AddTransactionDialog(@NonNull Context context, @NonNull Category category,
                                @NonNull List<Account> accounts, @Nullable UUID favoriteAccount,
                                @NonNull ParamsCompletionListener listener)
    {
        super(context);
        assert category != null : "Category can't be null";
        assert listener != null : "Listener can't be null";
        assert accounts != null : "Account list can't be null";
        assert accounts.size() != 0 : "Account list can't be empty";

        this.category = category;
        this.favoriteAccount = favoriteAccount;
        this.accounts = accounts;
        this.listener = listener;
        sortAccounts();
        if (favoriteAccount != null)
        {
            Account favAcc = accounts.stream()
                    .filter(acc -> acc.getId().equals(favoriteAccount))
                    .findFirst().get();
            accounts.remove(favAcc);
            accounts.add(0, favAcc);
        }
    }

    private void sortAccounts()
    {
        Collator collator = Collator.getInstance(getContext().getResources().getConfiguration().getLocales().get(0));
        Collections.sort(accounts, (first, second) -> collator.compare(first.getTitle(), second.getTitle()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle(category.getTitle());
        setContentView(R.layout.dlg_add_transaction);
        edAmount = findViewById(R.id.ed_transaction_amount);
        edAmount.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                String text = editable.toString();
                try
                {
                    amount = Double.parseDouble(text);
                    btnOk.setEnabled(checkParamsComplete());
                } catch (NumberFormatException e)
                {
                    amount = null;
                }
            }
        });

        edComment = findViewById(R.id.ed_transaction_comment);

        Spinner spinner = findViewById(R.id.listAccounts);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                account = accounts.get(i);
                btnOk.setEnabled(checkParamsComplete());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                btnOk.setEnabled(false);
            }
        });
        ArrayAdapter<Account> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, accounts);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        Button btn = findViewById(R.id.btnOk);
        btn.setOnClickListener(view -> {
            if (onOk())
                dismiss();
        });
        btnOk = btn;
        btnOk.setEnabled(false);

        btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(view -> {
            dismiss();
        });
    }

    private boolean checkParamsComplete()
    {
        return amount != null &&
                amount > 0D &&
                account != null;
    }

    private boolean onOk()
    {
        String comment = edComment.getText().toString();
        if (comment != null)
            comment = comment.trim();
        listener.setTransactionParams(amount, comment, category, account);
        return true;
    }

    public interface ParamsCompletionListener
    {
        void setTransactionParams(@NonNull Double amount, @Nullable String comment,
                                  @NotNull Category category, @NonNull Account account);
    }
}
