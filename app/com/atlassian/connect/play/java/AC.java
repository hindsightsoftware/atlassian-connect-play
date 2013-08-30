package com.atlassian.connect.play.java;

import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.connect.play.java.oauth.OAuthSignatureCalculator;
import com.atlassian.connect.play.java.token.Token;
import com.atlassian.connect.play.java.util.Environment;
import com.atlassian.fugue.Option;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import org.apache.commons.codec.binary.Base64;
import play.Play;
import play.api.libs.Crypto;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.mvc.Http;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static com.atlassian.connect.play.java.Constants.AC_DEV;
import static com.atlassian.connect.play.java.Constants.AC_HOST_PARAM;
import static com.atlassian.connect.play.java.Constants.AC_PLUGIN_KEY;
import static com.atlassian.connect.play.java.Constants.AC_TOKEN;
import static com.atlassian.connect.play.java.Constants.AC_USER_ID_PARAM;
import static com.atlassian.connect.play.java.util.Environment.OAUTH_LOCAL_PRIVATE_KEY;
import static com.atlassian.connect.play.java.util.Environment.OAUTH_LOCAL_PUBLIC_KEY;
import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Suppliers.memoize;
import static java.lang.String.format;
import static play.mvc.Http.Context.Implicit.request;

public final class AC
{
    private static final Long DEFAULT_TIMEOUT = TimeUnit.SECONDS.convert(5, TimeUnit.MILLISECONDS);

    public static String PLUGIN_KEY = Play.application().configuration().getString(AC_PLUGIN_KEY, isDev() ? "_add-on_key" : null);
    public static String PLUGIN_NAME = Option.option(Play.application().configuration().getString(Constants.AC_PLUGIN_NAME, isDev() ? "Atlassian Connect Play Add-on" : null)).getOrElse(PLUGIN_KEY);

    // the base URL
    public static BaseUrl baseUrl;
    public static long tokenExpiry;

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
                || Boolean.valueOf(Play.application().configuration().getString(AC_DEV, "false"))
                || Boolean.getBoolean(AC_DEV);
    }

    public static Option<String> getUser()
    {
        final Option<String> user = option(request().getQueryString(AC_USER_ID_PARAM));
        //user might have been set via com.atlassian.connect.play.java.token.PageTokenValidatorAction
        if (user.isEmpty())
        {
            return Option.option((String) getHttpContext().args.get(AC_USER_ID_PARAM));
        }
        return user;
    }

    public static WS.WSRequestHolder url(String url)
    {
        return url(url, getAcHost());
    }

    public static WS.WSRequestHolder url(String url, AcHost acHost)
    {
        return url(url, acHost, getUser());
    }

    public static WS.WSRequestHolder url(String url, AcHost acHost, Option<String> userId)
    {
        checkState(!url.matches("^[\\w]+:.*"), "Absolute request URIs are not supported for host requests");

        final String absoluteUrl = acHost.getBaseUrl() + url;

        LOGGER.debug(format("Creating request to '%s'", absoluteUrl));

        final WS.WSRequestHolder request = WS.url(absoluteUrl)
                .setTimeout(DEFAULT_TIMEOUT.intValue())
                .setFollowRedirects(false) // because we need to sign again in those cases.
                .sign(new OAuthSignatureCalculator());

        if (userId.isDefined())
        {
            request.setQueryParameter(AC_USER_ID_PARAM, userId.get());
        }
        return request;
    }

    public static AcHost getAcHost()
    {
        return (AcHost) getHttpContext().args.get(AC_HOST_PARAM);
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

    public static void refreshToken()
    {
        final Token token = new Token(AC.getAcHost().getKey(), AC.getUser(), System.currentTimeMillis());

        final String jsonToken = Base64.encodeBase64String(token.toJson().toString().getBytes());
        final String encryptedToken = Crypto.encryptAES(jsonToken);

        getHttpContext().args.put(AC_TOKEN, encryptedToken);
    }

    public static Option<Token> validateToken(final String encryptedToken)
    {
        try
        {
            final String decrypted = Crypto.decryptAES(encryptedToken);
            final Token token = Token.fromJson(Json.parse(new String(Base64.decodeBase64(decrypted))));
            if (token != null && (System.currentTimeMillis() - AC.tokenExpiry) <= token.getTimestamp())
            {
                return some(token);
            }
        }
        catch(Throwable t)
        {
            //Crypto throws Exceptions when there's issues decrypting.  That's normal usage in
            //case someone's trying to fake a token, so lets ignore it here.
        }

        return none();
    }

    public static Option<String> getToken()
    {
        return Option.option((String) getHttpContext().args.get(AC_TOKEN));
    }

    static AcHost setAcHost(AcHost host)
    {
        getHttpContext().args.put(AC_HOST_PARAM, host);
        return host;
    }

    private static Http.Context getHttpContext()
    {
        return Http.Context.current();
    }
}
