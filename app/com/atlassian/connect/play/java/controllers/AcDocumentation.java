package com.atlassian.connect.play.java.controllers;

import play.Play;
import play.mvc.*;

import views.html.ac.internal.*;

@With(AcDocumentation.IsDevAction.class)
public class AcDocumentation
{
    public static Result index()
    {
        return Results.ok(index_doc.render());
    }

    public static Result descriptor()
    {
        return Results.ok(descriptor_doc.render());
    }

    public static Result production()
    {
        return Results.ok(production_doc.render());
    }

    public static final class IsDevAction extends Action.Simple
    {
        @Override
        public Result call(Http.Context context) throws Throwable
        {
            return Play.isDev() ? delegate.call(context) : Results.notFound();
        }
    }
}