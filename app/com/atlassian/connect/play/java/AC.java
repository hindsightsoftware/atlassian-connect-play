package com.atlassian.connect.play.java;

import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.connect.play.java.oauth.OAuthSignatureCalculator;
import com.atlassian.connect.play.java.util.Environment;
import com.atlassian.fugue.Option;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import play.Play;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.WS;
import play.mvc.Http;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static com.atlassian.connect.play.java.util.Environment.OAUTH_LOCAL_PRIVATE_KEY;
import static com.atlassian.connect.play.java.util.Environment.OAUTH_LOCAL_PUBLIC_KEY;
import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Suppliers.memoize;
import static java.lang.String.format;
import static play.mvc.Http.Context.Implicit.request;

public final class AC
{
    public static class Session
    {
        public static final String USER_ID = "userId";
        public static final String AC_HOST_KEY = "ac_host";
    }

    private static final Long DEFAULT_TIMEOUT = TimeUnit.SECONDS.convert(5, TimeUnit.MILLISECONDS);

    public static final String USER_ID_QUERY_PARAMETER = "user_id";

    public static String PLUGIN_KEY = Play.application().configuration().getString("ac.key", isDev() ? "_add-on_key" : null);
    public static String PLUGIN_NAME = Option.option(Play.application().configuration().getString("ac.name", isDev() ? "Atlassian Connect Play Add-on" : null)).getOrElse(PLUGIN_KEY);

    // the base URL
    public static BaseUrl baseUrl;

    public static final Supplier<String> publicKey = memoize(new Supplier<String>()
    {
        @Override
        public String get()
        {
            return getKey(OAUTH_LOCAL_PUBLIC_KEY, "public-key.pem");
        }
    });

    public static final Supplier<String> privateKey = memoize(new Supplier<String>()
    {
        @Override
        public String get()
        {
            return getKey(OAUTH_LOCAL_PRIVATE_KEY, "private-key.pem");
        }
    });

    private static String getKey(String envKey, String fileName)
    {
        String key = Environment.getOptionalEnv(envKey, null);
        if (key == null && isDev())
        {
            try
            {
                key = getFileContent(fileName);
            }
            catch (IOException e)
            {
                LOGGER.warn(format("Could not read '%s' file.", fileName), e);
            }
        }
        if (key != null)
        {
            if (isDev())
            {
                LOGGER.debug(format("Loaded key '%s' as:\n%s", envKey, key));
            }
            return key;
        }
        throw new IllegalStateException(format("Could NOT find %s for OAuth!", envKey));
    }

    private static String getFileContent(String pathname) throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        Files.copy(new File(pathname), Charset.forName("UTF-8"), sb);
        return sb.toString();
    }

    public static boolean isDev()
    {
        return Play.isDev()
                || Play.isTest()
                || Boolean.valueOf(Play.application().configuration().getString("ac.dev", "false"))
                || Boolean.getBoolean("ac.dev");
    }

    public static Option<String> getUser()
    {
        return option(request().getQueryString(USER_ID_QUERY_PARAMETER));
    }

    public static WS.WSRequestHolder url(String url)
    {
        return url(url, getAcHost());
    }

    public static WS.WSRequestHolder url(String url, AcHost acHost)
    {
        return url(url, acHost, getUser().getOrNull());
    }

    public static WS.WSRequestHolder url(String url, AcHost acHost, String userId)
    {
        checkState(!url.matches("^[\\w]+:.*"), "Absolute request URIs are not supported for host requests");

        final String absoluteUrl = acHost.getBaseUrl() + url;

        LOGGER.debug(format("Creating request to '%s'", absoluteUrl));

        final WS.WSRequestHolder request = WS.url(absoluteUrl)
                .setTimeout(DEFAULT_TIMEOUT.intValue())
                .setFollowRedirects(false) // because we need to sign again in those cases.
                .sign(new OAuthSignatureCalculator());

        if (userId != null)
        {
            request.setQueryParameter(USER_ID_QUERY_PARAMETER, userId);
        }
        return request;
    }

    public static AcHost getAcHost()
    {
        final AcHost acHost = (AcHost) getHttpContext().args.get("ac_host");
        if(acHost == null)
        {
            //check if there's one in the session!
            final String hostKey = getHttpContext().session().get(Session.AC_HOST_KEY);
            if(hostKey != null)
            {
                return getAcHost(hostKey).getOrNull();
            }
        }
        return acHost;
    }

    public static AcHost setAcHost(String consumerKey)
    {
        return setAcHost(getAcHost(consumerKey).getOrError(Suppliers.ofInstance("An error occured getting the host application")));
    }

    public static Option<? extends AcHost> getAcHost(final String consumerKey)
    {
        try
        {
            return JPA.withTransaction(new F.Function0<Option<? extends AcHost>>()
            {
                @Override
                public Option<? extends AcHost> apply() throws Throwable
                {
                    return AcHostModel.findByKey(consumerKey);
                }
            });
        }
        catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    static AcHost setAcHost(AcHost host)
    {
        getHttpContext().args.put("ac_host", host);
        return host;
    }

    private static Http.Context getHttpContext()
    {
        return Http.Context.current();
    }
}
