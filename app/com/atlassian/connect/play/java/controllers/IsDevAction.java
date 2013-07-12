package com.atlassian.connect.play.java.controllers;

import com.atlassian.connect.play.java.AC;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

public final class IsDevAction extends Action.Simple
{
    @Override
    public Result call(Http.Context context) throws Throwable
    {
        return AC.isDev() ? delegate.call(context) : Results.notFound();
    }
}
