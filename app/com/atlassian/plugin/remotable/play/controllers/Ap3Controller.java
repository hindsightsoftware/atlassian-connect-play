package com.atlassian.plugin.remotable.play.controllers;

import com.atlassian.fugue.Option;
import com.google.common.base.Supplier;
import controllers.AssetsBuilder;
import models.Ap3Application;
import org.codehaus.jackson.JsonNode;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.mvc.BodyParser;
import play.mvc.Result;
import views.xml.ap3.internal.internal_descriptor;

import static com.atlassian.plugin.remotable.play.util.Utils.LOGGER;
import static play.mvc.Controller.request;
import static play.mvc.Results.ok;

public class Ap3Controller
{
    public static Result index()
    {
        return index(home(), descriptorSupplier());
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

    public static Result descriptor()
    {
        return descriptorSupplier().get();
    }

    public static Supplier<Result> home()
    {
        return new Supplier<Result>()
        {
            @Override
            public Result get()
            {
                return Ap3Documentation.index();
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
                return ok(internal_descriptor.render());
            }
        };
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result registration()
    {
        LOGGER.info("Registering host application!");

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

    private static AssetsBuilder delegate = new AssetsBuilder();

    public static Action<AnyContent> asset(String path, String file) {
        return delegate.at(path, file);
    }

    private static String getAttributeAsText(JsonNode json, String name)
    {
        return json.get(name).getTextValue();
    }
}
