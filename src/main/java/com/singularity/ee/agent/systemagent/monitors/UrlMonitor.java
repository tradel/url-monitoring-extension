package com.singularity.ee.agent.systemagent.monitors;

import com.singularity.ee.agent.systemagent.monitors.conf.*;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.net.ssl.SSLContext;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class UrlMonitor extends AManagedMonitor
{
    private static final Log log = LogFactory.getLog(UrlMonitor.class);
    private static final String DEFAULT_CONFIG_FILE = "config.yaml";
    private static final String CONFIG_FILE_PARAM = "config-file";
    protected MonitorConfig config;

    private SchemeIOSessionStrategy createSSLStrategy(MonitorConfig config)
    {
        SchemeIOSessionStrategy strategy = null;

        try
        {
            if (config.getClientConfig()
                    .isIgnoreSslErrors())
            {
                SSLContext sslContext = SSLContexts.custom()
                        .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                        .build();

                strategy = new SSLIOSessionStrategy(sslContext, SSLIOSessionStrategy.ALLOW_ALL_HOSTNAME_VERIFIER);

            }
            else
            {
                strategy = SSLIOSessionStrategy.getDefaultStrategy();
            }
        }
        catch (Exception e)
        {
            log.error("Error creating connection manager: " + e.getMessage(), e);
        }

        return strategy;
    }

    private RequestConfig createRequestConfig(DefaultSiteConfig siteConfig)
    {
        return RequestConfig.custom()
                .setSocketTimeout(siteConfig.getSocketTimeout())
                .setConnectTimeout(siteConfig.getConnectTimeout())
                .setRedirectsEnabled(siteConfig.isRedirectsAllowed())
                .setMaxRedirects(siteConfig.getMaxRedirects())
                .build();
    }

    private CredentialsProvider createAuthWallet(MonitorConfig config)
    {
        final CredentialsProvider wallet = new BasicCredentialsProvider();

        for (SiteConfig site : config.getSites())
        {
            if (StringUtils.isNotEmpty(site.getUsername()))
            {
                try
                {
                    URL url = new URL(site.getUrl());
                    AuthScope scope = new AuthScope(url.getHost(), url.getPort());
                    Credentials credentials = new UsernamePasswordCredentials(site.getUsername(), site.getPassword());
                    wallet.setCredentials(scope, credentials);
                    log.info("Added username/password credentials for " + url.getHost() + ":" + url.getPort());
                }
                catch (MalformedURLException e)
                {
                    log.error("URL syntax is incorrect: " + site.getUrl(), e);
                }
            }
        }

        return wallet;
    }

    private CloseableHttpAsyncClient createHttpClient(MonitorConfig config)
    {
        ClientConfig clientConfig = config.getClientConfig();
        RequestConfig requestConfig = createRequestConfig(config.getDefaultParams());

        return HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(clientConfig.getMaxConnPerRoute())
                .setMaxConnTotal(clientConfig.getMaxConnTotal())
                .setSSLStrategy(createSSLStrategy(config))
                .setDefaultCredentialsProvider(createAuthWallet(config))
                .build();
    }

    public MonitorConfig readConfigFile(String filename)
    {
        log.info("Reading configuration from " + filename);

        FileReader configReader;
        try
        {
            configReader = new FileReader(filename);
        }
        catch (FileNotFoundException e)
        {
            log.error("File not found: " + filename, e);
            return null;
        }

        Yaml yaml = new Yaml(new Constructor(MonitorConfig.class));
        MonitorConfig c = (MonitorConfig) yaml.load(configReader);
        return c;
    }

    @Override
    public TaskOutput execute(Map<String, String> taskParams, TaskExecutionContext taskContext)
            throws TaskExecutionException
    {
        String configFilename = DEFAULT_CONFIG_FILE;
        if (taskParams.containsKey(CONFIG_FILE_PARAM))
        {
            configFilename = taskParams.get(CONFIG_FILE_PARAM);
        }

        config = readConfigFile(configFilename);
        if (config == null)
            return null;

        log.info("HTTP client configuration: " + config.getClientConfig());
        log.info("Default request configuration: " + config.getDefaultParams());
        log.info("Sites configured: " + config.getSites().length);

        final CloseableHttpAsyncClient httpClient = createHttpClient(config);
        final CountDownLatch latch = new CountDownLatch(config.getTotalAttemptCount());
        log.info("Sending " + latch.getCount() + " HTTP requests asynchronously");

        final ConcurrentHashMap<SiteConfig, List<SiteResult>> results = new ConcurrentHashMap<SiteConfig, List<SiteResult>>();
        for (final SiteConfig site : config.getSites())
        {
            results.put(site, Collections.synchronizedList(new ArrayList<SiteResult>()));
        }


        try
        {
            final long overallStartTime = System.currentTimeMillis();

            httpClient.start();

            for (final SiteConfig site : config.getSites())
            {
                for (int i = 0; i < site.getNumAttempts(); i++)
                {
                    final HttpUriRequest request = RequestBuilder.create(site.getMethod()
                            .toUpperCase())
                            .setUri(site.getUrl())
                            .setConfig(createRequestConfig(site))
                            .build();

                    for (Map.Entry<String, String> header : site.getHeaders()
                            .entrySet())
                    {
                        request.addHeader(header.getKey(), header.getValue());
                    }

                    log.info(String.format("Sending request %d of %d to ", (i + 1),
                            site.getNumAttempts(), request.getRequestLine()));

                    final long startTime = System.currentTimeMillis();

                    try
                    {
                        httpClient.execute(request, new FutureCallback<HttpResponse>()
                        {
//                        private String metricPath = "Custom Metrics|URL Monitor|" + site.getName();

                            private void finish(SiteResult result)
                            {
//                            getMetricWriter(metricPath + "|Status",
//                                    MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
//                                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
//                                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(status);
//                            getMetricWriter(metricPath + "|Average Response Time (ms)",
//                                    MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
//                                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
//                                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(
//                                    Long.toString(elapsedTime));
                                results.get(site).add(result);
                                log.info((latch.getCount() - 1) + " requests remaining");
                                latch.countDown();
                            }

                            @Override
                            public void completed(HttpResponse response)
                            {
                                final long elapsedTime = System.currentTimeMillis() - startTime;
                                log.info("Request completed in " + elapsedTime + " ms");

                                int statusCode = response.getStatusLine()
                                        .getStatusCode();

//                            getMetricWriter(metricPath + "|Response Code",
//                                    MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
//                                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
//                                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(
//                                    Integer.toString(statusCode));
//                            getMetricWriter(metricPath + "|Response Bytes",
//                                    MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
//                                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
//                                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(
//                                    Long.toString(response.getEntity().getContentLength()));

                                SiteResult.ResultStatus status = SiteResult.ResultStatus.SUCCESS;

                                if (statusCode == 200)
                                {
                                    log.info(request.getRequestLine() + " -> " + response.getStatusLine());
                                }
                                else
                                {
                                    log.warn(request.getRequestLine() + " -> " + response.getStatusLine());
                                    status = SiteResult.ResultStatus.ERROR;
                                }

                                finish(new SiteResult(elapsedTime, status, statusCode,
                                        response.getEntity()
                                                .getContentLength()));
                            }

                            @Override
                            public void failed(Exception e)
                            {
                                log.error(request.getRequestLine() + " -> FAILED: " + e.getMessage(), e);
                                finish(new SiteResult(0, SiteResult.ResultStatus.FAILED, 0, 0));
                            }

                            @Override
                            public void cancelled()
                            {
                                log.info(request.getRequestLine() + " -> CANCELED");
                                finish(new SiteResult(0, SiteResult.ResultStatus.CANCELED, 0, 0));
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        log.error("Error sending request to " + site.getUrl(), e);
                        latch.countDown();
                    }
                }
            }

            latch.await();

            final long overallElapsedTime = System.currentTimeMillis() - overallStartTime;
            log.info("All requests completed in " + overallElapsedTime + " ms");

            for (final SiteConfig site : config.getSites())
            {


            }
        }
        catch (InterruptedException e)
        {
            log.error("Interrupted while waiting for client requests to complete", e);
        }
        finally
        {
            try
            {
                httpClient.close();
            }
            catch (IOException e)
            {
                log.error("Error closing HTTP client", e);
            }
        }

        return new TaskOutput("Success");
    }

    public static void main(String[] argv)
            throws Exception
    {
        Map<String, String> taskParams = new HashMap<String, String>();
        taskParams.put(CONFIG_FILE_PARAM, "config.yaml");

        UrlMonitor monitor = new UrlMonitor();
        monitor.execute(taskParams, null);
    }
}
