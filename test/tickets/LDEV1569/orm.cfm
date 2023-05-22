<cfscript>
    variables.test = EntityNew("Test");
    variables.test.setID(2);
    variables.test.setName(nullValue());

    EntitySave(variables.test);

    echo(SerializeJSON(variables.test));
</cfscript>