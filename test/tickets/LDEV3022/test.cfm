<cfscript>
    param name = "form.scene" default = "1";

    if( form.scene eq 1 ){
        result = queryExecute("
            SELECT *
            FROM LDEV3022
            WHERE price = :price",{ price = { value = 11.97, cfsqltype = "cf_sql_float" } }
        );
        writeoutput(result.price);
    }

    if( form.scene eq 2 ){
        result = queryExecute("
            SELECT *
            FROM LDEV3022
            WHERE price = :price",{ price = { value = 11.97, cfsqltype = "cf_sql_decimal" } }
        );
        writeoutput(result.price);
    }

</cfscript>