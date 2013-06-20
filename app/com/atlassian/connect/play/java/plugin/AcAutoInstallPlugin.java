package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.remoteapps.RemoteAppsClient;
import com.atlassian.connect.play.java.upm.UpmClient;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import play.Application;
import play.libs.F;

import java.net.URI;
import java.util.Set;

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
        install();
    }

    public static void install()
    {
        final Iterable<URI> listeningApplications = Iterables.filter(AUTOREGISTER_HOSTS, new IsApplicationListeningPredicate());
        final String playAppBaseUrl = AC.baseUrl.get();
        for (URI appUri : listeningApplications)
        {
            install(appUri, playAppBaseUrl);
        }
    }

    private static void install(final URI appUri, final String playAppBaseUrl)
    {
        final String baseUrl = appUri.toString();
        new UpmClient(baseUrl).install(playAppBaseUrl, new F.Callback<Boolean>()
        {
            @Override
            public void invoke(Boolean installed) throws Throwable
            {
                if (!installed)
                {
                    new RemoteAppsClient(baseUrl).install(playAppBaseUrl);
                }
            }
        });
    }
}
