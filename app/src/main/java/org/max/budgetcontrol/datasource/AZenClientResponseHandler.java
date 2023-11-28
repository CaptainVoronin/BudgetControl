package org.max.budgetcontrol.datasource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import okhttp3.Response;

public abstract class AZenClientResponseHandler
{
    private UUID tag;

    private ZenMoneyClient client;

    abstract public void onNon200Code(Response responze);

    abstract public void onResponseReceived(JSONObject jObject) throws JSONException;

    abstract public void processError(Exception e);

    public final void setRequestTag(ZenMoneyClient client, UUID tag)
    {
        this.tag = tag;
        this.client = client;
    }

    public final UUID getRequestTag()
    {
        return tag;
    }

    public void canceRequest()
    {
        client.cancel(tag);
    }
}
