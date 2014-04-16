package com.singularity.ee.agent.systemagent.monitors.conf;

public class MonitorConfig
{
    private ClientConfig clientConfig;
    private DefaultSiteConfig defaultParams;
    private SiteConfig[] sites;

    public ClientConfig getClientConfig()
    {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig)
    {
        this.clientConfig = clientConfig;
    }

    public DefaultSiteConfig getDefaultParams()
    {
        return defaultParams;
    }

    public void setDefaultParams(DefaultSiteConfig defaultParams)
    {
        this.defaultParams = defaultParams;
    }

    public SiteConfig[] getSites()
    {
        return sites;
    }

    public void setSites(SiteConfig[] sites)
    {
        this.sites = sites;
    }

    public int getTotalAttemptCount()
    {
        int total = 0;
        for (SiteConfig site : sites)
        {
            total += site.getNumAttempts();
        }

        return total;
    }
}
