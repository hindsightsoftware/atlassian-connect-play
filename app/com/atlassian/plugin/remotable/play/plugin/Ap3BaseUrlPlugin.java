package com.atlassian.plugin.remotable.play.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.remotable.play.Ap3;
import com.atlassian.plugin.remotable.play.BaseUrl;
import com.atlassian.plugin.remotable.play.util.Environment;
import play.Application;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.plugin.remotable.play.util.Utils.LOGGER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class Ap3BaseUrlPlugin extends AbstractPlugin
{
    private static final String DEFAULT_BASE_URL = "http://localhost:9000";

    public Ap3BaseUrlPlugin(Application application)
    {
        super(application);
        Ap3.baseUrl = new ApplicationBaseUrl(checkNotNull(application));
        LOGGER.info(format("Resolved base URL of application as '%s'", Ap3.baseUrl.get()));
    }

    private static final class ApplicationBaseUrl implements BaseUrl
    {
        private final Application application;

        public ApplicationBaseUrl(Application application)
        {
            this.application = checkNotNull(application);
        }

        @Override
        public String get()
        {
            return getBaseUrlFromEnv().getOrElse(getBaseUrlFromConfiguration());
        }

        private Option<String> getBaseUrlFromEnv()
        {
            return option(Environment.getOptionalEnv("BASE_URL", null));
        }

        private String getBaseUrlFromConfiguration()
        {
            return application.configuration().getString("application.baseUrl", DEFAULT_BASE_URL);
        }
    }
}
