package com.atlassian.connect.play.java.controllers;

import play.Play;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

public final class IsDevAction extends Action.Simple
{
    @Override
    public Result call(Http.Context context) throws Throwable
    {
        return Play.isDev() ? delegate.call(context) : Results.notFound();
    }
}
