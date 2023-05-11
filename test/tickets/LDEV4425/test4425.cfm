<cfscript>
param name="form.scene" default="1";

if(form.scene EQ 1) {
	try {
		queryExecute("INSERT INTO LDEV4425(test) VALUES ('test')", {}, {returntype="query", result="resultVar"});
		writeOutput("#structKeyExists(resultVar, "generatedKey")#");
	}
	catch(any e) {
		writeOutput(e.message); 
	}
}
else if(form.scene EQ 2) {
	try {
		queryExecute("INSERT INTO LDEV4425(test) VALUES ('test')", {}, {returntype="array", result="resultVar"});
		writeOutput("#structKeyExists(resultVar, "generatedKey")#");
	}
	catch(any e) {
		writeOutput(e.message); 
	}
}
else if(form.scene EQ 3) {
	try {
		queryExecute("INSERT INTO LDEV4425(test) VALUES ('test')", {}, {returntype="struct", result="resultVar"});
		writeOutput("#structKeyExists(resultVar, "generatedKey")#");
	}
	catch(any e) {
		writeOutput(e.message); 
	}
}
</cfscript>