package com.singularity.ee.agent.systemagent.monitors.conf;

import java.util.HashMap;
import java.util.Map;

public class SiteConfig extends DefaultSiteConfig
{
    private String name;
    private String url;
    private String username;
    private String password;
    private Map<String, String> headers = new HashMap<String, String>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "SiteConfig{" +
                "name='" + name + '\'' +
                '}';
    }
}
