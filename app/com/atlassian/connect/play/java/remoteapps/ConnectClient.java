package com.atlassian.connect.play.java.remoteapps;

import com.ning.http.client.Realm;
import play.libs.F;
import play.libs.WS;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class ConnectClient
{
    private final String baseUrl;

    public ConnectClient(String baseUrl)
    {
        this.baseUrl = checkNotNull(baseUrl);
    }

    public F.Promise<Boolean> install(final String uri)
    {
        final String userName = "admin";
        final String password = "admin";

        final String postUrl = baseUrl + "/rest/atlassian-connect/latest/installer";
        final String parameters = "url=" + uri;

        LOGGER.debug(format("Posting to URL '%s', with parameters '%s'", postUrl, parameters));

        return WS.url(postUrl)
                .setAuth(userName, password, Realm.AuthScheme.BASIC)
                .post(parameters)
                .map(new F.Function<WS.Response, Boolean>()
                {
                    public Boolean apply(WS.Response response) throws Throwable
                    {
                        LOGGER.info(format("Plugin successfully installed on %s (using the Atlassian Connect REST end point).", baseUrl));
                        return true;
                    }
                })
                .recover(new F.Function<Throwable, Boolean>()
                {
                    @Override
                    public Boolean apply(Throwable throwable) throws Throwable
                    {
                        LOGGER.error(format("Unable to install plugin into '%s'", baseUrl), throwable);
                        return false;
                    }
                });
    }
}
