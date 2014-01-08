package com.atlassian.connect.play.java.controllers;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.fugue.Option;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import controllers.AssetsBuilder;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.db.jpa.Transactional;
import play.libs.F;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Results;
import views.xml.ac.internal.internal_descriptor;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Suppliers.ofInstance;
import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static play.libs.F.Promise;
import static play.mvc.Controller.request;

public class AcController {
    public static Result index() {
        return index(home(), descriptorSupplier());
    }

    public static Result index(Supplier<Result> home, Supplier<Result> descriptor) {
        if (isRequestFromUpm()) {
            return descriptor.get();
        } else if (isAcceptHtml()) {
            return home.get();
        } else if (isAcceptXml()) {
            return descriptor.get();
        } else {
            throw new IllegalStateException("Why do we end up here!");
        }
    }

    private static boolean isAcceptXml() {
        return request().accepts("application/xml");
    }

    private static boolean isAcceptHtml() {
        return request().accepts("text/html");
    }

    private static boolean isRequestFromUpm() {
        return option(getPacClientInfoHeader()).fold(
                ofInstance(FALSE),
                new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String input) {
                        final boolean isUpm = input.startsWith("client=upm");
                        if (isUpm) {
                            LOGGER.debug(format("Upm is requesting the plugin descriptor: %s", input));
                        }
                        return isUpm;
                    }
                });
    }

    private static String getPacClientInfoHeader() {
        return request().getHeader("X-Pac-Client-Info");
    }

    public static Result descriptor() {
        return descriptorSupplier().get();
    }

    public static Supplier<Result> home() {
        return new Supplier<Result>() {
            @Override
            public Result get() {
                return AcDocumentation.index();
            }
        };
    }

    public static Supplier<Result> descriptorSupplier() {
        return new Supplier<Result>() {
            @Override
            public Result get() {
                return Results.ok(internal_descriptor.render());
            }
        };
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public static Promise<Result> registration() {
        LOGGER.info("Registering host application!");

        final JsonNode remoteApp = request().body().asJson();

        if (remoteApp == null) {
            return Promise.pure((Result)Results.badRequest("can't extract registration request json"));
        }

        // TODO check the key is the same as this app's
        getAttributeAsText(remoteApp, "key");

        final AcHostModel acHost = populateHostModel(remoteApp);

        Promise<Boolean> hostRegistered = AC.registerHost(acHost);
        return hostRegistered.map(new F.Function<Boolean, Result>() {
            @Override
            public Result apply(Boolean isRegistered) throws Throwable {
                return isRegistered ? Results.ok() : Results.badRequest(); // TODO: better feedback. Need to return something other than boolean from the service
            }
        });
    }

    private static AcHostModel populateHostModel(JsonNode remoteApp) {
        final String clientKey = getAttributeAsText(remoteApp, "clientKey");
        final String baseUrl = getAttributeAsText(remoteApp, "baseUrl");

        // TODO: The consequence of this is that we will overwrite registrations each time. Is that what we want?
        final AcHostModel acHost = AcHostModel.findByKey(clientKey)
                .orElse(new Supplier<Option<AcHostModel>>() {
                    @Override
                    public Option<AcHostModel> get() {
                        return AcHostModel.findByUrl(baseUrl);
                    }
                })
                .getOrElse(new AcHostModel());

        acHost.key = clientKey;
        acHost.baseUrl = baseUrl;
        acHost.publicKey = getAttributeAsText(remoteApp, "publicKey");
        acHost.sharedSecret = getAttributeAsText(remoteApp, "sharedSecret");
        acHost.name = getAttributeAsText(remoteApp, "productType");
        acHost.description = getAttributeAsText(remoteApp, "description");
        return acHost;
    }

    private static AssetsBuilder delegate = new AssetsBuilder();

    public static Action<AnyContent> asset(String path, String file) {
        return delegate.at(path, file);
    }

    private static String getAttributeAsText(JsonNode json, String name) {
        JsonNode jsonNode = json.get(name);
        return jsonNode == null ? null : jsonNode.textValue();
    }
}
