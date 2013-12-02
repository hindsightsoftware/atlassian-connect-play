package com.atlassian.connect.play.java.controllers;

import com.atlassian.connect.play.java.plugin.AcAutoInstallPlugin;
import com.atlassian.fugue.Pair;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;

import java.net.URI;
import java.util.List;

@With(IsDevAction.class)
public class UpmController extends Controller
{
    public static Promise<Result> install()
    {
        return AcAutoInstallPlugin.install().map(new F.Function<List<Pair<URI, Boolean>>, Result>()
        {
            @Override
            public Result apply(List<Pair<URI, Boolean>> pairs) throws Throwable
            {
                return Results.ok(newJsonHosts(pairs));
            }
        });
    }

    private static ObjectNode newJsonHosts(List<Pair<URI, Boolean>> pairs)
    {
        return addJsonHosts(Json.newObject(), pairs);
    }

    private static ObjectNode addJsonHosts(ObjectNode root, List<Pair<URI, Boolean>> pairs)
    {
        final ArrayNode hosts = root.putArray("hosts");
        for (Pair<URI, Boolean> host : pairs)
        {
            hosts.add(newJsonHost(host));
        }
        return root;
    }

    private static ObjectNode newJsonHost(Pair<URI, Boolean> host)
    {
        ObjectNode jsonHost = Json.newObject();
        jsonHost.put("uri", host.left().toString());
        jsonHost.put("installed", host.right());
        return jsonHost;
    }
}
