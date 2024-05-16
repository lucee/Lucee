<cfscript>
    param name="FORM.scene" default="";
    try {
        if (form.scene == 1) {
            transaction {
                entityNew("person");
            }
        }

        if (form.scene == 2) {
            transaction {
                res = entityNew("person", {id:"1", name="lucee CFML", givenName="lucee", surname="CFML" } );
            }
        }

        if (form.scene == 3) {
            transaction {
                res = entityNew("person", {id:"1", name="lucee CFML", givenName="lucee", surname="CFML" } );
                entitySave(res);
            }
        }
        writeoutput("success");
    }
    catch (any e) {
        writeoutput(e.message);
    }
</cfscript>