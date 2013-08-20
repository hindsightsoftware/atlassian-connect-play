package com.atlassian.connect.play.java.controllers;

import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import controllers.AssetsBuilder;
import com.atlassian.connect.play.java.model.AcHostModel;
import org.codehaus.jackson.JsonNode;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.db.jpa.Transactional;
import play.mvc.BodyParser;
import play.mvc.Result;
import views.xml.ac.internal.internal_descriptor;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Suppliers.ofInstance;
import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
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
        if (isRequestFromUpm())
        {
            return descriptor.get();
        }
        else if (isAcceptHtml())
        {
            return home.get();
        }
        else if (isAcceptXml())
        {
            return descriptor.get();
        }
        else
        {
            throw new IllegalStateException("Why do we end up here!");
        }
    }

    private static boolean isAcceptXml()
    {
        return request().accepts("application/xml");
    }

    private static boolean isAcceptHtml()
    {
        return request().accepts("text/html");
    }

    private static boolean isRequestFromUpm()
    {
        return option(getPacClientInfoHeader()).fold(
                ofInstance(FALSE),
                new Function<String, Boolean>()
                {
                    @Override
                    public Boolean apply(String input)
                    {
                        final boolean isUpm = input.startsWith("client=upm");
                        if (isUpm)
                        {
                            LOGGER.debug(format("Upm is requesting the plugin descriptor: %s", input));
                        }
                        return isUpm;
                    }
                });
    }

    private static String getPacClientInfoHeader()
    {
        return request().getHeader("X-Pac-Client-Info");
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
    @Transactional
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

    public static Action<AnyContent> asset(String path, String file)
    {
        return delegate.at(path, file);
    }

    private static String getAttributeAsText(JsonNode json, String name)
    {
        return json.get(name).getTextValue();
    }
}
