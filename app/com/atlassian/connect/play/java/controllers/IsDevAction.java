package com.atlassian.connect.play.java.controllers;

import com.atlassian.connect.play.java.AC;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Results;
import play.mvc.Result;

public final class IsDevAction extends Action.Simple
{
    @Override
    public Promise<Result> call(Http.Context context) throws Throwable
    {
        return AC.isDev() ? delegate.call(context) : Promise.pure((Result)Results.notFound());
    }
}
