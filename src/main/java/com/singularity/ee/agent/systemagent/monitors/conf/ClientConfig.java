package com.singularity.ee.agent.systemagent.monitors.conf;

public class ClientConfig
{
    private int maxConnTotal = 1000;
    private int maxConnPerRoute = 1000;
    private boolean ignoreSslErrors = false;

    public int getMaxConnTotal()
    {
        return maxConnTotal;
    }

    public void setMaxConnTotal(int maxConnTotal)
    {
        this.maxConnTotal = maxConnTotal;
    }

    public int getMaxConnPerRoute()
    {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute)
    {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public boolean isIgnoreSslErrors()
    {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors)
    {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    @Override
    public String toString()
    {
        return "maxConnTotal=" + maxConnTotal +
               ", maxConnPerRoute=" + maxConnPerRoute +
               ", ignoreSslErrors=" + ignoreSslErrors;
    }
}
