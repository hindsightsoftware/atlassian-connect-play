package com.atlassian.connect.play.java.plugin;

import com.google.common.base.Predicate;
import com.google.common.io.Closeables;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

final class IsApplicationListeningPredicate implements Predicate<URI>
{
    @Override
    public boolean apply(@Nullable URI uri)
    {
        checkNotNull(uri);

        LOGGER.debug(format("Scanning for application at '%s'", uri));
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
            LOGGER.warn(format("Could not resolve host name '%s'", hostName), e);
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            closeQuietly(socket);
        }
    }

    public static void closeQuietly(@Nullable Closeable closeable)
    {
        try
        {
            Closeables.close(closeable, true);
        } catch (IOException e) {
            LOGGER.error("IOException should not have been thrown.", e);
        }
    }
}
