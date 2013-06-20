package com.atlassian.connect.play.java.controllers;

import com.atlassian.connect.play.java.plugin.AcAutoInstallPlugin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;

@With(IsDevAction.class)
public class UpmController extends Controller
{
    public static Result install()
    {
        AcAutoInstallPlugin.install();
        return Results.ok();
    }
}
