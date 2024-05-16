<cfscript>
    qry = QueryNew("ID,Name");
    QueryAddRow(qry);
    QuerySetCell(qry,"ID",3);
    QuerySetCell(qry,"Name", nullValue());
    qEx = queryExecute("select * from qry",{}, {
        returntype: "array",
        dbtype: "query"
    });
    echo(serializeJSON(qEx));
</cfscript>