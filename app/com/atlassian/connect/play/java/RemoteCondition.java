package com.atlassian.connect.play.java;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Helper to return a valid JSON object for a remote condition request sent by the host application.
 * <p/>
 * Should be used like this:
 * <pre>
 * public Result checkCondition(String username, Long issueId)
 * {
 *  boolean hasAccess = permissionService(username, issueId);
 *  return RemoteCondition.of(hasAccess);
 * }
 * </pre>
 */
public final class RemoteCondition
{
    private static final JsonNode TRUE = Json.newObject();
    private static final JsonNode FALSE = Json.newObject();

    static
    {
        ((ObjectNode) TRUE).put("shouldDisplay", true);
        ((ObjectNode) FALSE).put("shouldDisplay", false);
    }

    private RemoteCondition() {}

    public static Result of(boolean conditionResult)
    {
        if (conditionResult)
        {
            return Results.ok(TRUE);
        }
        else
        {
            return Results.ok(FALSE);
        }
    }
}
