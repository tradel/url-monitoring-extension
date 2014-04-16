package com.singularity.ee.agent.systemagent.monitors.conf;

public class DefaultSiteConfig
{
    private String method = "HEAD";
    private int socketTimeout = 30000;
    private int connectTimeout = 30000;
    private boolean redirectsAllowed = true;
    private int maxRedirects = 10;
    private int numAttempts = 1;

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public int getSocketTimeout()
    {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout)
    {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public boolean isRedirectsAllowed()
    {
        return redirectsAllowed;
    }

    public void setRedirectsAllowed(boolean redirectsAllowed)
    {
        this.redirectsAllowed = redirectsAllowed;
    }

    public int getMaxRedirects()
    {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects)
    {
        this.maxRedirects = maxRedirects;
    }

    public int getNumAttempts()
    {
        return numAttempts;
    }

    public void setNumAttempts(int numAttempts)
    {
        this.numAttempts = numAttempts;
    }

    @Override
    public String toString()
    {
        return "method='" + method + '\'' +
               ", socketTimeout=" + socketTimeout +
               ", connectTimeout=" + connectTimeout +
               ", redirectsAllowed=" + redirectsAllowed +
               ", maxRedirects=" + maxRedirects;
    }
}
