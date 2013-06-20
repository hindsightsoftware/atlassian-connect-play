package com.atlassian.connect.play.java.controllers;

import play.mvc.*;

@With(IsDevAction.class)
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
}
