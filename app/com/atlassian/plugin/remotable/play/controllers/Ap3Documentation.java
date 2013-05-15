package com.atlassian.plugin.remotable.play.controllers;

import play.Play;
import play.mvc.*;

import views.html.ap3.internal.descriptor_doc;

@With(Ap3Documentation.IsDevAction.class)
public class Ap3Documentation
{
    public static Result descriptor()
    {
        return Results.ok(descriptor_doc.render());
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
