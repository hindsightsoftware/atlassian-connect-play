package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.remoteapps.ConnectClient;
import com.atlassian.connect.play.java.upm.UpmClient;
import com.atlassian.fugue.Pair;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import play.Application;
import play.libs.F;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static com.atlassian.fugue.Pair.pair;
import static com.google.common.collect.Iterables.transform;

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

    public static F.Promise<List<Pair<URI, Boolean>>> install()
    {
        final Iterable<URI> listeningApplications = Iterables.filter(AUTOREGISTER_HOSTS, new IsApplicationListeningPredicate());
        final String playAppBaseUrl = AC.baseUrl.get();

        return F.Promise.sequence(transform(listeningApplications,
                new Function<URI, F.Promise<? extends Pair<URI, Boolean>>>()
                {
                    @Override
                    public F.Promise<Pair<URI, Boolean>> apply(URI appUri)
                    {
                        return install(appUri, playAppBaseUrl);
                    }
                }));
    }

    private static F.Promise<Pair<URI, Boolean>> install(final URI appUri, final String playAppBaseUrl)
    {
        final String baseUrl = appUri.toString();
        return new UpmClient(baseUrl)
                .install(playAppBaseUrl, new F.Function<Boolean, F.Promise<Boolean>>()
                {
                    @Override
                    public F.Promise<Boolean> apply(Boolean installed) throws Throwable
                    {
                        if (!installed)
                        {
                            return new ConnectClient(baseUrl).install(playAppBaseUrl);
                        }
                        else
                        {
                            return F.Promise.pure(true);
                        }
                    }
                })
                .map(new F.Function<Boolean, Pair<URI, Boolean>>()
                {
                    @Override
                    public Pair<URI, Boolean> apply(Boolean installed) throws Throwable
                    {
                        return pair(appUri, installed);
                    }
                });
    }
}
