package com.atlassian.plugin.remotable.play;

import com.google.common.base.Predicate;
import com.google.common.io.Closeables;
import play.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

final class IsApplicationListeningPredicate implements Predicate<URI>
{
    @Override
    public boolean apply(@Nullable URI uri)
    {
        checkNotNull(uri);

        Logger.of("ap3").debug(format("Scanning for application at '%s'", uri));
        return canOpenSocket(uri.getHost(), uri.getPort());
    }

    private boolean canOpenSocket(String hostName, int port)
    {
        Socket socket = null;
        try
        {
            socket = new Socket(hostName, port);
            return true;
        }
        catch (UnknownHostException e)
        {
            Logger.of("ap3").warn(format("Could not resolve host name '%s'", hostName), e);
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            Closeables.closeQuietly(socket);
        }
    }
}
