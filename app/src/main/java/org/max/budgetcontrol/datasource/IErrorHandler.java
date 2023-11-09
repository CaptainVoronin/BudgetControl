package org.max.budgetcontrol.datasource;

import java.io.IOException;

import okhttp3.Call;

public interface IErrorHandler {
    void handleError(Call call, IOException e);
}
