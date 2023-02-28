package co.il.safevote;

public class AzureCreds
{
    private String endPoint;
    private String apiKey;

    public AzureCreds(String endPoint, String apiKey)
    {
        this.endPoint = endPoint;
        this.apiKey = apiKey;
    }

    public String getEndPoint()
    {
        return this.endPoint;
    }

    public String getApiKey()
    {
        return this.apiKey;
    }

    public void setEndPoint(String endPoint)
    {
        this.endPoint = endPoint;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public AzureCreds()
    {
        //required
    }
}
