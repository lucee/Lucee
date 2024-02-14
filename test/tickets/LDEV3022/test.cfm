<cfscript>
    param name = "form.scene" default = "1";

    if( form.scene eq "float" ){
        result = queryExecute("
            SELECT *
            FROM LDEV3022
            WHERE price = :price",{ price = { value = 11.97, cfsqltype = "cf_sql_float" } }
        );
        writeoutput(result.recordcount & "," & result.price);
    } else if ( form.scene eq "decimal" ){
        result = queryExecute("
            SELECT *
            FROM LDEV3022
            WHERE price = :price",{ price = { value = 11.97, cfsqltype = "cf_sql_decimal" } }
        );
        writeoutput(result.recordcount & "," & result.price);
    }

</cfscript>