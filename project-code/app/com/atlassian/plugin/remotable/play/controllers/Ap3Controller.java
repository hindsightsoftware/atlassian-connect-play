package com.atlassian.plugin.remotable.play.controllers;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.remotable.play.Ap3;
import com.google.common.base.Supplier;
import models.Ap3Application;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Result;

import views.html.ap3.internal.home;
import views.xml.ap3.internal.descriptor;

import static play.mvc.Controller.request;
import static play.mvc.Results.ok;

public class Ap3Controller
{
    public static Result index()
    {
        return index(homeSupplier(), descriptorSupplier());
    }

    public static Result index(Supplier<Result> home, Supplier<Result> descriptor)
    {
        if (request().accepts("text/html"))
        {
            return home.get();
        }
        else if (request().accepts("application/xml"))
        {
            return descriptor.get();
        }
        else
        {
            throw new IllegalStateException("Why do we end up here!");
        }
    }

    public static Result home()
    {
        return homeSupplier().get();
    }

    public static Result descriptor()
    {
        return descriptorSupplier().get();
    }

    public static Supplier<Result> homeSupplier()
    {
        return new Supplier<Result>()
        {
            @Override
            public Result get()
            {
                return ok(home.render());
            }
        };
    }

    public static Supplier<Result> descriptorSupplier()
    {
        return new Supplier<Result>()
        {
            @Override
            public Result get()
            {
                return ok(descriptor.render(Ap3.baseUrl.get(), Ap3.publicKey.get()));
            }
        };
    }

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
        return ok();
    }

    private static String getAttributeAsText(JsonNode json, String name)
    {
        return json.get(name).getTextValue();
    }
}
