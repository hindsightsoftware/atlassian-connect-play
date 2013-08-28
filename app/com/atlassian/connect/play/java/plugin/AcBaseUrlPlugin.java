package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.BaseUrl;
import com.atlassian.connect.play.java.util.Environment;
import com.atlassian.fugue.Option;
import play.Application;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class AcBaseUrlPlugin extends AbstractPlugin
{
    private static final String DEFAULT_BASE_URL = "http://localhost:9000";

    public AcBaseUrlPlugin(Application application)
    {
        super(application);
        AC.baseUrl = new ApplicationBaseUrl(checkNotNull(application));
        LOGGER.info(format("Resolved base URL of application as '%s'", AC.baseUrl.get()));
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
            final String baseUrl = getBaseUrlFromEnv().getOrElse(getBaseUrlFromConfiguration());
            return stripTrailingSlash(baseUrl);
        }

        /**
         * Strip trailing / which might have been configured by a plugin developer. This can cause issues with double
         * slashes and Oauth request validation.
         */
        private String stripTrailingSlash(final String baseUrl)
        {
            if (baseUrl.endsWith("/"))
            {
                return baseUrl.substring(0, baseUrl.length() - 1);
            }
            return baseUrl;
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
