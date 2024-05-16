<cfparam name="FORM.scene" default="">
<cfscript>
    try {
        BasicEntity = entityNew("BasicEntity");
        if (form.scene == 1) {
            writeOutput(BasicEntity.ViaImportPath());
        }
        if (form.scene == 2) {
            entitySave(BasicEntity);
            EntityLoaded = entityLoad("BasicEntity")[1];
            writeOutput(EntityLoaded.ViaImportPath());
        }
    }
    catch (any e) {
        writeoutput(e.message);
    }
</cfscript>