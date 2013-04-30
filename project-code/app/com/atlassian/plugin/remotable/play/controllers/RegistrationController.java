package com.atlassian.plugin.remotable.play.controllers;

import com.atlassian.fugue.Option;
import com.google.common.base.Supplier;
import models.Ap3Application;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Results;

import static play.mvc.Controller.request;

public final class RegistrationController
{
    @BodyParser.Of(BodyParser.Json.class)
    public static Result registration()
    {
        Logger.of("ap3").info("Registering host application!");

        final JsonNode remoteApp = request().body().asJson();

        // TODO check the key is the same as this app's
        getAttributeAsText(remoteApp, "key");

        final String clientKey = getAttributeAsText(remoteApp, "clientKey");
        final String baseUrl = getAttributeAsText(remoteApp, "baseUrl");

        final Ap3Application ap3Application = Ap3Application.findByKey(clientKey)
                .orElse(new Supplier<Option<Ap3Application>>()
                {
                    @Override
                    public Option<Ap3Application> get()
                    {
                        return Ap3Application.findByUrl(baseUrl);
                    }
                })
                .getOrElse(new Ap3Application());

        ap3Application.key = clientKey;
        ap3Application.baseUrl = baseUrl;
        ap3Application.publicKey = getAttributeAsText(remoteApp, "publicKey");
        ap3Application.name = getAttributeAsText(remoteApp, "productType");
        ap3Application.description = getAttributeAsText(remoteApp, "description");

        Ap3Application.create(ap3Application);
        return Results.ok();
    }

    private static String getAttributeAsText(JsonNode json, String name)
    {
        return json.get(name).getTextValue();
    }
}
