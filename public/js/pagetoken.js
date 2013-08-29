(function($) {
    $(function() {
        var getMeta = function(name) { return $('meta[name='+name+']').attr("content") };
        var isAbsolute = /^https?:\/\//i;

        var pageToken = getMeta("acpt");
        var consumer_key = getMeta("acck");
        var user_id = getMeta("acuid");

        var extraUrlParams =
                "acpt=" + encodeURIComponent(pageToken) +
                "&acck=" + encodeURIComponent(consumer_key);
        if(user_id) {
            extraUrlParams += "&acuid=" + encodeURIComponent(user_id);
        }

        //setup ajax requests
        $.ajaxSetup({
            headers: {
                "X-acpt" : pageToken,
                "X-acck": consumer_key,
                "X-acuid": user_id
            }
        });

        var decorateUrl = function($elem, attribute) {
            var url = $elem.attr(attribute);
            if(url && !isAbsolute.test(url)) {
                var separator = url.indexOf("?") !== -1 ? "&" : "?";
                var newUrl = url + separator + extraUrlParams;
                $elem.attr(attribute, newUrl);
            }
        };

        //decorate links & forms
        $("a").each(function(i, link) {
            decorateUrl($(link), "href");
        });
        $("form").each(function(i, form) {
            decorateUrl($(form), "action");
        });
    });
})((window.AJS && AJS.$) || jQuery);