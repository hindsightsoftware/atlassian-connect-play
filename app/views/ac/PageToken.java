package views.ac;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.token.TokenKey;
import com.atlassian.fugue.Option;

public class PageToken
{
    public static boolean hasToken()
    {
        final Option<AcHost> acHost = Option.option(AC.getAcHost());
        if (acHost.isDefined())
        {
            return AC.tokenStore.get(new TokenKey(acHost.get().getKey(), AC.getUser()), System.currentTimeMillis()).isDefined();
        }
        return false;
    }

    public static Option<String> getToken()
    {
        return AC.tokenStore.get(new TokenKey(AC.getAcHost().getKey(), AC.getUser()), System.currentTimeMillis());
    }
}
