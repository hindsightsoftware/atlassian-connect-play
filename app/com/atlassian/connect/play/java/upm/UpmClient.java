package com.atlassian.connect.play.java.upm;

import akka.actor.Cancellable;
import com.ning.http.client.Realm;
import org.codehaus.jackson.JsonNode;
import play.libs.Akka;
import play.libs.F;
import play.libs.WS;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class UpmClient
{
    private static final String UPM_REST_PATH = "/rest/plugins/1.0";

    private final String baseUrl;

    public UpmClient(String baseUrl)
    {
        this.baseUrl = checkNotNull(baseUrl);
    }

    public F.Promise<String> getToken()
    {
        return url("/").head().map(
                new F.Function<WS.Response, String>()
                {
                    @Override
                    public String apply(WS.Response response) throws Throwable
                    {
                        final String token = response.getHeader("upm-token");
                        LOGGER.trace("UPM token is " + token);
                        return token;
                    }
                });
    }

    public F.Promise<Boolean> install(final String uri, final F.Function<Boolean, F.Promise<Boolean>> callback)
    {
        return getToken().flatMap(new F.Function<String, F.Promise<Boolean>>()
        {
            @Override
            public F.Promise<Boolean> apply(String token) throws Throwable
            {
                return url("/")
                        .setQueryParameter("token", token)
                        .setHeader("Content-Type", "application/vnd.atl.plugins.remote.install+json")
                        .post("{ \"pluginUri\": \"" + uri + "\" }")
                        .flatMap(new F.Function<WS.Response, F.Promise<Boolean>>()
                        {
                            @Override
                            public F.Promise<Boolean> apply(WS.Response response) throws Throwable
                            {
                                if (is2xxResponse(response))
                                {
                                    checkInstallStatus(InstallStatus.fromResponse(response), callback);
                                    return F.Promise.pure(true);
                                }
                                else
                                {
                                    LOGGER.debug(format("Could not install plugin (%s) to '%s' via UPM", uri, baseUrl));
                                    LOGGER.debug(format("UPM responded with status code %s (%s) and the following message:\n%s", response.getStatus(), response.getStatusText(), response.getBody()));
                                    return callback.apply(false);
                                }
                            }
                        })
                        .recover(new F.Function<Throwable, Boolean>()
                        {
                            @Override
                            public Boolean apply(Throwable throwable) throws Throwable
                            {
                                LOGGER.error(format("An error occurred installing plugin (%s) to '%s' via UPM", uri, baseUrl), throwable);
                                return false;
                            }
                        });
            }
        });
    }

    private F.Promise<InstallStatus> checkInstallStatus(String id, final F.Function<Boolean, F.Promise<Boolean>> callback)
    {
        return url(format("/pending/%s", id))
                .get()
                .map(new F.Function<WS.Response, InstallStatus>()
                {
                    @Override
                    public InstallStatus apply(WS.Response response) throws Throwable
                    {
                        if (is2xxResponse(response))
                        {
                            return InstallStatus.fromResponse(response);
                        }
                        else if (response.getStatus() == 303) // see other
                        {
                            LOGGER.info(format("Plugin successfully installed on %s (using the UPM REST end point).", baseUrl));
                            callback.apply(true);
                            return InstallStatus.of(true, false);
                        }
                        else
                        {
                            LOGGER.error(
                                    format("Could not check status of plugin install! UPM responded with %s (%s) and the following message:\n:%s",
                                            response.getStatus(), response.getStatusText(), response.getBody()));
                            callback.apply(false);
                            return InstallStatus.of(true, true);
                        }
                    }
                })
                .recover(new F.Function<Throwable, InstallStatus>()
                {
                    @Override
                    public InstallStatus apply(Throwable throwable) throws Throwable
                    {
                        LOGGER.error("An error occurred checking the status of a plugin install", throwable);
                        callback.apply(false);
                        return InstallStatus.of(true, true);
                    }
                });
    }

    private void checkInstallStatus(final InstallStatus status, final F.Function<Boolean, F.Promise<Boolean>> callback)
    {
        if (!status.done)
        {
            LOGGER.trace("Checking the status...");
            scheduleOnce(status.ping, new Runnable()
            {
                @Override
                public void run()
                {
                    checkInstallStatus(status.id, callback).onRedeem(new F.Callback<InstallStatus>()
                    {
                        @Override
                        public void invoke(InstallStatus installStatus) throws Throwable
                        {
                            checkInstallStatus(installStatus, callback);
                        }
                    });
                }
            });
        }
    }

    private Cancellable scheduleOnce(int ping, Runnable runnable)
    {
        return Akka.system().scheduler().scheduleOnce(
                Duration.create(ping, TimeUnit.MILLISECONDS),
                runnable,
                Akka.system().dispatcher()
        );
    }

    private boolean is2xxResponse(WS.Response response)
    {
        final int status = response.getStatus();
        return 200 <= status && status < 300;
    }

    private WS.WSRequestHolder url(String path)
    {
        return WS.url(absoluteUrl(path))
                .setFollowRedirects(false)
                .setAuth("admin", "admin", Realm.AuthScheme.BASIC);
    }

    private String absoluteUrl(String path)
    {
        return baseUrl + UPM_REST_PATH + path;
    }

    private static final class InstallStatus
    {
        public final String id;
        public final boolean done;
        public final int ping;
        public final boolean error;

        private InstallStatus(String id, boolean done, int ping, boolean error)
        {
            this.id = id;
            this.done = done;
            this.ping = ping;
            this.error = error;
        }

        static InstallStatus of(boolean done, boolean error)
        {
            return new InstallStatus(null, done, 0, error);
        }

        static InstallStatus fromResponse(WS.Response response)
        {
            final JsonNode jsonNode = response.asJson();
            final int ping = jsonNode.get("pingAfter").getIntValue();
            final String self = jsonNode.get("links").get("self").getTextValue();
            final String id = self.substring(self.lastIndexOf('/') + 1, self.length());
            final boolean done = jsonNode.get("status").get("done").getBooleanValue();

            return new InstallStatus(id, done, ping, false);
        }
    }
}
