package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.ning.http.client.Realm;
import play.Application;
import play.libs.F;
import play.libs.WS;
import play.mvc.Result;
import play.mvc.Results;

import java.net.URI;
import java.util.Set;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static java.lang.String.format;

public final class AcAutoInstallPlugin extends AbstractDevPlugin
{
    private static final Set<URI> AUTOREGISTER_HOSTS = ImmutableSet.of(
            URI.create("http://localhost:1990/confluence"),
            URI.create("http://localhost:2990/jira"),
            URI.create("http://localhost:5990/refapp"));

    public AcAutoInstallPlugin(Application application)
    {
        super(application);
    }

    @Override
    public void onStart()
    {
        final Iterable<URI> listeningApplications = Iterables.filter(AUTOREGISTER_HOSTS, new IsApplicationListeningPredicate());
        final String playAppBaseUrl = AC.baseUrl.get();
        for (URI appUri : listeningApplications)
        {
            install(appUri, playAppBaseUrl);
        }
    }

    private void install(final URI appUri, String playAppBaseUrl)
    {
        final String userName = "admin";
        final String password = "admin";

        final String postUrl = appUri.toString() + "/rest/remotable-plugins/latest/installer";
        final String parameters = "url=" + playAppBaseUrl;

        LOGGER.debug(format("Posting to URL '%s', with parameters '%s'", postUrl, parameters));

        WS.url(postUrl)
                .setAuth(userName, password, Realm.AuthScheme.BASIC)
                .post(parameters)
                .map(new F.Function<WS.Response, Result>()
                {
                    public Result apply(WS.Response response) throws Throwable
                    {
                        final String msg = format("Plugin successfully installed into '%s'", appUri);
                        LOGGER.info(msg);
                        return Results.ok(msg);
                    }
                })
                .recover(new F.Function<Throwable, Result>()
                {
                    @Override
                    public Result apply(Throwable throwable) throws Throwable
                    {
                        final String msg = format("Unable to install plugin into '%s'", appUri);
                        LOGGER.error(msg, throwable);
                        return Results.internalServerError(msg);
                    }
                });
    }
}
