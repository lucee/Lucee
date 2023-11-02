<cfscript>
	param name="form.SCENE" default="";

	obj = createobject("component","obj");
	if( form.scene eq 1 ){
		objTest = obj.testlock();
		writeoutput(objTest);
	}
	if( form.scene eq 2 ){
		objtestOne = obj.testonelock();
		writeoutput(objtestOne);
	}
</cfscript>