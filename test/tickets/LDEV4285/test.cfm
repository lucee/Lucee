<cfparam name="FORM.scene" default="">
<cfscript>
	entityName="test";

	try {
		if( FORM.scene == 1 ) {
			res = isArray(entityLoad(entityName));
		} else if( FORM.scene == 2 ) {
			res = isArray(entityLoad(entityName, {}));
		} else if( FORM.scene == 3 ) {
			res = isArray(entityLoad(entityName, {}, ""));
		} else if( FORM.scene == 4 ) {
			res = isArray(entityLoad(entityName, {}, "", {}));
		} else if( FORM.scene == 5 ) {
			res = isArray(entityLoad(name=entityName, id={}, unique="", options={}));
		} else if( FORM.scene == 6 ) {
			res = isArray(entityLoad(name=entityName, id={}, options={}));
		} else if( FORM.scene == 7 ) {
			res = isArray(entityLoad(name=entityName, id={}));
		} else if( FORM.scene == 8 ) {
			res = isArray(entityLoad(name=entityName, options={}));
		} else if( FORM.scene == 9 ) {
			res = isArray(entityLoad(name=entityName, unique=""));
		} else if( FORM.scene == 10 ) {
			res = isArray(entityLoad(name=entityName, id={}, unique=""));
		} else {
			res = " unknown scene #form.scene#";
		}
	} catch(any e) {
		res = e.stacktrace;
	}
	writeOutput(res);
</cfscript>