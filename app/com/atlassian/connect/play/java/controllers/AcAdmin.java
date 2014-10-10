package com.atlassian.connect.play.java.controllers;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.service.AcHostService;
import com.atlassian.connect.play.java.service.InjectorFactory;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import static java.lang.String.format;
import static play.mvc.Results.internalServerError;

import play.mvc.Results;

@With(IsDevAction.class)
public class AcAdmin
{
    private static final AcHostService acHostService = InjectorFactory.getAcHostService();

    public static Result index()
    {
        try {
            return Results.ok(views.html.ac.internal.admin.index.render(acHostService.all()));
        } catch (Throwable throwable) {
            return internalServerError(message("Error accessing the database", throwable.getMessage()));
        }
    }

    public static Promise<Result> clearMacroCache(final String key)
    {
        return AC.getAcHost(key).fold(
                noAcHost(),
                new Function<AcHost, F.Promise<Result>>()
                {
                    @Override
                    public F.Promise<Result> apply(final AcHost host)
                    {
                        return AC.url("/rest/atlassian-connect/1/macro/app/" + AC.PLUGIN_KEY, host).delete().map(new F.Function<WSResponse, Result>()
                        {
                            @Override
                            public Result apply(WSResponse response) throws Throwable
                            {
                                if (response.getStatus() == Http.Status.NO_CONTENT)
                                {
                                    return Results.ok(message("Cache cleared",
                                            format("The macro cache for host at '%s' was cleared.", host.getBaseUrl())));
                                }
                                else
                                {
                                    return Results.badRequest(message("Unknown error",
                                            format("An unknown error happened clearing the cache for host at '%s'. Http status is %s (%s).",
                                                    host.getBaseUrl(), response.getStatus(), response.getStatusText())));
                                }
                            }
                        });
                    }
                });
    }

    private static Supplier<F.Promise<Result>> noAcHost()
    {
        return new Supplier<F.Promise<Result>>()
        {
            @Override
            public F.Promise<Result> get()
            {
                return F.Promise.<Result>pure(Results.badRequest(message(
                        "No AC host",
                        "Couldn't find AC host to send request to."
                )));
            }
        };
    }

    public static JsonNode message(String title, String message)
    {
        final ObjectNode result = Json.newObject();
        result.put("title", title);
        result.put("message", message);
        return result;
    }
}
