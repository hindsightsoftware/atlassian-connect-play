package views.ac;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;

import static com.atlassian.connect.play.java.AC.Session;
import static play.mvc.Http.Context.current;

/**
 * Helper to get the baseurl from either the current context or session
 */
public class BaseUrl
{

    public static String get()
    {
        final AcHost acHost = (AcHost) current().args.get(Session.AC_HOST_KEY);
        if (acHost != null)
        {
            return acHost.getBaseUrl();
        }

        //perhaps we're not in an OAuth request. Lets try to get it from the session
        final String acHostKey = current().session().get(Session.AC_HOST_KEY);
        if (acHostKey != null)
        {
            final AcHost host = AC.getAcHost(acHostKey).getOrNull();
            if (host != null)
            {
                return host.getBaseUrl();
            }
        }
        return null;
    }
}
