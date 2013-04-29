package com.atlassian.plugin.remotable.play;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.ning.http.client.Realm;
import play.Application;
import play.Logger;
import play.Plugin;
import play.libs.F;
import play.libs.WS;
import play.mvc.Result;
import play.mvc.Results;

import java.net.URI;
import java.util.Set;

import static java.lang.String.format;

public final class Ap3Plugin extends Plugin
{
    private static final Set<URI> AUTOREGISTER_HOSTS = ImmutableSet.of(
            URI.create("http://localhost:1990/confluence"),
            URI.create("http://localhost:2990/jira"),
            URI.create("http://localhost:5990/refapp"));

    private final Application application;

    public Ap3Plugin(Application application)
    {
        this.application = application;
    }

    @Override
    public boolean enabled()
    {
        return application.isDev();
    }

    @Override
    public void onStart()
    {
        final Iterable<URI> listeningApplications = Iterables.filter(AUTOREGISTER_HOSTS, new IsApplicationListeningPredicate());
        final String playAppBaseUrl = getBaseUrl();
        for (URI appUri : listeningApplications)
        {
            install(appUri, playAppBaseUrl);
        }
    }

    private void install(final URI appUri, String playAppBaseUrl)
    {
        final String userName = "admin";
        final String password = "admin";

        WS.url(appUri.toString() + "/rest/remoteapps/latest/installer")
                .setAuth(userName, password, Realm.AuthScheme.BASIC)
                .post("url=" + playAppBaseUrl)
                .map(new F.Function<WS.Response, Result>()
                {
                    public Result apply(WS.Response response) throws Throwable
                    {
                        final String msg = format("Plugin successfully installed into '%s'", appUri);
                        Logger.of("ap3").info(msg);
                        return Results.ok(msg);
                    }
                })
                .recover(new F.Function<Throwable, Result>()
                {
                    @Override
                    public Result apply(Throwable throwable) throws Throwable
                    {
                        final String msg = format("Unable to install plugin into '%s'", appUri);
                        Logger.of("ap3").error(msg, throwable);
                        return Results.internalServerError(msg);
                    }
                });
    }

    private String getBaseUrl()
    {
        return application.configuration().getString("application.baseUrl", "http://localhost:9000");
    }

    @Override
    public void onStop()
    {
    }
}
