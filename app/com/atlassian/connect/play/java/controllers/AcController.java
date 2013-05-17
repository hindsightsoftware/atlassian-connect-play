package com.atlassian.connect.play.java.controllers;

import com.atlassian.fugue.Option;
import com.google.common.base.Supplier;
import controllers.AssetsBuilder;
import models.AcHostModel;
import org.codehaus.jackson.JsonNode;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.mvc.BodyParser;
import play.mvc.Result;
import views.xml.ac.internal.internal_descriptor;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static play.mvc.Controller.request;
import static play.mvc.Results.ok;

public class AcController
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
                return AcDocumentation.index();
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

        final AcHostModel acHost = AcHostModel.findByKey(clientKey)
                .orElse(new Supplier<Option<AcHostModel>>()
                {
                    @Override
                    public Option<AcHostModel> get()
                    {
                        return AcHostModel.findByUrl(baseUrl);
                    }
                })
                .getOrElse(new AcHostModel());

        acHost.key = clientKey;
        acHost.baseUrl = baseUrl;
        acHost.publicKey = getAttributeAsText(remoteApp, "publicKey");
        acHost.name = getAttributeAsText(remoteApp, "productType");
        acHost.description = getAttributeAsText(remoteApp, "description");

        AcHostModel.create(acHost);
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
