<cfscript>
	param name="form.scene" default="1";

	if(form.scene eq 1) {
		res = entityLoad("test");
		entityOut = entitytoquery(res);
		writeoutput(entityOut.name[1]);
	}

	else if(form.scene eq 2) {
		res = entityLoad("test");
		entityOut = entitytoquery(res,"test");
		writeoutput(entityOut.name[1]);
	}
</cfscript>	