package views.ac;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;

/**
 * Helper to get the baseurl from either the current context
 */
public class BaseUrl
{
    public static String get()
    {
        final Option<AcHost> acHost = Option.option(AC.getAcHost());
        if (acHost.isDefined())
        {
            return acHost.get().getBaseUrl();
        }
        return null;
    }
}
