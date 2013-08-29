package views.ac;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.token.Token;
import com.atlassian.fugue.Option;
import com.google.common.base.Function;

public class PageToken
{
    public static Option<String> getToken()
    {
        final Option<Token> token = AC.getToken();
        return token.map(new Function<Token, String>()
        {
            @Override
            public String apply(final Token input)
            {
                return input.getToken();
            }
        });
    }
}
