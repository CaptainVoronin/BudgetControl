package org.max.budgetcontrol.datasource;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import okhttp3.Response;

public abstract class AZenClientResponseHandler
{
    private UUID tag;

    private ZenMoneyClient client;

    abstract public void onNon200Code(@NonNull Response response);

    abstract public void onResponseReceived(@NonNull JSONObject jObject) throws JSONException;

    abstract public void processError(@NonNull Exception e);

    public final void setRequestTag(@NonNull ZenMoneyClient client, @NonNull UUID tag)
    {
        this.tag = tag;
        this.client = client;
    }

    public final @NonNull UUID getRequestTag()
    {
        return tag;
    }

    public void cancelRequest()
    {
        client.cancel(tag);
    }
}
