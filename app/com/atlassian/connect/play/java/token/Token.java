package com.atlassian.connect.play.java.token;

import com.atlassian.fugue.Option;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;

import static com.atlassian.fugue.Option.option;

public final class Token
{
    private final String acHost;
    private final Option<String> user;
    private final long timestamp;

    public Token(final String acHost, final Option<String> user, final long timestamp)
    {
        this.acHost = acHost;
        this.user = user;
        this.timestamp = timestamp;
    }

    public String getAcHost()
    {
        return acHost;
    }

    public Option<String> getUser()
    {
        return user;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public JsonNode toJson()
    {
        final ObjectNode jsonToken = Json.newObject();
        jsonToken.put("h", acHost);
        if (user.isDefined())
        {
            jsonToken.put("u", user.get());
        }
        jsonToken.put("t", System.currentTimeMillis());
        return jsonToken;
    }

    public static Token fromJson(final JsonNode jsonToken)
    {
        return new Token(jsonToken.get("h").asText(),
                option(jsonToken.get("u").asText()),
                jsonToken.get("t").asLong());

    }
}
