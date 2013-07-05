AJS.$(function ($)
{
    $("a.clear-cache").click(function (e)
    {
        var link = $(this);
        $.ajax({
            url: link.attr("href"),
            type: "DELETE",
            dataType: "json",
            error: function (xhr)
            {
                var data = $.parseJSON(xhr.responseText);
                AJS.messages.error({
                    title: data.title,
                    body: data.message
                });
            },
            success: function (data)
            {
                AJS.messages.success({
                    title: data.title,
                    body: data.message
                });
            }
        });
        return false;
    });
});