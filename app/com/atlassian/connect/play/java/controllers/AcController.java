package com.atlassian.connect.play.java.controllers;

import com.atlassian.connect.play.java.auth.PublicKeyVerificationFailureException;
import com.atlassian.connect.play.java.service.AcHostService;
import com.atlassian.connect.play.java.service.InjectorFactory;
import com.atlassian.connect.play.java.util.DescriptorUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import controllers.AssetsBuilder;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.db.jpa.Transactional;
import play.libs.F;
import play.mvc.BodyParser;
import play.mvc.Result;

import java.io.IOException;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Suppliers.ofInstance;
import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static play.libs.F.Promise;
import static play.mvc.Controller.request;
import static play.mvc.Results.*;

public class AcController {

    private static final String CLIENT_KEY = "clientKey";
    private static final String BASE_URL = "baseUrl";
    private static final String SHARED_SECRET = "sharedSecret";
    private static final String PRODUCT_TYPE = "productType";
    private static final String PUBLIC_KEY_ELEMENT_NAME = "publicKey";

    @VisibleForTesting
    public static AcHostService acHostService = InjectorFactory.getAcHostService();

    public static Result index() {
        return index(home(), descriptorSupplier());
    }

    public static Result index(Supplier<Result> home, Supplier<Result> descriptor) {
        if (isRequestFromUpm()) {
            return descriptor.get();
        } else if (isAcceptHtml()) {
            return home.get();
        } else if (isAcceptJson()) {
            return descriptor.get();
        } else {
            throw new IllegalStateException("Why do we end up here!");
        }
    }

    private static boolean isAcceptJson() {
        return request().accepts("application/json");
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
        try {
            return ok(DescriptorUtils.substituteVariablesInDefaultFile());
        } catch (IOException e) {
            LOGGER.error("Failed to create substituted descriptor", e);
            return internalServerError("Failed to create substituted descriptor: " + e.getMessage());
//            throw new RuntimeException("Failed to create substituted descriptor", e);
        }
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
                return descriptor();
            }
        };
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public static Promise<Result> registration() {
        LOGGER.info("Registering host application!");

        final JsonNode remoteApp = request().body().asJson();

        if (remoteApp == null) {
            return Promise.pure((Result) badRequest("can't extract registration request json"));
        }

        final String clientKey = getAttributeAsText(remoteApp, CLIENT_KEY);
        Promise<Void> hostRegistered = acHostService.registerHost(clientKey,
                getAttributeAsText(remoteApp, BASE_URL),
                getAttributeAsText(remoteApp, PUBLIC_KEY_ELEMENT_NAME),
                getAttributeAsText(remoteApp, SHARED_SECRET),
                getAttributeAsText(remoteApp, PRODUCT_TYPE));

        Promise<Result> resultPromise = hostRegistered.map(new F.Function<Void, Result>() {
            @Override
            public Result apply(Void nada) throws Throwable {
                return ok();
            }
        });

        return resultPromise.recover(new F.Function<Throwable, Result>() {
            @Override
            public Result apply(Throwable throwable) throws Throwable {
                LOGGER.warn("Failed to register host (key = " + clientKey + ")", throwable);

                if (throwable instanceof PublicKeyVerificationFailureException) {
                    return internalServerError("failed to fetch public key from host for verification");
                }
                return badRequest("Unable to register host. Request invalid"); // TODO: better analysis of failure and feedback to caller
            }
        });
    }

    private static String getAttributeAsText(JsonNode json, String name) {
        JsonNode jsonNode = json.get(name);
        return jsonNode == null ? null : jsonNode.textValue();
    }

    private static AssetsBuilder delegate = new AssetsBuilder();

    public static Action<AnyContent> asset(String path, String file) {
        return delegate.at(path, file);
    }

}
