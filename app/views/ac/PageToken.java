package views.ac;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.token.Token;
import com.atlassian.connect.play.java.token.TokenKey;
import com.atlassian.fugue.Option;

import static com.atlassian.fugue.Option.some;

public class PageToken
{
    public static Option<String> getToken()
    {
        final Option<Token> token = AC.tokenStore.get(new TokenKey(AC.getAcHost().getKey(), AC.getUser()), System.currentTimeMillis());
        if (token.isDefined())
        {
            return some(token.get().getToken());
        }
        return Option.none();
    }
}
