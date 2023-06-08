<cfparam name="FORM.scene" default="">
<cfscript>
	try {
		if( FORM.scene == 1 ) {
			res = isArray(entityLoad("test"));
		} else if( FORM.scene == 2 ) {
			res = isArray(entityLoad("test", {}));
		} else if( FORM.scene == 3 ) {
			res = isArray(entityLoad("test", {}, ""));
		} else if( FORM.scene == 4 ) {
			res = isArray(entityLoad("test", {}, "", {}));
		} else if( FORM.scene == 5 ) {
			res = isArray(entityLoad(name="test", id={}, unique="", options={}));
		} else if( FORM.scene == 6 ) {
			res = isArray(entityLoad(name="test", id={}, options={}));
		} else if( FORM.scene == 7 ) {
			res = isArray(entityLoad(name="test", id={}));
		} else if( FORM.scene == 8 ) {
			res = isArray(entityLoad(name="test", options={}));
		} else if( FORM.scene == 9 ) {
			res = isArray(entityLoad(name="test", unique=""));
		} else if( FORM.scene == 10 ) {
			res = isArray(entityLoad(name="test", id={}, unique=""));
		} else {
			res="what scene? #form.scene#";
		}
	} catch(any e) {
		res = e.stacktrace;
	}	
	writeOutput(res);
</cfscript>