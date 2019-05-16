package com.atlassian.connect.play.java.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.Optional;

public final class Token
{
    private final String acHost;
    private final Optional<String> userAccountId;
    private final long timestamp;
    private boolean allowInsecurePolling;

    public Token(final String acHost, final Optional<String> userAccountId, final long timestamp, boolean allowInsecurePolling)
    {
        this.acHost = acHost;
        this.userAccountId = userAccountId;
        this.timestamp = timestamp;
        this.allowInsecurePolling = allowInsecurePolling;
    }

    public String getAcHost()
    {
        return acHost;
    }

    public Optional<String> getUserAccountId()
    {
        return  userAccountId;
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
        if (userAccountId.isPresent())
        {
            jsonToken.put("a", userAccountId.get());
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
                Optional.ofNullable(jsonToken.get("a").asText()),
                jsonToken.get("t").asLong(),
                jsonToken.has("p"));

    }
}
