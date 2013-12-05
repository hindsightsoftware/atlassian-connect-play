package com.atlassian.connect.play.java.token;

import com.atlassian.fugue.Option;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import static com.atlassian.fugue.Option.option;

public final class Token
{
    private final String acHost;
    private final Option<String> user;
    private final long timestamp;
    private boolean allowInsecurePolling;

    public Token(final String acHost, final Option<String> user, final long timestamp, boolean allowInsecurePolling)
    {
        this.acHost = acHost;
        this.user = user;
        this.timestamp = timestamp;
        this.allowInsecurePolling = allowInsecurePolling;
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

    public boolean isAllowInsecurePolling()
    {
        return allowInsecurePolling;
    }

    public JsonNode toJson()
    {
        final ObjectNode jsonToken = Json.newObject();
        jsonToken.put("h", acHost);
        if (user.isDefined())
        {
            jsonToken.put("u", user.get());
        }
        if (allowInsecurePolling)
        {
            jsonToken.put("p", "1");
        }
        jsonToken.put("t", System.currentTimeMillis());
        return jsonToken;
    }

    public static Token fromJson(final JsonNode jsonToken)
    {
        return new Token(jsonToken.get("h").asText(),
                option(jsonToken.get("u").asText()),
                jsonToken.get("t").asLong(),
                jsonToken.has("p"));

    }
}
