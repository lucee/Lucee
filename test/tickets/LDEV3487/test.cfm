<cfscript>
    try {
        res = queryExecute("INSERT INTO LDEV3487_MYSQL(test) VALUES ('test')",{},{datasource="LDEV_3487",result="resultVar"});
        writeoutput("#structKeyExists(resultVar,"generatedKey")#,#resultVar.generatedKey#");
    }
    catch(any e){
        writeoutput(e.message); 
    }
</cfscript>