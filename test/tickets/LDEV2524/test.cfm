<cfscript>
	param name="FORM.scene" default="";
	if(form.scene eq 1){
		uppercase = '{"foo": "\u000B"}';
		uppercase = DeserializeJSON(uppercase);
		writeoutput(asc(uppercase.foo));
	}
	else{
		lowercase = '{"foo": "\u000b"}';
		lowercase = DeserializeJSON(lowercase);
		writeoutput(asc(lowercase.foo));
	}
</cfscript>