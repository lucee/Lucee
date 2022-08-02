<cfscript>
    result = [
        form: form,
        url: url
    ];

    json = result.toJson();

    content type="application/json" variable="json";
</cfscript>