package com.atlassian.connect.play.java;

import com.atlassian.connect.play.java.auth.jwt.JwtAuthConfig;
import com.atlassian.connect.play.java.auth.jwt.JwtAuthorizationGenerator;
import com.atlassian.connect.play.java.auth.jwt.JwtSignatureCalculator;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.connect.play.java.service.AcHostHttpClient;
import com.atlassian.connect.play.java.service.AcHostService;
import com.atlassian.connect.play.java.service.AcHostServiceImpl;
import com.atlassian.connect.play.java.token.Token;
import com.atlassian.fugue.Option;
import com.google.common.base.Suppliers;
import org.apache.commons.codec.binary.Base64;
import play.Play;
import play.api.libs.Crypto;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.mvc.Http;

import java.util.concurrent.TimeUnit;

import static com.atlassian.connect.play.java.Constants.*;
import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static play.libs.F.Promise;
import static play.libs.WS.WSRequestHolder;

public final class AC
{
    private static final Long DEFAULT_TIMEOUT = TimeUnit.SECONDS.convert(5, TimeUnit.MILLISECONDS);

    public static String PLUGIN_KEY = Play.application().configuration().getString(AC_PLUGIN_KEY, isDev() ? "_add-on_key" : null);
    public static String PLUGIN_NAME = Option.option(Play.application().configuration().getString(Constants.AC_PLUGIN_NAME, isDev() ? "Atlassian Connect Play Add-on" : null)).getOrElse(PLUGIN_KEY);

    // the base URL
    public static BaseUrl baseUrl;
    public static long tokenExpiry;

    // TODO: DI of some sort would be nice
    private static final JwtAuthorizationGenerator jwtAuthorisationGenerator = JwtAuthConfig.getJwtAuthorizationGenerator();
    private static final AcHostService acHostService = new AcHostServiceImpl(new AcHostHttpClient() {
        @Override
        public WSRequestHolder url(String url) {
            return AC.url(url);
        }

        @Override
        public WSRequestHolder url(String url, AcHost acHost, boolean signRequest) {
            return AC.url(url, acHost, signRequest);
        }

        @Override
        public WSRequestHolder url(String url, AcHost acHost, Option<String> userId) {
            return AC.url(url, acHost, userId);
        }
    });

    public static boolean isDev()
    {
        return Play.isDev()
                || Play.isTest()
                || Boolean.valueOf(Play.application().configuration().getString(AC_DEV, "false"))
                || Boolean.getBoolean(AC_DEV);
    }

    public static Option<String> getUser()
    {
        return Option.option((String) getHttpContext().args.get(AC_USER_ID_PARAM));
    }

    public static void setUser(String user)
    {
        getHttpContext().args.put(AC_USER_ID_PARAM, user);
    }

    public static WSRequestHolder url(String url)
    {
        return url(url, checkNotNull(getAcHost(), "No AcHost found in HttpContext"));
    }

    public static WSRequestHolder url(String url, AcHost acHost)
    {
        return url(url, acHost, true);
    }

    public static WSRequestHolder url(String url, AcHost acHost, boolean signRequest)
    {
        return url(url, acHost, getUser(), signRequest);
    }

    public static WSRequestHolder url(String url, AcHost acHost, Option<String> userId)
    {
        return url(url, acHost, userId, true);
    }

    public static WSRequestHolder url(String url, AcHost acHost, Option<String> userId, boolean signRequest)
    {
        checkNotNull(url, "Url must be non-null");
        checkNotNull(acHost, "acHost must be non-null");

        final String absoluteUrl = getAbsoluteUrl(url, acHost);

        LOGGER.debug(format("Creating request to '%s'", absoluteUrl));

        final WSRequestHolder request = WS.url(absoluteUrl)
                .setTimeout(DEFAULT_TIMEOUT.intValue());

        if (signRequest) {
            request.setFollowRedirects(false) // because we need to sign again in those cases.
            .sign(new JwtSignatureCalculator(jwtAuthorisationGenerator, acHost, userId));
        }

        return request;
    }

    private static String getAbsoluteUrl(String url, AcHost acHost) {
        String absoluteUrl;
        if (url.matches("^[\\w]+:.*"))
        {
            checkArgument(url.startsWith(acHost.getBaseUrl()), "Absolute request URL must begin with the host base URL");
            absoluteUrl = url;
        }
        else
        {
            absoluteUrl = acHost.getBaseUrl() + url;
        }
        return absoluteUrl;
    }

    public static AcHost getAcHost()
    {
        return (AcHost) getHttpContext().args.get(AC_HOST_PARAM);
    }

    public static AcHost getAcHostOrThrow()
    {
        AcHost acHost = getAcHost();
        return checkNotNull(acHost);
    }

    public static AcHost setAcHost(String consumerKey)
    {
        return setAcHost(getAcHost(consumerKey).getOrError(Suppliers.ofInstance("An error occurred getting the host application")));
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

    public static Promise<Void> registerHost(AcHost acHost)
    {
        return acHostService.registerHost(acHost);
    }

    public static void refreshToken(boolean allowInsecurePolling)
    {
        final Token token = new Token(AC.getAcHost().getKey(), AC.getUser(), System.currentTimeMillis(), allowInsecurePolling);

        final String jsonToken = Base64.encodeBase64String(token.toJson().toString().getBytes());
        final String encryptedToken = Crypto.encryptAES(jsonToken);

        getHttpContext().args.put(AC_TOKEN, encryptedToken);
    }

    public static Option<Token> validateToken(final String encryptedToken, final boolean allowInsecurePolling)
    {
        try
        {
            final String decrypted = Crypto.decryptAES(encryptedToken);
            final Token token = Token.fromJson(Json.parse(new String(Base64.decodeBase64(decrypted))));
            //only accept tokens which allowInsecurePolling from Actions that were annotated with this option set
            //to true!
            if(!allowInsecurePolling && token.isAllowInsecurePolling())
            {
                return none();
            }
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
