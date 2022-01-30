<cfscript>
    result = [
        form: form,
        url: url
    ];

    content type="application/json";
    echo(result.toJson());
    abort;
</cfscript>